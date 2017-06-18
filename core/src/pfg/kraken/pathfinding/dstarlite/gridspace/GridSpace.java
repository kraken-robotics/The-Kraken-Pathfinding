/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.pathfinding.dstarlite.gridspace;

import config.Config;
import graphic.Fenetre;
import graphic.AbstractPrintBuffer;
import graphic.printable.Layer;
import graphic.printable.Printable;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.Couleur;
import pfg.kraken.obstacles.container.DynamicObstacles;
import pfg.kraken.obstacles.container.ObstaclesFixes;
import pfg.kraken.obstacles.types.Obstacle;
import pfg.kraken.utils.Log;
import pfg.kraken.utils.XY;

import java.awt.Graphics;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

/**
 * La classe qui contient la grille utilisée par le pathfinding.
 * Utilisée uniquement pour le pathfinding DStarLite.
 * Notifie quand il y a un changement d'obstacles
 * 
 * @author pf
 *
 */

public class GridSpace implements Printable
{
	private static final long serialVersionUID = 3849267693380819201L;
	protected Log log;
	private DynamicObstacles dynamicObs;
	private PointGridSpaceManager pointManager;
	private AbstractPrintBuffer buffer;
	private int distanceMinimaleEntreProximite;
	private int rayonRobot, rayonRobotObstaclesFixes;

	// cette grille est constante, c'est-à-dire qu'elle ne contient que les
	// obstacles fixes
	private BitSet grilleStatique = new BitSet(PointGridSpace.NB_POINTS);
	private BitSet grilleStatiqueModif = new BitSet(PointGridSpace.NB_POINTS);
	private BitSet newObstacles = new BitSet(PointGridSpace.NB_POINTS * 8);
	private BitSet oldObstacles = new BitSet(PointGridSpace.NB_POINTS * 8);
	private BitSet intersect = new BitSet(PointGridSpace.NB_POINTS * 8);
	private BitSet[] newOldObstacles = new BitSet[2];
	private Couleur[] grid = new Couleur[PointGridSpace.NB_POINTS];

	public GridSpace(Log log, ObstaclesFixes fixes, DynamicObstacles dynamicObs, PointGridSpaceManager pointManager, AbstractPrintBuffer buffer, Config config)
	{
		this.dynamicObs = dynamicObs;
		this.log = log;
		this.pointManager = pointManager;
		this.buffer = buffer;
		newOldObstacles[0] = oldObstacles;
		newOldObstacles[1] = newObstacles;

		distanceMinimaleEntreProximite = config.getInt(ConfigInfoKraken.DISTANCE_BETWEEN_PROXIMITY_OBSTACLES);
		rayonRobot = config.getInt(ConfigInfoKraken.DILATATION_ROBOT_DSTARLITE);
		rayonRobotObstaclesFixes = config.getInt(ConfigInfoKraken.RAYON_ROBOT_SUPPRESSION_OBSTACLES_FIXES);

		// on ajoute les obstacles fixes une fois pour toute si c'est demandé
		if(config.getBoolean(ConfigInfoKraken.GRAPHIC_FIXED_OBSTACLES))
			for(Obstacle o : fixes.getObstacles())
				buffer.add(o);

		log.debug("Grille statique initialisée");

		double distance = rayonRobot + PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS / 2;
		distance = distance * distance;

		for(int i = 0; i < PointGridSpace.NB_POINTS; i++)
		{
			for(Obstacle o : fixes.getObstacles())
				if(o.squaredDistance(pointManager.get(i).computeVec2()) <= (int) (distance))
				{
					// Pour le D* Lite, il faut dilater les obstacles du rayon
					// du robot
					grilleStatique.set(i);
					break; // on ne vérifie pas les autres obstacles
				}
		}

		grilleStatiqueModif.clear();
		grilleStatiqueModif.or(grilleStatique);

		// l'affichage du d* lite est géré par le gridspace
		if(config.getBoolean(ConfigInfoKraken.GRAPHIC_D_STAR_LITE) || config.getBoolean(ConfigInfoKraken.GRAPHIC_D_STAR_LITE_FINAL))
		{
			buffer.add(this);
			reinitGraphicGrid();
		}

	}

	/**
	 * Réinitialise la grille d'affichage
	 */
	public void reinitGraphicGrid()
	{
		synchronized(buffer)
		{
			for(int i = 0; i < PointGridSpace.NB_POINTS; i++)
				if(grilleStatique.get(i))
					grid[pointManager.get(i).hashcode] = Couleur.NOIR;
				else
					grid[pointManager.get(i).hashcode] = null;

			buffer.notify();
		}
	}

	/**
	 * Renvoie la distance en fonction de la direction.
	 * Attention ! Ne prend pas en compte les obstacles dynamiques
	 * 
	 * @param i
	 * @return
	 */
	public int distanceStatique(PointDirige point)
	{
		// s'il y a un obstacle statique
		PointGridSpace voisin = pointManager.getGridPointVoisin(point);
		if(grilleStatiqueModif.get(point.point.hashcode) || voisin == null || grilleStatiqueModif.get(voisin.hashcode))
			return Integer.MAX_VALUE;
		return point.dir.distance;
	}

	public boolean isInGrilleStatique(PointGridSpace p)
	{
		return grilleStatiqueModif.get(p.hashcode);
	}

	/**
	 * Un nouveau DStarLite commence. Il faut lui fournir les obstacles actuels
	 * 
	 * @return
	 */
/*	public BitSet getCurrentObstacles()
	{
		iteratorDStarLiteFirst = dynamicObs.getCurrentDynamicObstacles();
		iteratorDStarLiteLast = dynamicObs.getCurrentDynamicObstacles();
		newObstacles.clear();

		while(iteratorDStarLiteLast.hasNext())
		{
			List<PointDirige> tmp = iteratorDStarLiteLast.next().getMasque().masque;
			for(PointDirige p : tmp)
				// si on est déjà dans un obstacle, la distance ne change pas
				if(distanceStatique(p) != Integer.MAX_VALUE)
					newObstacles.set(p.hashCode());
		}

		return newObstacles;
	}*/

	/**
	 * Retourne les obstacles à supprimer (indice 0) et ceux à ajouter (indice
	 * 1) dans le DStarLite
	 */
	public BitSet[] getOldAndNewObstacles()
	{
/*		synchronized(obstaclesMemory)
		{
			oldObstacles.clear();
			newObstacles.clear();

			while(iteratorDStarLiteFirst.hasNextDead())
			{
				// log.debug("Mort");
				List<PointDirige> tmp = iteratorDStarLiteFirst.next().getMasque().masque;
				for(PointDirige p : tmp)
					if(distanceStatique(p) != Integer.MAX_VALUE)
						oldObstacles.set(p.hashCode());
			}

			ObstacleProximity o;
			while((o = obstaclesMemory.pollMortTot()) != null)
			{
				// log.debug("Mort tôt");
				List<PointDirige> tmp = o.getMasque().masque;
				for(PointDirige p : tmp)
					if(distanceStatique(p) != Integer.MAX_VALUE)
						oldObstacles.set(p.hashCode());
			}

			while(iteratorDStarLiteLast.hasNext())
			{
				// log.debug("Nouveau");
				List<PointDirige> tmp = iteratorDStarLiteLast.next().getMasque().masque;
				for(PointDirige p : tmp)
					if(distanceStatique(p) != Integer.MAX_VALUE)
						newObstacles.set(p.hashCode());
			}
*/
			/**
			 * On ne va pas enlever un point pour le remettre juste après…
			 */

			intersect.clear();
			intersect.or(newObstacles);
			intersect.and(oldObstacles);
			newObstacles.andNot(intersect);
			oldObstacles.andNot(intersect);

			return newOldObstacles;
//		}
	}

	@Override
	public void print(Graphics g, Fenetre f)
	{
		for(int i = 0; i < PointGridSpace.NB_POINTS; i++)
			if(grid[i] != null)
			{
				g.setColor(grid[i].couleur);
				pointManager.get(i).print(g, f);
			}
		g.setColor(Couleur.NOIR.couleur);
	}

	/**
	 * Permet au D* Lite d'afficher des couleurs
	 * 
	 * @param gridpoint
	 * @param couleur
	 */
	public void setColor(PointGridSpace gridpoint, Couleur couleur)
	{
		if(gridpoint != null)
			synchronized(buffer)
			{
				grid[gridpoint.hashcode] = couleur;
				buffer.notify();
			}
	}

	@Override
	public int getLayer()
	{
		return Layer.MIDDLE.ordinal();
	}

	public void disableObstaclesFixes(XY position, Obstacle obstacle)
	{
		// on initialise comme la grille statique classique
		grilleStatiqueModif.clear();
		grilleStatiqueModif.or(grilleStatique);
		for(int i = 0; i < PointGridSpace.NB_POINTS; i++)
			if(grilleStatiqueModif.get(i) && ((position != null && pointManager.get(i).computeVec2().distanceFast(position) < rayonRobotObstaclesFixes) || obstacle.squaredDistance(pointManager.get(i).computeVec2()) == 0))
				grilleStatiqueModif.clear(i);
	}

}
