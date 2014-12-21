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
		if(chaine == "vert" || chaine == "Vert" || chaine == "VERT"
				|| chaine == "green" || chaine == "Green" || chaine == "GREEN")
			return GREEN;
		return YELLOW;
	}

	public boolean isSymmetry() {
		return symmetry;
	}
	
}
