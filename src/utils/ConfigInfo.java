/*
Copyright (C) 2016 Pierre-François Gimenez

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

package utils;

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
	RAYON_ROBOT(150), // TODO
	DISTANCE_ROUES_AVANT_ET_ARRIERE(300), // la distance entre l'axe des roues avant et l'axe des roues arrières TODO
	DISTANCE_ROUES_GAUCHE_ET_DROITE(200), // la distance entre la roue arrière gauche et la roue arrière droite TODO
	LONGUEUR_NON_DEPLOYE(400), // distance entre le bord avant et le bord arrière du robot non-déployé TODO
	LARGEUR_NON_DEPLOYE(200), // distance entre le bord gauche et le bord droit du robot non-déployé TODO
	
	/**
	 * Paramètres du log
	 */
	FAST_LOG(false), // affichage plus rapide des logs
	AFFICHE_DEBUG(true), // affiche aussi les log.debug
	SAUVEGARDE_FICHIER(false), // sauvegarde les logs dans un fichier externe
	AFFICHE_CONFIG(false), // affiche la configuration complète au lancement
	
	/**
	 * Infos sur l'ennemi
	 */
	RAYON_ROBOT_ADVERSE(200), // le rayon supposé du robot adverse, utilisé pour créer des obstacles de proximité
	
	/**
	 * Paramètres du pathfinding
	 */
	COURBURE_MAX(8.), // quelle courbure maximale la trajectoire du robot peut-elle avoir TODO
	TEMPS_REBROUSSEMENT(700), // temps qu'il faut au robot pour rebrousser chemin
	PF_MARGE_NECESSAIRE(2), // combien d'arc de pathfinding le bas niveau doit-il toujours avoir
	PF_ANTICIPATION(2), // combien d'arcs avant le dernier valide supprime-t-on avant de replanifier
	
	/**
	 * Paramètres de la série
	 */
	SERIAL_TIMEOUT(30), // quel TIMEOUT pour le protocole série des trames ? (en ms) TODO
	BAUDRATE(115200), // le baudrate de la liaison série
	SERIAL_PORT("/dev/ttyS0"), // le port de la liaison série
	SLEEP_ENTRE_TRAMES(0),	// la durée minimale entre deux envois de nouvelles trames
	SIMULE_SERIE(false), // la série doit-elle être simulée (utile pour debug)
	
	/**
	 * Paramètres bas niveau des capteurs
	 */
	SENSORS_SEND_PERIOD(20), // période d'envoi des infos des capteurs (ms)
	SENSORS_PRESCALER(5), // sur combien de trames a-t-on les infos des capteurs
	
	/**
	 * Paramètres du traitement des capteurs
	 */
	NB_CAPTEURS(2), // TODO
	DUREE_PEREMPTION_OBSTACLES(3000), // pendant combien de temps va-t-on garder un obstacle de proximité
	DISTANCE_MAX_ENTRE_MESURE_ET_OBJET(50), // quelle marge d'erreur autorise-t-on entre un objet et sa détection
	DISTANCE_BETWEEN_PROXIMITY_OBSTACLES(50), // sous quelle distance fusionne-t-on deux obstacles de proximité ?

	/**
	 * Paramètres sur la gestion de la mémoire
	 */
	NB_INSTANCES_NODE(500),
//	NB_INSTANCES_NODE(50000),	// nombre d'instances pour les nœuds du pathfinding
	NB_INSTANCES_OBSTACLES(NB_INSTANCES_NODE.getDefaultValue()), // nombre d'instances pour les obstacles rectangulaires
	
	/**
	 * Debug
	 */
	DEBUG_SERIE_TRAME(false), // debug verbeux sur le contenu des trames
	DEBUG_SERIE(false), // debug sur la série
	GENERATE_DEPENDENCY_GRAPH(false), // génère le graphe des dépendances
	
	/**
	 * Interface graphique
	 */
	GRAPHIC_ENABLE(false), // désactive tout affichage (empêche le thread d'affichage de se lancer)
	GRAPHIC_D_STAR_LITE(false), // affiche les calculs du D* Lite
	GRAPHIC_D_STAR_LITE_FINAL(false), // affiche l'itinéraire final du D* Lite
	GRAPHIC_PROXIMITY_OBSTACLES(false), // affiche les obstacles de proximité
	GRAPHIC_TRAJECTORY(false), // affiche les trajectoires temporaires
	GRAPHIC_TRAJECTORY_FINAL(false), // affiche les trajectoires
	GRAPHIC_FIXED_OBSTACLES(false), // affiche les obstacles fixes
	GRAPHIC_BACKGROUND(false), // affiche d'image de la table
	GRAPHIC_SIZE_X(1000), // taille par défaut (sans image) de la fenêtre
	GRAPHIC_ALL_OBSTACLES(false), // affiche absolument tous les obstacles créés
	GRAPHIC_ROBOT_AND_SENSORS(false), // affiche le robot et ses capteurs

	/**
	 * Config dynamique
	 */
	COULEUR(RobotColor.getCouleurSansSymetrie(), false), // quelle est la couleur du robot
	MATCH_DEMARRE(false, false), // le match a-t-il démarré
	DATE_DEBUT_MATCH(0, false); // date du début du match

	private Object defaultValue;
	private boolean constant;
	
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
	
	boolean isConstant()
	{
		return constant;
	}
	
	Object getDefaultValue()
	{
		return defaultValue;
	}
	
}
