/*
Copyright (C) 2016 Pierre-François Gimenez

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
	FENETRE(ThreadFenetre.class),
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
