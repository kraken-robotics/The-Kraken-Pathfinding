package obstacles.types;
import java.util.ArrayList;

import pathfinding.GridSpace;
import permissions.ReadOnly;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2;

/**
 * Superclasse abstraite des obstacles.
 * @author pf
 *
 */

public abstract class Obstacle
{
	public final Vec2<ReadOnly> position;
	protected int distance_dilatation;
	protected static Log log;
	
    protected static int largeurRobot; // le sens gauche-droite du robot
    protected static int longueurRobot; // le sens avant-arrière du robot
    protected static int rayonRobot;
    protected static int marge;
	protected static double anglePas; // utilisé pour les calculs de collision pendant les rotations	
	protected static ArrayList<Vec2<ReadOnly>> pourtourGrillePatron; // utilisé pour les obstacles mobiles et le gridspace
	
	public static void setLog(Log log)
	{
		Obstacle.log = log;
	}
	
	public static void useConfig(Config config)
	{
		largeurRobot = config.getInt(ConfigInfo.LARGEUR_ROBOT_AXE_GAUCHE_DROITE);
		longueurRobot = config.getInt(ConfigInfo.LONGUEUR_ROBOT_AXE_AVANT_ARRIERE);
		rayonRobot = config.getInt(ConfigInfo.RAYON_ROBOT);
		marge = config.getInt(ConfigInfo.MARGE);
		anglePas = Math.PI-2*Math.atan2(largeurRobot, longueurRobot);

		pourtourGrillePatron = new ArrayList<Vec2<ReadOnly>>();
//		int enTout = 0;
		int rayonObstacleDilate = config.getInt(ConfigInfo.RAYON_ROBOT_ADVERSE) + rayonRobot;
		int squaredRayonObstacleDilate = rayonObstacleDilate * rayonObstacleDilate;
		;
		for(int x = (int) Math.round(- rayonObstacleDilate/GridSpace.DISTANCE_ENTRE_DEUX_POINTS - 1) ; x <= (int) Math.round(rayonObstacleDilate/GridSpace.DISTANCE_ENTRE_DEUX_POINTS + 1) ; x++)
			for(int y = (int) Math.round(- rayonObstacleDilate/GridSpace.DISTANCE_ENTRE_DEUX_POINTS - 1) ; y <= (int) Math.round(rayonObstacleDilate/GridSpace.DISTANCE_ENTRE_DEUX_POINTS + 1) ; y++)
			{
				Vec2<ReadOnly> point = new Vec2<ReadOnly>((int) Math.round(GridSpace.DISTANCE_ENTRE_DEUX_POINTS*x), (int) Math.round(GridSpace.DISTANCE_ENTRE_DEUX_POINTS*y));
				if(point.squaredLength() < squaredRayonObstacleDilate)
				{
//					enTout++;
					for(int x2 = -1 ; x2 <= 1 ; x2++)
						for(int y2 = -1 ; y2 <= 1 ; y2++)
							if((new Vec2<ReadOnly>((int) Math.round(GridSpace.DISTANCE_ENTRE_DEUX_POINTS*(x2+x)), (int) Math.round(GridSpace.DISTANCE_ENTRE_DEUX_POINTS*(y2+y)))).squaredLength() >= squaredRayonObstacleDilate)
							{
//								log.debug("Point du patron : "+point+" ie "+new Vec2<ReadOnly>(x, y));
								pourtourGrillePatron.add(new Vec2<ReadOnly>(x, y));
								x2 = 10;
								y2 = 10;								
							}
				}
			}
//		log.debug("Taille patron : "+pourtourGrillePatron.size());
//		log.debug("Taille totale de l'obstacle : "+enTout);
	}
	
	public Obstacle (Vec2<ReadOnly> position)
	{
		this.position = position;
	}
	
	public abstract boolean isProcheObstacle(Vec2<ReadOnly> point, int distance);
	public abstract boolean isInObstacle(Vec2<ReadOnly> point);
	
/*
	public static final void setPosition(Obstacle o, Vec2<ReadOnly> v)
	{
		Vec2.copy(v, o.position);
	}
*/
	public String toString()
	{
		return "Obstacle en "+position;
	}
	
}
