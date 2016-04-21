package obstacles;

import robot.Cinematique;

/**
 * Un groupe de mesures qui proviennent des capteurs
 * @author pf
 *
 */

public class IncomingData
{
	public Cinematique cinematique;
	/** Ce que voit chacun des capteurs */
	public int[] mesures;
	
	public IncomingData(int[] mesures, Cinematique cinematique)
	{
		this.mesures = mesures;
		this.cinematique = cinematique;
	}
	
}
