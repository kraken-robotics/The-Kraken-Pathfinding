package obstacles;

import java.util.Iterator;

import obstacles.types.ObstacleProximity;
import utils.Log;

/**
 * Itérator permettant de manipuler facilement les obstacles mobiles
 * @author pf
 *
 */

public class ObstaclesIterator implements Iterator<ObstacleProximity>
{
    private Log log;
    private ObstaclesMemory memory;
    
    private int firstNotDead = 0;
    private int nbTmp;
    private long lastDate = -1;
    private long dateInit;
    private boolean needInit = true;
	
    public ObstaclesIterator(Log log, ObstaclesMemory memory)
    {
        this.log = log;
        this.memory = memory;
    }
    
    public ObstaclesIterator clone(long date)
    {
    	ObstaclesIterator cloned_manager = new ObstaclesIterator(log, memory);
		copy(cloned_manager, date);
		return cloned_manager;
    }
    
    /**
     * Nécessaire au fonctionnement du memory manager
     * @param other
     */
    public void copy(ObstaclesIterator other, long date)
    {
    	other.firstNotDead = firstNotDead;
    	other.needInit = true;
    	other.dateInit = date;
    }
    
    @Override
    public int hashCode()
    {
    	return firstNotDead;
    }
    /**
     * Utilisé afin de calculer la péremption du cache du gridspace
     * @param other
     * @return
     */
    public boolean equals(ObstaclesIterator other)
    {
        return firstNotDead == other.firstNotDead;
    }

	/**
	 * Quelque chose change si un obstacle disparaît
	 * Si jamais rien ne change, renvoie Long.MAX_VALUE
	 * @return
	 */
	public long getDateSomethingChange()
	{
	    if(firstNotDead < memory.size())
	    	return memory.getObstacle(firstNotDead).getDeathDate();
	    else
	    	return Long.MAX_VALUE;
	}
	
	/**
	 * Réinitialise l'itérateur avec la date donnée à la dernière initialisation
	 */
	public void reinit()
	{
		if(needInit)
			init(dateInit);
		if(lastDate == -1)
			reinitNow();
		else
			nbTmp = firstNotDead;
	}

	/**
	 * Calcule l'entrée où commencent les obstacles maintenant
	 */
	public void reinitNow()
	{
		firstNotDead = memory.getFirstNotDeadNow();
		nbTmp = firstNotDead;
	}
	
	/**
	 * Calcule l'entrée où commence les obstacles à cette date.
	 * Se fait à la copie.
	 * @param date
	 */
	private void init(long date)
	{
		// Si on a avancé dans le futur, on sait que firstNotDead ne peut qu'être plus grand
		if(date < lastDate)
			firstNotDead = memory.getFirstNotDeadNow();
		
		ObstacleProximity next;
		while(firstNotDead < memory.size())
		{
			next = memory.getObstacle(firstNotDead);
			if(next.isDestructionNecessary(date))
				firstNotDead++;
			else
				break;
		}
		nbTmp = firstNotDead;		
		lastDate = date;
		needInit = false;
	}
	
	@Override
	public boolean hasNext()
	{		
		return nbTmp < memory.size();
	}

	// Pour parcourir tous ceux qui sont morts
	public boolean hasNextDead()
	{		
		return nbTmp < memory.getFirstNotDeadNow();
	}

	@Override
	public ObstacleProximity next()
	{
		return memory.getObstacle(nbTmp++);
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	

}
