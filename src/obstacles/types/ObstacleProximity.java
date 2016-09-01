package obstacles.types;

import java.util.ArrayList;

import utils.Vec2;
import utils.permissions.ReadOnly;

/**
 * Obstacles détectés par capteurs de proximité (ultrasons et infrarouges)
 * @author pf, marsu
 */
public class ObstacleProximity extends ObstacleCircular
{
	private long death_date;
	private ArrayList<Integer> masque;
	
	public ObstacleProximity(Vec2<ReadOnly> position, int rad, long death_date, ArrayList<Integer> masque)
	{
		super(position,rad);
		this.death_date = death_date;
		this.masque = masque;
	}

	public ArrayList<Integer> getMasque()
	{
		return masque;
	}
	
	@Override
	public String toString()
	{
		return super.toString()+", meurt à "+death_date+" ms";
	}
	
	public boolean isDestructionNecessary(long date)
	{
		return death_date < date;
	}
		
	public long getDeathDate()
	{
		return death_date;
	}
	
}
