package pathfinding.astarCourbe;

import container.Service;
import robot.Cinematique;
import utils.Config;
import utils.Log;
import utils.Vec2;
import utils.permissions.ReadOnly;
import utils.permissions.ReadWrite;

/**
 * Heuristique pour l'AStarCourbe pour la planification.
 * Pour le pathfinding dynamique, on utilise l'heuristique basée sur le DStarLite
 * @author pf
 *
 */

public class HeuristiqueSimple implements HeuristiqueCourbe, Service
{
	private Vec2<ReadWrite> arrivee = new Vec2<ReadWrite>();
	protected Log log;

	public HeuristiqueSimple(Log log)
	{
		this.log = log;
	}
	
	public void setPositionArrivee(Vec2<ReadOnly> arrivee)
	{
		Vec2.copy(arrivee, this.arrivee);
	}
	
	@Override
	public double heuristicCostCourbe(Cinematique c)
	{
//		log.debug(c.getPosition()+" "+arrivee);
		// TODO prendre en compte l'orientation et la courbure à proximité
		double distance = arrivee.distance(c.getPosition());
//		if(distance > 400)
			return distance;
//		else
//			return distance + 200*Math.abs((c.orientation - Math.atan2(arrivee.y - c.getPosition().y, arrivee.x - c.getPosition().x)) % 2*Math.PI);
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
