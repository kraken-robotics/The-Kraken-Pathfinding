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

package obstacles.types;

import config.Config;
import config.ConfigInfo;
import config.Configurable;
import graphic.PrintBuffer;
import graphic.printable.Layer;
import graphic.printable.Printable;
import utils.Log;
import utils.Vec2RO;
import utils.Vec2RW;

/**
 * Superclasse abstraite des obstacles.
 * @author pf
 *
 */

public abstract class Obstacle implements Printable, Configurable
{
	protected Vec2RW position;
	protected int distance_dilatation;
	protected static Log log;
	protected static PrintBuffer buffer;
	
    protected int distanceApprox; // TODO : pas utilisé
    protected static boolean printAllObstacles = false; // static car commun à tous
	protected Layer l = null;
	
	public static void set(Log log, PrintBuffer buffer)
	{
		Obstacle.log = log;
		Obstacle.buffer = buffer;
	}
	
	@Override
	public void useConfig(Config config)
	{
		printAllObstacles = config.getBoolean(ConfigInfo.GRAPHIC_ALL_OBSTACLES);
		distanceApprox = config.getInt(ConfigInfo.DISTANCE_MAX_ENTRE_MESURE_ET_OBJET);
	}

	public Obstacle(Vec2RO position, Layer l)
	{
		this(position);
		this.l = l;
	}
	
	/**
	 * Constructeur. La position est celle du centre de rotation de l'obstacle
	 * @param position
	 */
	public Obstacle(Vec2RO position)
	{
		l = Layer.MIDDLE;
		if(position != null)
		{
			this.position = position.clone();
			if(printAllObstacles)
				buffer.addSupprimable(this);
		}
		else
			this.position = null;
	}
	
	@Override
	public String toString()
	{
		return "Obstacle en "+position;
	}

	/**
	 * Renvoie la distance au carré de l'obstacle avec cette position
	 * @param position
	 * @return
	 */
	public abstract double squaredDistance(Vec2RO position);
	
	/**
	 * Renvoi "vrai" si position est à moins de distance d'un bord de l'obstacle ou à l'intérieur
	 * @param position
	 * @param distance
	 * @return
	 */
	public boolean isProcheObstacle(Vec2RO position, int distance)
	{
		return squaredDistance(position) < distance * distance;
	}

	/**
	 * Renvoi "vrai" si le centre de obs est à moins de distance d'un bord de l'obstacle ou à l'intérieur
	 * Ce n'est pas pareil que vérifier une collision !
	 * @param position
	 * @param distance
	 * @return
	 */
	public boolean isProcheObstacle(Obstacle obs, int distance)
	{
		return squaredDistance(obs.position) < distance * distance;
	}
	
	/**
	 * Renvoie vrai s'il y a collision avec obs
	 * @param obs
	 * @return
	 */
	public abstract boolean isColliding(ObstacleRectangular obs);

	/**
	 * Revoie vrai s'il y a une collision avec obs
	 * @param obs
	 * @return
	 */
	public boolean isColliding(ObstacleArcCourbe obs)
	{
		for(ObstacleRectangular o : obs.ombresRobot)
		{
			if(isColliding(o))
				return true;
		}
		return false;
	}
	
	@Override
	public Layer getLayer()
	{
		return l;
	}

}
