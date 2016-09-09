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

import container.Container;
import exceptions.ContainerException;


/**
 * Thread qui sera exécuté à la fin du programme
 * @author pf
 *
 */

public class ThreadShutdown extends Thread
{
	protected Container container;
	private static ThreadShutdown instance = null;
	
	public static ThreadShutdown getInstance()
	{
		return instance;
	}
	
	public static ThreadShutdown makeInstance(Container container)
	{
		return instance = new ThreadShutdown(container);
	}

	private ThreadShutdown(Container container)
	{
		this.container = container;
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName("ThreadRobotShutdown");
		try {
			container.destructor(false);
		} catch (ContainerException | InterruptedException e) {
			System.out.println(e);
		}
	}
}