package utils;

/**
 * Informations accessibles par la config
 * En cas de problème de config, on duplique les valeurs ici.
 * De préférence, ce sont celles de config.ini qui sont utilisées.
 * Mais en cas de problème de config, on utilise celle-là.
 * @author pf
 *
 */

public enum ConfigInfo {
	TABLE_X("3000"),
	TABLE_Y("2000"),
	DUREE_MATCH_EN_S("90"),
	TEMPS_FUNNY_ACTION("0"),
	LARGEUR_ROBOT("245"),
	LONGUEUR_ROBOT("300"),
	RAYON_ROBOT("250"),
	RAYON_ROBOT_ADVERSE("230"),
	SLEEP_BOUCLE_ACQUITTEMENT("50"),
	COULEUR("vert"),
	HORIZON_CAPTEURS("500"),
	DUREE_PEREMPTION_OBSTACLES("5000"),
	CAPTEURS_FREQUENCE("12"),
	CAPTEURS_TEMPORISATION_OBSTACLES("100"),
	DISTANCE_DETECTION("300"),
	CAPTEURS_ON("true"),
	AFFICHE_DEBUG("false"),
	SAUVEGARDE_FICHIER("true"),
	LASERS_FREQUENCE("15"),
	DISTANCE_MAX_ENTRE_MESURE_ET_OBJET("50"),
	HOOKS_TOLERANCE_MM("20"),
	DISQUE_TOLERANCE_MAJ("100"),
	DISQUE_TOLERANCE_CONSIGNE("40"),
	TRAJECTOIRE_COURBE("false"),
	NB_TENTATIVES("30"),
	ANTICIPATION_TRAJECTOIRE_COURB("200"),
	ANGLE_DEGAGEMENT_ROBOT("0.2"),
	DISTANCE_DEGAGEMENT_ROBOT("30"),
	MARGE("20"),
	TEMPS_MAX_ANTICIPATION_EN_S("30"),
	TEST1("test2"),
	TOLERANCE_DEPART_SCRIPT("20"),
	DISTANCE_ENNEMI_URGENCE("550"),
	CHECK_POINTS_SORTIE("false"),
	TEMPS_AVANT_BLOCAGE("200");

	private String defaultValue;
	
	private ConfigInfo(String defaultValue)
	{
		this.defaultValue = defaultValue;
	}
	
	public String getDefaultValue()
	{
		return defaultValue;
	}
	
}
