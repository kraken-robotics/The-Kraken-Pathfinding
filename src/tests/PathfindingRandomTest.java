package tests;

//import java.util.ArrayList;
//import java.util.Random;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import enums.Vitesse;
import pathfinding.Pathfinding;
//import pathfinding.Pathfinding;
//import robot.RobotChrono;
import robot.RobotVrai;
import smartMath.Vec2;
//import table.Table;
import utils.Sleep;

public class PathfindingRandomTest extends JUnit_Test
{

	private RobotVrai robotvrai;
	Pathfinding finder;
//	private RobotChrono robotchrono;
//	private Table table;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		config.set("couleur", "jaune");
		
		//scriptmanager = (ScriptManager)container.getService("ScriptManager");
		robotvrai = (RobotVrai)container.getService("RobotVrai");
//		robotchrono = new RobotChrono(config, log);
//		robotchrono.majRobotChrono(robotvrai);
//		table = (Table)container.getService("Table");
		Vec2 initpos = new Vec2(-1034,688);
		robotvrai.setPosition(initpos);
		Sleep.sleep(100);
		robotvrai.setPosition(initpos);
		robotvrai.setOrientation((float)Math.PI);
        robotvrai.set_vitesse(Vitesse.ENTRE_SCRIPTS);
		container.getService("threadPosition");
		finder = (Pathfinding) container.getService("Pathfinding");
		container.demarreThreads();
		// init
		robotvrai.setPosition(initpos);
		Sleep.sleep(100);
		robotvrai.setPosition(initpos);
	}

	
	// ===========================================  Va au point spécifié
	@Test
	public void test_simple() throws Exception
	{
	
				
		Vec2 arrivee = new Vec2(1004,688);
		
		
		ArrayList<Vec2> chemin = finder.chemin(robotvrai.getPosition(), arrivee);
		
		if (chemin != null)
		{
			
			
			// suit le teajet
			for(int j = 0; j < chemin.size(); j++)
			{
				Vec2 newpos = new Vec2(0,0);
				newpos.x =  chemin.get(j).x;
				newpos.y =  chemin.get(j).y;
				
				ArrayList<Vec2> chemin_final = new ArrayList<Vec2>();
				chemin_final.add(newpos);
				robotvrai.suit_chemin(chemin_final, null);
				
			}
			
		}
		
	}
	
	
	// ================================ Test de marche aléatoire ========================================

	@Test
	public void test_marche_aleatoire() throws Exception
	{
		
		// init =======================
		
/**
		super.setUp();
		config.set("couleur", "jaune");
		
		//scriptmanager = (ScriptManager)container.getService("ScriptManager");
		robotvrai = (RobotVrai)container.getService("RobotVrai");
		robotchrono = new RobotChrono(config, log);
		robotchrono.majRobotChrono(robotvrai);
		table = (Table)container.getService("Table");
		hookgenerator = (HookGenerator)container.getService("HookGenerator");
		robotvrai.setPosition(new Vec2(1300, 1200));
		robotvrai.setOrientation((float)Math.PI);
		robotvrai.set_vitesse_rotation("entre_scripts");
		robotvrai.set_vitesse_translation("entre_scripts");
		container.getService("threadPosition");
		container.demarreThreads();
		robotvrai.set_vitesse_translation("30");
		robotvrai.avancer(100);
		
		**/
		
		// ============================

/*		int compteTrajets = 0;
		int cmParCase =2;
		
		Pathfinding finder = new Pathfinding(table, config, log, cmParCase);
		Random randomGenerator = new Random();
		
	    while(true)
	    {
			
			Vec2 arrivee = new Vec2(randomGenerator.nextInt(3000)-1500,randomGenerator.nextInt(2000))
					,depart;
			while (finder.map.canCross((int)((float)(arrivee.x + 1500) / cmParCase /10), (int)((float)(arrivee.y) / cmParCase /10)) == false)
			{
				arrivee.x = randomGenerator.nextInt(3000)-1500;
				arrivee.y = randomGenerator.nextInt(2000); 
			}
			depart = robotvrai.getPosition();
			if (finder.map.canCross((int)((float)(depart.x + 1500) / cmParCase /10), (int)((float)(depart.y) / cmParCase /10)) == false)
				System.out.println("depart not crossable");
			

			System.out.println("==========================================================================================================\nNouveau trajet : Depart = " + depart.x + " ; "+ depart.y + "     Arrivée = " + arrivee.x + " ; "+ arrivee.y);
			ArrayList<Vec2> chemin = finder.chemin(depart, arrivee);

			if (chemin != null)
			{
	*/			
				
				/*
				// affiche la feuille de route
				Vec2 newpos = new Vec2(0,0);
				System.out.println("Chemin (test_marche_aleatoire) : ");
				//newpos.x = depart.x +  chemin.get(0).x;
				//.y = depart.y +  chemin.get(0).y;
				System.out.println("pox n°" + 0 + " : " + newpos);
				for(int j = 0; j < chemin.size(); j++)
				{
					newpos.x = chemin.get(j).x;
					newpos.y = chemin.get(j).y;
					System.out.println("pox n°" + j + " : " + newpos);
					
				}
				

				// Affiche le calcul du chemin
				String out = "";
				Integer i = 1;
				for (int  j = 0; j < finder.map.getSizeX(); ++j)
				{
					for (int  k = finder.map.getSizeY() - 1; k >= 0; --k)
					{
						
						IntPair pos = new IntPair(j,k);
						if (finder.getDepart().x ==j && finder.getDepart().y ==k)
							out += 'D';
						else if (finder.getArrivee().x ==j && finder.getArrivee().y ==k)
							out += 'A';
						else if (chemin.contains(pos))
						{
							out += i.toString();
							i++;
						}
						
						else if(finder.map.canCross(j, k))
							out += '.';
						else
							out += 'X';
						
						
					}
					
					out +='\n';
				}
				System.out.println(out);	
				*/			
				
				
				
				// suit le trajet
//				Vec2 newpos = new Vec2(0,0);
			/*	newpos.x = depart.x +  chemin.get(0).x;
				newpos.y = depart.y +  chemin.get(0).y;
				System.out.println("Goto : " + newpos);
				robotvrai.va_au_point(newpos);*/
/*				for(int j = 0; j < chemin.size(); j++)
				{
					newpos.x = chemin.get(j).x;
					newpos.y = chemin.get(j).y;
					

					//System.out.println("Goto : " + newpos);
					robotvrai.va_au_point(newpos);
					//Thread.sleep(1000);
					
				}
				compteTrajets++;

				System.out.println("Trajets effectués : " + compteTrajets);
				
			}
	    }
    */          

	}
}
