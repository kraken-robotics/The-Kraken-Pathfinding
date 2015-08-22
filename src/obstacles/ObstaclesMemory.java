package obstacles;

import java.util.ArrayList;

import obstacles.types.ObstacleProximity;
import permissions.ReadOnly;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2;
import container.Service;

/**
 * Mémorise tous les obstacles mobiles qu'on a rencontré jusque là.
 * On peut ajouter mais pas retirer d'obstacles : les obstacles périmés sont conservés en mémoire.
 * Il y a un mécanisme de libération de mémoire transparent.
 * @author pf
 *
 */

public class ObstaclesMemory implements Service
{
    // Les obstacles mobiles, c'est-à-dire des obstacles de proximité
    private volatile ArrayList<ObstacleProximity> listObstaclesMobiles = new ArrayList<ObstacleProximity>();
    private int dureeAvantPeremption;
	private int rayonEnnemi;
	private volatile int size = 0;
	private volatile int firstNotDeadNow;
	
	protected Log log;
	
	public ObstaclesMemory(Log log)
	{
		this.log = log;
	}
	
	public synchronized void add(Vec2<ReadOnly> position, long date_actuelle)
	{
        ObstacleProximity obstacle = new ObstacleProximity(position, rayonEnnemi, date_actuelle+dureeAvantPeremption);
//      log.warning("Obstacle créé, rayon = "+rayon_robot_adverse+", centre = "+position+", meurt à "+(date_actuelle+dureeAvantPeremption), this);
        listObstaclesMobiles.add(obstacle);
        size++;
		notify();
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
		return listObstaclesMobiles.get(nbTmp-firstNotDeadNow);
	}

	public synchronized void deleteOldObstacles()
	{
		boolean shouldNotify = false;
		while(!listObstaclesMobiles.isEmpty())
		{
			if(listObstaclesMobiles.get(0).isDestructionNecessary(System.currentTimeMillis()))
			{
				firstNotDeadNow++;
				listObstaclesMobiles.remove(0);
				shouldNotify = true;
			}
			else
				break;
		}
		if(shouldNotify)
			notify();
	}
	
	public synchronized long getNextDeathDate()
	{
	    if(!listObstaclesMobiles.isEmpty())
	    	return listObstaclesMobiles.get(0).getDeathDate();
	    else
	    	return Long.MAX_VALUE;
	}
	
	public synchronized int getFirstNotDeadNow()
	{
		return firstNotDeadNow;
	}

}
