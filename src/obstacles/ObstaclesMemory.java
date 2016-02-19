package obstacles;

import java.util.LinkedList;

import obstacles.types.ObstacleProximity;
import permissions.ReadOnly;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2;
import container.Service;

/**
 * Mémorise tous les obstacles mobiles qu'on a rencontré jusque là.
 * Il y a un mécanisme de libération de mémoire transparent.
 * @author pf
 *
 */

public class ObstaclesMemory implements Service
{
    // Les obstacles mobiles, c'est-à-dire des obstacles de proximité
    private volatile LinkedList<ObstacleProximity> listObstaclesMobiles = new LinkedList<ObstacleProximity>();
    private int dureeAvantPeremption;
	private int rayonEnnemi;
	private volatile int size = 0;
	private volatile int firstNotDeadNow;
	
	protected Log log;
	
	public ObstaclesMemory(Log log)
	{
		this.log = log;
	}

	public synchronized ObstacleProximity add(Vec2<ReadOnly> position, boolean urgent)
	{
		return add(position, System.currentTimeMillis(), urgent);
	}
	
	public synchronized ObstacleProximity add(Vec2<ReadOnly> position, long date, boolean urgent)
	{
        ObstacleProximity obstacle = new ObstacleProximity(position, rayonEnnemi, date+dureeAvantPeremption, urgent);
//      log.warning("Obstacle créé, rayon = "+rayon_robot_adverse+", centre = "+position+", meurt à "+(date_actuelle+dureeAvantPeremption), this);
        listObstaclesMobiles.add(obstacle);
        size++;
		return obstacle;
	}
	
	public synchronized int size()
	{
		return size;
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		rayonEnnemi = config.getInt(ConfigInfo.RAYON_ROBOT_ADVERSE);
		dureeAvantPeremption = config.getInt(ConfigInfo.DUREE_PEREMPTION_OBSTACLES);
	}

	public synchronized ObstacleProximity getObstacle(int nbTmp)
	{
		if(nbTmp < firstNotDeadNow)
			return null;
		return listObstaclesMobiles.get(nbTmp-firstNotDeadNow);
	}

	/**
	 * Renvoie vrai s'il y a effectivement suppression
	 * @return
	 */
	public synchronized boolean deleteOldObstacles()
	{
		long dateActuelle = System.currentTimeMillis();
		boolean destroyed = false;
		while(!listObstaclesMobiles.isEmpty())
		{
			if(listObstaclesMobiles.getFirst().isDestructionNecessary(dateActuelle))
			{
				firstNotDeadNow++;
				listObstaclesMobiles.removeFirst();
				destroyed = true;
			}
			else
				break;
		}
		return destroyed;
	}
	
	public synchronized long getNextDeathDate()
	{
	    if(!listObstaclesMobiles.isEmpty())
	    	return listObstaclesMobiles.getFirst().getDeathDate();
	    else
	    	return Long.MAX_VALUE;
	}
	
	public synchronized int getFirstNotDeadNow()
	{
		return firstNotDeadNow;
	}

	/**
	 * Utilisé à fin de test uniquement
	 * @return
	 */
	public LinkedList<ObstacleProximity> getListObstaclesMobiles()
	{
		return listObstaclesMobiles;
	}

}
