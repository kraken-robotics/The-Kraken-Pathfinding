package robot;

import java.util.ArrayList;

import pathfinding.astarCourbe.ArcCourbe;
import permissions.ReadOnly;
import permissions.ReadWrite;
import hook.Hook;
import utils.Log;
import utils.Vec2;
import exceptions.FinMatchException;

/**
 * Robot particulier qui fait pas bouger le robot réel, mais détermine la durée des actions
 * @author pf
 */

public class RobotChrono extends Robot
{
	private static final long[] paliers = new long[7];
	
	protected final Vec2<ReadWrite> position = new Vec2<ReadWrite>();
	protected int positionGridSpace;
//	protected PathfindingNodes positionPathfinding;
//	protected PathfindingNodes positionPathfindingAnterieure;
//	protected boolean isPositionPathfindingActive = false;
	protected double orientation;
	private static int tempsMax = 90000;
   
	// Date en millisecondes depuis le début du match.
	protected long date;
	
	/** valeur approchée du temps (en millisecondes) nécessaire pour qu'une information que l'on envoie à la série soit acquittée */
//	private final static int approximateSerialLatency = 50;

	private final static int sleepAvanceDuration = /*approximateSerialLatency+*/Speed.translationStopDuration;
	private final static int sleepTourneDuration = /*approximateSerialLatency+*/Speed.rotationStopDuration;
//	private final static int sleepTourneAndAvanceDuration = sleepTourneDuration + sleepAvanceDuration;
	
	static
	{
		paliers[0] = 20000;
		paliers[1] = 35000;
		paliers[2] = 48000;
		paliers[3] = 60000;
		paliers[4] = 70000;
		paliers[5] = 78000;
		paliers[6] = 84000;
	}
	
	public RobotChrono(Log log)
	{
		super(log);
	}
	
//	@Override
//	public void setPositionOrientationSTM(Vec2<ReadOnly> position, double orientation) {
//		Vec2.copy(position, this.position);
//		this.orientation = orientation;
//		isPositionPathfindingActive = false;
//		positionPathfindingAnterieure = null;
//		positionPathfinding = null;
//		date += approximateSerialLatency;
//	}

	/**
	 * Mise à jour permettant de modifier, pour RobotChrono, la date limite de la recherche stratégique
	 * @param dateLimite
	 */
	public static void setTempsMax(int tempsMax)
	{
		RobotChrono.tempsMax = tempsMax;
	}
	
	public boolean estArrive(RobotChrono autre)
	{
		return position.squaredDistance(autre.position) < 50*50; // TODO prendre en compte l'orientation, la courbure, ...
	}
	
	@Override
    public void avancer(int distance, ArrayList<Hook> hooks, boolean mur) throws FinMatchException
	{
		date += Math.abs(distance)*vitesse.invertedTranslationnalSpeed + sleepAvanceDuration;
	
        Vec2<ReadWrite> ecart = new Vec2<ReadWrite>((int)(distance*Math.cos(orientation)), (int)(distance*Math.sin(orientation)));

		checkHooks(position.getReadOnly(), position.plusNewVector(ecart).getReadOnly(), hooks);
		Vec2.plus(position, ecart);
//		isPositionPathfindingActive = false;
//		positionPathfindingAnterieure = null;
//		positionPathfinding = null;
	}
	
	@Override
	public void setVitesse(Speed vitesse)
	{
	    this.vitesse = vitesse;
//		date += approximateSerialLatency;
	}
	
	@Override
	public long getTempsDepuisDebutMatch()
	{
		return date;
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

/*	private void va_au_point_no_hook(Vec2<ReadOnly> point) throws FinMatchException
	{
		double orientation_finale = Math.atan2(point.y - position.y, point.x - position.x);
		tourner(orientation_finale);
		date += position.distance(point)*vitesse.invertedTranslationnalSpeed + sleepAvanceDuration;
		Vec2.copy(point, position);
		isPositionPathfindingActive = false;
		positionPathfindingAnterieure = null;
		positionPathfinding = null;
	}*/
	
	/**
	 * @param n
	 * @param hooks
	 * @throws FinMatchException
	 */
	/*	public void va_au_point_pathfinding(PathfindingNodes n, int differenceDistance, ArrayList<Hook> hooks) throws FinMatchException
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
	}*/

	/**
	 * Optimisation incroyable, même si on ne dirait pas comme ça.
	 * @param n
	 * @throws FinMatchException
	 */
	/*	public void va_au_point_pathfinding_no_hook(LocomotionArc segment) throws FinMatchException
	{
		PathfindingNodes n = segment.objectifFinal;
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
		setPositionPathfinding(n);
	}*/

	@Override
	public void sleep(long duree, ArrayList<Hook> hooks) throws FinMatchException 
	{
		this.date += duree;
		checkHooks(position.getReadOnly(), position.getReadOnly(), hooks);
	}
    
/*    @Override
    public void desactiveAsservissement()
    {
		date += approximateSerialLatency;
    }

    @Override
    public void activeAsservissement()
    {
		date += approximateSerialLatency;
    }*/

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
				hook.trigger();
		
		// vérification de la fin de la recherche
		if(tempsMax < date)
			throw new FinMatchException();
	}

	public void setPositionGridSpace(int gridpoint)
	{
		positionGridSpace = gridpoint;
	}
	
	@Override
	public int getPositionGridSpace()
	{
		return positionGridSpace;
	}
	
	/** Inverse le sens actuel de la marche.
	 * Utilisé lors d'un point de rebroussement
	 */
	public void inverseSensMarche()
	{
		enMarcheAvant = !enMarcheAvant;
	}
	

	
/*	public void setPositionPathfinding(PathfindingNodes n)
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
	}*/

	/**
	 * UTILISE UNIQUEMENT PAR LES TESTS
	 */
	public void reinitDate()
	{
		date = 0;
	}

	// Permet de commander le lissage en vérifiant si on part d'un point qui n'est pas un node
/*	public boolean isAtPathfindingNodes()
	{
		return isPositionPathfindingActive;
	}*/
	
	// TODO: passer en hashCode et equals()
	
	public int getHashLPAStar()
	{
		int hash = 7;
		// Calcul du palier
		for(int i = 0; i < 7 ; i++)
		{
			if(date < paliers[i])
			{
				hash = i;
				break;
			}
		}
/*		if(isPositionPathfindingActive)
		{
			hash = 0;
//			hash = tapisPoses?1:0; // information sur les tapis
			hash = (hash << 6) | positionPathfinding.ordinal(); // codé sur 6 bits (ce qui laisse de la marge)
			hash = (hash << 9) | pointsObtenus; // d'ici provient le &511 de StrategyArcManager (511 = 2^9 - 1)
		}
		else
		{*/
			// Pour la position, on ne prend pas les bits de poids trop faibles dont le risque de collision est trop grand
//			hash = tapisPoses?1:0; // information sur les tapis
			hash = (hash << 3) | ((position.x >> 2)&7); // petit hash sur 3 bits
			hash = (hash << 3) | ((position.y >> 2)&7); // petit hash sur 3 bits
			hash = (hash << 9) | pointsObtenus; // d'ici provient le &511 de StrategyArcManager (511 = 2^9 - 1)
//		}
		return hash;
	}

	public void vaAuPointAStar() {
		// TODO Auto-generated method stub
		
	}

	public void suitArcCourbe(ArcCourbe came_from_arc) {
		// TODO Auto-generated method stub
		
	}

/*	public void printHash()
	{
//		log.debug("Tapis posés: "+tapisPoses);
		if(isPositionPathfindingActive)
			log.debug("Position pathfinding: "+positionPathfinding);
		else
			log.debug("Hash position: "+((position.x >> 2)&7)+" et "+((position.y >> 2)&7));
		log.debug("Points obtenus: "+pointsObtenus);
	}*/
	
}
