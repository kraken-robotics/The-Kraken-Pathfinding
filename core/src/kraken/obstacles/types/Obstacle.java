/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


package kraken.obstacles.types;

import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import javax.imageio.ImageIO;
import config.Config;
import graphic.PrintBufferInterface;
import graphic.printable.Layer;
import graphic.printable.Printable;
import kraken.ConfigInfoKraken;
import kraken.Couleur;
import kraken.utils.Log;
import kraken.utils.Vec2RO;
import kraken.utils.Vec2RW;

/**
 * Superclasse abstraite des obstacles.
 * 
 * @author pf
 *
 */

public abstract class Obstacle implements Printable, Serializable, ObstacleInterface
{
	private static final long serialVersionUID = -2508727703931042322L;
	protected Vec2RW position;
	protected transient int distance_dilatation;
	protected static Log log;
	protected static PrintBufferInterface buffer;

	// Pour l'affichage du robot
	protected static Image imageRobot = null, imageRobotRoueG = null, imageRobotRoueD = null;
	protected static int L, d;
	protected static Vec2RO centreRotationGauche, centreRotationDroite;
	protected static boolean printAllObstacles = false;
	protected Layer l = null;
	public Color c;

	public static void set(Log log, PrintBufferInterface buffer)
	{
		Obstacle.log = log;
		Obstacle.buffer = buffer;
	}

	public static void useConfig(Config config)
	{
		printAllObstacles = config.getBoolean(ConfigInfoKraken.GRAPHIC_ALL_OBSTACLES);
		if(imageRobot == null && config.getBoolean(ConfigInfoKraken.GRAPHIC_ROBOT_AND_SENSORS))
		{
			L = config.getInt(ConfigInfoKraken.CENTRE_ROTATION_ROUE_X);
			d = config.getInt(ConfigInfoKraken.CENTRE_ROTATION_ROUE_Y);
			centreRotationGauche = new Vec2RO(L, d);
			centreRotationDroite = new Vec2RO(L, -d);
			try
			{
				imageRobot = ImageIO.read(new File(config.getString(ConfigInfoKraken.GRAPHIC_ROBOT_PATH)));
				imageRobotRoueG = ImageIO.read(new File(config.getString(ConfigInfoKraken.GRAPHIC_ROBOT_ROUE_GAUCHE_PATH)));
				imageRobotRoueD = ImageIO.read(new File(config.getString(ConfigInfoKraken.GRAPHIC_ROBOT_ROUE_DROITE_PATH)));
			}
			catch(IOException e)
			{
				log.warning(e);
			}
		}
	}

	public Obstacle(Vec2RO position, Couleur c)
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
	public boolean isProcheObstacle(Vec2RO position, int distance)
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
	public Layer getLayer()
	{
		if(l == null)
			return Layer.MIDDLE;
		return l;
	}

	@Override
	public Vec2RO getPosition()
	{
		return position;
	}

}
