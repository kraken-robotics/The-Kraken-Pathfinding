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

package graphic;

import java.awt.Graphics;
import java.util.ArrayList;

import robot.RobotReal;
import utils.Config;
import utils.Log;
import container.Service;
import graphic.printable.Layer;
import graphic.printable.Printable;

/**
 * Buffer de ce qu'il faut afficher
 * @author pf
 *
 */

public class PrintBuffer implements Service
{	
	private ArrayList<ArrayList<Printable>> elementsAffichablesSupprimables = new ArrayList<ArrayList<Printable>>();
	private ArrayList<ArrayList<Printable>> elementsAffichables = new ArrayList<ArrayList<Printable>>();

	protected Log log;
	
	public PrintBuffer(Log log)
	{
		this.log = log;
		for(int i = 0 ; i < Layer.values().length; i++)
		{
			elementsAffichablesSupprimables.add(new ArrayList<Printable>());
			elementsAffichables.add(new ArrayList<Printable>());
		}
	}

	/**
	 * Supprime tous les obstacles
	 * @param c
	 */
	public synchronized void clearSupprimables()
	{
		for(int i = 0 ; i < Layer.values().length; i++)
			elementsAffichablesSupprimables.get(i).clear();
		notify();
	}

	/**
	 * Ajoute un obstacle
	 * @param o
	 */
	public synchronized void addSupprimable(Printable o)
	{
		elementsAffichablesSupprimables.get(o.getLayer().ordinal()).add(o);
		notify();
	}

	/**
	 * Ajoute un obstacle
	 * @param o
	 */
	public synchronized void add(Printable o)
	{
		elementsAffichables.get(o.getLayer().ordinal()).add(o);
		notify();
	}

	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

	public synchronized void print(Graphics g, Fenetre f, RobotReal robot)
	{
		for(int i = 0 ; i < Layer.values().length; i++)
		{
			for(Printable p : elementsAffichablesSupprimables.get(i))
				p.print(g, f, robot);

			for(Printable p : elementsAffichables.get(i))
				p.print(g, f, robot);
		}
	}

	public synchronized void removeSupprimable(Printable o)
	{
		if(elementsAffichablesSupprimables.get(o.getLayer().ordinal()).remove(o))
			notify();
	}
	
}
