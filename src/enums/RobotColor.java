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
				chaine.toLowerCase().contains("green"))
			return GREEN;
		return YELLOW;
	}

	public boolean isSymmetry() {
		return symmetry;
	}
	
}
