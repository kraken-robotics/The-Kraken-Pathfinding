package obstacles;

import obstacles.types.Obstacle;
import obstacles.types.ObstacleCircular;
import obstacles.types.ObstacleRectangular;
import permissions.ReadOnly;
import utils.Vec2;

/**
 * Enumération des obstacles fixes.
 * Afin que les obstacles fixes soient facilement modifiables d'une coupe à l'autre.
 * @author pf
 *
 */

public enum ObstaclesFixes {

	DEPOT_SABLE_1(new ObstacleRectangular(new Vec2<ReadOnly>(0,1250), 1200, 22), false),
	DEPOT_SABLE_2(new ObstacleRectangular(new Vec2<ReadOnly>(0,950), 48, 600), true),

	REGLETTE_DUNE_1(new ObstacleRectangular(new Vec2<ReadOnly>(-700,1900), 22, 200), false),
	REGLETTE_DUNE_2(new ObstacleRectangular(new Vec2<ReadOnly>(700,1900), 22, 200), false),

	ROCHER_1(new ObstacleCircular(new Vec2<ReadOnly>(-1500,0), 250), false),
	ROCHER_2(new ObstacleCircular(new Vec2<ReadOnly>(1500,0), 250), false),

	CABINE_1(new ObstacleRectangular(new Vec2<ReadOnly>(-1200,1950), 100, 100), true),
	CABINE_2(new ObstacleRectangular(new Vec2<ReadOnly>(-900,1950), 100, 100), true),
	CABINE_3(new ObstacleRectangular(new Vec2<ReadOnly>(1200,1950), 100, 100), true),
	CABINE_4(new ObstacleRectangular(new Vec2<ReadOnly>(900,1950), 100, 100), true),

	REPOSE_FILET_1(new ObstacleRectangular(new Vec2<ReadOnly>(-561,11), 22, 22), false),
	REPOSE_FILET_2(new ObstacleRectangular(new Vec2<ReadOnly>(561,11), 22, 22), false),
	
	// bords
    BORD_BAS(new ObstacleRectangular(new Vec2<ReadOnly>(0,0),3000,0), false),
    BORD_GAUCHE(new ObstacleRectangular(new Vec2<ReadOnly>(-1500,1000),0,2000), false),
    BORD_DROITE(new ObstacleRectangular(new Vec2<ReadOnly>(1500,1000),0,2000), false),
    BORD_HAUT(new ObstacleRectangular(new Vec2<ReadOnly>(0,2000),3000,0), false);

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
