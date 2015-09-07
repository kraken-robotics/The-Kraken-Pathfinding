package utils;

import enums.RobotColor;

/**
 * Informations accessibles par la config
 * Les informations de config.ini surchargent celles-ci
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
	LARGEUR_ROBOT_AXE_GAUCHE_DROITE("245", true),
	LONGUEUR_ROBOT_AXE_AVANT_ARRIERE("300", true),
	RAYON_ROBOT("250", true),
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
//	NB_TENTATIVES("30"),
//	ANTICIPATION_TRAJECTOIRE_COURBE("200"),
//	ANGLE_DEGAGEMENT_ROBOT("0.2"),
//	DISTANCE_DEGAGEMENT_ROBOT("30"),
	MARGE("20", true),
	TEMPS_MAX_ANTICIPATION_EN_S("30", true),
	TOLERANCE_DEPART_SCRIPT("20", true),
	DISTANCE_ENNEMI_URGENCE("550", true),
	CHECK_POINTS_SORTIE("false", true),
	PREVISION_COLLISION("50", true),
	BAUDRATE("57600", true),
	NB_CAPTEURS_PROXIMITE("8", true),
	NB_COUPLES_CAPTEURS_PROXIMITE("4", true),
	MS_MAX_AVANT_EVITEMENT("5", true),
	DISTANCE_URGENCE("200", true),
	NB_SUCCESSEUR_MAX("2", true),
	NB_OMBRES_PAR_2PI("40", true),
	/**
	 * Config dynamique
	 */
	COULEUR(RobotColor.getCouleurSansSymetrie(), false),
	TRAJECTOIRE_COURBE("true", false),
	MATCH_DEMARRE("false", false),
	DATE_DEBUT_MATCH("0", false),
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
