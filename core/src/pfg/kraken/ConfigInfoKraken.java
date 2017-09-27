/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;


import java.util.ArrayList;
import java.util.List;

/**
 * Informations accessibles par la config
 * Les informations de config.ini surchargent celles-ci
 * Certaines valeurs sont constantes, ce qui signifie qu'elles ne peuvent être
 * modifiées dynamiquement au cours d'un match.
 * Chaque variable a une valeur par défaut, afin de pouvoir lancer le programme
 * sans config.ini.
 * 
 * @author pf
 *
 */

import pfg.config.ConfigInfo;

public enum ConfigInfoKraken implements ConfigInfo
{
	/**
	 * Config du navmesh
	 */
	NAVMESH_OBSTACLE_DILATATION(100), // dilatation des obstacles dans le D* Lite.
									// Comme c'est une heuristique, on peut
									// prendre plus petit que la vraie valeur
	LARGEST_TRIANGLE_AREA_IN_NAVMESH(20000), // in mm²
	LONGEST_EDGE_IN_NAVMESH(200), // in mm
	NAVMESH_FILENAME("navmesh.krk"), // the filename of the navmesh
	
	/**
	 * Research and mechanical parameter
	 */
	MAX_CURVATURE_DERIVATIVE(5), // maximal curvature derivative, in m⁻¹s⁻¹
	MAX_LATERAL_ACCELERATION(3), // maximal lateral acceleration, in m/s²
	DEFAULT_MAX_SPEED(1), // in m/s (or mm/ms)
	MAX_CURVATURE(5), // in m⁻¹
	STOP_DURATION(800), // temps qu'il faut au robot pour s'arrêter et repartir
						// (par exemple à cause d'un rebroussement) in ms
	SEARCH_TIMEOUT(10000), // in ms

	/**
	 * Paramètres du traitement des capteurs
	 */
//	SUPPRESSION_AUTO_OBSTACLES_FIXES(true), // si on démarre dans un obstacle
											// fixe, est-ce qu'on le vire ?

	ENABLE_DEBUG_MODE(false), // enable the debug mode
	
	/**
	 * Paramètres sur la gestion de la mémoire
	 */
	NODE_MEMORY_POOL_SIZE(20000),
	OBSTACLES_MEMORY_POOL_SIZE(50000), // nombre d'instances pour les obstacles
			
	/**
	 * Interface graphique
	 */
	ENABLE_CONSOLE(true), // enable the console
	GRAPHIC_HEURISTIC(false), // affichage des orientations heuristiques
								// données par le D* Lite
	GRAPHIC_ENABLE(false), // désactive tout affichage si faux (empêche le
							// thread d'affichage de se lancer)
	GRAPHIC_D_STAR_LITE(false), // affiche les calculs du D* Lite
	GRAPHIC_TENTACLES(false), // affiche les trajectoires temporaires
//	GRAPHIC_TRAJECTORY_FINAL(true), // affiche les trajectoires
	GRAPHIC_ROBOT_COLLISION(false), // affiche les obstacles du robot lors de la
									// vérification des collisions
//	GRAPHIC_ROBOT_AND_SENSORS(true), // affiche le robot et ses capteurs
	GRAPHIC_NAVMESH(false), // show the navmesh ?

	ALLOW_BACKWARD_MOTION(true), // allow the pathfinding to find a path with backward motion by default
	
	CONSOLE_NB_ROWS(40), // nombre de lignes dans la console affichée
	CONSOLE_NB_COLUMNS(20), // nombre de colonnes dans la console affichée
	
	DISPLAY_GRID(true),
	BACKGROUND_PATH(""), // background path ; empty if none	
	SIZE_X_WINDOW(900), // taille par défaut (sans image) de la fenêtre
	SIZE_Y_WINDOW(600), // taille par défaut (sans image) de la fenêtre
	
	/**
	 * Log parameters
	 */
	SAVE_LOG(false), // sauvegarde les logs dans un fichier externe
	FAST_LOG(false), // log rapide, sans reflection
	STDOUT_LOG(false); // log into the stdout

	private Object defaultValue;
	public volatile boolean uptodate;

	public static List<ConfigInfo> getGraphicConfigInfo()
	{
		List<ConfigInfo> out = new ArrayList<ConfigInfo>();
		for(ConfigInfoKraken c : values())
			if(c.toString().startsWith("GRAPHIC_"))
				out.add(c);
		return out;
	}
	
	/**
	 * Par défaut, une valeur est constante
	 * 
	 * @param defaultValue
	 */
	private ConfigInfoKraken(Object defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	public Object getDefaultValue()
	{
		return defaultValue;
	}
}
