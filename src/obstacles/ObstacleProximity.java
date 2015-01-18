package obstacles;

import robot.Speed;
import utils.Vec2;

/**
 * Obstacles détectés par capteurs de proximité (ultrasons et infrarouges)
 * @author pf, marsu
 */
public class ObstacleProximity extends ObstacleCircular
{
	private int death_date;

	public ObstacleProximity(Vec2 position, int rad, int death_date)
	{
		super(position,rad);
		this.death_date = death_date;
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

	/**
	 * Peut-on aller de A à B sans 
	 * @param A
	 * @param B
	 * @param distance
	 * @param date
	 * @return
	 */
    public boolean obstacle_proximite_dans_segment(Vec2 A, Vec2 B, int distance, int date)
    {
    	// si l'obstacle est présent dans le segment...
    	if(death_date > date && obstacle_proximite_dans_segment(A, B, distance))
    	{
    		// on vérifie si par hasard il ne disparaîtrait pas avant qu'on y arrive
    		int tempsRestant = death_date - date;
    		
    		// distance maximale à parcourir alors que l'obstacle est encore là
    		double distanceMax = tempsRestant*Speed.BETWEEN_SCRIPTS.translationnalSpeed;
    		// C est le point auquel l'obstacle disparaît
    		Vec2 C = B.minusNewVector(A);
    		double distanceBetweenAandB = (long)A.distance(B);

    		// si C est au-delà de B, ce n'est même pas la peine d'essayer
    		if(distanceMax > distanceBetweenAandB)
    			return true;

    		double facteurMultiplicatif = distanceMax / distanceBetweenAandB;
    		C.x = (int)(C.x * facteurMultiplicatif);
    		C.y = (int)(C.y * facteurMultiplicatif);
    		C.plus(A);
    		return obstacle_proximite_dans_segment(A, C, distance);
    	}
    	return false;
    }

    /**
     * Utilisé pour mettre à jour l'ennemi hypothétique
     * @param clone
     */
	public void setPosition(Vec2 position)
	{
		position.copy(this.position);
	}

    /**
     * Utilisé pour mettre à jour l'ennemi hypothétique
     * @param clone
     */
	public void setDeathDate(int death_date)
	{
		this.death_date = death_date;
	}
	
	public int getDeathDate()
	{
		return death_date;
	}

}
