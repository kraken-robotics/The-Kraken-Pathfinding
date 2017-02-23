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

import container.Container;
import exceptions.ContainerException;
import robot.RobotReal;
import scripts.Strategie;
import serie.BufferOutgoingOrder;
import serie.Ticket;
import utils.Log;

/**
 * Un lanceur de match
 * @author pf
 *
 */

public class LanceurMatch {

	public static void main(String[] args)
	{
		try {
			Container container = new Container();
			Log log = container.getService(Log.class);
			Strategie strat = container.getService(Strategie.class);
			BufferOutgoingOrder data = container.getService(BufferOutgoingOrder.class);
			RobotReal robot = container.getService(RobotReal.class);
			
			/*
			 * Initialise les actionneurs
			 */
			robot.initActionneurs();
			
			log.debug("Actionneurs initialisés");
			
			/*
			 * Demande de la couleur
			 */
			Ticket.State etat;
			do {
				Ticket t = data.demandeCouleur();
				synchronized(t)
				{
					if(t.isEmpty())
						t.wait();
					etat = t.getAndClear();
				}
			} while(etat != Ticket.State.OK);
			
			log.debug("Couleur récupérée");
			
			/*
			 * La couleur est connue : on commence le stream de position
			 */
			data.startStream();
			
			log.debug("Stream des positions et des capteurs lancé");
			
			/*
			 * On attend d'avoir l'info de position
			 */
			synchronized(robot)
			{
				if(!robot.isCinematiqueInitialised())
					robot.wait();
			}
			
			log.debug("Cinématique initialisée : "+robot.getCinematique());
			
			/*
			 * Attente du jumper
			 */
			do {
				Ticket t = data.waitForJumper();
				synchronized(t)
				{
					if(t.isEmpty())
						t.wait();
					etat = t.getAndClear();
				}
			} while(etat != Ticket.State.OK);

			log.debug("LE MATCH COMMENCE !");
			
			/*
			 * Le match a commencé !
			 */
			data.startMatchChrono();
			
			log.debug("Chrono démarré");
			
			/*
			 * On appelle la stratégie
			 */
			strat.doWinMatch();
		} catch (ContainerException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
		}
	}
	
}
