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

import graphic.Fenetre;
import graphic.PrintBuffer;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;

/**
 * S'occupe de la mise à jour graphique
 * @author pf
 *
 */

public class ThreadFenetre extends ThreadService
{

	protected Log log;
	private Fenetre fenetre;
	private PrintBuffer buffer;
	
	private boolean print;
	
	public ThreadFenetre(Log log, Fenetre fenetre, PrintBuffer buffer)
	{
		this.log = log;
		this.buffer = buffer;
		this.fenetre = fenetre;
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName(getClass().getSimpleName());
		log.debug("Démarrage de "+Thread.currentThread().getName());
		try {
			if(!print)
			{
				log.debug(getClass().getSimpleName()+" annulé ("+ConfigInfo.GRAPHIC_ENABLE+" = "+print+")");
				while(true)
					Thread.sleep(10000);
			}
			
			while(true)
			{
				synchronized(buffer)
				{
					buffer.wait();
				}
				fenetre.refresh();
				Thread.sleep(50); // on ne met pas à jour plus souvent que toutes les 50ms
			}
		} catch (InterruptedException e) {
			log.debug("Arrêt de "+Thread.currentThread().getName());
		}
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		print = config.getBoolean(ConfigInfo.GRAPHIC_ENABLE);
	}

}
