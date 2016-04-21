package obstacles;

import robot.CinematiqueSansVitesse;

/**
 * Un groupe de mesures qui proviennent des capteurs
 * @author pf
 *
 */

public class IncomingData
{
	public CinematiqueSansVitesse cinematique;
	/** Ce que voit chacun des capteurs */
	public int[] mesures;
	
	public IncomingData(int[] mesures, CinematiqueSansVitesse cinematique)
	{
		this.mesures = mesures;
		this.cinematique = cinematique;
	}
	
}
