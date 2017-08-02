/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;


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
	 * Infos sur le robot
	 */
	DILATATION_ROBOT_DSTARLITE(60), // dilatation des obstacles dans le D* Lite.
									// Comme c'est une heuristique, on peut
									// prendre plus petit que la vraie valeur
	DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE(80), // distance entre le centre du robot
											// et le bord arrière du robot
											// non-déployé
	DEMI_LONGUEUR_NON_DEPLOYE_AVANT(332 - 80), // distance entre le centre du
												// robot et le bord avant du
												// robot non-déployé
	LARGEUR_NON_DEPLOYE(228), // distance entre le bord gauche et le bord droit
								// du robot non-déployé

	
	LARGEST_TRIANGLE_AREA_IN_NAVWESH(4000),

	/**
	 * Paramètres du log
	 */
	ENABLE_LOG(false), // désactivation du log
	FAST_LOG(false), // affichage plus rapide des logs
	SAUVEGARDE_LOG(false), // sauvegarde les logs dans un fichier externe
	COLORED_LOG(false), // de la couleur dans les sauvegardes de logs !

	/**
	 * Paramètres du pathfinding
	 */
	COURBURE_MAX(3), // quelle courbure maximale la trajectoire du robot
						// peut-elle avoir
	TEMPS_ARRET(800), // temps qu'il faut au robot pour s'arrêter et repartir
						// (par exemple à cause d'un rebroussement)
	DUREE_MAX_RECHERCHE_PF(10000), // durée maximale que peut prendre le
									// pathfinding

	/**
	 * Paramètres du traitement des capteurs
	 */
	DISTANCE_BETWEEN_PROXIMITY_OBSTACLES(5), // sous quelle distance
												// fusionne-t-on deux obstacles
												// de proximité ?
	RAYON_ROBOT_SUPPRESSION_OBSTACLES_FIXES(300), // dans quel rayon
													// supprime-t-on les
													// obstacles fixes si on est
													// dedans
	SUPPRESSION_AUTO_OBSTACLES_FIXES(true), // si on démarre dans un obstacle
											// fixe, est-ce qu'on le vire ?

	/**
	 * Paramètres sur la gestion de la mémoire
	 */
	NB_INSTANCES_NODE(20000),
	NB_INSTANCES_OBSTACLES(50000), // nombre d'instances pour les obstacles
									// rectangulaires

	/**
	 * Verbose
	 */
	DEBUG_REPLANIF(false), // debug verbeux sur la replanif
	DEBUG_PF(false), // affichage de plein d'infos
	DEBUG_DEBUG(false), // affichage des messages "debug"

	/**
	 * Interface graphique
	 */
	GRAPHIC_HEURISTIQUE(false), // affichage des orientations heuristiques
								// données par le D* Lite
	GRAPHIC_ENABLE(false), // désactive tout affichage si faux (empêche le
							// thread d'affichage de se lancer)
	GRAPHIC_D_STAR_LITE(false), // affiche les calculs du D* Lite
	GRAPHIC_D_STAR_LITE_FINAL(false), // affiche l'itinéraire final du D* Lite
	GRAPHIC_TRAJECTORY(false), // affiche les trajectoires temporaires
	GRAPHIC_TRAJECTORY_ALL(false), // affiche TOUTES les trajectoires
									// temporaires
	GRAPHIC_TRAJECTORY_FINAL(true), // affiche les trajectoires
	GRAPHIC_FIXED_OBSTACLES(true), // affiche les obstacles fixes
	GRAPHIC_ROBOT_COLLISION(false), // affiche les obstacles du robot lors de la
									// vérification des collisions
	GRAPHIC_ALL_OBSTACLES(false), // affiche absolument tous les obstacles créés
	GRAPHIC_ROBOT_AND_SENSORS(true), // affiche le robot et ses capteurs
	GRAPHIC_CERCLE_ARRIVEE(false), // affiche le cercle d'arrivée
	GRAPHIC_TRACE_ROBOT(true), // affiche la trace du robot
	GRAPHIC_EXTERNAL(true), // l'affichage doit-il être déporté par le serveur
							// d'affichage ?
	GRAPHIC_DIFFERENTIAL(true); // sauvegarde d'une "vidéo" pour visionner les
								// images plus tard

	private Object defaultValue;
	public boolean overridden = false;
	public volatile boolean uptodate;

	public static void unsetGraphic()
	{
		for(ConfigInfoKraken c : values())
			if(c.toString().startsWith("GRAPHIC_"))
				c.setDefaultValue(false);
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

	/**
	 * Pour les modifications de config avant même de démarrer le service de
	 * config
	 * 
	 * @param o
	 */
	public void setDefaultValue(Object o)
	{
		defaultValue = o;
		overridden = true;
	}

	@Override
	public boolean isMutable()
	{
		return !overridden;
	}

}
