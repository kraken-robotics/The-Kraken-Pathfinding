package enums;

/**
 * Les deux couleurs possibles pour le robot.
 * @author pf
 *
 */

public enum RobotColor {
	
	// TODO attendre le règlement
	
	/**
	 * Ces noms sont utilisés pour les tests uniquement. Sinon, on utilise le boolean symmetry
	 */
	VERT(false),
	VIOLET(true);
	
	public final boolean symmetry;
	
	private RobotColor(boolean symmetry)
	{
		this.symmetry = symmetry;
	}
	
	/**
	 * Convertit une chaîne de caractère en enum
	 * @param chaine
	 * @return
	 */
	public static RobotColor parse(String chaine)
	{
		if(chaine.toLowerCase().contains(VERT.name().toLowerCase()) ||
				chaine.toLowerCase().contains("nosym") ||
				chaine.toLowerCase().contains("false"))
			return VERT;
		return VIOLET;
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
/*	public static String getCouleurAvecSymetrie()
	{
		for(RobotColor r: RobotColor.values())
			if(r.symmetry)
				return r.toString();
		return null;
	}*/

	public static String getCouleur(boolean symetrie)
	{
		for(RobotColor r: RobotColor.values())
			if(symetrie == r.symmetry)
				return r.toString();
		return null;
	}
	
}
