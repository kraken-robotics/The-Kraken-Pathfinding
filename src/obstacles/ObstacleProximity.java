package obstacles;

import smartMath.Vec2;

/**
 * Obstacles détectés par capteurs de proximité (ultrasons et infrarouges)
 * @author pf, marsu
 */
public class ObstacleProximity extends ObstacleCircular
{
	private long death_date;

	public ObstacleProximity (Vec2 position, int rad, long death_date)
	{
		super(position,rad);
		this.death_date = death_date;
	}
	
	@Override
	public ObstacleProximity clone()
	{
		return new ObstacleProximity(position.clone(), radius, death_date);
	}
	
	@Override
	public String toString()
	{
		return super.toString()+", meurt à "+death_date+" ms";
	}
	
	@Override
	public boolean isDestructionNecessary(long date)
	{
		return death_date < date;
	}
}
