/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.obstacles.container.DynamicObstacles;
import pfg.kraken.obstacles.container.StaticObstacles;
import pfg.kraken.utils.XY;

/**
 * Moteur physique par défaut
 * @author pf
 *
 */

public final class DefaultPhysicsEngine implements PhysicsEngine
{
	private List<Obstacle> fixed;
	private DynamicObstacles dynamicObs;
	private List<Obstacle> currentObstacles = new ArrayList<Obstacle>();

	public DefaultPhysicsEngine(StaticObstacles fixes, DynamicObstacles dynamicObs)
	{
//		this.fixes = fixes;
		fixed = fixes.getObstacles();
		this.dynamicObs = dynamicObs;
		coins[0] = fixes.getBottomLeftCorner();
		coins[2] = fixes.getTopRightCorner();
		coins[1] = new XY(coins[0].getX(), coins[2].getY());
		coins[3] = new XY(coins[2].getX(), coins[0].getY());
	}
	
	private XY[] coins = new XY[4];
	
	@Override
	public void update()
	{
		// on récupère les obstacles courants une fois pour toutes
		currentObstacles.clear();
		Iterator<Obstacle> iter = dynamicObs.getCurrentDynamicObstacles();
		while(iter.hasNext())
			currentObstacles.add(iter.next());
	}
	
	@Override
	public boolean isThereCollision(Iterable<RectangularObstacle> tentacle)
	{
		for(RectangularObstacle co : tentacle)
		{
			// On vérifie la collision avec les murs
			for(int i = 0; i < 4; i++)
				if(co.isColliding(coins[i], coins[(i+1)&3]))
					return true;

			// Collision avec un obstacle fixe?
			for(Obstacle o : fixed)
				if(o.isColliding(co))
					// log.debug("Collision avec "+o);
					return true;

			// Collision avec un obstacle de proximité ?
			// TODO : utiliser getFutureDynamicObstacles
			for(Obstacle n : currentObstacles)
				if(n.isColliding(co))
					return true;
		}

		return false;
	}

}
