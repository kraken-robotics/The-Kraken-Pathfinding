/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package obstacles.types;

import graphic.printable.Couleur;
import utils.Vec2RO;

/**
 * Enumération des obstacles fixes.
 * Afin que les obstacles fixes soient facilement modifiables d'une coupe à
 * l'autre.
 * 
 * @author pf
 *
 */

public enum ObstaclesFixes
{
	// TODO visibilité à mettre à jour selon le robot / les capteurs
	ZONE_DEPART_ENNEMI(new ObstacleRectangular(new Vec2RO(1070 / 2 - 1500, 2000 - 360 / 2), 1070, 360, Couleur.OBSTACLES), false, false),
	BASCULE(new ObstacleRectangular(new Vec2RO(1140 - 350 / 2, 2000 - 360 / 2), 350, 360, Couleur.OBSTACLES), true, false),
	ZONE_DEPART_A_NOUS(new ObstacleRectangular(new Vec2RO(1500 - 360 / 2, 2000 - 360 / 2), 360, 360, Couleur.OBSTACLES), false, false),

	TASSEAU_DROITE(new ObstacleRectangular(new Vec2RO(1500 - 710 / 2, 2000 - 360 - 11), 710, 22, Couleur.OBSTACLES), true, false),
	TASSEAU_GAUCHE(new ObstacleRectangular(new Vec2RO(-1500 + 710 / 2, 2000 - 360 - 11), 710, 22, Couleur.OBSTACLES), true, false),

	BAC_GAUCHE(new ObstacleRectangular(new Vec2RO(54 - 1500, 1075), 108, 494, Couleur.OBSTACLES), false, false),
	BAC_DROIT(new ObstacleRectangular(new Vec2RO(1500 - 54, 1075), 108, 494, Couleur.OBSTACLES), false, false),

	CENTRE_VILLAGE(new ObstacleCircular(new Vec2RO(0, 0), 200, Couleur.OBSTACLES), true, false),

	SUPPORT_MODULE_GAUCHE(new ObstacleRectangular(new Vec2RO(0, 500).rotateNewVector(-Math.PI / 4, new Vec2RO(0, 0)), 140, 600, -Math.PI / 4, Couleur.OBSTACLES), true, false),
	SUPPORT_MODULE_MILIEU(new ObstacleRectangular(new Vec2RO(0, 500), 140, 600, Couleur.OBSTACLES), true, false),
	SUPPORT_MODULE_DROITE(new ObstacleRectangular(new Vec2RO(0, 500).rotateNewVector(Math.PI / 4, new Vec2RO(0, 0)), 140, 600, Math.PI / 4, Couleur.OBSTACLES), true, false),

	PETIT_CRATERE_HAUT_GAUCHE(new ObstacleCircular(new Vec2RO(650 - 1500, 2000 - 540), 125, Couleur.OBSTACLES), false, false),
	PETIT_CRATERE_HAUT_DROITE(new ObstacleCircular(new Vec2RO(1500 - 650, 2000 - 540), 125, Couleur.OBSTACLES), false, false),

	PETIT_CRATERE_BAS_GAUCHE(new ObstacleCircular(new Vec2RO(1070 - 1500, 2000 - 1870), 125, Couleur.OBSTACLES), false, false),
	PETIT_CRATERE_BAS_DROITE(new ObstacleCircular(new Vec2RO(1500 - 1070, 2000 - 1870), 125, Couleur.OBSTACLES), false, false),

	// Les gros cratères ne sont pas des obstacles pour le pathfinding
	GROS_CRATERE_GAUCHE(new ObstacleCircular(new Vec2RO(-1500, 0), 540, Couleur.OBSTACLES), false, false),
	GROS_CRATERE_DROITE(new ObstacleCircular(new Vec2RO(1500, 0), 540, Couleur.OBSTACLES), false, false),

	FUSEE_HAUT_GAUCHE(new ObstacleCircular(new Vec2RO(-350, 1960), 40, Couleur.OBSTACLES), true, true),
	FUSEE_HAUT_DROITE(new ObstacleCircular(new Vec2RO(350, 1960), 40, Couleur.OBSTACLES), true, true),

	FUSEE_BORD_GAUCHE(new ObstacleCircular(new Vec2RO(-1460, 650), 40, Couleur.OBSTACLES), true, true),
	FUSEE_BORD_DROIT(new ObstacleCircular(new Vec2RO(1460, 650), 40, Couleur.OBSTACLES), true, true),

	// bords
	BORD_BAS(new ObstacleRectangular(new Vec2RO(0, 0), 3000, 5, Couleur.OBSTACLES), true, false),
	BORD_GAUCHE(new ObstacleRectangular(new Vec2RO(-1500, 1000), 5, 2000, Couleur.OBSTACLES), true, false),
	BORD_DROITE(new ObstacleRectangular(new Vec2RO(1500, 1000), 5, 2000, Couleur.OBSTACLES), true, false),
	BORD_HAUT(new ObstacleRectangular(new Vec2RO(0, 2000), 3000, 5, Couleur.OBSTACLES), true, false);

	private final Obstacle obstacle;
	private final boolean[] visible = new boolean[2];
	public final boolean bordure;

	private ObstaclesFixes(Obstacle obstacle, boolean visibleBas, boolean visibleHaut)
	{
		this.obstacle = obstacle;
		visible[0] = visibleBas;
		visible[1] = visibleHaut;
		bordure = name().startsWith("BORD_");
	}

	public Obstacle getObstacle()
	{
		return obstacle;
	}

	/**
	 * Cet obstacle est-il visible pour un capteur surélevé ou non ?
	 * 
	 * @param sureleve
	 * @return
	 */
	public boolean isVisible(boolean sureleve)
	{
		return visible[sureleve ? 1 : 0];
	}

}
