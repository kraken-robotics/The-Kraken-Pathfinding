package threads;

import threads.serie.ThreadSerialInputCoucheOrdre;
import threads.serie.ThreadSerialInputCoucheTrame;
import threads.serie.ThreadSerialOutput;
import threads.serie.ThreadSerialOutputTimeout;

/**
 * Tous les threads à instancier au début du match. Utilisé par le container
 * @author pf
 *
 */

public enum ThreadName
{
	CAPTEURS(ThreadCapteurs.class),
	CONFIG(ThreadConfig.class),
	PATHFINDING(ThreadPathfinding.class),
	PEREMPTION(ThreadPeremption.class),
	SERIAL_INPUT_ORDRE(ThreadSerialInputCoucheOrdre.class),
	SERIAL_INPUT_TRAME(ThreadSerialInputCoucheTrame.class),
	SERIAL_OUTPUT(ThreadSerialOutput.class),
	SERIAL_OUTPUT_TIMEOUT(ThreadSerialOutputTimeout.class);
	
	public Class<? extends ThreadService> c;
	
	private ThreadName(Class<? extends ThreadService> c)
	{
		this.c = c;
	}
	

}
