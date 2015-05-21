package robot;

import java.util.ArrayList;

import permissions.ReadOnly;
import permissions.ReadWrite;
import planification.LocomotionArc;
import planification.astar.arc.PathfindingNodes;
import hook.Hook;
import utils.Log;
import utils.Vec2;
import exceptions.ChangeDirectionException;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.WallCollisionDetectedException;

/**
 * Robot particulier qui fait pas bouger le robot réel, mais détermine la durée des actions
 * @author pf et marsu !
 */

public class RobotChrono extends Robot
{
	protected final Vec2<ReadWrite> position = new Vec2<ReadWrite>();
	protected PathfindingNodes positionPathfinding;
	protected PathfindingNodes positionPathfindingAnterieure;
	protected boolean isPositionPathfindingActive = false;
	protected double orientation;
	
	// Date en millisecondes depuis le début du match.
	protected int date;
	
	/** valeur approchée du temps (en milisecondes) nécéssaire pour qu'une information que l'on envois a la série soit aquité */
	private final static int approximateSerialLatency = 50;

	private final static int sleepAvanceDuration = approximateSerialLatency+Speed.translationStopDuration;
	private final static int sleepTourneDuration = approximateSerialLatency+Speed.rotationStopDuration;
	private final static int sleepTourneAndAvanceDuration = sleepTourneDuration + sleepAvanceDuration;
	
	public RobotChrono(Log log)
	{
		super(log);
	}
	
	@Override
	public void setPosition(Vec2<ReadOnly> position) {
		Vec2.copy(position, this.position);
		isPositionPathfindingActive = false;
		positionPathfindingAnterieure = null;
		positionPathfinding = null;
		date += approximateSerialLatency;
	}
	
	@Override
	public void setOrientation(double orientation) {
		this.orientation = orientation;
		date += approximateSerialLatency;
	}

	@Override
    public void avancer(int distance, ArrayList<Hook> hooks, boolean mur) throws FinMatchException
	{
		date += Math.abs(distance)*vitesse.invertedTranslationnalSpeed + sleepAvanceDuration;
	
        Vec2<ReadWrite> ecart = new Vec2<ReadWrite>((int)(distance*Math.cos(orientation)), (int)(distance*Math.sin(orientation)));

		checkHooks(position.getReadOnly(), position.plusNewVector(ecart).getReadOnly(), hooks);
		Vec2.plus(position, ecart);
		isPositionPathfindingActive = false;
		positionPathfindingAnterieure = null;
		positionPathfinding = null;
	}
	
	@Override
	public void set_vitesse(Speed vitesse)
	{
	    this.vitesse = vitesse;
		date += approximateSerialLatency;
	}
	
	@Override
	public int getTempsDepuisDebutMatch()
	{
		return date;
	}

	@Override
	public RobotChrono cloneIntoRobotChrono()
	{
		RobotChrono cloned_robotchrono = new RobotChrono(log);
		copy(cloned_robotchrono);
		return cloned_robotchrono;
	}

	/**
	 * Donne l'angle (non orienté, en valeur absolue) entre
	 * l'orientation actuelle et l'angle donné en argument
	 * @param angle
	 * @return
	 */
	public double calculateDelta(double angle)
	{
		double delta = orientation-angle % (2*Math.PI);
		delta = Math.abs(delta);
		if(delta > Math.PI)
			delta = 2*Math.PI - delta;
		return delta;
	}
	
	@Override
    public void tourner(double angle)
	{
		// TODO: avec les trajectoires courbes, les durées changent
		// et la marche arrière automatique?
		double delta = calculateDelta(angle);
		orientation = angle;
		date += delta*vitesse.invertedRotationnalSpeed + sleepTourneDuration;
	}

	@Override
    public void suit_chemin(ArrayList<LocomotionArc> chemin, ArrayList<Hook> hooks) throws FinMatchException
	{
//		for(LocomotionArc point: chemin)
//			va_au_point_pathfinding(point.objectifFinal, point.differenceDistance, hooks);
	}
	
	private void va_au_point_no_hook(Vec2<ReadOnly> point) throws FinMatchException
	{
		double orientation_finale = Math.atan2(point.y - position.y, point.x - position.x);
		tourner(orientation_finale);
		date += position.distance(point)*vitesse.invertedTranslationnalSpeed + sleepAvanceDuration;
		Vec2.copy(point, position);
		isPositionPathfindingActive = false;
		positionPathfindingAnterieure = null;
		positionPathfinding = null;
	}
	
	/**
	 * @param n
	 * @param hooks
	 * @throws FinMatchException
	 */
	public void va_au_point_pathfinding(PathfindingNodes n, int differenceDistance, ArrayList<Hook> hooks) throws FinMatchException
	{
		// Compensation de la trajectoire courbe
		if(differenceDistance != 0)
			date -= differenceDistance*vitesse.invertedTranslationnalSpeed;// + sleepTourneAndAvanceDuration;

		if(!isPositionPathfindingActive)
		{
			checkHooks(position.getReadOnly(), n.getCoordonnees(), hooks);
			va_au_point_no_hook(n.getCoordonnees());
		}
		else if(positionPathfindingAnterieure != null && vitesse == Speed.BETWEEN_SCRIPTS)
		{
			date += positionPathfinding.angleWith(positionPathfindingAnterieure, n)
					+ positionPathfinding.timeTo(n)
					+ sleepTourneAndAvanceDuration;
			checkHooks(position.getReadOnly(), n.getCoordonnees(), hooks);
		}
		else if(vitesse == Speed.BETWEEN_SCRIPTS)
		{
			tourner(positionPathfinding.getOrientationFinale(n));
			date += positionPathfinding.timeTo(n) + sleepAvanceDuration;
			checkHooks(position.getReadOnly(), n.getCoordonnees(), hooks);
		}
		else
		{
			tourner(positionPathfinding.getOrientationFinale(n));
			date += positionPathfinding.distanceTo(n)*vitesse.invertedTranslationnalSpeed + sleepAvanceDuration;
			checkHooks(position.getReadOnly(), n.getCoordonnees(), hooks);
		}
		setPositionPathfinding(n);
	}

	/**
	 * Optimisation incroyable, même si on ne dirait pas comme ça.
	 * @param n
	 * @throws FinMatchException
	 */
	public void va_au_point_pathfinding_no_hook(LocomotionArc segment) throws FinMatchException
	{
/*		PathfindingNodes n = segment.objectifFinal;
		// Compensation de la trajectoire courbe
		if(segment.differenceDistance != 0)
			date -= segment.differenceDistance*vitesse.invertedTranslationnalSpeed;// + sleepTourneAndAvanceDuration;
		
		if(!isPositionPathfindingActive)
			va_au_point_no_hook(n.getCoordonnees());
		else if(positionPathfindingAnterieure != null && vitesse == Speed.BETWEEN_SCRIPTS)
		{
			date += positionPathfinding.angleWith(positionPathfindingAnterieure, n)
					+ positionPathfinding.timeTo(n)
					+ sleepTourneAndAvanceDuration;
		}
		else if(vitesse == Speed.BETWEEN_SCRIPTS)
		{
			tourner(positionPathfinding.getOrientationFinale(n));
			date += positionPathfinding.timeTo(n) + sleepAvanceDuration;
		}
		else
		{
			tourner(positionPathfinding.getOrientationFinale(n));
			date += positionPathfinding.distanceTo(n)*vitesse.invertedTranslationnalSpeed + sleepAvanceDuration;
		}
		setPositionPathfinding(n);*/
	}

	@Override
	public void sleep(long duree, ArrayList<Hook> hooks) throws FinMatchException 
	{
		this.date += duree;
		checkHooks(position.getReadOnly(), position.getReadOnly(), hooks);
	}
	
	@Override
    public void stopper()
    {
		date += sleepAvanceDuration;
    }

    @Override
    public Vec2<ReadOnly> getPosition()
    {
        return position.getReadOnly();
    }

    @Override
    public double getOrientation()
    {
        return orientation;
    }
    
    @Override
    public void copy(RobotChrono rc)
    {
        super.copy(rc);
        Vec2.copy(position.getReadOnly(), rc.position);
        rc.orientation = orientation;
    	rc.positionPathfinding = positionPathfinding;
    	rc.positionPathfindingAnterieure = positionPathfindingAnterieure;
    	rc.isPositionPathfindingActive = isPositionPathfindingActive;
    }

    public void desactiver_asservissement_rotation()
    {
		date += approximateSerialLatency;
    }

    public void desactiver_asservissement_translation()
    {
		date += approximateSerialLatency;
    }

    public void activer_asservissement_rotation()
    {
    	date += approximateSerialLatency;
    }

	/**
	 * On déclenche tous les hooks entre le point A et le point B.
	 * Il faut appeler checkHooks APRÈS avoir mis à jour date!
	 * @param pointA
	 * @param pointB
	 * @param hooks
	 * @throws FinMatchException 
	 */
	private void checkHooks(Vec2<ReadOnly> pointA, Vec2<ReadOnly> pointB, ArrayList<Hook> hooks) throws FinMatchException
	{
		for(Hook hook: hooks)
			if(hook.simulated_evaluate(pointA, pointB, date))
				try {
					hook.trigger();
				} catch (ScriptHookException e) {
					// Impossible qu'en simulation on ait des scripts de hooks
					e.printStackTrace();
				} catch (WallCollisionDetectedException e) {
					// Impossible
					e.printStackTrace();
				} catch (ChangeDirectionException e) {
					// Impossible
					e.printStackTrace();
				}
		
		// le hook de fin de match est particulier, car il est toujours appelé, qu'il soit dans la liste ou non
		if(hookFinMatch.simulated_evaluate(pointA, pointB, date))
			try {
				hookFinMatch.trigger();
			} catch (ScriptHookException e) {
				// Impossible
				e.printStackTrace();
			} catch (WallCollisionDetectedException e) {
				// Impossible
				e.printStackTrace();
			} catch (ChangeDirectionException e) {
				// Impossible
				e.printStackTrace();
			}
	}

	public void setPositionPathfinding(PathfindingNodes n)
	{
		Vec2.copy(n.getCoordonnees(), position);
		positionPathfindingAnterieure = positionPathfinding;
		positionPathfinding = n;
		isPositionPathfindingActive = true;
	}
	
	public PathfindingNodes getPositionPathfinding()
	{
		if(isPositionPathfindingActive)
			return positionPathfinding;
		return null;
	}

	/**
	 * UTILISE UNIQUEMENT PAR LES TESTS
	 */
	public void reinitDate()
	{
		date = 0;
	}

	// Permet de commander le lissage en vérifiant si on part d'un point qui n'est pas un node
	public boolean isAtPathfindingNodes()
	{
		return isPositionPathfindingActive;
	}
	
	// TODO: passer en hashCode et equals()
	
	public int getHash()
	{
		int hash;
		if(isPositionPathfindingActive)
		{
			hash = 0;
//			hash = tapisPoses?1:0; // information sur les tapis
			hash = (hash << 6) | positionPathfinding.ordinal(); // codé sur 6 bits (ce qui laisse de la marge)
			hash = (hash << 9) | pointsObtenus; // d'ici provient le &511 de StrategyArcManager (511 = 2^9 - 1)
		}
		else
		{
			hash = 0;
			// Pour la position, on ne prend pas les bits de poids trop faibles dont le risque de collision est trop grand
//			hash = tapisPoses?1:0; // information sur les tapis
			hash = (hash << 3) | ((position.x >> 2)&7); // petit hash sur 3 bits
			hash = (hash << 3) | ((position.y >> 2)&7); // petit hash sur 3 bits
			hash = (hash << 9) | pointsObtenus; // d'ici provient le &511 de StrategyArcManager (511 = 2^9 - 1)
		}
		return hash;
	}

	public void printHash()
	{
//		log.debug("Tapis posés: "+tapisPoses);
		if(isPositionPathfindingActive)
			log.debug("Position pathfinding: "+positionPathfinding);
		else
			log.debug("Hash position: "+((position.x >> 2)&7)+" et "+((position.y >> 2)&7));
		log.debug("Points obtenus: "+pointsObtenus);
	}
	
}
