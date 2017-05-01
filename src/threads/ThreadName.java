/*
Copyright (C) 2013-2017 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package threads;

import threads.serie.ThreadSerialInputCoucheOrdre;
import threads.serie.ThreadSerialInputCoucheTrame;
import threads.serie.ThreadSerialOutputBytes;
import threads.serie.ThreadSerialOutputOrder;
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
	FENETRE(ThreadFenetre.class),
	PRINT_SERVER(ThreadPrintServer.class),
	UPDATE_PATHFINDING(ThreadUpdatePathfinding.class),
	PREPARE_PATHFINDING(ThreadPreparePathfinding.class),
	PEREMPTION(ThreadPeremption.class),
	SERIAL_INPUT_ORDRE(ThreadSerialInputCoucheOrdre.class),
	SERIAL_INPUT_TRAME(ThreadSerialInputCoucheTrame.class),
	SERIAL_OUTPUT_BYTES(ThreadSerialOutputBytes.class),
	SERIAL_OUTPUT_ORDER(ThreadSerialOutputOrder.class),
	SERIAL_OUTPUT_TIMEOUT(ThreadSerialOutputTimeout.class);
	
	public Class<? extends ThreadService> c;
	
	private ThreadName(Class<? extends ThreadService> c)
	{
		this.c = c;
	}

}
