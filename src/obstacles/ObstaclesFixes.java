package obstacles;

import permissions.ReadOnly;
import utils.Vec2;

/**
 * Enumération des obstacles fixes.
 * Afin que les obstacles fixes soient facilement modifiables d'une coupe à l'autre.
 * @author pf
 *
 */

public enum ObstaclesFixes {

    TEST(new ObstacleCircular(new Vec2<ReadOnly>(200,1200),200)), // plaque rouge

    // DEPENDS_ON_RULES
    PLASQUE_ROUGE(new ObstacleRectangular(new Vec2<ReadOnly>(0,100),800,200)), // plaque rouge
    ESCALIER(new ObstacleRectangular(new Vec2<ReadOnly>(0,2000-580/2),1066,580)), // escalier
    BANDE_1(new ObstacleRectangular(new Vec2<ReadOnly>(-1500+400/2,1200),400,22)), // bandes de bois zone de départ
    BANDE_2(new ObstacleRectangular(new Vec2<ReadOnly>(1500-400/2,1200),400,22)),
    BANDE_3(new ObstacleRectangular(new Vec2<ReadOnly>(-1500+400/2,800),400,22)),
    BANDE_4(new ObstacleRectangular(new Vec2<ReadOnly>(1500-400/2,800),400,22)),
    DISTRIBUTEUR_1(new ObstacleRectangular(new Vec2<ReadOnly>(-1200+50/2,2000-50/2),50,50)), // distributeurs
    DISTRIBUTEUR_2(new ObstacleRectangular(new Vec2<ReadOnly>(-900+50/2,2000-50/2),50,50)),
    DISTRIBUTEUR_3(new ObstacleRectangular(new Vec2<ReadOnly>(900-50/2,2000-50/2),50,50)),
    DISTRIBUTEUR_4(new ObstacleRectangular(new Vec2<ReadOnly>(1200-50/2,2000-50/2),50,50)),

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
