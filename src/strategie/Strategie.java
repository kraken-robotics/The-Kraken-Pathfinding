package strategie;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

import enums.Cote;
import robot.RobotChrono;
import robot.RobotVrai;
import scripts.Script;
import scripts.ScriptManager;
import strategie.NoteScriptVersion;
import strategie.arbre.Branche;
import threads.ThreadTimer;
import utils.Log;
import utils.Read_Ini;
import utils.Sleep;
import container.Service;
import exceptions.strategie.PathfindingException;
import exceptions.strategie.ScriptException;
import smartMath.Vec2;

/**
 * Classe qui prend les décisions et exécute les scripts
 * @author pf, krissprolls, marsu
 *
 */

public class Strategie implements Service {

	// Dépendances
	private MemoryManager memorymanager;
	private ScriptManager scriptmanager;
	private GameState<RobotVrai> real_state;
	private Log log;
	
	private Map<String,Integer> echecs = new Hashtable<String,Integer>();

	private NoteScriptVersion scriptEnCours;
	private NoteScriptMetaversion MetaScriptEnCours;
	
	public int TTL; //time toDatUltimateBest live
	public ArrayList<NoteScriptMetaversion> decisionHistory;	// l'historique des décisions faites par la stratégie

	// TODO initialisations des variables = première action
	// Prochain script à exécuter si on est interrompu par l'ennemi
	private volatile NoteScriptVersion prochainScriptEnnemi;
	
	// Prochain script à exécuter si l'actuel se passe bien
	private volatile NoteScriptVersion prochainScript;
	
	public Strategie(MemoryManager memorymanager, ScriptManager scriptmanager, GameState<RobotVrai> real_state, Read_Ini config, Log log)
	{
		this.memorymanager = memorymanager;
		this.scriptmanager = scriptmanager;
		this.real_state = real_state;
		this.log = log;
		prochainScript = new NoteScriptVersion();
		decisionHistory = new ArrayList<NoteScriptMetaversion>();
		maj_config();
	}

	/**
	 * Méthode appelée à la fin du lanceur et qui exécute la meilleure stratégie (calculée dans threadStrategie)
	 */
	public void boucle_strategie()
	{		
		
		// demande au robot de faire l'arbre n°2 en premier dans la match
		log.debug("debut du match : action scriptée", this);
		NoteScriptMetaversion meilleur = new NoteScriptMetaversion();
		NoteScriptVersion meilleur_version = new NoteScriptVersion();
		try {
			meilleur_version.script = scriptmanager.getScript("ScriptLances");
			meilleur.script = scriptmanager.getScript("ScriptLances");
		} catch (ScriptException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		meilleur.metaversion = 0;
		meilleur_version.version = 0;
		meilleur_version.note = 42.0f;
		setProchainScript(meilleur_version);
		setMetaScriptEnCours(meilleur);

		log.debug("prochain script : " + meilleur_version,this);
		
		
		
		
		// l'historique des décisions doit être vide lors du lancement de la boucle.
		decisionHistory.clear();

		log.debug("Stratégie lancée", this);
		while(!ThreadTimer.fin_match)
		{
			if(prochainScript != null && prochainScript.script != null)
				synchronized(prochainScript)
				{
					if(prochainScript != null)
					{
						scriptEnCours = prochainScript.clone();
						prochainScript = null;
					}
					else
						scriptEnCours = null;
				}

			// le script a écécuter ne doit pas être ul pour pouvoir l'éxécuter
			if(scriptEnCours != null && scriptEnCours.script != null)
			{
				boolean dernier = (nbScriptsRestants() == 1);

				// imprime l'état du jeu
				log.debug("=============== New Script =========================", this);
				log.debug("Position Robot : "+ real_state.robot.getPosition(), this);
				log.debug("Mammouth Gauche feu  : "+ real_state.table.isLeftMammothHit(), this);
				log.debug("Mammouth Right feu  : "+ real_state.table.isRightMammothHit(), this);
				log.debug("Pince Gauche feu  : "+ real_state.robot.isTient_feu(Cote.GAUCHE), this);
				log.debug("Pince Droite feu  : "+ real_state.robot.isTient_feu(Cote.DROIT), this);
				log.debug("Tree 0 : "+ real_state.table.isTreeTaken(0), this);
				log.debug("Tree 1 : "+ real_state.table.isTreeTaken(1), this);
				log.debug("Tree 2 : "+ real_state.table.isTreeTaken(2), this);
				log.debug("Tree 3 : "+ real_state.table.isTreeTaken(3), this);
				log.debug("Nb fruit bac : "+ real_state.robot.get_nombre_fruits_bac(), this);
				log.debug("Temps actuels : "+real_state.time_depuis_debut/1000 + "s", this);
				log.debug("Points actuels : "+real_state.pointsObtenus, this);
				log.debug("Pts/s moyen : "+(float)real_state.pointsObtenus*1000.0f/(float)real_state.time_depuis_debut, this);
				log.debug("      ==============            ===========         ", this);
				log.debug("Stratégie fait: "+scriptEnCours+", dernier: "+dernier, this);
				log.debug("====================================================", this);
				
				// ajoute l'action que l'on fait a l'historique de ce que l'on a déjà fait
				decisionHistory.add(getMetaScriptEnCours());
				
				try 
				{
					// le dernier argument, retenter_si_blocage, est vrai si c'est le dernier script. Sinon, on change de script sans attendre
					scriptEnCours.script.agit(scriptEnCours.version, real_state, dernier);
				}
				catch(Exception e)
				{
					// enregistrement de l'erreur pour ne pas la refaire (ajout a la liste d'exclusion de l'arbre)
					String nom = scriptEnCours.script.toString();
					if(echecs.containsKey(nom))
					{
						int nb = echecs.get(nom);
						echecs.put(nom, nb+1);
					}
					else
						echecs.put(nom, 1);
				}
			}
			else
			{
				log.critical("Aucun ordre n'est à disposition. Attente.", this);
				Sleep.sleep(100);
			}

		}
		log.debug("Arrêt de la stratégie", this);
	}

	/**
	 * Méthode qui, à partir de l'emplacement des ennemis, 
	 * essaye de trouver ce qu'ils font
	 */
	public void analyse_ennemi(Vec2[] positionsfreeze, int[] duree_freeze)
	//en fait on n'a pas besoin de la date des freezes mais de la durée des freeze
	{
		
		int distance_influence = 500; //50 cm
		int duree_standard = 5000;
		int duree_blocage = 15000;
		//int larg_max = 100; //10 cm est la largeur maximale de la fresque
		//valeur amenée à être modifiée
		//inutile en fait
		int i_min_fire;
		int i_min_tree ;
		int i_min_fresco;
		int i_min_fixed_fire;
		
		
		// ractangle ou un freeze ennemi empèche la dépose de fruits 
		int bacCriticalmaxX = -500;
		int bacCriticalminX = -1000;
		int bacCriticalminY = 1300;
		int bacCriticalmaxY = 1700;
		
		
		
		
		
		for(int i = 0; i <2; i++)
		{
			/*
			 * Je mets en garde contre la façon dont peut être utilisé positionsfreeze
			 * en effet, une fois que le robot adverse a été considéré commme preneur
			 * de feu ou de fruit, alors il faut remettre à 0 le compteur mais si on fait ça, 
			 * on ne se prémunit pas, entre autre, contre les freezes
			 * 			 * 
			 */
			
			// détermine les plus proches actions du robot ennemi 
			i_min_fire = real_state.table.nearestUntakenFire(positionsfreeze[i]);
			i_min_tree = real_state.table.nearestUntakenTree(positionsfreeze[i]);
			i_min_fresco = real_state.table.nearestFreeFresco(positionsfreeze[i]);
			i_min_fixed_fire = real_state.table.nearestUntakenFixedFire(positionsfreeze[i]);
			
			
			//Pour déboguer
			for(int p = 0; p <2; p++)
			{
				if(duree_freeze[p] > 5000)
			
				{
					/*
					log.debug("La position du freeze est : "+positionsfreeze[p]+"pour le robot : "+p, this);
					log.debug("L'indice du plus proche arbre non pris est : "+ i_min_tree,this);
					log.debug("L'indice du plus proche feu non pris est : "+i_min_fire,this);
					log.debug("L'indice de la place libre la plus proche pour les fresques  : "+i_min_fresco, this);
					log.debug("L'indice du plus proche feu fixe non pris est : "+i_min_fixed_fire, this);
					*/
					
				}
			}
			
			
			if (duree_freeze[i] > duree_blocage)
			{
				//Il y a un blocage de l'ennemi, réfléchissons un peu et agissons optimalement
				//Pour l'instant  la stratégie est trop bonne pour qu'on en ait à faire quelque chose
				
				
				if(positionsfreeze[i].x < bacCriticalmaxX && positionsfreeze[i].x > bacCriticalminX && positionsfreeze[i].y > bacCriticalminY && positionsfreeze[i].y < bacCriticalmaxY)
				    real_state.table.setProbaFire(i_min_fire,0.9f);
				
			}
			
			
			if (duree_freeze[i] > duree_standard)
			{
				// si l'ennemi a pris un arbre
				if (real_state.table.distanceTree(positionsfreeze[i], i_min_tree) < distance_influence)
				    real_state.table.setProbaTree(i_min_tree, 0.9f);

				// si l'ennemi a pris un feu
				if(real_state.table.distanceFire(positionsfreeze[i], i_min_fire) < distance_influence)
				    real_state.table.setProbaFire(i_min_fire,0.9f);
				
				// si l'ennmei prose les fresques
				if(real_state.table.distanceFresco(positionsfreeze[i], i_min_fresco) < distance_influence)
				    real_state.table.setProbaFresco(i_min_fresco,0.9f);
				
				// si l'ennemi prend un feu fixe 
				if(real_state.table.distanceFixedFire(positionsfreeze[i], i_min_fresco) < distance_influence)
				    real_state.table.setProbaFixedFire(i_min_fixed_fire,0.9f);
			}
			
			/*
			 * 
			 * else if(table.distanceFresque(positionsfreeze[i], i_min_tree) < distance_influence && duree_freeze[i] > duree_standard)
				{
				table.putOnFresque(larg_max);
				
				}
			 * 
			 * 
			 */
		}
		
		/*
		 *On prend pas en compte le lancer de balles
		 *car on aura pas d'information sur le lancer potentiel qu'un adversaire a fait
		 * (et pis en plus ca nous arrange bien qu'il tire ses balles) 
		 *Et pour la funny action, il n'y a pas de stratégie nécessaire
		 */
		
		
		// modificiation de la table en conséquence
		/*
		 * Où l'ennemi dépose-t-il ses feux?
		 * Où l'ennemi dépose-t-il sa fresque?
		 * Quel arbre récupère-t-il?
		 * Quelle torche vide-t-il?
		 * Où tire-t-il ses balles? (tirer au moins une balle là où il a tiré)
		 */
		
		// Plus le robot ennemi reste fixe, plus le TTL doit être grand.
		// Le TTL est une durée en ms sur laquelle on estime que le robot demeurera immobile
		
	} 
	
	
	
	public float[] meilleurVersion(int meta_id, Script script, GameState<RobotChrono> state) throws PathfindingException
	{
		int id = -1;
		float meilleurNote = -1;
		int score;
		int duree_script;
		if(script == null)
			throw new PathfindingException();
	//	log.debug("meilleurVersion started, analyse " + script + " metaversion " + meta_id + " through versions : " + script.version_asso(meta_id),this);

		
		for(int i : script.version_asso(meta_id))
		{
			score = script.score(id, state);
			try
			{
				duree_script = (int)script.calcule(i, state, true);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				duree_script = 9999;
				//continue;
			}
			
			//log.debug("meilleurVersion analyse " + script + "version " + i + " note " + calculeNote(score,duree_script, i,script, state), this);
			if(calculeNote(score,duree_script, i,script, state)>meilleurNote)
			{
				id = i;
				meilleurNote = calculeNote(score,duree_script, i,script,state);
			}
			
		}
		if(id == -1)
			throw new PathfindingException();
		
		float[] a= {id, meilleurNote};
		return a;
	}
	
	
	/**
	 * La note d'un script est fonction de son score, de sa durée, de la distance de l'ennemi à l'action 
	 * @param score
	 * @param duree
	 * @param id
	 * @param script
	 * @return note
	 */
	private float calculeNote(int score, int duree, int id, Script script, GameState<?> state)
	{
		if (score != 0)
			return 1000.0f*(float)score*script.poids(state)/(float)duree;
		else
			return 0.1f*script.poids(state)/(float)duree;
	}
	
	/* Méthode qui calcule la note de cette branche en calculant celles de ses sous branches, puis en combinant leur notes
	 * C'est là qu'est logé le DFS
	 * 
	 * @param profondeur : la profondeur d'exploration de l'arbre, 1 pour n'explorer qu'un niveau
	 */
	public NoteScriptMetaversion evaluate(ArrayList<NoteScriptMetaversion> errorList)
	{
		/*
		 * 	Algorithme : Itterative Modified DFS
		 *  ( la différence entre un vrai DFS et l'algo qu'on utilise ici est que la branche parente
		 *    doit être évalué uniquement une fois que tout ses enfants ont étés évalués. Dans un
		 *    DFS normal, c'est le parent qui est évalué d'abord, et ses enfants ensuite )
		 *    
		 * Psuedocode :
		 * 
		 * soit P une pile
		 * mettre la racine au dessus de P 		// Dans notre cas, il y a autant de racines que de prochaine action possible
		 * 
		 * tant que P est non vide
		 * 		v = l'élément du dessus de P
		 * 		si v a des enfants, et qu'ils sont non notés
		 * 			mettre tout les enfants de v au dessus de P
		 * 		sinon
		 * 			calculer la note de v
		 * 			enlever v de P 
		 * 
		 * 
		 */
		
		// Pile des branches de l'arbre qu'il reste a explorer
		Stack<Branche> scope = new Stack<Branche>();
		
		// ajoute les différentes possibiités pour la prochaine action dans la pile
		Script mScript = null;
		ArrayList<Integer> metaversionList;
		GameState<RobotChrono> mState = memorymanager.getClone(0);
		Branche current;
		
		// se place déjà dans le futur : le script actuel est déjà exécuté.
		if(getScriptEnCours() != null)
		{
			try {
				getScriptEnCours().script.calcule(getScriptEnCours().version, mState, false);
			} catch (PathfindingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		// racourccis pour les racines, afin du calcul du max final :
		ArrayList<Branche> rootList = new ArrayList<Branche>();
		
		// Pour le critère d'arrèt d'exploration de l'arbre : un TTL ira bien pour l'instant :
		// les action a anticiper doivent commencer dans les 30 prochaines secondes

		//Config pour 1 sec d'exécution sur raspbe
		int		TTL = 24000;	// les actions anticipés doivent débuter dans les 24 prochaines secondes   
		int maxProf	= 99999;	// En moyenne, réduire ce nombre consuit a sabrer les branches les plus prometteuses
		
		//Config pour 4 sec d'exécution sur raspbe
	//	int		TTL = 25000;	// les actions anticipés doivent débuter dans les 14 prochaines secondes   
	//	int maxProf	= 3;
		
		
		
		long TrueAStarTTL = 8000;	// A partir de quand on considère qu'on ne sais plus ou est l'ennemi, et qu'on peut passer sur cache
		int TimeBeforeGiveUp	= 3000;	// Si on reste bloqué dans l'arbre, on garde un oeil sur la montre pour être sur de pouvoir retenter sa chance
		
		long startTime = System.currentTimeMillis();
//		int Branchcount = 0;
		boolean temp;
		boolean branchHasChild;
		
		
		//TODO: mettre tout en une seule racine
		
		
		
		// ajoute tous les scrips disponibles scripts
		for(String nom_script : scriptmanager.getNomsScripts())
		{

			mState = memorymanager.getClone(1);
			

			try
			{
				mScript = scriptmanager.getScript(nom_script);
			}
			catch(ScriptException e)
			{
				e.printStackTrace(); 
			}
			
			// On ne s'encombre pas de scripts qu'on ne doit pas faire.
			if(mScript.poids(mState) == 0)
				continue;
			
			
			metaversionList = mScript.meta_version(	mState	);
			if(metaversionList == null)
				continue;
			
			
			
			// ajoute toutes les métaversions de tous les scipts
			for(int metaversion : metaversionList)
			{
				
				// n'ajoute pas les branches exclues par argument 
				if (errorList != null)
				{
					temp = false;
					for(NoteScriptMetaversion n : errorList)
					{
						if(n != null && n.script != null && mScript.toString() == n.script.toString() && metaversion == n.metaversion)
							temp = true;
					}
					if(temp)
						continue;
				}
				
				
				//log.debug("Ajout de la racine " + mScript.toString() + " metaversion : " + metaversion, this);
				mState = memorymanager.getClone(1);
				mState.pathfinding.setPrecision(4);
				scope.push( new Branche(	TTL,							// Il reste tout le TTL sur chacune des racines
											false,							// N'utilise pas le cache pour le premier niveau de profondeur 
											2,								// Profondeur de la raine: 2 (1 pour le présent, et 2 pour un cran dans le futur) Ici 2 car on est dans le futur (on n'anticipe pas e script en cours)
											mScript, 						// Une branche par script et par métaversion
											metaversion, 
											mState	) );					// état de jeu
				rootList.add(scope.lastElement());
			}	
		}

	
		// ajuste le critère d'arret d'expansion de l'arbre en fonction du nombre de racines de l'arbre (indiquant grosso modo le nombre de branches qu'il y aura au total)
		//TTL = (int) (10000+55000000*(Math.exp(14-scope.size())/Math.exp(14)));
		if(scope.size() == 14)
			TTL = 15500; 
		else if(scope.size() == 13)
			TTL = 16000; 
		else if(scope.size() == 12)
			TTL = 17000; 
		else if(scope.size() == 11)
			TTL = 18000; 
		else if(scope.size() == 10)
			TTL = 22000; 
		else if(scope.size() == 9)
			TTL = 23000; 
		else if(scope.size() == 8)
			TTL = 24000; 
		else if(scope.size() == 7)
			TTL = 30000; 
		else if(scope.size() == 6)
			TTL = 34000; 
		else if(scope.size() == 5)
			TTL = 38000; 
		else if(scope.size() == 4)
			TTL = 45000; 
		else if(scope.size() == 3)
			TTL = 50000; 
		else if(scope.size() == 2)
			TTL = 55000; 
		else if(scope.size() == 1)
			TTL = 60000; 
		
		TTL /=1.3;
		
		
		for (int i = 0; i < rootList.size(); ++i)
			rootList.get(i).TTL = TTL;
		
		
	//	log.debug("TTL = " + TTL + "   scope.size() :" + scope.size(), this);
		// Boucle principale d'exploration des branches
		if(scope.size() > 1)
			while (scope.size() != 0)
			{
				
				// Sécurité pour être certain que le DFS ne tombe pas en boucle infinie
				if(startTime + TimeBeforeGiveUp < System.currentTimeMillis())
				{
					log.debug(TimeBeforeGiveUp + "ms since IA calculation started : Giving up", this);
					break;
				}
				
				
				//log.debug("Nouveau tour de boucle", this);
				//log.debug("Taille de la stack :" + scope.size(), this);
				current = scope.lastElement();			
					mState = memorymanager.getClone(current.profondeur-1);
					current.computeActionCharacteristics();
				
				// Condition d'ajout des sous-branches : respecter le critère d'arret d'expansion, et ne pas les ajouter 2 fois.
				if ( current.TTL - current.dureeScript > 0 && maxProf >= current.profondeur //&& mState.time_depuis_debut +5000 < ThreadTimer.duree_match	// critère d'arret d'expansion
						&& (current.sousBranches.size() == 0))	// ne pas ajouter créer 2 fois les fils (si on a déja des fils, c'est qu'ils ont déja tous été créés)
				{
					// ajoute a la pile a explorer l'ensemble des scripts disponibles pour cet étage
					
					
					// On vérifiera si la branche peut déployer des enfants jusqu'a son TTL
					branchHasChild = false;
					// ajoute tous les scrips disponibles
					for(String nomScript : scriptmanager.getNomsScripts())
					{
					
						try
						{
							mScript = scriptmanager.getScript(nomScript);
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
						

						// On ne s'encombre pas de scripts qu'on ne doit pas faire.
						if(mScript.poids(mState) == 0)
							continue;
						
						metaversionList = mScript.meta_version(	mState	);
						if(metaversionList == null)
							continue;
						
						
						// ajoute toutes les métaversions de tous les scipts
						for(int metaversion : metaversionList)
						{
	
							// n'ajoute pas les branches exclues par argument 
							if (errorList != null)
							{
								temp = false;
								for(NoteScriptMetaversion n : errorList)
								{
									if(n != null && n.script != null && mScript.toString() == n.script.toString() && metaversion == n.metaversion)
										temp = true;
								}
								if(temp)
									continue;
							}
							
							//log.debug("profondeur parente : "  + current.profondeur,this);
							//log.debug("robot position : "  + mState.robot.getPosition(),this);
							//log.debug("Ajout d'une branche avec script" + nomScript + " et metaversion : " + metaversion, this);
							
							mState = memorymanager.getClone(current.profondeur-1);
							mState.table.supprimerObstaclesPerimes(mState.time_depuis_racine);
							mState.pathfinding.setPrecision(5);
							current.computeActionCharacteristics();
//							Branchcount++; // Remarque: non utilisé
							branchHasChild = true;
							scope.push( new Branche(	(int)(current.TTL - current.dureeScript),	// TTL restant : celui du restant moins la durée de son action	
														mState.time_depuis_racine >= TrueAStarTTL || current.profondeur > 1,//true,//current.profondeur >= 2,					// Utiliser le cache dès le second niveau de profondeur
														current.profondeur+1,						// différence de profondeur entre la racine et ici, donc 1 + celle du parent
														mScript, 									// Une branche par script et par métaversion
														metaversion, 
														mState	) );								// état de jeu
							
							// enregistre cette branche comme sous branche de son parent
							current.sousBranches.add(scope.lastElement());
						}
					}
					
					// on tue la branche si elle n'a pas été capable de déployer d'enfants
						// (Sinon elle reste en haut de stack avec du TTL et on bloucle infinie sur la recherche d'enfants)
					if(branchHasChild == false)
						current.TTL = 0;
					
					
				}
				else	// Soit on a atteint la profondeur maximale, soit les enfants ont étés traités donc on calcule la note de ce niveau
				{
					current.computeNote();
					scope.pop();
				}
				
			}	// fin boucle principale d'exploration
		//log.debug("Explored "+ Branchcount + " branches in " + (System.currentTimeMillis() - startTime) + " ms", this);
		//log.debug("IA completed in " + (System.currentTimeMillis() - startTime) + " ms with TTL = " + TTL + "ms   rootList.size() :" + rootList.size(), this);// + "	Explored "+ Branchcount + " branches", this);
		
		// simu raspbe
		/*
		log.debug("WARING : Simu rapsbe active, strategy evaluation pausing for 1s", this);
		Sleep.sleep(1000);
		*/
		 


		// Affihcage des notes des racines
		//for (int i = 0; i < rootList.size(); ++i)
		//	log.debug("Note of " + rootList.get(i).script.toString() + " is " + rootList.get(i).note, this);
		
		
		// la meilleure action a une meilleure note que les autres branches. Donc on calcule le max des notes des branches 
		NoteScriptMetaversion DatUltimateBest = new NoteScriptMetaversion(-42, null, 0);
		for (int i = 0; i < rootList.size(); ++i)
		{
			current = rootList.get(i);
			if (current.note > DatUltimateBest.note)
			{
				DatUltimateBest.note = current.note;
				DatUltimateBest.script = current.script;
				DatUltimateBest.metaversion = current.metaversion;
				
			}
		}
		
		
		//log.debug("Décision finale :" + DatUltimateBest.script + " metaversion : " + DatUltimateBest.metaversion + " Note : " + DatUltimateBest.note,this);
		
		return DatUltimateBest;
	}
	
	
	
	

	/**
	 * Renvoie le nombre de scripts qui peuvent encore être exécutés (indépendamment de leur nombre de version)
	 * @return
	 */
	private int nbScriptsRestants()
	{
		int compteur = 0;
		for(String nom_script : scriptmanager.getNomsScripts())
		{
			try {
				Script script = scriptmanager.getScript(nom_script);
				if(script.meta_version(real_state).size() >= 1)
					compteur++;		
			} catch (ScriptException e) {
				e.printStackTrace();
			}
		}
		return compteur;
	}
	
	
	/*
	 * GETTERS AND SETTERS
	 */
	
	/**
	 * Permet au thread de stratégie de définir le script à exécuter en cas de rencontre avec l'ennemi
	 * @param prochainScriptEnnemi
	 */
	public void setProchainScriptEnnemi(NoteScriptVersion prochainScriptEnnemi)
	{
		synchronized(this.prochainScriptEnnemi)
		{
			this.prochainScriptEnnemi = prochainScriptEnnemi;
		}
	}

	/**
	 * Permet au thread de stratégie de définir le prochain script à faire
	 * @param prochainScript
	 */
	public synchronized void setProchainScript(NoteScriptVersion prochainScript)
	{
			this.prochainScript = prochainScript;
	}

	
	/**
	 * @return the metaScriptEnCours
	 */
	public NoteScriptMetaversion getMetaScriptEnCours() 
	{
		return MetaScriptEnCours;
	}

	/**
	 * @param metaScriptEnCours the metaScriptEnCours to set
	 */
	public void setMetaScriptEnCours(NoteScriptMetaversion metaScriptEnCours) 
	{
		MetaScriptEnCours = metaScriptEnCours;
	}
	
	
	public boolean besoin_ProchainScript()
	{
		synchronized(prochainScript)
		{
			return prochainScript == null;
		}
	}
	
	private NoteScriptVersion getScriptEnCours()
	{
		if(scriptEnCours == null)
			return null;
		synchronized(scriptEnCours)
		{
			return scriptEnCours.clone();
		}
	}

	public void maj_config()
	{
	}
	
}

