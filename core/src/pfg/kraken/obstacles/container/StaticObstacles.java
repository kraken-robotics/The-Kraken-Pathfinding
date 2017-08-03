/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.obstacles.container;

import java.util.ArrayList;
import java.util.List;
import pfg.kraken.obstacles.Obstacle;

/**
 * Classe qui contient les obstacles fixes
 * @author pf
 *
 */

public class StaticObstacles
{

    private List<Obstacle> obstacles = new ArrayList<Obstacle>();

    public boolean addAll(List<Obstacle> o)
    {
    	return obstacles.addAll(o);
    }
    
    public List<Obstacle> getObstacles()
    {
    	return obstacles;
    }

    public int hashCode()
    {
    	int out = 0;
    	for(Obstacle o : obstacles)
    		out += o.hashCode();
    	return out;
    }
    
}