package enums;

/**
 * Les deux couleurs possibles pour le robot.
 * @author pf
 *
 */

public enum RobotColor {
	GREEN,
	YELLOW;
	
	public static RobotColor parse(String chaine)
	{
		if(chaine == "vert" || chaine == "Vert" || chaine == "VERT"
				|| chaine == "green" || chaine == "Green" || chaine == "GREEN")
			return GREEN;
		return YELLOW;
	}
	
}
