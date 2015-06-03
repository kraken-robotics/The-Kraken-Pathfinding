package obstacles;

import permissions.ReadOnly;
import permissions.ReadWrite;
import utils.Vec2;

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
	protected int squared_radius;
	protected static int squared_radius_with_dilatation_obstacle = -1;
	
	public ObstacleCircular(Vec2<ReadOnly> position, int rad)
	{
		super(position);
		this.radius = rad;
		squared_radius = rad * rad;
		if(squared_radius_with_dilatation_obstacle == -1)
		{
			squared_radius_with_dilatation_obstacle = radius + marge + rayonRobot;
			squared_radius_with_dilatation_obstacle *= squared_radius_with_dilatation_obstacle;
		}
	}

	public int getRadius()
	{
		return radius;
	}

	public String toString()
	{
		return super.toString()+", rayon: "+radius;
	}

	private boolean isInObstacle(Vec2<ReadOnly> point, int distance)
	{
		return point.squaredDistance(position) <= (radius+distance)*(radius+distance);
	}

	public boolean isInObstacleDilatation(Vec2<ReadOnly> point)
	{
		return point.squaredDistance(position) <= squared_radius_with_dilatation_obstacle;
	}

	public boolean isInObstacle(Vec2<ReadOnly> point)
	{
		return point.squaredDistance(position) <= squared_radius;
	}

	public boolean isProcheObstacle(Vec2<ReadOnly> point, int distance)
	{
		return point.squaredDistance(position) <= (radius+distance)*(radius+distance);
	}

	private boolean collisionDroite(Vec2<ReadOnly> A, Vec2<ReadOnly> B, int distance)
	{
		Vec2<ReadOnly> C = position.getReadOnly();
		Vec2<ReadWrite> AB = B.minusNewVector(A);
		Vec2<ReadWrite> AC = C.minusNewVector(A);
	    double numerateur = Math.abs(AB.x*AC.y - AB.y*AC.x);
	    double denominateur = AB.squaredLength();
	    double CI = numerateur*numerateur / denominateur;
	    return CI < (radius+distance)*(radius+distance);
	}

	private boolean collisionDroiteDilatation(Vec2<ReadOnly> A, Vec2<ReadOnly> B)
	{
		Vec2<ReadOnly> C = position.getReadOnly();
		Vec2<ReadWrite> AB = B.minusNewVector(A);
		Vec2<ReadWrite> AC = C.minusNewVector(A);
	    double numerateur = Math.abs(AB.x*AC.y - AB.y*AC.x);
	    double denominateur = AB.squaredLength();
	    double CI = numerateur*numerateur / denominateur;
	    return CI < squared_radius_with_dilatation_obstacle;
	}

    /**
     * Ce cercle croise-t-il le segment [A,B]?
     * @param A
     * @param B
     * @param distance: dilatation. 0 pour les obstacles de proximité, non nul pour les éléments de jeux circulaires.
     * @return
     */
    public boolean obstacle_proximite_dans_segment(Vec2<ReadOnly> A, Vec2<ReadOnly> B, int distance)
    {
    	// Ceci peut être le cas pour certain élément de jeu dont il ne faut pas vérifier les collisions
    	if(radius < 0)
    		return false;
    	/**
    	 * Ce code a été honteusement pompé sur http://openclassrooms.com/courses/theorie-des-collisions/formes-plus-complexes
    	 */
    	
    	if (!collisionDroite(A, B, distance))
	        return false;  // si on ne touche pas la droite, on ne touchera jamais le segment
    	
    	double pscal1 = B.minusNewVector(A).dot(position.minusNewVector(A));
    	double pscal2 = A.minusNewVector(B).dot(position.minusNewVector(B));
	    if (pscal1>=0 && pscal2>=0)
	       return true;
	    // dernière possibilité, A ou B dans le cercle
	    return isInObstacle(A, distance) || isInObstacle(B, distance);
    }

    public boolean obstacle_proximite_dans_segment_dilatation(Vec2<ReadOnly> A, Vec2<ReadOnly> B)
    {
    	// Ceci peut être le cas pour certain élément de jeu dont il ne faut pas vérifier les collisions
    	if(radius < 0)
    		return false;
    	/**
    	 * Ce code a été honteusement pompé sur http://openclassrooms.com/courses/theorie-des-collisions/formes-plus-complexes
    	 */
    	
    	if (!collisionDroiteDilatation(A, B))
	        return false;  // si on ne touche pas la droite, on ne touchera jamais le segment
    	
    	double pscal1 = B.minusNewVector(A).dot(position.minusNewVector(A));
    	double pscal2 = A.minusNewVector(B).dot(position.minusNewVector(B));
	    if (pscal1>=0 && pscal2>=0)
	       return true;
	    // dernière possibilité, A ou B dans le cercle
	    return isInObstacleDilatation(A) || isInObstacleDilatation(B);
    }

}
