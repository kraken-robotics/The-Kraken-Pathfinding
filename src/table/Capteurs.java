package table;

import permissions.ReadOnly;
import permissions.ReadWrite;
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
	public static final double angleCone = 35.*Math.PI/180.;
	
	private static final double cos = Math.cos(Math.PI/2-angleCone);
	
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
	 * Le point est dans le référentiel du robot
	 * @param point
	 * @param nbCapteur
	 * @return
	 */
	public static boolean canBeSeen(Vec2<ReadOnly> point, int nbCapteur)
	{
		Vec2<ReadOnly> tmp = point.minusNewVector(positionsRelatives[nbCapteur]).getReadOnly();
		return tmp.dot(cones[nbCapteur][0]) > 0 && tmp.dot(cones[nbCapteur][1]) > 0 && tmp.dot(cones[nbCapteur][2]) > 0;
	}
	
	/**
	 * Retourne la position ajustée de l'obstacle vu.
	 * Cette position est donnée dans le référentiel du capteur qui voit
	 * @param nbCouple
	 * @param gauche: est-ce le capteur gauche qui voit l'obstacle?
	 * @param mesure
	 * @return
	 */
	public static Vec2<ReadWrite> getPositionAjustee(int nbCouple, int distanceAjustement, boolean gauche, int mesure)
	{
		int capteurQuiVoit = coupleCapteurs[nbCouple][gauche?0:1];
		int capteurQuiNeVoitPas = coupleCapteurs[nbCouple][gauche?1:0];
		Vec2<ReadWrite> intersectionPoint;
		int distance = coupleCapteurs[nbCouple][2];
		double b = -2.*distance*cos;
		double c = distance*distance - mesure*mesure;
		double delta = b*b-4*c;
		
/*		System.out.println("gauche: "+gauche);
		System.out.println("distance entre capteurs: "+distance);
		System.out.println("mesure: "+mesure);
		System.out.println("cos: "+cos);
	*/	
		if(delta < 0)
		{
			System.out.println("Pas d'intersection: delta négatif");
			return null;
		}

		// On prend la plus grande solution
		double s = (-b+Math.sqrt(delta))/2;
		if(s <= 0)
		{
			System.out.println("Pas d'intersection: distance négative");
			return null;
		}
			
/*		System.out.println("Distance 1: "+s);
		System.out.println("Distance 2: "+(-b-Math.sqrt(delta))/2);
*/
		if(gauche)
			intersectionPoint = new Vec2<ReadWrite>((int)s, angleCone);
		else
			intersectionPoint = new Vec2<ReadWrite>((int)s, -angleCone);

//		System.out.println("Point avant repère: "+intersectionPoint);
		
		// changement de repère
		Vec2.plus(intersectionPoint, positionsRelatives[capteurQuiNeVoitPas]);

//		System.out.println("Point après repère: "+intersectionPoint);
		
//		System.out.println("Distance: "+intersectionPoint.distance(positionsRelatives[capteurQuiVoit]));
		
		if(!canBeSeen(intersectionPoint.getReadOnly(), capteurQuiVoit))
		{
			System.out.println("Ce point n'est pas visible");
			return null; // le point d'intersection n'est pas vu par le capteur qui voit l'obstacle
		}
		
//		System.out.println("Autre point: "+new Vec2<ReadWrite>(mesure, gauche?angleCone:-angleCone));
		
		Vec2.plus(intersectionPoint, Vec2.plus(new Vec2<ReadWrite>(mesure, gauche?angleCone:-angleCone), positionsRelatives[capteurQuiVoit]));
		double longueur = intersectionPoint.length();
		Vec2.scalar(intersectionPoint, (mesure+distanceAjustement)/longueur);
		
		return intersectionPoint;
	}
	
}
