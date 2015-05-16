package enums;

/**
 * Les deux couleurs possibles pour le robot.
 * @author pf
 *
 */

public enum RobotColor {
	GREEN(false),
	YELLOW(true);
	
	private boolean symmetry;
	
	private RobotColor(boolean symmetry)
	{
		this.symmetry = symmetry;
	}
	
	public static RobotColor parse(String chaine)
	{
		if(chaine.toLowerCase().contains("vert") ||
				chaine.toLowerCase().contains("green") ||
				chaine.toLowerCase().contains("nosym"))
			return GREEN;
		return YELLOW;
	}

	public boolean isSymmetry() {
		return symmetry;
	}
	
	/**
	 * Récupère la couleur pour laquelle il n'y a pas de symétrie.
	 * Utilisé pour les tests sans avoir à hardcoder la couleur.
	 * @return
	 */
	public static String getCouleurSansSymetrie()
	{
		for(RobotColor r: RobotColor.values())
			if(!r.symmetry)
				return r.toString();
		return null;
	}
	
	/**
	 * Récupère la couleur pour laquelle il y a symétrie.
	 * Utilisé pour les tests sans avoir à hardcoder la couleur.
	 * @return
	 */
	public static String getCouleurAvecSymetrie()
	{
		for(RobotColor r: RobotColor.values())
			if(r.symmetry)
				return r.toString();
		return null;
	}
	
}
