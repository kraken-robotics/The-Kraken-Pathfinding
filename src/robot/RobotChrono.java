package robot;

import java.util.ArrayList;

import pathfinding.astarCourbe.arcs.ArcCourbe;
import pathfinding.astarCourbe.arcs.ArcCourbeClotho;
import pathfinding.astarCourbe.arcs.ArcCourbeCubique;
import pathfinding.astarCourbe.arcs.ClothoidesComputer;
import hook.Hook;
import utils.Log;
import utils.Vec2;
import utils.permissions.ReadOnly;
import utils.permissions.ReadWrite;
import exceptions.FinMatchException;

/**
 * Robot particulier qui fait pas bouger le robot réel, mais détermine la durée des actions
 * @author pf
 */

public class RobotChrono extends Robot
{
	private static final long[] paliers = new long[7];

	protected int positionGridSpace;
	private static int tempsMax = 90000;
   
	// Date en millisecondes depuis le début du match.
	protected long date;
	
	/** valeur approchée du temps (en millisecondes) nécessaire pour qu'une information que l'on envoie à la série soit acquittée */
//	private final static int approximateSerialLatency = 50;

	private final static int sleepAvanceDuration = /*approximateSerialLatency+*/Speed.translationStopDuration;
	private final static int sleepTourneDuration = /*approximateSerialLatency+*/Speed.rotationStopDuration;
	
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
	
	RobotChrono(Log log, Cinematique cinematique)
	{
		super(log);
		this.cinematique = new Cinematique(cinematique);
	}

	/**
	 * Mise à jour permettant de modifier, pour RobotChrono, la date limite de la recherche stratégique
	 * @param dateLimite
	 */
	public static void setTempsMax(int tempsMax)
	{
		RobotChrono.tempsMax = tempsMax;
	}
	
	@Override
    public void avancer(int distance, ArrayList<Hook> hooks, boolean mur, Speed vitesse) throws FinMatchException
	{
		date += Math.abs(distance)*vitesse.translationalSpeed + sleepAvanceDuration;
	
        Vec2<ReadWrite> ecart = new Vec2<ReadWrite>((int)(distance*Math.cos(cinematique.orientation)), (int)(distance*Math.sin(cinematique.orientation)));

		checkHooks(cinematique.getPosition(), cinematique.getPosition().plusNewVector(ecart).getReadOnly(), hooks);
		Vec2.plus(cinematique.getPositionEcriture(), ecart);
//		isPositionPathfindingActive = false;
//		positionPathfindingAnterieure = null;
//		positionPathfinding = null;
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
		double delta = cinematique.orientation-angle % (2*Math.PI);
		delta = Math.abs(delta);
		if(delta > Math.PI)
			delta = 2*Math.PI - delta;
		return delta;
	}
	
	@Override
    public void tourner(double angle, Speed vitesse)
	{
		// TODO: avec les trajectoires courbes, les durées changent
		// et la marche arrière automatique?
		double delta = calculateDelta(angle);
		cinematique.orientation = angle;
		date += delta*vitesse.rotationalSpeed + sleepTourneDuration;
	}
	
	@Override
	public void sleep(long duree, ArrayList<Hook> hooks) throws FinMatchException 
	{
		this.date += duree;
		checkHooks(cinematique.getPosition(), cinematique.getPosition(), hooks);
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
		cinematique.enMarcheAvant = !cinematique.enMarcheAvant;
	}

	/**
	 * UTILISE UNIQUEMENT PAR LES TESTS
	 */
	public void reinitDate()
	{
		date = 0;
	}

	public void suitArcCourbeClotho(ArcCourbeClotho came_from_arc)
	{
		// TODO compléter
		came_from_arc.arcselems[ClothoidesComputer.NB_POINTS-1].copy(cinematique);
	}

	public void suitArcCourbeCubique(ArcCourbeCubique came_from_arc)
	{
		// TODO compléter
		came_from_arc.arcs.get(came_from_arc.arcs.size()-1).copy(cinematique);
	}
	
	public Cinematique getCinematique()
	{
		return cinematique;
	}
	
}
