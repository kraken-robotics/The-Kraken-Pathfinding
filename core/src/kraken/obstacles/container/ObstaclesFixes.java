/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken.obstacles.container;

import java.util.ArrayList;
import java.util.List;
import kraken.obstacles.types.Obstacle;
import kraken.utils.Log;

/**
 * Classe qui contient les obstacles fixes
 * @author pf
 *
 */

public class ObstaclesFixes
{

    private List<Obstacle> obstacles = new ArrayList<Obstacle>();
    protected Log log;
    
    public ObstaclesFixes(Log log)
    {
    	this.log = log;
    }

    public boolean addAll(List<Obstacle> o)
    {
    	return obstacles.addAll(o);
    }
    
    public List<Obstacle> getObstacles()
    {
    	return obstacles;
    }

}
