package threads;

import java.util.ArrayList;

import exceptions.strategie.PathfindingException;
import robot.RobotVrai;
import strategie.MemoryManager;
import strategie.NoteScriptMetaversion;
import strategie.NoteScriptVersion;
import pathfinding.Pathfinding;
import strategie.Strategie;
import table.Table;
import utils.Sleep;


/**
 * Thread qui calculera en continu la stratégie à adopter
 * @author pf, Krissprolls, marsu
 *
 */

public class ThreadStrategie extends AbstractThread {

	// Dépendances
	private Strategie strategie;
	private MemoryManager memorymanager;

	ThreadStrategie(Strategie strategie, Table table, RobotVrai robotvrai, MemoryManager memorymanager, Pathfinding pathfinding)
	{
		this.strategie = strategie;
		this.memorymanager = memorymanager;
		maj_config();
		Thread.currentThread().setPriority(5);
	}
	
	@Override
	public void run()
	{
		log.debug("Lancement du thread de stratégie", this);
		

		// attends que le match démarre
		while(!ThreadTimer.match_demarre)
		{
			if(stop_threads)
			{
				log.debug("Stoppage du thread de stratégie", this);
				return;
			}
			
			// vérifie tout les dixièmes de seconde si le match a démarré
			Sleep.sleep(100);
		}
		
		// boucle principale de stratégie 
		while(!stop_threads)
		{
			// Evaluation du prochain coup en supposant que celui-ci se passe sans problème
			maj_prochainScript();
			
			Sleep.sleep(50);
			
		}
	}

	
	
	private void maj_prochainScript()
	{

		NoteScriptMetaversion meilleur = new NoteScriptMetaversion();
		
		ArrayList<NoteScriptMetaversion> exclusionList = new ArrayList<NoteScriptMetaversion>();
		
		// n'essaye pas de réfléchir sur la version du script que l'on est en train de faire
	//	exclusionList.add(strategie.getMetaScriptEnCours());
		

		for (NoteScriptMetaversion previous : strategie.decisionHistory)
			exclusionList.add(previous);
		
		
		// meilleurVersion rends un floart[] = {id, meilleurenotre}
		float[] meilleurVersion = null;
		boolean stupidDecision = false;
		while(	meilleurVersion == null ||	// recommance tant que meilleurversion ne revois rien
				stupidDecision )			// vérifie que la décision n'a pas déjà été faite a l'identique par le passé.
		{
			meilleur = strategie.evaluate(exclusionList);
			try
			{
				meilleurVersion = strategie.meilleurVersion(meilleur.metaversion, meilleur.script, memorymanager.getClone(0));
//				log.debug("la meilleure version est : " + meilleurVersion[0],this);
			}
			catch(PathfindingException e)
			{

				log.debug("La branche " + meilleur + " renvoyée par l'arbre n'a pas de metaversion acessible, on relance l'arbre sans cette branche",this);
				exclusionList.add(meilleur);
				//e.printStackTrace();
			}
			stupidDecision = isStupidDecision(meilleur);
			if(stupidDecision)
			{
				log.warning("La branche " + meilleur + " renvoyée par l'arbre a déjà été faite, nouvelle tentative",this);
				
				// en attendant qu'une décision moins stupide soit prise, on fait comme si on allait faire la funny action (ya pas moins con commme décision)
				NoteScriptVersion actionDefault = new NoteScriptVersion();
				log.warning("TODO : thread a un scriptmanager et donne funnyaction dans ce cas",this);
				strategie.setProchainScript(actionDefault);
			}
				
			
		}
		NoteScriptVersion meilleur_version = new NoteScriptVersion();
		meilleur_version.script = meilleur.script;
		meilleur_version.version = (int)meilleurVersion[0];
		meilleur_version.note = meilleurVersion[1];
		strategie.setProchainScript(meilleur_version);
		strategie.setMetaScriptEnCours(meilleur);
//		log.debug("prochain script : " + meilleur_version,this);
	}
	
	public void maj_config()
	{
	}
	private boolean isStupidDecision(NoteScriptMetaversion action)
	{
		// une action invalide est stupide
		if(action == null || action.script == null)
			return true;
		
		// rechercher dans l'historique si on déjà 
		for (NoteScriptMetaversion previous : strategie.decisionHistory)
			if(	action.script.toString() == previous.script.toString() && 
				action.metaversion == previous.metaversion )
				return true;
		return false;
	}
}
