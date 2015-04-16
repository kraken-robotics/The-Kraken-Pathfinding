package obstacles;

import permissions.Permission;
import permissions.ReadOnly;
import permissions.ReadWrite;
import robot.Speed;
import utils.Vec2;

/**
 * Obstacles détectés par capteurs de proximité (ultrasons et infrarouges)
 * @author pf, marsu
 */
public class ObstacleProximity<T extends Permission> extends ObstacleCircular<T>
{
	private int death_date;

	public ObstacleProximity(Vec2<T> position, int rad, int death_date)
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
    	ObstacleRectangular<ReadOnly> r = new ObstacleRectangular<ReadOnly>(A,B);
    	if(death_date > date && r.isColliding(getReadOnly()))
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
        	ObstacleRectangular<ReadOnly> r2 = new ObstacleRectangular<ReadOnly>(A,C.getReadOnly());
        	return r2.isColliding(getReadOnly());
    	}
    	return false;
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
