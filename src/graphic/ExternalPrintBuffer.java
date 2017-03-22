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

package graphic;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import graphic.printable.Layer;
import graphic.printable.Printable;
import robot.RobotReal;
import utils.Log;

/**
 * PrintBuffer prévu pour l'externalisation de l'affichage
 * @author pf
 *
 */

public class ExternalPrintBuffer implements PrintBufferInterface {
	private List<ArrayList<Printable>> elementsAffichablesSupprimables = new ArrayList<ArrayList<Printable>>();
	private List<ArrayList<Printable>> elementsAffichables = new ArrayList<ArrayList<Printable>>();

	protected Log log;
	
	public ExternalPrintBuffer(Log log)
	{
		this.log = log;
		for(int i = 0 ; i < Layer.values().length; i++)
		{
			elementsAffichablesSupprimables.add(new ArrayList<Printable>());
			elementsAffichables.add(new ArrayList<Printable>());
		}
	}

	/**
	 * Supprime tous les obstacles supprimables
	 * @param c
	 */
	@Override
	public synchronized void clearSupprimables()
	{
		for(int i = 0 ; i < Layer.values().length; i++)
			elementsAffichablesSupprimables.get(i).clear();
		notify();
	}

	/**
	 * Ajoute un obstacle dans la liste des supprimables
	 * @param o
	 */
	@Override
	public synchronized void addSupprimable(Printable o)
	{
		elementsAffichablesSupprimables.get(o.getLayer().ordinal()).add(o);
		notify();
	}

	/**
	 * Ajoute un obstacle dans la liste des supprimables
	 * @param o
	 */
	@Override
	public synchronized void addSupprimable(Printable o, Layer l)
	{
		elementsAffichablesSupprimables.get(l.ordinal()).add(o);
		notify();
	}

	/**
	 * Ajoute un obstacle
	 * @param o
	 */
	@Override
	public synchronized void add(Printable o)
	{
		elementsAffichables.get(o.getLayer().ordinal()).add(o);
		notify();
	}

	/**
	 * Affiche tout
	 * @param g
	 * @param f
	 * @param robot
	 */
	@Override
	public synchronized void print(Graphics g, Fenetre f, RobotReal robot)
	{}

	/**
	 * Supprime un printable ajouté à la liste des supprimables
	 * Ce n'est pas grave s'il y a une double suppression
	 * @param o
	 */
	@Override
	public synchronized void removeSupprimable(Printable o)
	{
		if(elementsAffichablesSupprimables.get(o.getLayer().ordinal()).remove(o))
			notify();
	}

	@Override
	public boolean needRefresh()
	{
		return false;
	}

}
