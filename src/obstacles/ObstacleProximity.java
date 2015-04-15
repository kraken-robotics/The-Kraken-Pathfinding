package obstacles;

import robot.Speed;
import vec2.ReadOnly;
import vec2.ReadWrite;
import vec2.Vec2;

/**
 * Obstacles détectés par capteurs de proximité (ultrasons et infrarouges)
 * @author pf, marsu
 */
public class ObstacleProximity extends ObstacleCircular
{
	private int death_date;

	public ObstacleProximity(Vec2<ReadOnly> position, int rad, int death_date)
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
	 * @param date
	 * @return
	 */
    public boolean obstacle_proximite_dans_segment(Vec2<ReadOnly> A, Vec2<ReadOnly> B, int date)
    {
    	// si l'obstacle est présent dans le segment...
    	ObstacleRectangular r = new ObstacleRectangular(A,B);
    	if(death_date > date && r.isColliding(this))
    	{
    		// on vérifie si par hasard il ne disparaîtrait pas avant qu'on y arrive
    		int tempsRestant = death_date - date;
    		
    		// distance maximale à parcourir alors que l'obstacle est encore là
    		double distanceMax = tempsRestant*Speed.BETWEEN_SCRIPTS.translationnalSpeed;
    		// C est le point auquel l'obstacle disparaît
    		Vec2<ReadWrite> C = B.minusNewVector(A);
    		double distanceBetweenAandB = (long)A.distance(B);

    		// si C est au-delà de B, ce n'est même pas la peine d'essayer
    		if(distanceMax > distanceBetweenAandB)
    			return true;

    		double facteurMultiplicatif = distanceMax / distanceBetweenAandB;
    		C.x = (int)(C.x * facteurMultiplicatif);
    		C.y = (int)(C.y * facteurMultiplicatif);
    		Vec2.plus(C, A);
        	ObstacleRectangular r2 = new ObstacleRectangular(A,C.getReadOnly());
        	return r2.isColliding(this);
    	}
    	return false;
    }

    /**
     * Utilisé pour mettre à jour l'ennemi hypothétique
     * @param clone
     */
	public void setPosition(Vec2<ReadOnly> position)
	{
		position.copy(this.position.getReadWrite());
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
