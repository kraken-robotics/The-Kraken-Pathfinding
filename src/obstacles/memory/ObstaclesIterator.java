package obstacles.memory;

import java.util.Iterator;

import obstacles.types.ObstacleProximity;
import utils.Log;

/**
 * Itérator permettant de manipuler facilement les obstacles mobiles
 * @author pf
 *
 */

public abstract class ObstaclesIterator implements Iterator<ObstacleProximity>
{
    protected Log log;
    protected ObstaclesMemory memory;
    
    protected int nbTmp;
	
    public ObstaclesIterator(Log log, ObstaclesMemory memory)
    {
        this.log = log;
        this.memory = memory;
    }
		
	@Override
	public boolean hasNext()
	{
		return nbTmp + 1 < memory.size();
	}

	@Override
	public ObstacleProximity next()
	{
		return memory.getObstacle(++nbTmp);
	}

	@Override
	public void remove()
	{
		memory.remove(nbTmp--);
	}

}
