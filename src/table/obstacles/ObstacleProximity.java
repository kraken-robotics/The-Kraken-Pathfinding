package table.obstacles;

import smartMath.Vec2;

/**
 * Obstacles détectés par capteurs de proximité (ultrasons et infrarouges)
 * @author pf, marsu
 */
class ObstacleProximity extends ObstacleCircular
{
	public long death_date;

	public ObstacleProximity (Vec2 position, int rad, long death_date)
	{
		super(position,rad);
		this.death_date = death_date;
	}
	
	public ObstacleProximity clone()
	{
		return new ObstacleProximity(position.clone(), getRadius(), death_date);
	}
	
	public String toString()
	{
		return super.toString()+", meurt dans "+(death_date-System.currentTimeMillis())+" ms";
	}
}
