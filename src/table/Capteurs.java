package table;

import permissions.ReadOnly;
import utils.Vec2;

/**
 * Cette classe contient les informations sur la situation
 * spatiale des capteurs sur le robot.
 * @author pf
 *
 */

// TODO: faire ça proprement

@SuppressWarnings("unchecked")
public class Capteurs {
	// DEPENDS ON ROBOT
	
	public static final int nbCapteurs = 2;
	
	public static final int nbCouples = 1;
	
	/**
	 * Les ultrasons ont un cône de 35°
	 */
	private static final double angleCone = 35.*Math.PI/180.;
	
	/**
	 * Les positions relatives des capteurs par rapport au centre du
	 * robot lorsque celui-ci a une orientation nulle.
	 */
	public static final Vec2<ReadOnly>[] positionsRelatives;
	
	public static final Vec2<ReadOnly>[][] cones;
	
	/**
	 * coupleCapteurs[i] est un tableau qui contient les numéros des
	 * capteurs du couple i ainsi que la distance entre ces deux capteurs
	 */
	public static int[][] coupleCapteurs;

	/**
	 * L'orientation des capteurs lorsque le robot a une orientation nulle
	 */
	public static double[] orientationsRelatives;

	static
	{
		positionsRelatives = new Vec2[nbCapteurs];
		positionsRelatives[0] = new Vec2<ReadOnly>(50, 100);
		positionsRelatives[1] = new Vec2<ReadOnly>(50, -100);
		
		orientationsRelatives = new double[nbCapteurs];
		orientationsRelatives[0] = 0.;
		orientationsRelatives[1] = 0.;
		
		cones = new Vec2[nbCapteurs][3];
		for(int i = 0; i < nbCapteurs; i++)
		{
			cones[i][0] = new Vec2<ReadOnly>(orientationsRelatives[i]);
			cones[i][1] = new Vec2<ReadOnly>(orientationsRelatives[i]+Math.PI/2-angleCone);
			cones[i][2] = new Vec2<ReadOnly>(orientationsRelatives[i]-Math.PI/2+angleCone);
		}
		
		coupleCapteurs = new int[nbCouples][3];
		coupleCapteurs[0][0] = 0;
		coupleCapteurs[0][1] = 1;
		coupleCapteurs[0][2] = (int)positionsRelatives[coupleCapteurs[0][0]].distance(positionsRelatives[coupleCapteurs[0][1]]);
		System.out.println("distance: "+coupleCapteurs[0][2]);
	}
	
	/**
	 * Ce point peut-il être vu par ce capteur?
	 * @param point
	 * @param nbCapteur
	 * @return
	 */
	public static boolean canBeSeen(Vec2<ReadOnly> point, int nbCapteur)
	{
		Vec2<ReadOnly> tmp = point.plusNewVector(positionsRelatives[nbCapteur]).getReadOnly();
		return tmp.dot(cones[nbCapteur][0]) > 0 && tmp.dot(cones[nbCapteur][1]) > 0 && tmp.dot(cones[nbCapteur][2]) > 0;
	}
	
}
