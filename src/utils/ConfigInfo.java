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
	TABLE_X("3000", true),
	TABLE_Y("2000", true),
	DUREE_MATCH_EN_S("90", true),
	FAST_LOG("false", true),
//	TEMPS_FUNNY_ACTION("0"),
	LARGEUR_ROBOT_AXE_GAUCHE_DROITE("180", true),
	LONGUEUR_ROBOT_AXE_AVANT_ARRIERE("140", true),
	LARGEUR_ROBOT_AXE_GAUCHE_DROITE_DEPLOYE("350", true),
	LONGUEUR_ROBOT_AXE_AVANT_ARRIERE_DEPLOYE("220", true),
	RAYON_ROBOT("150", true),
	RAYON_ROBOT_ADVERSE("200", true),
	DUREE_PEREMPTION_OBSTACLES("3000", true),
	HORIZON_CAPTEURS("800", true),
	CAPTEURS_TEMPORISATION_OBSTACLES("100", true),
//	DISTANCE_DETECTION("300"),
	
	AFFICHE_DEBUG("true", true),
	SAUVEGARDE_FICHIER("true", true),
	DISTANCE_MAX_ENTRE_MESURE_ET_OBJET("50", true),
	HOOKS_TOLERANCE_MM("20", true),
	DISQUE_TOLERANCE_MAJ("100", true),
	DISQUE_TOLERANCE_CONSIGNE("40", true),
	ATTENTE_ENNEMI_PART("500", true),
	DISTANCE_DEGAGEMENT_ROBOT("200", true),
	MARGE("20", true),
	TEMPS_MAX_ANTICIPATION_EN_S("30", true),
	DISTANCE_ENNEMI_URGENCE("550", true),
	PREVISION_COLLISION("50", true),
	NB_CAPTEURS_PROXIMITE("2", true),
	MS_MAX_AVANT_EVITEMENT("5", true),
	DISTANCE_URGENCE("200", true),
	NB_SUCCESSEUR_MAX("2", true),
	COURBURE_MAX("10.", true),
	AFFICHE_CONFIG("false", true),

	/**
	 * Paramètres de la série
	 */
	SERIAL_TIMEOUT("20", true),
	BAUDRATE("115200", true),
	SERIAL_PORT("/dev/ttyS0", true),
	SLEEP_ENTRE_TRAMES("0", true),	
	
	/**
	 * Paramètres des capteurs
	 */
	SENSORS_SEND_PERIOD("20", true),
	SENSORS_PRESCALER("5", true),
	
	/**
	 * Config dynamique
	 */
	COULEUR(RobotColor.getCouleurSansSymetrie(), false),
	TRAJECTOIRE_COURBE("true", false),
	MATCH_DEMARRE("false", false),
	DATE_DEBUT_MATCH("0", false),
	BALISE_PRESENTE("false", false),
	FIN_MATCH("false", false);

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
