package table;

import java.util.Iterator;

import obstacles.ObstacleProximity;
import container.Service;
import utils.Config;
import utils.Log;

/**
 * Service qui traite tout ce qui concerne la gestion des obstacles.
 * @author pf
 *
 */

public class ObstacleManager implements Service, Iterator<ObstacleProximity>
{
    private Log log;
    private ObstaclesMobilesMemory memory;
    
    private int firstNotDead = 0;
    private int nbTmp;
    private int lastDate = -1;
	
    public ObstacleManager(Log log, ObstaclesMobilesMemory memory)
    {
        this.log = log;
        this.memory = memory;
    }
    
    public int getHash()
    {
    	return firstNotDead;
    }
    
    public ObstacleManager clone()
    {
    	ObstacleManager cloned_manager = new ObstacleManager(log, memory);
		copy(cloned_manager);
		return cloned_manager;
    }
    
    /**
     * Nécessaire au fonctionnement du memory manager
     * @param other
     */
    public void copy(ObstacleManager other)
    {
    	other.firstNotDead = firstNotDead;
    }
    
    /**
     * Utilisé afin de calculer la péremption du cache du gridspace
     * @param other
     * @return
     */
    public boolean equals(ObstacleManager other)
    {
        return firstNotDead == other.firstNotDead;
    }

	@Override
	public void updateConfig(Config config)
	{}
    
	@Override
	public void useConfig(Config config)
	{}
	
	/**
	 * Utilisé pour la copie
	 * @return
	 */
	public int getFirstNotDead()
	{
		return firstNotDead;
	}

	/**
	 * Quelque chose change si un obstacle disparaît
	 * Si jamais rien ne change, renvoie Long.MAX_VALUE
	 * @return
	 */
	public long getDateSomethingChange()
	{
	    if(firstNotDead < memory.nbMax())
	    	return memory.getObstacle(firstNotDead).getDeathDate();
	    else
	    	return Long.MAX_VALUE;
	}
	
	/**
	 * Réinitialise l'itérateur avec la date donnée à la dernière initialisation
	 */
	public void reinit()
	{
		nbTmp = firstNotDead;
	}
	
	/**
	 * Calcule l'entrée où commence les obstacles maintenant
	 */
	public void initNow()
	{
		firstNotDead = memory.getFirstNotDeadNow();
		nbTmp = firstNotDead;
	}
	
	/**
	 * Calcule l'entrée où commence les obstacles à cette date
	 * @param date
	 */
	public void init(int date)
	{
		// Si on a avancé dans le futur, on sait que firstNotDead ne peut qu'être plus grand
		if(date < lastDate)
			firstNotDead = memory.getFirstNotDeadNow();
		
		ObstacleProximity next;
		while(firstNotDead < memory.nbMax())
		{
			next = memory.getObstacle(firstNotDead);
			if(next.isDestructionNecessary(date))
				firstNotDead++;
			else
				break;
		}
		nbTmp = firstNotDead;		
		lastDate = date;
	}
	
	@Override
	public boolean hasNext()
	{		
		return nbTmp < memory.nbMax();
	}
	
	@Override
	public ObstacleProximity next()
	{
		return memory.getObstacle(nbTmp++);
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub		
	}

	

}
