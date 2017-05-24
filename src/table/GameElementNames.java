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

package table;

import capteurs.CapteursRobot;
import graphic.printable.Couleur;
import obstacles.types.ObstacleCircular;
import obstacles.types.ObstacleInterface;
import obstacles.types.ObstacleMasque;
import utils.Vec2RO;

/**
 * Enumérations contenant tous les éléments de jeux
 * 
 * @author pf
 *
 */

public enum GameElementNames
{
	MINERAI_CRATERE_HAUT_GAUCHE(new ObstacleCircular(new Vec2RO(650 - 1500, 2000 - 555), 125, Couleur.GAME_ELEMENT), -Math.PI / 2, new Double[]{-Math.PI, 0., 2.6, Math.PI}),
	MINERAI_CRATERE_HAUT_DROITE(new ObstacleCircular(new Vec2RO(1500 - 650, 2000 - 555), 125, Couleur.GAME_ELEMENT), -Math.PI / 2, new Double[]{-Math.PI, 0.54}),

	MINERAI_CRATERE_BAS_GAUCHE(new ObstacleCircular(new Vec2RO(1070 - 1500, 2000 - 1870), 125, Couleur.GAME_ELEMENT), 3 * Math.PI / 4, null),
	MINERAI_CRATERE_BAS_DROITE(new ObstacleCircular(new Vec2RO(1500 - 1070, 2000 - 1870), 125, Couleur.GAME_ELEMENT), Math.PI / 4, null),

	MINERAI_GROS_CRATERE_GAUCHE_1(new ObstacleCircular(new Vec2RO(-1500 + 125, 125), 125, Couleur.GAME_ELEMENT), Math.PI / 4, null),
	MINERAI_GROS_CRATERE_GAUCHE_2(new ObstacleCircular(new Vec2RO(-1500 + 125, 125 + 220), 125, Couleur.GAME_ELEMENT), Math.PI / 4, null),
	MINERAI_GROS_CRATERE_GAUCHE_3(new ObstacleCircular(new Vec2RO(-1500 + 125 + 250 * 0.5, 150 + 250 * 0.5), 125, Couleur.GAME_ELEMENT), Math.PI / 4, null),
	MINERAI_GROS_CRATERE_GAUCHE_4(new ObstacleCircular(new Vec2RO(-1500 + 125 + 220, 125), 125, Couleur.GAME_ELEMENT), Math.PI / 4, null),

	MINERAI_GROS_CRATERE_DROITE_1(new ObstacleCircular(new Vec2RO(1500 - 125, 125), 125, Couleur.GAME_ELEMENT), 3 * Math.PI / 4, null),
	MINERAI_GROS_CRATERE_DROITE_2(new ObstacleCircular(new Vec2RO(1500 - 125, 125 + 220), 125, Couleur.GAME_ELEMENT), 3 * Math.PI / 4, null),
	MINERAI_GROS_CRATERE_DROITE_3(new ObstacleCircular(new Vec2RO(1500 - (125 + 250 * 0.5), 150 + 250 * 0.5), 125, Couleur.GAME_ELEMENT), 3 * Math.PI / 4, null),
	MINERAI_GROS_CRATERE_DROITE_4(new ObstacleCircular(new Vec2RO(1500 - (125 + 220), 125), 125, Couleur.GAME_ELEMENT), 3 * Math.PI / 4, null),

	// CYLINDRE_1_G(new ObstacleMasque(new Vec2RO(950-1500, 1800), 32,
	// Couleur.GAME_ELEMENT, null)),
	// CYLINDRE_1_D(new ObstacleMasque(new Vec2RO(1500-950, 1800), 32,
	// Couleur.GAME_ELEMENT, null)),
	CYLINDRE_2_G(new ObstacleMasque(new ObstacleCircular(new Vec2RO(200 - 1500, 1400), 32, Couleur.GAME_ELEMENT), null)),
	CYLINDRE_2_D(new ObstacleMasque(new ObstacleCircular(new Vec2RO(1500 - 200, 1400), 32, Couleur.GAME_ELEMENT), null)),
	CYLINDRE_3_G(new ObstacleMasque(new ObstacleCircular(new Vec2RO(1000 - 1500, 1400), 32, Couleur.GAME_ELEMENT), null)),
	CYLINDRE_3_D(new ObstacleMasque(new ObstacleCircular(new Vec2RO(1500 - 1000, 1400), 32, Couleur.GAME_ELEMENT), null)),
	CYLINDRE_4_G(new ObstacleMasque(new ObstacleCircular(new Vec2RO(500 - 1500, 900), 32, Couleur.GAME_ELEMENT), null)),
	CYLINDRE_4_D(new ObstacleMasque(new ObstacleCircular(new Vec2RO(1500 - 500, 900), 32, Couleur.GAME_ELEMENT), null)),
	CYLINDRE_5_G(new ObstacleMasque(new ObstacleCircular(new Vec2RO(900 - 1500, 600), 32, Couleur.GAME_ELEMENT), null)),
	CYLINDRE_5_D(new ObstacleMasque(new ObstacleCircular(new Vec2RO(1500 - 900, 600), 32, Couleur.GAME_ELEMENT), null)),
	CYLINDRE_6_G(new ObstacleMasque(new ObstacleCircular(new Vec2RO(800 - 1500, 150), 32, Couleur.GAME_ELEMENT), null)),
	CYLINDRE_6_D(new ObstacleMasque(new ObstacleCircular(new Vec2RO(1500 - 800, 150), 32, Couleur.GAME_ELEMENT), null));

	public final ObstacleInterface obstacle; // il se trouve qu'ils sont tous
												// circulaires…
	public final boolean aUnMasque;
	public final boolean cylindre;
	public final double orientationArriveeDStarLite;
	public final Double[] anglesAttaque;

	private GameElementNames(ObstacleInterface obs, double orientationArriveeDStarLite, Double[] anglesAttaque)
	{
		this.anglesAttaque = anglesAttaque;
		cylindre = toString().startsWith("CYLINDRE");
		aUnMasque = obs instanceof ObstacleMasque;
		obstacle = obs;
		this.orientationArriveeDStarLite = orientationArriveeDStarLite;
	}

	private GameElementNames(ObstacleInterface obs)
	{
		anglesAttaque = null;
		cylindre = toString().startsWith("CYLINDRE");
		aUnMasque = obs instanceof ObstacleMasque;
		obstacle = obs;
		orientationArriveeDStarLite = 0;
	}

	public boolean isVisible(CapteursRobot c, boolean sureleve)
	{
		// cas particulier
		if(c == CapteursRobot.ToF_LONG_AVANT && cylindre)
			return true;
		// les capteurs bas les voient, les hauts non
		return !sureleve;
	}

}
