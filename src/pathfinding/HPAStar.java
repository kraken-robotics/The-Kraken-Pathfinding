/**
 * 
 */
package pathfinding;

import java.util.ArrayList;

import pathfinding.SearchSpace.Grid2DSpace;
import smartMath.Vec2;
import utils.Log;
import exceptions.strategie.PathfindingException;

/**
 * Classe encapsulant le calcul d'un chemin par l'agorithme dit HPAStar
 * Le développement de cette classe n'est pas prioritaire et est pour le moment
 * mis en pause.
 * 
 * @author karton, refactoring de l'implémentaiton de pf
 *
 */

/* Classe de calcul de chemin
 * 
 * Diviser pour régner!
 * Puisqu'un chemin fait un moyenne 1000mm ou moins, en équilibrant les différentes étages on tombe sur:
 * degré i: (précision macroscopique) * (précision microscopique)
 * degré 0: 32*32
 * degré 1: 16*32
 * degré 2: 16*16
 * degré 3: 18*6
 * degré 4: 8*8
 * degré 5 et plus: pas de HPA*
 * Je (PF) pense qu'un HPA* sur plus d'étages serait plus une perte de performances qu'un gain.
 */

public class HPAStar 
{

	public Grid2DSpace espace;		// espace de travail
	private ArrayList<Vec2> chemin;	// réceptacle du calcul
	

	private static int nb_precisions;
	
	private AStar[] solvers;
	private AStar solver;
	private int degree;
	private static Log log;
	

	/** Constructeur
	 * @param espaceVoulu
	 * @param requestedLog
	 */
	public HPAStar(Grid2DSpace espaceVoulu, Log requestedLog)
	{
		espace = espaceVoulu;

		chemin = new ArrayList<Vec2>();	
	}
	
	
	/** Méthode de calcul du chemin selon l'algorithme
	 * @param depart
	 * @param arrivee
	 * @return le chemin comme liste de point de passage, en incluant le point d'arrivée mais pas celui de départ
	 * @throws PathfindingException
	 */
	public ArrayList<Vec2> process(Vec2 depart, Vec2 arrivee) throws PathfindingException
	{
		// nettoie l'espace de travail
		chemin.clear();

		try 
		{

			// pas d'AStar macroscopique utilisé si la précision demandée est déjà très très faible
			if(degree >= 5)
				return cheminAStar(depart, arrivee);
			
			// Le degré macro est celui utilisé pour le A* macroscopique
			int degree_macro = 5 -(degree+1)/2;

			
			// Première recherche, précision faible
			ArrayList<Vec2> chemin = solvers[degree_macro].process(depart, arrivee);
			// Lissage (très important, car diminue le nombre de recherche de chemin à l'étage inférieur)
			chemin = solvers[degree_macro].espace.lissage(chemin);
			
			System.out.println("HPA*, A* Macroscopique : " + chemin);

			// Seconde recherche
			ArrayList<Vec2> output = new ArrayList<Vec2>();

			/*
			 * Rappel: AStar(A, B) donne un itinéraire entre A et B, mais sans inclure A!
			 * Exemple d'itinéraire entre A et B: [D, F, G, B].
			 * C'est pratique pour le HPA*, comme ça on peut concaténer sans répétition.
			 */
			
			Vec2 depart_hpa = depart;
			Vec2 arrivee_hpa;
			for(int i = 1; i < chemin.size(); i++)
			{
				depart_hpa = chemin.get(i-1);
				arrivee_hpa = chemin.get(i);
				
				// Soucis : ce canCrossLine est true en permanance à cause du A* Macroscopique, puis du lissage
				// qui garantit que 2 points sucessinf sont atteignable en signe droite.
				
				// En fait le A* macroscopique ne devrait pas raisonner sur des lignes droites, mais sur un
				// graphe d'acessibilité
				if(solver.espace.canCrossLine(depart_hpa, arrivee_hpa))
					output.add(arrivee_hpa);
				else
					output.addAll(solver.process(depart_hpa, arrivee_hpa));//TODO : correct coordinate system
			}

			output = solver.espace.lissage(chemin);

			return output;
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	    throw new PathfindingException();
	}	// process


	/**
	 * Retourne l'itinéraire pour aller d'un point de départ à un point d'arrivée
	 * @param depart, dans le système de coords de Grid2DSpace
	 * @param arrivee système de coords IDEM
	 * @return l'itinéraire, exprimé comme des vecteurs de déplacement, et non des positions absolues, et en millimètres
	 * 			Si l'itinéraire est non trouvable, une exception est retournée.
	 * @throws PathfindingException 
	 */
	public ArrayList<Vec2> cheminAStar(Vec2 depart, Vec2 arrivee) throws PathfindingException
	{
		// calcule le chemin. Lève une exception en cas d'erreur. 
		ArrayList<Vec2> chemin = solver.process(depart, arrivee);
		log.debug("Chemin avant lissage : " + chemin, this);
		
		chemin = solver.espace.lissage(chemin);
		log.debug("Chemin : " + chemin, this);
		
		return chemin;
	}


	public void setPrecision(int precision) 
	{
		if(precision >= nb_precisions)
		{
			log.critical("Précision demandée ("+precision+") impossible. Précision utilisée: "+(nb_precisions-1), this);
			degree = nb_precisions-1;
		}
		else
			degree = precision;
		solver = solvers[degree];
	}
	
	
}
