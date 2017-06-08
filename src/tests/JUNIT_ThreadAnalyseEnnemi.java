package tests;

import robot.RobotChrono;
import robot.RobotVrai;
import smartMath.Vec2;
import strategie.MemoryManager;
import table.Table;
import utils.Sleep;

import org.junit.Before;
import org.junit.Test;

import enums.Vitesse;

/**
 * Tests unitaires de la stratégie
 * @author krissprolls
 *
 */
public class JUNIT_ThreadAnalyseEnnemi extends JUnit_Test {
	private RobotVrai robotvrai;
	//private Strategie strategie;
	private MemoryManager memorymanager;
	//private Pathfinding pathfinder;
	//private Table table;
	private RobotChrono robotchrono;
	@Before
	public void setUp() throws Exception {
		//Le setUp vient du code JUnit_StrategieThreadStrategie
		super.setUp();
		robotvrai = (RobotVrai)container.getService("RobotVrai");
		//strategie = (Strategie) container.getService("Strategie");
		memorymanager = (MemoryManager) container.getService("MemoryManager");
		//pathfinder = (Pathfinding) container.getService("Pathfinding");
		//table = (Table) container.getService("Table");
		robotchrono = new RobotChrono(config, log);
		config.set("couleur", "jaune");
		
		//table = (Table)container.getService("Table");
		Vec2 initpos = new Vec2(1000,1400);

		robotvrai.setOrientation((float)Math.PI);
        robotvrai.set_vitesse(Vitesse.ENTRE_SCRIPTS);
		container.getService("threadStrategie");
		container.getService("thredAnalyseEnnemie");
		
		robotvrai.setPosition(initpos);
		robotchrono.setPosition(initpos);
		Sleep.sleep(100);
		
		container.demarreThreads();
		
		robotvrai.setPosition(initpos);
		robotchrono.setPosition(initpos);
		Sleep.sleep(100);
	

	}
 
	
	@Test
	public void test_duree_freeze() throws  Exception
	{
		log.debug("Début du test de durée freeze : ", this);
	}
	@Test 
	public void test_analyse_ennemi() throws Exception
	{
		Table table = memorymanager.getClone(0).table;
		while(true)
		{
			Sleep.sleep(1000);
			table =  memorymanager.getClone(0).table;
			log.debug("La position du robot adverse 0 donnée par le laser est : "+table.get_positions_ennemis()[0], this);
			log.debug("La position du robot adverse 1 donnée par le laser est : "+table.get_positions_ennemis()[1], this);

			log.debug("pour les arbres : ",this);
			for(int a = 0; a<table.getListTree().length; a++)
			{
				log.debug(a+" : "+table.getProbaTree(a),this);
			}
			log.debug("pour les feux : ", this);
			for(int b = 0; b <table.getListFire().length;b++)
			{
				log.debug(b+" : "+table.setProbaFire(b),this);
			}
			log.debug("pour les fresques : ", this);
			for(int c = 0 ; c<3; c++)
			{
				log.debug(c+" : "+table.getProbaFresco(c), this);
			}
			log.debug("pour les feux fixes (feu au bord)",this);
			for(int d = 0; d < table.getListFixedFire().length;d++)
			{
				log.debug(d+" : "+table.getProbaFixedFire(d), this);
			}
			log.debug("pout les torches : ", this);
			for(int e = 0; e < table.getListTorch().length ;e++)
			{
				log.debug(e+" : "+table.prendreProbaTorch(e), this);
			}			
		}		
	}
}



