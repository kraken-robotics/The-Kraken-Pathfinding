package obstacles;

import robot.Cinematique;

/**
 * Un groupe de mesures qui proviennent des capteurs
 * @author pf
 *
 */

public class SensorsData
{
	public Cinematique cinematique;
	/** Ce que voit chacun des capteurs */
	public int[] mesures;
	
	public SensorsData(int[] mesures, Cinematique cinematique)
	{
		this.mesures = mesures;
		this.cinematique = cinematique;
	}
	
}
