package pathfinding.thetastar;

/**
 * Scénario utilisé par l'arc manager
 * @author pf
 *
 */

public class ScenarioThetaStar
{
	public final int noeudActuel;
	public final VitesseCourbure rayonCourbure;
	
	public ScenarioThetaStar(int noeudActuel, VitesseCourbure rayonCourbure)
	{
		this.noeudActuel = noeudActuel;
		this.rayonCourbure = rayonCourbure;
	}
}

