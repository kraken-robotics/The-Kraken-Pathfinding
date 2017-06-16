/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


package pfg.kraken.obstacles.types;

import java.awt.Color;
import java.io.Serializable;
import config.Config;
import graphic.AbstractPrintBuffer;
import graphic.printable.Layer;
import graphic.printable.Printable;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.Couleur;
import pfg.kraken.utils.Log;
import pfg.kraken.utils.XY;
import pfg.kraken.utils.XY_RW;

/**
 * Superclasse abstraite des obstacles.
 * 
 * @author pf
 *
 */

public abstract class Obstacle implements Printable, Serializable, ObstacleInterface
{
	private static final long serialVersionUID = -2508727703931042322L;
	protected XY_RW position;
	protected transient int distance_dilatation;
	protected static Log log;
	protected static AbstractPrintBuffer buffer;

	// Pour l'affichage du robot
	protected static boolean printAllObstacles = false;
	protected Layer l = null;
	public Color c;

	public static void set(Log log, AbstractPrintBuffer buffer)
	{
		Obstacle.log = log;
		Obstacle.buffer = buffer;
	}

	public static void useConfig(Config config)
	{
		printAllObstacles = config.getBoolean(ConfigInfoKraken.GRAPHIC_ALL_OBSTACLES);
	}

	public Obstacle(XY position, Couleur c)
	{
		this(position);
		this.l = c.l;
		this.c = c.couleur;
	}

	/**
	 * Constructeur. La position est celle du centre de rotation de l'obstacle
	 * 
	 * @param position
	 */
	public Obstacle(XY position)
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
		return "Obstacle en " + position;
	}

	/**
	 * Renvoi "vrai" si position est à moins de distance d'un bord de l'obstacle
	 * ou à l'intérieur
	 * 
	 * @param position
	 * @param distance
	 * @return
	 */
	@Override
	public boolean isProcheObstacle(XY position, int distance)
	{
		return squaredDistance(position) < distance * distance;
	}

	/**
	 * Renvoi "vrai" si le centre de obs est à moins de distance d'un bord de
	 * l'obstacle ou à l'intérieur
	 * Ce n'est pas pareil que vérifier une collision !
	 * 
	 * @param position
	 * @param distance
	 * @return
	 */
	@Override
	public boolean isProcheObstacle(Obstacle obs, int distance)
	{
		return squaredDistance(obs.position) < distance * distance;
	}

	/**
	 * Revoie vrai s'il y a une collision avec obs
	 * 
	 * @param obs
	 * @return
	 */
	@Override
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
	public int getLayer()
	{
		if(l == null)
			return Layer.MIDDLE.ordinal();
		return l.ordinal();
	}

	@Override
	public XY getPosition()
	{
		return position;
	}

}
