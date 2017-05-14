/*
Copyright (C) 2013-2017 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package config;

import robot.RobotColor;

/**
 * Informations accessibles par la config
 * Les informations de config.ini surchargent celles-ci
 * Certaines valeurs sont constantes, ce qui signifie qu'elles ne peuvent être modifiées dynamiquement au cours d'un match.
 * Chaque variable a une valeur par défaut, afin de pouvoir lancer le programme sans config.ini.
 * @author pf
 *
 */

public enum ConfigInfo {
	/**
	 * Infos sur le robot
	 */
	DILATATION_ROBOT_DSTARLITE(60), // dilatation des obstacles dans le D* Lite. Comme c'est une heuristique, on peut prendre plus petit que la vraie valeur
	CENTRE_ROTATION_ROUE_X(204), // la position du centre de rotation des roues. Est utilisé pour la rotation des capteurs
	CENTRE_ROTATION_ROUE_Y(64),
	DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE(80), // distance entre le centre du robot et le bord arrière du robot non-déployé
	DEMI_LONGUEUR_NON_DEPLOYE_AVANT(332-80), // distance entre le centre du robot et le bord avant du robot non-déployé
	LARGEUR_NON_DEPLOYE(228), // distance entre le bord gauche et le bord droit du robot non-déployé
	DILATATION_OBSTACLE_ROBOT(30), // la dilatation du robot dans l'A*. S'ajoute à gauche et à droite
	RAYON_ROBOT_SUPPRESSION_OBSTACLES_FIXES(300), // dans quel rayon supprime-t-on les obstacles fixes si on est dedans
	SUPPRESSION_AUTO_OBSTACLES_FIXES(true), // si on démarre dans un obstacle fixe, est-ce qu'on le vire ?
	
	/**
	 * Paramètres des scripts
	 */
	RAYON_CERCLE_ARRIVEE(200), // distance souhaitée entre le centre du robot et le centre du cratère
	VITESSE_ROBOT_TEST(300), // vitesse de test en mm/s
	VITESSE_ROBOT_STANDARD(500), // vitesse standard en mm/s
	VITESSE_ROBOT_BASCULE(300), // vitesse pour passer la bascule en mm/s
	VITESSE_ROBOT_REPLANIF(200), // vitesse en replanification en mm/s

	/**
	 * Paramètres du log
	 */
	FAST_LOG(false), // affichage plus rapide des logs
	SAUVEGARDE_LOG(false), // sauvegarde les logs dans un fichier externe
	AFFICHE_CONFIG(false), // affiche la configuration complète au lancement
	COLORED_LOG(false), // de la couleur dans les sauvegardes de logs !
	
	/**
	 * Infos sur l'ennemi
	 */
	LARGEUR_OBSTACLE_ENNEMI(100), // largeur du robot vu
	LONGUEUR_OBSTACLE_ENNEMI(200), // longueur / profondeur du robot vu
	
	/**
	 * Paramètres du pathfinding
	 */
	COURBURE_MAX(3), // quelle courbure maximale la trajectoire du robot peut-elle avoir
	TEMPS_ARRET(800), // temps qu'il faut au robot pour s'arrêter et repartir (par exemple à cause d'un rebroussement)
	PF_MARGE_AVANT_COLLISION(100), // combien de mm laisse-t-on au plus % avant la collision
	PF_MARGE_NECESSAIRE(50), // combien de mm le bas niveau doit-il toujours avoir
	PF_MARGE_INITIALE(100), // combien de mm garde-t-on obligatoirement au début de la replanification
	DUREE_MAX_RECHERCHE_PF(3000), // durée maximale que peut prendre le pathfinding
	TAILLE_FAISCEAU_PF(20), // combien de voisins sont ajoutés à l'openset à chaque itération. CONFIG IGNORÉE !
	NB_ESSAIS_PF(2), // nombre d'essais du pathfinding
	ALLOW_PRECOMPUTED_PATH(true), // autorise-t-on l'utilisation de chemins précalculés
	
	/**
	 * Paramètres de la série
	 */
	SERIAL_TIMEOUT(30), // quel TIMEOUT pour le protocole série des trames ? (en ms)
	BAUDRATE(115200), // le baudrate de la liaison série
	SERIAL_PORT("/dev/ttyS0"), // le port de la liaison série
	SLEEP_ENTRE_TRAMES(0),	// la durée minimale entre deux envois de nouvelles trames
	SIMULE_SERIE(false), // la série doit-elle être simulée (utile pour debug)
	
	/**
	 * Paramètres bas niveau des capteurs
	 */
	SENSORS_SEND_PERIOD(40), // période d'envoi des infos des capteurs (ms)
	SENSORS_PRESCALER(1), // sur combien de trames a-t-on les infos des capteurs
	
	/**
	 * Paramètres du traitement des capteurs
	 */
	DUREE_PEREMPTION_OBSTACLES(1000), // pendant combien de temps va-t-on garder un obstacle de proximité
	DISTANCE_MAX_ENTRE_MESURE_ET_OBJET(50), // quelle marge d'erreur autorise-t-on entre un objet et sa détection
	DISTANCE_BETWEEN_PROXIMITY_OBSTACLES(5), // sous quelle distance fusionne-t-on deux obstacles de proximité ?
	IMPRECISION_MAX_POSITION(20.), // quelle imprecision maximale sur la position du robot peut-on attendre (en mm)
	IMPRECISION_MAX_ORIENTATION(0.1), // quelle imprecision maximale sur l'angle du robot peut-on attendre (en radians)
	TAILLE_BUFFER_RECALAGE(5), // combien de mesures sont nécessaires pour obtenir une correction de recalage
	PEREMPTION_CORRECTION(100), // temps maximale entre deux mesures de correction au sein d'un même buffer (en ms)
	ENABLE_CORRECTION(true), // la correction de position et d'orientation est-elle activée ?
	
	/**
	 * Paramètres sur la gestion de la mémoire
	 */
	NB_INSTANCES_NODE(20000),
	NB_INSTANCES_OBSTACLES(50000), // nombre d'instances pour les obstacles rectangulaires
	
	/**
	 * Verbose
	 */
	DEBUG_SERIE_TRAME(false), // debug verbeux sur le contenu des trames
	DEBUG_SERIE(false), // debug sur la série
	DEBUG_CAPTEURS(false), // debug verbeux sur les capteurs
	DEBUG_REPLANIF(false), // debug verbeux sur la replanif
	DEBUG_ACTIONNEURS(false), // debug verbeux sur les actionneurs
	DEBUG_CACHE(false), // debug du cache de chemins
	DEBUG_PF(false), // affichage de plein d'infos
	DEBUG_DEBUG(false), // affichage des messages "debug"

	/**
	 * Graphe de dépendances
	 */
	GENERATE_DEPENDENCY_GRAPH(false), // génère le graphe des dépendances
	
	/**
	 * Interface graphique
	 */
	GRAPHIC_HEURISTIQUE(false), // affichage des orientations heuristiques données par le D* Lite
	GRAPHIC_ENABLE(false), // désactive tout affichage si faux (empêche le thread d'affichage de se lancer)
	GRAPHIC_D_STAR_LITE(false), // affiche les calculs du D* Lite
	GRAPHIC_D_STAR_LITE_FINAL(false), // affiche l'itinéraire final du D* Lite
	GRAPHIC_PROXIMITY_OBSTACLES(true), // affiche les obstacles de proximité
	GRAPHIC_TRAJECTORY(false), // affiche les trajectoires temporaires
	GRAPHIC_TRAJECTORY_ALL(false), // affiche TOUTES les trajectoires temporaires
	GRAPHIC_TRAJECTORY_FINAL(true), // affiche les trajectoires
	GRAPHIC_FIXED_OBSTACLES(true), // affiche les obstacles fixes
	GRAPHIC_GAME_ELEMENTS(true), // affiche les éléments de jeux
	GRAPHIC_ROBOT_COLLISION(false), // affiche les obstacles du robot lors de la vérification des collisions
	GRAPHIC_BACKGROUND_PATH("img/background-2017-color.png"), // affiche d'image de la table
	GRAPHIC_ROBOT_PATH("img/robot_sans_roues.png"), // image du robot sans les roues
	GRAPHIC_ROBOT_ROUE_GAUCHE_PATH("img/robot_roue_gauche.png"), // image de la roue gauche
	GRAPHIC_ROBOT_ROUE_DROITE_PATH("img/robot_roue_droite.png"), // image de la roue droite
	GRAPHIC_PRODUCE_GIF(false), // produit un gif ?
	GRAPHIC_BACKGROUND(true), // affiche d'image de la table
	GRAPHIC_SIZE_X(1000), // taille par défaut (sans image) de la fenêtre
	GRAPHIC_ALL_OBSTACLES(false), // affiche absolument tous les obstacles créés
	GRAPHIC_ROBOT_AND_SENSORS(true), // affiche le robot et ses capteurs
	GRAPHIC_CERCLE_ARRIVEE(false), // affiche le cercle d'arrivée
	GRAPHIC_TIME(false), // affiche le temps écoulé
	GRAPHIC_TRACE_ROBOT(true), // affiche la trace du robot
	GRAPHIC_EXTERNAL(true), // l'affichage doit-il être déporté par le serveur d'affichage ?
	GRAPHIC_DIFFERENTIAL(true), // sauvegarde d'une "vidéo" pour visionner les images plus tard
	
	/**
	 * Config dynamique
	 */
	COULEUR(RobotColor.getCouleurSansSymetrie(), false), // quelle est la couleur du robot
	MATCH_DEMARRE(false, false), // le match a-t-il démarré
	DATE_DEBUT_MATCH(0, false); // date du début du match

	private Object defaultValue;
	public boolean overridden = false;
	public final boolean constant;
	
	/**
	 * Par défaut, une valeur est constante
	 * @param defaultValue
	 */
	private ConfigInfo(Object defaultValue)
	{
		this(defaultValue, true);
	}
	
	private ConfigInfo(Object defaultValue, boolean constant)
	{
		this.defaultValue = defaultValue;
		this.constant = constant;
	}
	
	Object getDefaultValue()
	{
		return defaultValue;
	}
	
	/**
	 * Pour les modifications de config avant même de démarrer le service de config
	 * @param o
	 */
	public void setDefaultValue(Object o)
	{
		defaultValue = o;
		overridden = true;
	}
	
}
