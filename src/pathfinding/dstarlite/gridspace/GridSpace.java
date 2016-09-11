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

package pathfinding.dstarlite.gridspace;

import graphic.Fenetre;
import graphic.Fenetre.Couleur;

import java.util.ArrayList;
import java.util.BitSet;

import obstacles.ObstaclesFixes;
import obstacles.memory.ObstaclesIteratorPresent;
import obstacles.memory.ObstaclesMemory;
import obstacles.types.ObstacleProximity;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2RO;
import container.Service;

/**
 * La classe qui contient la grille utilisée par le pathfinding.
 * Utilisée uniquement pour le pathfinding DStarLite.
 * Notifie quand il y a un changement d'obstacles
 * @author pf
 *
 */

public class GridSpace implements Service
{
	protected Log log;
	private ObstaclesIteratorPresent iteratorDStarLite;
	private ObstaclesIteratorPresent iteratorRemoveNearby;
	private ObstaclesMemory obstaclesMemory;
	private PointGridSpaceManager pointManager;
	private PointDirigeManager pointDManager;
	private Fenetre fenetre;

	private int distanceMinimaleEntreProximite;
	private int rayonRobot;
	
	// cette grille est constante, c'est-à-dire qu'elle ne contient que les obstacles fixes
	private BitSet grilleStatique = new BitSet(PointGridSpace.NB_POINTS);
	
	private ArrayList<PointDirige> masque = new ArrayList<PointDirige>();
	private int centreMasque;
	private long deathDateLastObstacle;
	
	public GridSpace(Log log, ObstaclesIteratorPresent iteratorDStarLite, ObstaclesIteratorPresent iteratorRemoveNearby, ObstaclesMemory obstaclesMemory, PointGridSpaceManager pointManager, PointDirigeManager pointDManager, Fenetre fenetre)
	{
		this.obstaclesMemory = obstaclesMemory;
		this.log = log;
		this.pointManager = pointManager;
		this.pointDManager = pointDManager;
		this.iteratorDStarLite = iteratorDStarLite;
		this.iteratorRemoveNearby = iteratorRemoveNearby;
		this.fenetre = fenetre;
	}
	

	@Override
	public void useConfig(Config config)
	{
		distanceMinimaleEntreProximite = config.getInt(ConfigInfo.DISTANCE_BETWEEN_PROXIMITY_OBSTACLES);
		rayonRobot = config.getInt(ConfigInfo.RAYON_ROBOT);
		int rayonEnnemi = config.getInt(ConfigInfo.RAYON_ROBOT_ADVERSE);
		int rayonPoint = (int) Math.round((rayonEnnemi + rayonRobot) / PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS);
		int tailleMasque = 2*(rayonPoint+1)+1;
		int squaredRayonPoint = rayonPoint * rayonPoint;
		centreMasque = tailleMasque / 2;
		for(int i = 0; i < tailleMasque; i++)
			for(int j = 0; j < tailleMasque; j++)
				if((i-centreMasque) * (i-centreMasque) + (j-centreMasque) * (j-centreMasque) > squaredRayonPoint)
					for(Direction d : Direction.values())
					{
						int i2 = i + d.deltaX, j2 = j + d.deltaY;
						if((i2-centreMasque) * (i2-centreMasque) + (j2-centreMasque) * (j2-centreMasque) <= squaredRayonPoint)
							masque.add(pointDManager.get(j,i,d));
					}

		for(int i = 0; i < PointGridSpace.NB_POINTS; i++)
			for(ObstaclesFixes o : ObstaclesFixes.values())
				if(o.getObstacle().squaredDistance(pointManager.get(i).computeVec2())
						<= (int)(PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS * PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS)/2)
				{
					grilleStatique.set(i);
					fenetre.setColor(pointManager.get(i), Couleur.NOIR);
					break; // on ne vérifie pas les autres obstacles
				}

		log.debug("Grille statique initialisée");

	}

	@Override
	public void updateConfig(Config config)
	{}

	/**
	 * Renvoie la distance en fonction de la direction.
	 * Attention ! Ne prend pas en compte les obstacles dynamiques
	 * @param i
	 * @return
	 */
	public int distanceStatique(PointDirige point)
	{
		// s'il y a un obstacle statique
		if(grilleStatique.get(point.point.hashCode()))
			return Integer.MAX_VALUE;
		if(point.dir.isDiagonal())
			return 1414;
		return 1000;
	}

	/**
	 * Ajoute le contour d'un obstacle de proximité dans la grille dynamique
	 * @param o
	 */
	private ArrayList<PointDirige> getMasqueObstacle(Vec2RO position)
	{
		PointGridSpace p = pointManager.get(position);
		ArrayList<PointDirige> out = new ArrayList<PointDirige>();
		
		for(PointDirige c : masque)
		{
			PointDirige point = pointDManager.get(pointManager.get(c.point.x + p.x - centreMasque, c.point.y + p.y - centreMasque), c.dir);
			if(point != null)
				out.add(point);
		}
		return out;
	}

	/**
	 * Supprime les anciens obstacles et notifie si changement
	 */
	public synchronized void deleteOldObstacles()
	{
		// S'il y a effectivement suppression, on régénère la grille
		if(obstaclesMemory.deleteOldObstacles())
			notify(); // changement de la grille dynamique !
	}

	/**
	 * Fournit au thread de péremption la date de la prochaine mort
	 * @return
	 */
	public long getNextDeathDate()
	{
		return obstaclesMemory.getNextDeathDate();
	}

	/**
	 * Un nouveau DStarLite commence. Il faut lui fournir les obstacles actuels
	 * @return
	 */
	public ArrayList<PointDirige> getCurrentObstacles()
	{
		iteratorDStarLite.reinit();
		ArrayList<PointDirige> out = new ArrayList<PointDirige>();
		ObstacleProximity o = null;
		while(iteratorDStarLite.hasNext())
		{
			o = iteratorDStarLite.next();
//			log.debug("Ajout d'un obstacle au début du dstarlite");
			out.addAll(o.getMasque());
		}
		if(o != null)
			deathDateLastObstacle = o.getDeathDate();
		else
			deathDateLastObstacle = 0;
		
		return out;
	}
	
	/**
	 * Retourne les obstacles à supprimer (indice 0) et ceux à ajouter (indice 1) dans le DStarLite
	 */
	public ArrayList<ObstacleProximity>[] getOldAndNewObstacles()
	{
		synchronized(obstaclesMemory)
		{
			@SuppressWarnings("unchecked")
			ArrayList<ObstacleProximity>[] out = new ArrayList[2];
			out[0] = new ArrayList<ObstacleProximity>();
			out[1] = new ArrayList<ObstacleProximity>();
	
			while(iteratorDStarLite.hasNextDead())
				out[0].add(iteratorDStarLite.next());
			ObstacleProximity p;
			while((p = obstaclesMemory.pollMortTot()) != null)
				out[0].add(p);
	
			long tmp = deathDateLastObstacle;
			while(iteratorDStarLite.hasNext())
			{
				ObstacleProximity o = iteratorDStarLite.next();
				long deathDate = o.getDeathDate();
				if(deathDate > deathDateLastObstacle)
				{
					tmp = deathDate;
					out[1].add(o);
				}
			}
			deathDateLastObstacle = tmp;
			iteratorDStarLite.reinit(); // l'itérateur reprendra juste avant les futurs obstacles périmés
			return out;
		}
	}

    /**
	 * Appelé par le thread des capteurs par l'intermédiaire de la classe capteurs
	 * Ajoute l'obstacle à la mémoire et dans le gridspace
     * Supprime les obstacles mobiles proches
     * Ça allège le nombre d'obstacles.
     * Utilisé par les capteurs
     * @param position
     * @return 
     * @return
     */
    public ObstacleProximity addObstacleAndRemoveNearbyObstacles(Vec2RO position)
    {
    	iteratorRemoveNearby.reinit();
    	while(iteratorRemoveNearby.hasNext())
        	if(iteratorRemoveNearby.next().isProcheCentre(position, distanceMinimaleEntreProximite))
        		iteratorRemoveNearby.remove();

    	ArrayList<PointDirige> masque = getMasqueObstacle(position);
		ObstacleProximity o = obstaclesMemory.add(position, masque);
		// pour un ajout, pas besoin de tout régénérer
		return o;
    }

}
