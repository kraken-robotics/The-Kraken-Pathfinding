package buffer;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Liste de message à envoyer à la série.
 * Munie d'une priorité
 * @author pf
 *
 */

public class SerialOutput implements Comparator<SerialOutput>
{
	public ArrayList<String> output;
	public int priorite;
	
	public SerialOutput(ArrayList<String> output, int priorite)
	{
		this.output = output;
		this.priorite = priorite;
	}

	@Override
	public int compare(SerialOutput arg0, SerialOutput arg1)
	{
		return arg0.priorite - arg1.priorite;
	}

}
