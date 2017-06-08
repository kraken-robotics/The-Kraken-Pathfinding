package table.obstacles;

import smartMath.Vec2;

/**
 * Obstacles détectés par capteurs de proximité (ultrasons et infrarouges)
 * @author pf, marsu
 */
class ObstacleProximity extends ObstacleCircular
{
	public ObstacleProximity (Vec2 position, int rad)
	{
		super(position,rad);
	}
	
	public ObstacleProximity clone()
	{
		return new ObstacleProximity(position.clone(), getRadius());
	}
}
