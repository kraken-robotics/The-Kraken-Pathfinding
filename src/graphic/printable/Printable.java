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

package graphic.printable;

import java.awt.Graphics;
import graphic.Fenetre;
import robot.RobotReal;

/**
 * Élément affichable
 * 
 * @author pf
 *
 */

public interface Printable
{
	/**
	 * Affiche cet objet
	 * 
	 * @param g
	 */
	public void print(Graphics g, Fenetre f, RobotReal robot);

	/**
	 * Récupère le layer sur lequel afficher l'objet
	 * 
	 * @return
	 */
	public Layer getLayer();
}
