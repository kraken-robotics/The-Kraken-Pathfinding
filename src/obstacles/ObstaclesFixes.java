package obstacles;

import utils.Vec2;

/**
 * Enumération des obstacles fixes.
 * Afin que les obstacles fixes soient facilement modifiables d'une coupe à l'autre.
 * @author pf
 *
 */

public enum ObstaclesFixes {

	// DEPENDS_ON_RULES
    PLASQUE_ROUGE(new Vec2(0,100),800,200), // plaque rouge
    ESCALIER(new Vec2(0,2000-580/2),1066,580), // escalier
    BANDE_1(new Vec2(-1500+400/2,1200),400,22), // bandes de bois zone de départ
    BANDE_2(new Vec2(1500-400/2,1200),400,22),
    BANDE_3(new Vec2(-1500+400/2,800),400,22),
    BANDE_4(new Vec2(1500-400/2,800),400,22),
    DISTRIBUTEUR_1(new Vec2(-1200+50/2,2000-50/2),50,50), // distributeurs
    DISTRIBUTEUR_2(new Vec2(-900+50/2,2000-50/2),50,50),
    DISTRIBUTEUR_3(new Vec2(900-50/2,2000-50/2),50,50),
    DISTRIBUTEUR_4(new Vec2(1200-50/2,2000-50/2),50,50),

    // bords
    BORD_BAS(new Vec2(0,0),3000,1),
    BORD_GAUCHE(new Vec2(-1500,1000),1,2000),
    BORD_DROITE(new Vec2(1500,1000),1,2000),
    BORD_HAUT(new Vec2(0,2000),3000,1);

    public final Vec2 position;
    public final int sizeX;
    public final int sizeY;
    
    private ObstaclesFixes(Vec2 position, int sizeX, int sizeY)
    {
    	this.position = position;
    	this.sizeX = sizeX;
    	this.sizeY = sizeY;
    }
    
}
