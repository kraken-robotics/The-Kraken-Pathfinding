package buffer;

/**
 * Un groupe de mesures qui proviennent des capteurs
 * @author pf
 *
 */

public class IncomingData
{
	/** Ce que voit chacun des capteurs */
	public int[] mesures;
	
	/** Faut-il ignorer les capteurs ? */
	public boolean capteursOn;

	public IncomingData(int[] mesures, boolean capteursOn)
	{
		this.mesures = mesures;
		this.capteursOn = capteursOn;
	}
	
}
