package pathfinding.thetastar;

/**
 * Scénario utilisé par l'arc manager
 * @author pf
 *
 */

public class ScenarioThetaStar
{
	public final int noeudActuel;
	public final RayonCourbure rayonCourbure;
	
	public ScenarioThetaStar(int noeudActuel, RayonCourbure rayonCourbure)
	{
		this.noeudActuel = noeudActuel;
		this.rayonCourbure = rayonCourbure;
	}
}

