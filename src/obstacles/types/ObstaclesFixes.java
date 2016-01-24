package obstacles.types;

import permissions.ReadOnly;
import utils.Vec2;

/**
 * Enumération des obstacles fixes.
 * Afin que les obstacles fixes soient facilement modifiables d'une coupe à l'autre.
 * @author pf
 *
 */

public enum ObstaclesFixes {

	DEPOT_SABLE_1(new ObstacleRectangular(new Vec2<ReadOnly>(0,1250), 1200, 22)),
	DEPOT_SABLE_2(new ObstacleRectangular(new Vec2<ReadOnly>(0,950), 48, 600)),

	REGLETTE_DUNE_1(new ObstacleRectangular(new Vec2<ReadOnly>(-700,1900), 22, 200)),
	REGLETTE_DUNE_2(new ObstacleRectangular(new Vec2<ReadOnly>(700,1900), 22, 200)),

	ROCHER_1(new ObstacleCircular(new Vec2<ReadOnly>(-1500,0), 250)),
	ROCHER_2(new ObstacleCircular(new Vec2<ReadOnly>(1500,0), 250)),

	CABINE_1(new ObstacleRectangular(new Vec2<ReadOnly>(-1200,1950), 100, 100)),
	CABINE_2(new ObstacleRectangular(new Vec2<ReadOnly>(-900,1950), 100, 100)),
	CABINE_3(new ObstacleRectangular(new Vec2<ReadOnly>(1200,1950), 100, 100)),
	CABINE_4(new ObstacleRectangular(new Vec2<ReadOnly>(900,1950), 100, 100)),

	REPOSE_FILET_1(new ObstacleRectangular(new Vec2<ReadOnly>(-561,11), 22, 22)),
	REPOSE_FILET_2(new ObstacleRectangular(new Vec2<ReadOnly>(561,11), 22, 22)),
	
	// bords
    BORD_BAS(new ObstacleRectangular(new Vec2<ReadOnly>(0,0),3000,0)),
    BORD_GAUCHE(new ObstacleRectangular(new Vec2<ReadOnly>(-1500,1000),0,2000)),
    BORD_DROITE(new ObstacleRectangular(new Vec2<ReadOnly>(1500,1000),0,2000)),
    BORD_HAUT(new ObstacleRectangular(new Vec2<ReadOnly>(0,2000),3000,0));

    private final Obstacle obstacle;
    public static final ObstaclesFixes[] values;
    
    static
    {
    	values = values();
    }
    
    private ObstaclesFixes(ObstacleRectangular obstacle)
    {
    	this.obstacle = obstacle;
    }

    private ObstaclesFixes(ObstacleCircular obstacle)
    {
    	this.obstacle = obstacle;
    }

    public Obstacle getObstacle()
    {
    	return obstacle;
    }

}
