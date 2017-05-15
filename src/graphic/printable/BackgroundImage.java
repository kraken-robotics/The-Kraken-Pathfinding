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
import java.awt.Image;
import graphic.Fenetre;
import robot.RobotReal;

/**
 * Image de fond
 * 
 * @author pf
 *
 */

public class BackgroundImage implements Printable
{
	private Image image;

	public BackgroundImage(Image image)
	{
		this.image = image;
	}

	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		g.drawImage(image, 0, 0, f);
	}

	@Override
	public Layer getLayer()
	{
		return Layer.IMAGE_BACKGROUND;
	}

}
