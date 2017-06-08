import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import robot.RobotVrai;
import robot.cartes.Actionneurs;
import robot.cartes.Capteurs;
import robot.cartes.Deplacements;
import robot.hautniveau.DeplacementsHautNiveau;
import scripts.Script;
import scripts.ScriptManager;
import smartMath.Vec2;
import strategie.GameState;
import threads.ThreadManager;
import threads.ThreadTimer;
import utils.Log;
import utils.Read_Ini;
import utils.Sleep;
import container.Container;
import exceptions.deplacements.MouvementImpossibleException;
import exceptions.serial.SerialException;

public class lanceur_sans_strategie {

	static Container container;
	static Read_Ini config;
	static GameState<RobotVrai> real_state;
	static ScriptManager scriptmanager;
	static DeplacementsHautNiveau deplacements;
	static Deplacements dep;
	static Actionneurs act;
	static Capteurs capteurs;
	static Log log;
	static String couleur;
	static int pwm;
	static double inverse_vitesse_mmpms;
	static double inverse_vitesse_rpms;
	
	private static void avancer(long distance) throws MouvementImpossibleException
	{
		try
		{
			dep.avancer(distance);
		}
		catch (SerialException e)
		{
			e.printStackTrace();
		}
		// sleep avec marge de 20%
		sleep((long)(Math.abs(distance)*inverse_vitesse_mmpms*1.20));
	}
	
	private static void sleep(long ms) throws MouvementImpossibleException
	{
		long nb = Math.round(((double)ms) / 100.);
		for(int i = 0; i < nb; i++)
		{
			int mesure = capteurs.mesurer();
//				int mesure = 3000;
			if(mesure < 3*pwm/2)
			{
				log.critical("Ennemi détecté à "+mesure, container);
				try
				{
					dep.stopper();
				}
				catch (SerialException e)
				{
					e.printStackTrace();
				}
				throw new MouvementImpossibleException();
			}
			try
			{
				Thread.sleep(90);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private static void set_vitesse(int pwm)
	{
        inverse_vitesse_mmpms = (int) (1./(((float)2500)/((float)613.52 * (float)(Math.pow((double)pwm,(double)(-1.034))))/1000));
        inverse_vitesse_rpms = (int) (1./(((float)Math.PI)/((float)277.85 * (float)Math.pow(pwm,(-1.222)))/1000));
		lanceur_sans_strategie.pwm = pwm;
		try {
			dep.set_vitesse_translation(pwm);
			dep.set_vitesse_rotation(pwm);
		}
		catch(Exception e) {}
	}
	
	private static void tourner(double angle) throws MouvementImpossibleException
	{
		try {
			if(couleur.contains("rouge"))
				dep.tourner(Math.PI - angle);
			else
				dep.tourner(angle);
			Thread.sleep(1000);
		}
		catch(Exception e) {}
	}

	private static void sleep_sans_capteurs(long ms)
	{
		try {
			Thread.sleep(ms);
		}
		catch(Exception e) {}
	}

	// en ligne droite
	private static void va_au_point(Vec2 point)
	{
		try
		{
	        double[] infos = dep.get_infos_x_y_orientation();
	        Vec2 position = new Vec2((int)infos[0],(int)infos[1]);
	        double orientation = infos[2]/1000; // car get_infos renvoie des milliradians
	        
	        point.Minus(position);
	        double distance = point.Length();
	        double angle =  Math.atan2(point.y, point.x);
	        tourner(angle);
			avancer((int)distance);
		}
		catch (SerialException e)
		{
			e.printStackTrace();
		}
		catch (MouvementImpossibleException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		container = new Container();
		config = (Read_Ini) container.getService("Read_Ini");
		log = (Log) container.getService("Log");	// les logs sont fais sous l'identité de container, c'est ptet un peu crade...

		log.warning("LANCEUR SANS STRATEGIE, SANS SCRIPT, ÉCRITS AVEC MA BITE ET MON COUTEAU : initialisation",container);

		//Début des paramétrages
		configCouleur();

		// initialise les singletons
		//real_state = (GameState<RobotVrai>) container.getService("RealGameState");
//		scriptmanager = (ScriptManager) container.getService("ScriptManager");
		dep = (Deplacements)container.getService("Deplacements");
		act = (Actionneurs)container.getService("Actionneurs");
//		deplacements = (DeplacementsHautNiveau)container.getService("DeplacementsHautNiveau");
		capteurs = (Capteurs) container.getService("Capteur");
//		real_state = (GameState<RobotVrai>) container.getService("RealGameState");

//		real_state.robot.initialiser_actionneurs_deplacements();

		// PHASE DE PREPARATION
		act.rateau_ranger_droit();
		act.rateau_ranger_gauche();
		
		act.recharger();
		
		set_vitesse(200);

		while(capteurs.demarrage_match())
		{
			try {Thread.sleep(100);}
			catch(Exception e){}
		}
		while(!capteurs.demarrage_match())
		{
			try {Thread.sleep(100);}
			catch(Exception e){}
		}
		
		if(couleur.contains("rouge"))
			dep.set_orientation(0);
		else
			dep.set_orientation(Math.PI);
		dep.set_x(-1350);
		dep.set_y(1500);

/*		if(couleur.contains("rouge"))
		{
			dep.tourner(Math.PI);
			sleep(1000);
			act.allume_ventilo();
			sleep(300);
			act.recharger();
			sleep(1000);
			dep.avancer(-100);
			act.eteint_ventilo();
			sleep(1000);
			act.allume_ventilo();
			sleep(1000);
			dep.tourner(0);
			sleep(1000);
		}
		else
		{
*//*			act.allume_ventilo();
			sleep(300);
			act.recharger();
			sleep(1000);
			act.eteint_ventilo();
			avancer(150);
			act.allume_ventilo();
			sleep(1000);
			act.eteint_ventilo();*/
	//	}
				
		// FRESQUES
		try {
			avancer(1200);
			
			tourner(Math.PI/2);
			
			set_vitesse(90);
			avancer(100);
			
			dep.avancer(400);
			sleep_sans_capteurs(1000);
			
			set_vitesse(160);
	
			dep.avancer(-100);
			sleep_sans_capteurs(1000);
	
			tourner(-Math.PI/2);		
			avancer(400);
	        double[] infos = dep.get_infos_x_y_orientation();
	        Vec2 position = new Vec2((int)infos[0],(int)infos[1]);
			System.out.println("Lances: "+position);
			
		}
		catch(MouvementImpossibleException e)
		{
			Thread.sleep(2000);
			va_au_point(new Vec2(147, 1514));
		}
        
		// LANCES
		tourner(-Math.PI/4);
		avancer(200);
		tourner(0);
		avancer(450);
		
		if(couleur.contains("jaune"))
			tourner(Math.PI);
		act.allume_ventilo();
		sleep_sans_capteurs(1500);
		act.tirerBalle();
		try {Thread.sleep(1500);}
		catch(Exception e){}

		for(int i = 0; i < 2; i++)
		{
			if(couleur.contains("jaune"))
				avancer(-100);
			else
				avancer(100);
			act.tirerBalle();
			try {Thread.sleep(1500);}
			catch(Exception e){}
			act.tirerBalle();
			try {Thread.sleep(1500);}
			catch(Exception e){}
		}
		
		act.eteint_ventilo();
		if(couleur.contains("jaune"))
			avancer(-200);
		else
			avancer(200);
		
		tourner(-Math.PI/2);

		// Torche
        double[] infos = dep.get_infos_x_y_orientation();
        Vec2 position = new Vec2((int)infos[0],(int)infos[1]);
		System.out.println("Torche: "+position);

		avancer(900);
/*		avancer(-200);
		sleep(1000);
		tourner(Math.PI*13/14);
		set_vitesse(200);
		sleep(1000);
		avancer(1500);
	*/	

		if(true)
			return;

		// Threads
		try 
		{
//			log.debug("Création du Thread Capteur",container);
//			container.getService("threadCapteurs");
			log.debug("Création du Thread Timer",container);
			container.getService("threadTimer");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		container.demarreThreads();
		
		
		//	recalerRobot();
	

		// attends que le jumper soit retiré
		attendreDebutMatch();

		System.out.println("Le robot commence le match");
		
		try {
        real_state.robot.avancer(300);
        real_state.robot.tourner(-2*Math.PI/3);
        real_state.robot.avancer(200);
		}
		catch(Exception e)
		{}
        
        Script tree = (Script)scriptmanager.getScript("ScriptTree");
		Script deposer_fruits = (Script)scriptmanager.getScript("ScriptDeposerFruits");
		Script lances = (Script)scriptmanager.getScript("ScriptLances");

        // Boucle principale du match
        while(true)// (pourquoi pas while(!ThreadTimer.fin_match) ?)
        {
        	try {
        	lances.agit(0, real_state, false);
        	} catch(Exception e)
        	{}
        	// fait tout les arbres du plus proche au plus loin
        	for(int version_arbre = 0; version_arbre < 4; version_arbre++)
        	{
	        	try
	        	{
			        tree.agit(version_arbre, real_state, false);
	        	}
				catch(Exception e)
				{
				}
	        	
	        	// va immédiatement déposer les fruits
	        	for(int version_depose = 0; version_depose < 2; version_depose++)
	        	{
		        	try
		        	{
		        		if(deposer_fruits.meta_version(real_state) != null && deposer_fruits.meta_version(real_state).size() != 0)
		        			deposer_fruits.agit(version_depose, real_state, false);
					}
		        	catch(Exception e)
					{
					}
	        	}
        	}
        	try {
        	lances.agit(1, real_state, false);
        	} catch(Exception e)
        	{}
        }
		
		
		
	}

	/**
	 * Demande si la couleur est rouge au jaune
	 * @throws Exception
	 */
	static void configCouleur()  throws Exception
	{
		couleur = "";
		while(!couleur.contains("rouge") && !couleur.contains("jaune"))
		{
			System.out.println("Rentrez \"jaune\" ou \"rouge\" : ");
			BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in)); 

			couleur = keyboard.readLine(); 
			if(couleur.contains("rouge"))
				config.set("couleur","rouge");
			else if(couleur.contains("jaune"))
				config.set("couleur", "jaune");

		}

	}

	/**
	 * Recale le robot
	 * @throws Exception
	 */
	static void recalerRobot()  throws Exception
	{

		System.out.println("Pret au recalage, appuyez sur entrée pour continuer");
		config.set("capteurs_on", false);	// pas de capteurs durant le recalage
		capteurs.maj_config();

		// attends la pression sur entrée
		new BufferedReader(new InputStreamReader(System.in)).readLine();

		//recale
		real_state.robot.recaler();

	}
	
	/**
	 * Attends que le match soit lancé
	 * @throws Exception
	 */
	static void attendreDebutMatch()  throws Exception
	{

		System.out.println("Robot pret pour le match, attente du retrait du jumper");
		
		// hack si le jumper est inopérant
		//ThreadTimer.match_demarre = true;

		while(!capteurs.demarrage_match())
				Sleep.sleep(100);
		config.set("capteurs_on", true);	// pas de capteurs durant le recalage
		capteurs.maj_config();
	}


	/**
	 * initialise le départ non Rapide
	 * @throws Exception
	 */
	static void initialiserDepartStandard()  throws Exception
	{
		real_state.robot.avancer(50);
		real_state.robot.tourner(-1.8);
		real_state.robot.avancer(100);
	}

	

	/**
	 * effectue le jépart non Rapide
	 * @throws Exception
	 */
	static void faireDepartStandard()  throws Exception
	{

		real_state.robot.avancer(150);
	}
	
	static void setFruitNoirPositions()  throws Exception
	{

		/*
		 * On a 2 inputs 
		 * Le deuxième pour les arbres 0 et 3 (on donne pour 0 et pour le 3 ça sera calculé facilement)
		 * Le troisième pour les arbres 1 et 2 (one donne pour 1 et pour les 2 ça sera calculé facilement) 
		 * La position des fruits dans un arbre est expliqué dans la classe Tree
		*/		
		//Pour les fruits noirs
		String pos_noir1 = "";
		String pos_noir2 = "";
		
		while(!(pos_noir1.contains("-1")||pos_noir1.contains("0")|| pos_noir1.contains("1")|| pos_noir1.contains("2")||pos_noir1.contains("3")||pos_noir1.contains("4")||pos_noir1.contains("5")))
		{
			System.out.println("Donnez la position des fruits noirs pour les arbres 0 et 3 : (la référence est l'arbre 0. -1 corrrespond à la position des fruits noirs inconnue");
			BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in)); 
			pos_noir1 = keyboard.readLine();
			if(pos_noir1.contains("-1"))
			{
				//Les fruits sont considérés comme tous violets
			}
			else if(pos_noir1.contains("0"))
			{
				real_state.table.setFruitNoir(0, 0);
				real_state.table.setFruitNoir(3, 3);
			}
			else if(pos_noir1.contains("1"))
			{
				real_state.table.setFruitNoir(0, 1);
				real_state.table.setFruitNoir(3, 4);
			}
			else if (pos_noir1.contains("2"))
			{
				real_state.table.setFruitNoir(0, 2);
				real_state.table.setFruitNoir(3, 5);
			}
			else if (pos_noir1.contains("3"))
			{
				real_state.table.setFruitNoir(0, 3);
				real_state.table.setFruitNoir(3, 0);
			}
			else if (pos_noir1.contains("4"))
			{
				real_state.table.setFruitNoir(0, 4);
				real_state.table.setFruitNoir(3, 1);
			}
			else if (pos_noir1.contains("5"))
			{
				real_state.table.setFruitNoir(0, 5);
				real_state.table.setFruitNoir(3, 2);
			}
		}		
		while(!(pos_noir2.contains("-1")||pos_noir2.contains("0")|| pos_noir2.contains("1")|| pos_noir2.contains("2")||pos_noir2.contains("3")||pos_noir2.contains("4")||pos_noir2.contains("5")))
		{
			System.out.println("Donnez la position des fruits noirs pour les arbres 1 et 2 : (la référence est l'arbre 0. : -1 corrrespond à la position des fruits noirs inconnue");
			BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in)); 
			pos_noir2 = keyboard.readLine(); 
			if(pos_noir2.contains("-1"))
			{
				//Les fruits sont considérés comme tous violets
			}
			else if(pos_noir2.contains("0"))
			{
				real_state.table.setFruitNoir(1, 0);
				real_state.table.setFruitNoir(2, 3);
			}
			else if(pos_noir2.contains("1"))
			{
				real_state.table.setFruitNoir(1, 1);
				real_state.table.setFruitNoir(2, 4);
			}
			else if (pos_noir2.contains("2"))
			{
				real_state.table.setFruitNoir(1, 2);
				real_state.table.setFruitNoir(2, 5);
			}
			else if (pos_noir2.contains("3"))
			{
				real_state.table.setFruitNoir(1, 3);
				real_state.table.setFruitNoir(2, 0);
			}
			else if (pos_noir2.contains("4"))
			{
				real_state.table.setFruitNoir(1, 4);
				real_state.table.setFruitNoir(2, 1);
			}
			else if (pos_noir2.contains("5"))
			{
				real_state.table.setFruitNoir(1, 5);
				real_state.table.setFruitNoir(2, 2);
			}
				
		}
		/*
		System.out.println("L'arbre 0 et 3 sont ");
		for(int i= 0; i< 4; i++)
		{
			System.out.println("L'arbre 0 et 3 sont ");
			System.out.println(real_state.table.getListTree()[i].nbrLeft());
			System.out.println(real_state.table.getListTree()[i].nbrRight());
		}
		*/
	}

}
