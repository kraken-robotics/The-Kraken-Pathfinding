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
import config.Configurable;
import graphic.Fenetre;
import graphic.PrintBuffer;
import utils.Log;

/**
 * S'occupe de la mise à jour graphique
 * @author pf
 *
 */

public class ThreadFenetre extends ThreadService implements Configurable
{

	protected Log log;
	private Fenetre fenetre;
	private PrintBuffer buffer;
	private boolean gif;
	private boolean print;
	private long derniereSauv = 0;
	
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
					if(!buffer.needRefresh())
						buffer.wait();
				}
				fenetre.refresh();
				if(gif && System.currentTimeMillis() - derniereSauv > 300)
				{ // on sauvegarde une image toutes les 300ms au plus
					fenetre.saveImage();
					derniereSauv = System.currentTimeMillis();
				}
				Thread.sleep(50); // on ne met pas à jour plus souvent que toutes les 50ms
			}
		} catch (InterruptedException e) {
			log.debug("Arrêt de "+Thread.currentThread().getName());
			if(gif)
				fenetre.saveGif("output.gif", 200);
		}
	}

	@Override
	public void useConfig(Config config)
	{
		gif = config.getBoolean(ConfigInfo.GRAPHIC_PRODUCE_GIF);
		print = config.getBoolean(ConfigInfo.GRAPHIC_ENABLE);
	}

}
