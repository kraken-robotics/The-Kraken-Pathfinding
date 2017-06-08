package strategie.arbre;

import java.util.ArrayList;

import exceptions.strategie.PathfindingException;
import robot.RobotChrono;
import scripts.Script;
import strategie.GameState;

/**
 * Classe formalisant le concept d'une branche (ou d'une sous-branche) de l'arbre des possibles.
 * En termes de graphes, l'arbre est défini commme suit :
 * 		chaque sommet de l'arbre est une table de jeu dans un certain état.
 * 		chaque arc de l'arbre est une action qui modifie l'état d'une table
 * Une branche est un arc avec une note suivit d'un sommet duquel partent à leurs tours une mutitude de sous-branches.
 * Une branche possède une note, égale a une combinaison des notes de ses sous-branches.
 * 		(ie branche = triplet note-action-table)
 * Le role de Stratégie est de faire calculer les notes de toutes les branches de l'arbres en les considérants jusqu'à une certaine profondeur.
 * 
 * @author karton
 *
 */

public class Branche 
{
	
	// Paramètres généraux
	private boolean useCachedPathfinding; 	// utiliser un pathfinding en cache ou calculée spécialement pour l'occasion ?
	// TODO : changer en profondeur restante
	public int profondeur;					// Cette branche est-elle la dernière à évaluer, ou faut-il prendre en compte des sous-branches ? 
	public long date;						// date à laquelle cette branche est parcourue
	public int TTL;							// combien de temps allons-nous continuer à anticiper après la première action de cette branche
	
	// Notes
	public float note;				// note de toute la branche, prenant en comte les notes des sous-branches
	public float localNote; 		// note de l'action effectuée au début de cette branche
	public boolean isNoteComputed;	// l'attribut "note" a-t-il été calculé ?
	
	// Action a executer entre le sommet juste avant cette branche et l'état final
	public Script script;							// script de l'action
	public int metaversion;							// metaversion du script
	public long dureeScript;						// durée nécessaire pour effectuer le script
	private int scoreScript;						// durée nécessaire pour effectuer le script
	public boolean isActionCharacteisticsComputed;  // la durée et le score du script ont-ils étés calculés ?
	
	GameState<RobotChrono> state;
	
	// Sous branches contenant toutes les autres actions possibles a partir de l'état final
	public ArrayList<Branche> sousBranches;
	
	
	/**
	 * @param useCachedPathfinding
	 * @param prodondeur
	 * @param date
	 * @param script
	 * @param metaversion
	 * @param etatInitial
	 * @param robot
	 * @param pathfinder
	 */
	public Branche(int TTL, boolean useCachedPathfinding, int profondeur, Script script, int metaversion, GameState<RobotChrono> state)
	{
	    this.TTL = TTL;
	    this.state = state;
		this.useCachedPathfinding = useCachedPathfinding;
		this.profondeur = profondeur;
		this.script = script;
		this.metaversion = metaversion;
		sousBranches = new ArrayList<Branche>();
		isNoteComputed  = false;
		isActionCharacteisticsComputed = false;
		computeActionCharacteristics();
		
	}

	
	public void computeActionCharacteristics()
	{
		if(!isActionCharacteisticsComputed)
		{
			scoreScript = script.meta_score(metaversion, state);
			try
			{
				dureeScript = script.metacalcule(metaversion, state, useCachedPathfinding);
				//System.out.println(script.toString() +".duree = " + dureeScript);
				
			}
			catch (PathfindingException e)
			{
				dureeScript = -1;
			}
			isActionCharacteisticsComputed = true;
		}
	}
	
	
	/** Méthode qui prend les notes de chaque sous branche (en supposant qu'elles sont déjà calculés et qu'il n'y a plus qu'a
	 * les considérer) et les mélange popur produire la note de  toute cette branche
	 * Il n'y a que cette méthode qui doit modifier this.note
	 */
	public void computeNote()
	{
		computeLocalNote();

		// On prend en compte les éventuelles sous branches
		// TODO : mixer les notes des sous-branches avec noteLocale
		// pour l'instant, un simple max ira bien :         note = noteLocale + max( note des sous branches ), le max valant 0 si il n'y a pas de sous branche
		note = 0;	
		for (int i = 0; i< sousBranches.size(); ++i)
			if(sousBranches.get(i).note > this.note)
				note = sousBranches.get(i).note; 
		note += localNote;
		
		
		// marque la note comme calculée 
		isNoteComputed = true;
		
	}


	/** Méthode qui calcule la note d'une action. La note calculée ici prend en compte une unique action, pas un enchainement
	 * La note d'un script est fonction de son score, de sa durée, du poids donné au script (donnant une facon de personaliser le comportement de l'IA), et de la probabilité que l'ennemi ait déjà fait l'action
	 * @return note
	 */
	private void computeLocalNote()
	{ 
		// la note de la branche se base sur certaines caractéristiques de l'action par laquelle elle débute.
		if(!isActionCharacteisticsComputed)
			computeActionCharacteristics();
		
		// si le script ne rapporte pas en lui même des points, on lui donne une note en fonction de la duréée qu'il prend.
		if(scoreScript != 0)
			localNote = (float)scoreScript*1000.0f/(float)dureeScript;
		else 
			localNote = 0.1f/(float)dureeScript;	// les scripts qui ne rapportent pas de points sont donc choisis en fonction de leur proximité. Attention a ce que ce 1/durée ne soit pas supérieur a un autre script
		
		// le poids du script est facteur de la note
		localNote*=script.poids(state);
		
		// prise en compte de la probabilité que l'ennemi ait déjà fait l'action
		if(script.toString() == "ScriptFeuBord" || script.toString() == "ScriptTree")
			localNote*= (1 - script.probaDejaFait(metaversion, state));
			
			
		
	}
	
	
	
	
	
}
