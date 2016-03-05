package pathfinding.astarCourbe;

import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;

import pathfinding.RealGameState;
import pathfinding.dstarlite.GridSpace;
import permissions.ReadOnly;
import container.Service;
import exceptions.FinMatchException;
import robot.DirectionStrategy;
import tests.graphicLib.Fenetre;
import utils.Config;
import utils.Log;
import utils.Vec2;

/**
 * AStar* simplifié, qui lisse le résultat du D* Lite et fournit une trajectoire courbe
 * On suppose qu'il n'y a jamais collision de noeuds
 * (je parle de collision dans le sens "égalité", pas "robot qui fonce dans le mur"…)
 * @author pf
 *
 */

public class AStarCourbe implements Service
{
	protected DirectionStrategy directionstrategyactuelle;
	protected Log log;
	protected HeuristiqueCourbe heuristique;
	protected AStarCourbeArcManager arcmanager;
	protected RealGameState state;
	protected AStarCourbeMemoryManager memorymanager;
	protected Fenetre fenetre;
	protected Vec2<ReadOnly> arrivee;
	protected AStarCourbeNode depart;
	protected Collection<ArcCourbe> chemin;

	private class AStarCourbeNodeComparator implements Comparator<AStarCourbeNode>
	{
		@Override
		public int compare(AStarCourbeNode arg0, AStarCourbeNode arg1)
		{
			int out = (arg0.f_score - arg1.f_score) << 1;
			if(arg0.g_score > arg1.g_score)
				out++;
			return out;
		}
	}

	private PriorityQueue<AStarCourbeNode> openset = new PriorityQueue<AStarCourbeNode>(GridSpace.NB_POINTS, new AStarCourbeNodeComparator());

	/**
	 * Constructeur du AStarCourbe
	 */
	public AStarCourbe(Log log, AStarCourbeArcManager arcmanager, RealGameState state, AStarCourbeMemoryManager memorymanager, HeuristiqueCourbe heuristique, Collection<ArcCourbe> chemin)
	{
		this.log = log;
		this.arcmanager = arcmanager;
		this.state = state;
		this.memorymanager = memorymanager;
		this.chemin = chemin;
		depart = new AStarCourbeNode();

		if(Config.graphicAStarCourbe)
			fenetre = Fenetre.getInstance();
	}

	protected void printChemin()
	{
//		ArcCourbe[] cheminAff = cheminContainer.get();	
//		for(ArcCourbe arc : cheminAff)
//			fenetre.setColor(arc.getGridpointArrivee(), Fenetre.Couleur.VIOLET);
	}
	

	/**
	 * Le calcul du AStarCourbe
	 * @param depart
	 * @return
	 */
	protected Collection<ArcCourbe> process()
	{
		depart.came_from = null;
		depart.g_score = 0;
		depart.f_score = arcmanager.heuristicCost(depart);

		memorymanager.empty();

		openset.clear();
		openset.add(depart);	// Les nœuds à évaluer

		AStarCourbeNode current, successeur;

		do
		{
			current = openset.poll();
			try {
				if(!arcmanager.execute(current))
					continue; // collision mécanique attendue. On passe au suivant !
			} catch (FinMatchException e) {
				continue;
			}
			
			// Si on est arrivé, on reconstruit le chemin
			if(current.state.robot.getPosition().squaredDistance(arrivee) < 50*50)
			{
				partialReconstruct(current, true);
				memorymanager.empty();
				return chemin;
			}
			
			// On parcourt les voisins de current

			arcmanager.reinitIterator(current, directionstrategyactuelle);
			
			while(arcmanager.hasNext())
			{
				if(doitFixerCheminPartiel())
				{
					partialReconstruct(current, false);
					// Il est nécessaire de copier current dans depart car current
					// est effacé quand le memorymanager est vidé. Cette copie n'est effectuée qu'ici
					current.copyReconstruct(depart);
					memorymanager.empty();
					openset.clear();
					openset.add(depart);
					break;
				}

				successeur = memorymanager.getNewNode();
				arcmanager.next(successeur);
				 
				successeur.g_score = current.g_score + arcmanager.distanceTo(successeur);
				successeur.f_score = successeur.g_score + arcmanager.heuristicCost(successeur);

				openset.add(successeur);
				
//				if(Config.graphicAStarCourbe)
//					fenetre.setColor(current.gridpoint, Fenetre.Couleur.ROUGE);

			}
			
		} while(!openset.isEmpty());
		
		/**
		 * Impossible car un nombre infini de nœuds !
		 */
		log.critical("AStarCourbe n'a pas trouvé de chemin !");
		return null;
	}
	
	/**
	 * Reconstruit le chemin. Il peut reconstruire le chemin même si celui-ci n'est pas fini.
	 * En effet, en faisant "openset.clear()", il force le pathfinding a continuer sur sa lancée sans
	 * remettre en cause la trajectoire déjà calculée
	 * @param best
	 * @param last
	 */
	private void partialReconstruct(AStarCourbeNode best, boolean last)
	{
		synchronized(chemin)
		{
			AStarCourbeNode noeud_parent = best;
			ArcCourbe arc_parent = best.came_from_arc;
			while(best.came_from != null)
			{
				chemin.add(arc_parent);
				noeud_parent = noeud_parent.came_from;
				arc_parent = noeud_parent.came_from_arc;
			}
//			chemin.setFinish(last);
			chemin.notify(); // on prévient le thread d'évitement qu'un chemin est disponible
		}
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
		
	protected boolean doitFixerCheminPartiel()
	{
		return false;
	}
	
}