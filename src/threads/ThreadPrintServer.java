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

import config.Config;
import config.ConfigInfo;
import container.Container;
import container.dependances.GUIClass;
import graphic.ExternalPrintBuffer;
import graphic.Fenetre;
import utils.Log;

/**
 * Thread du serveur d'affichage
 * @author pf
 *
 */

public class ThreadPrintServer extends ThreadService implements GUIClass
{

	protected Log log;
	private Fenetre fenetre;
	private ExternalPrintBuffer buffer;
	private boolean external;
	
	public ThreadPrintServer(Log log, Container container, ExternalPrintBuffer buffer, Config config)
	{
		this.log = log;
		this.buffer = buffer;
		external = config.getBoolean(ConfigInfo.GRAPHIC_EXTERNAL);
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName(getClass().getSimpleName());
		log.debug("Démarrage de "+Thread.currentThread().getName());
		try {
			if(!external)
			{
				log.debug(getClass().getSimpleName()+" annulé ("+ConfigInfo.GRAPHIC_EXTERNAL+" = "+external+")");
				while(true)
					Thread.sleep(10000);
			}
			
			while(true)
			{
				synchronized(buffer)
				{
					if(!buffer.needRefresh())
						buffer.wait();
				}
				fenetre.refresh();
				Thread.sleep(50); // on ne met pas à jour plus souvent que toutes les 50ms
			}
		} catch (InterruptedException e) {
			log.debug("Arrêt de "+Thread.currentThread().getName());
		}
	}

}
