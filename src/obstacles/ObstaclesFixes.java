package obstacles;

import obstacles.types.Obstacle;
import obstacles.types.ObstacleCircular;
import obstacles.types.ObstacleRectangular;
import utils.Vec2;
import utils.permissions.ReadOnly;

/**
 * Enumération des obstacles fixes.
 * Afin que les obstacles fixes soient facilement modifiables d'une coupe à l'autre.
 * @author pf
 *
 */

public enum ObstaclesFixes {

	// bords
    BORD_BAS(new ObstacleRectangular(new Vec2<ReadOnly>(0,0),3000,5), false),
    BORD_GAUCHE(new ObstacleRectangular(new Vec2<ReadOnly>(-1500,1000),5,2000), false),
    BORD_DROITE(new ObstacleRectangular(new Vec2<ReadOnly>(1500,1000),5,2000), false),
    BORD_HAUT(new ObstacleRectangular(new Vec2<ReadOnly>(0,2000),3000,5), false);

    private final Obstacle obstacle;
    private boolean visible;
    public static final ObstaclesFixes[] values;
    public static final ObstaclesFixes[] obstaclesFixesVisibles;
    
    static
    {
    	int nbVisibles = 0;
    	values = values();
    	for(ObstaclesFixes o : values)
    		if(o.visible)
    			nbVisibles++;
    	obstaclesFixesVisibles = new ObstaclesFixes[nbVisibles];
    	int i = 0;
    	for(ObstaclesFixes o : values)
    		if(o.visible)
    			obstaclesFixesVisibles[i++] = o;
    }

    private ObstaclesFixes(ObstacleRectangular obstacle, boolean visible)
    {
    	this.obstacle = obstacle;
    	this.visible = visible;
    }

    private ObstaclesFixes(ObstacleCircular obstacle, boolean visible)
    {
    	this.obstacle = obstacle;
    	this.visible = visible;
    }

    public Obstacle getObstacle()
    {
    	return obstacle;
    }

}
