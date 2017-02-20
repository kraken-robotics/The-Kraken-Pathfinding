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
import scripts.Strategie;
import serie.BufferOutgoingOrder;
import serie.Ticket;

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
			Strategie strat = container.getService(Strategie.class);
			BufferOutgoingOrder data = container.getService(BufferOutgoingOrder.class);
			
			/**
			 * Attente du jumper
			 */
			Ticket.State etat;
			do {
				Ticket t = data.waitForJumper();
				synchronized(t)
				{
					if(t.isEmpty())
						t.wait();
				}
				etat = t.getAndClear();
			} while(etat != Ticket.State.OK);
			
			strat.doWinMatch();
		} catch (ContainerException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
