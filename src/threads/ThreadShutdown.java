/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package threads;

import utils.Log;
import container.Container;
import container.Container.ErrorCode;
import container.Service;

/**
 * Thread qui sera exécuté à la fin du programme
 * 
 * @author pf
 *
 */

public class ThreadShutdown extends Thread implements Service
{
	protected Container container;
	protected Log log;

	public ThreadShutdown(Container container, Log log)
	{
		this.container = container;
		this.log = log;
	}

	@Override
	public void run()
	{
		try {
			Thread.currentThread().setName(getClass().getSimpleName());
			if(!container.isShutdownInProgress())
			{
				log.debug("Appel prématuré à " + Thread.currentThread().getName());
				container.interruptWithCodeError(ErrorCode.TERMINATION_SIGNAL);
				// c'est le thread principal qui va terminer ce thread
				while(true)
					Thread.sleep(100);
			}
			// si l'arrêt a déjà était fait, ce thread ne fait rien
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}