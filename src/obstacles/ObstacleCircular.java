package obstacles;

import smartMath.Vec2;

/**
 * Obstacle circulaire
 * @author pf
 *
 */
public class ObstacleCircular extends Obstacle
{
	// le Vec2 "position" indique le centre de l'obstacle
	
	// rayon de cet obstacle
	protected int radius;
	
	public ObstacleCircular(Vec2 position, int rad)
	{
		super(position);
		this.radius = rad;
	}
	
	public ObstacleCircular clone()
	{
		return new ObstacleCircular(position.clone(), radius);
	}

	public int getRadius()
	{
		return radius;
	}
	
	// Copie this dans oc, sans modifier this
	public void clone(ObstacleCircular oc)
	{
		oc.position = position;
		oc.radius = radius;
	}

	public String toString()
	{
		return super.toString()+", rayon: "+radius;
	}
	
	public boolean isInObstacle(Vec2 point)
	{
		return point.squaredDistance(position) <= radius*radius;
	}

	public boolean isProcheObstacle(Vec2 point, int distance)
	{
		return point.squaredDistance(position) <= (radius+distance)*(radius+distance);
	}

	private boolean collisionDroite(Vec2 A, Vec2 B)
	{
		Vec2 C = position;
		Vec2 AB = B.minusNewVector(A);
		Vec2 AC = C.minusNewVector(A);
	    float numerateur = Math.abs(AB.x*AC.y - AB.y*AC.x);
	    float denominateur = AB.squaredLength();
	    float CI = numerateur*numerateur / denominateur;
	    if (CI < radius*radius)
	       return true;
	    else
	       return false;
	}
	
    /**
     * Ce cercle croise-t-il le segment [A,B]?
     * @param A
     * @param B
     * @return
     */
    public boolean obstacle_proximite_dans_segment(Vec2 A, Vec2 B)
    {
    	// Ceci peut être le cas pour certain élément de jeu dont il ne faut pas vérifier les collisions
    	if(radius < 0)
    		return false;
    	/**
    	 * Ce code a été honteusement pompé sur http://openclassrooms.com/courses/theorie-des-collisions/formes-plus-complexes
    	 */
    	
    	if (!collisionDroite(A, B))
	        return false;  // si on ne touche pas la droite, on ne touchera jamais le segment
    	
    	float pscal1 = B.minusNewVector(A).dot(position.minusNewVector(A));
    	float pscal2 = A.minusNewVector(B).dot(position.minusNewVector(B));
	    if (pscal1>=0 && pscal2>=0)
	       return true;
	    // dernière possibilité, A ou B dans le cercle
	    if(isInObstacle(A) || isInObstacle(B))
	      return true;
	    return false;
    }

}
