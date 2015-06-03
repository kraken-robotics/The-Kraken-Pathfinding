package utils;

import enums.RobotColor;

/**
 * Informations accessibles par la config
 * En cas de problème de config, on duplique les valeurs ici.
 * De préférence, ce sont celles de config.ini qui sont utilisées.
 * Mais en cas de problème de config, on utilise celle-là.
 * @author pf
 *
 */

public enum ConfigInfo {
	/**
	 * Config statique, présente dans config.ini
	 */
	TABLE_X("3000", true),
	TABLE_Y("2000", true),
	DUREE_MATCH_EN_S("90", true),
	FAST_LOG("false", true),
//	TEMPS_FUNNY_ACTION("0"),
	LARGEUR_ROBOT("245", true),
	LONGUEUR_ROBOT("300", true),
	RAYON_ROBOT("250", true),
	RAYON_ROBOT_ADVERSE("200", true),
	DUREE_PEREMPTION_OBSTACLES("5000", true),
	CAPTEURS_TEMPORISATION_OBSTACLES("100", true),
//	DISTANCE_DETECTION("300"),
	AFFICHE_DEBUG("false", true),
	SAUVEGARDE_FICHIER("true", true),
	DISTANCE_MAX_ENTRE_MESURE_ET_OBJET("50", true),
	HOOKS_TOLERANCE_MM("20", true),
	DISQUE_TOLERANCE_MAJ("100", true),
	DISQUE_TOLERANCE_CONSIGNE("40", true),
//	TRAJECTOIRE_COURBE("false"),
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
	
	
	/**
	 * Config dynamique
	 */
	NB_CAPTEURS_PROXIMITE("8", false),
	NB_COUPLES_CAPTEURS_PROXIMITE("4", false),
	COULEUR(RobotColor.getCouleurSansSymetrie(), false),
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
