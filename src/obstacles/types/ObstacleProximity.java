package obstacles.types;

import java.util.ArrayList;

import pathfinding.GridSpace;
import permissions.ReadOnly;
import permissions.ReadWrite;
import robot.Speed;
import utils.Vec2;

/**
 * Obstacles détectés par capteurs de proximité (ultrasons et infrarouges)
 * @author pf, marsu
 */
public class ObstacleProximity extends ObstacleCircular
{
	private long death_date;
	private ArrayList<Integer> pourtourGrille;
	
	public ObstacleProximity(Vec2<ReadOnly> position, int rad, long death_date)
	{
		super(position,rad);
		this.death_date = death_date;
		int centreX = (int) Math.round(position.x / 50. + GridSpace.NB_POINTS_PAR_METRE*1.5);
		int centreY = (int) Math.round(position.y / 50.);
		for(Vec2<ReadOnly> point : Obstacle.pourtourGrillePatron)
			// On s'assure bien d'être dans la grille
			if(centreX + point.x >= 0 && centreX + point.x < 3 * GridSpace.NB_POINTS_PAR_METRE && centreY + point.y >= 0 && centreY + point.y < 2 * GridSpace.NB_POINTS_PAR_METRE)
				pourtourGrille.add(4 * (3 * GridSpace.NB_POINTS_PAR_METRE * (centreY+point.y) + (centreX+point.x)));
	}
	
	public ArrayList<Integer> getPourtourGrille()
	{
		return pourtourGrille;
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
    public boolean obstacle_proximite_dans_segment(Vec2<ReadOnly> A, Vec2<ReadOnly> B, long date)
    {
    	// si l'obstacle est présent dans le segment...
    	ObstacleRectangular r = new ObstacleRectangular(A,B);
    	if(death_date > date && r.isColliding(this))
    	{
    		// on vérifie si par hasard il ne disparaîtrait pas avant qu'on y arrive
    		int tempsRestant = (int)(death_date - date);
    		
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
	public void setDeathDate(long death_date)
	{
		this.death_date = death_date;
	}
	
	public long getDeathDate()
	{
		return death_date;
	}
	
}
