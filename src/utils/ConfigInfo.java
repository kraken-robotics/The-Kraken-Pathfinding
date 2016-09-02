package utils;

import enums.RobotColor;

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
	 * Config statique, surchargeable avec config.ini
	 */
	
	/**
	 * Infos sur le robot
	 */
	RAYON_ROBOT("150", true),
	
	/**
	 * Paramètres du log
	 */
	FAST_LOG("false", true), // affichage plus rapide des logs
	AFFICHE_DEBUG("true", true), // affiche aussi les log.debug
	SAUVEGARDE_FICHIER("false", true), // sauvegarde les logs dans un fichier externe
	AFFICHE_CONFIG("false", true), // affiche la configuration complète au lancement
	
	/**
	 * Infos sur l'ennemi
	 */
	RAYON_ROBOT_ADVERSE("200", true), // le rayon supposé du robot adverse, utilisé pour créer des obstacles de proximité
	
	/**
	 * Paramètres du pathfinding
	 */
	ATTENTE_ENNEMI_PART("500", true),	// pendant combien de temps peut-on attendre que l'ennemi parte avant d'abandonner ?
	DISTANCE_DEGAGEMENT_ROBOT("200", true),
	MARGE("20", true),
	COURBURE_MAX("10.", true), // quelle courbure maximale la trajectoire du robot peut-elle avoir 
	PATHFINDING_UPDATE_TIMEOUT("50", true), // au bout de combien de temps le pathfinding est-il obligé de fournir un chemin partiel

	/**
	 * Paramètres de la série
	 */
	SERIAL_TIMEOUT("20", true), // quel TIMEOUT pour le protocole série des trames ?
	BAUDRATE("115200", true), // le baudrate de la liaison série
	SERIAL_PORT("/dev/ttyS0", true), // le port de la liaison série
	SLEEP_ENTRE_TRAMES("0", true),	// la durée minimale entre deux envois de nouvelles trames
	SIMULE_SERIE("false", true), // la série doit-elle être simulée (utile pour debug)
	
	/**
	 * Paramètres bas niveau des capteurs
	 */
	SENSORS_SEND_PERIOD("20", true), // période d'envoi des infos des capteurs (ms)
	SENSORS_PRESCALER("5", true), // sur combien de trames a-t-on les infos des capteurs
	
	/**
	 * Paramètres du traitement des capteurs
	 */
	HORIZON_CAPTEURS("800", true), // jusqu'où peuvent voir les capteurs
	DUREE_PEREMPTION_OBSTACLES("3000", true), // pendant combien de temps va-t-on garder un obstacle de proximité
	DISTANCE_MAX_ENTRE_MESURE_ET_OBJET("50", true), // quelle marge d'erreur autorise-t-on entre un objet et sa détection
	DISTANCE_BETWEEN_PROXIMITY_OBSTACLES("20", true), // sous quelle distance fusionne-t-on deux obstacles de proximité ?

	/**
	 * Config dynamique
	 */
	COULEUR(RobotColor.getCouleurSansSymetrie(), false), // quelle est la couleur du robot
	MATCH_DEMARRE("false", false), // le match a-t-il démarré
	DATE_DEBUT_MATCH("0", false), // date du début du match
	FIN_MATCH("false", false); // le match est-il fini

	private String defaultValue;
	private boolean constant;
	
	private ConfigInfo(String defaultValue, boolean constant)
	{
		this.defaultValue = defaultValue;
		this.constant = constant;
	}
	
	public boolean isConstant()
	{
		return constant;
	}
	
	public String getDefaultValue()
	{
		return defaultValue;
	}
	
}
