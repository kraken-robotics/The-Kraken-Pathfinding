package obstacles;

import utils.ConfigInfo;
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
	
	public ObstacleCircular(Vec2 position, int rad)
	{
		super(position);
		this.radius = rad;
		squared_radius = rad * rad;
		if(squared_radius_with_dilatation_obstacle == -1)
		{
			squared_radius_with_dilatation_obstacle = radius + config.getInt(ConfigInfo.MARGE) + config.getInt(ConfigInfo.RAYON_ROBOT);
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

	private boolean isInObstacle(Vec2 point, int distance)
	{
		return point.squaredDistance(position) <= (radius+distance)*(radius+distance);
	}

	public boolean isInObstacleDilatation(Vec2 point)
	{
		return point.squaredDistance(position) <= squared_radius_with_dilatation_obstacle;
	}

	public boolean isInObstacle(Vec2 point)
	{
		return point.squaredDistance(position) <= squared_radius;
	}

	public boolean isProcheObstacle(Vec2 point, int distance)
	{
		return point.squaredDistance(position) <= (radius+distance)*(radius+distance);
	}

	private boolean collisionDroite(Vec2 A, Vec2 B, int distance)
	{
		Vec2 C = position;
		Vec2 AB = B.minusNewVector(A);
		Vec2 AC = C.minusNewVector(A);
	    float numerateur = Math.abs(AB.x*AC.y - AB.y*AC.x);
	    float denominateur = AB.squaredLength();
	    float CI = numerateur*numerateur / denominateur;
	    return CI < (radius+distance)*(radius+distance);
	}

	private boolean collisionDroiteDilatation(Vec2 A, Vec2 B)
	{
		Vec2 C = position;
		Vec2 AB = B.minusNewVector(A);
		Vec2 AC = C.minusNewVector(A);
	    float numerateur = Math.abs(AB.x*AC.y - AB.y*AC.x);
	    float denominateur = AB.squaredLength();
	    float CI = numerateur*numerateur / denominateur;
	    return CI < squared_radius_with_dilatation_obstacle;
	}

    /**
     * Ce cercle croise-t-il le segment [A,B]?
     * @param A
     * @param B
     * @param distance: dilatation. 0 pour les obstacles de proximité, non nul pour les éléments de jeux circulaires.
     * @return
     */
    public boolean obstacle_proximite_dans_segment(Vec2 A, Vec2 B, int distance)
    {
    	// Ceci peut être le cas pour certain élément de jeu dont il ne faut pas vérifier les collisions
    	if(radius < 0)
    		return false;
    	/**
    	 * Ce code a été honteusement pompé sur http://openclassrooms.com/courses/theorie-des-collisions/formes-plus-complexes
    	 */
    	
    	if (!collisionDroite(A, B, distance))
	        return false;  // si on ne touche pas la droite, on ne touchera jamais le segment
    	
    	float pscal1 = B.minusNewVector(A).dot(position.minusNewVector(A));
    	float pscal2 = A.minusNewVector(B).dot(position.minusNewVector(B));
	    if (pscal1>=0 && pscal2>=0)
	       return true;
	    // dernière possibilité, A ou B dans le cercle
	    return isInObstacle(A, distance) || isInObstacle(B, distance);
    }

    public boolean obstacle_proximite_dans_segment_dilatation(Vec2 A, Vec2 B)
    {
    	// Ceci peut être le cas pour certain élément de jeu dont il ne faut pas vérifier les collisions
    	if(radius < 0)
    		return false;
    	/**
    	 * Ce code a été honteusement pompé sur http://openclassrooms.com/courses/theorie-des-collisions/formes-plus-complexes
    	 */
    	
    	if (!collisionDroiteDilatation(A, B))
	        return false;  // si on ne touche pas la droite, on ne touchera jamais le segment
    	
    	float pscal1 = B.minusNewVector(A).dot(position.minusNewVector(A));
    	float pscal2 = A.minusNewVector(B).dot(position.minusNewVector(B));
	    if (pscal1>=0 && pscal2>=0)
	       return true;
	    // dernière possibilité, A ou B dans le cercle
	    return isInObstacleDilatation(A) || isInObstacleDilatation(B);
    }

}
