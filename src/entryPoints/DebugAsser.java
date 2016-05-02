package entryPoints;

import org.jfree.ui.RefineryUtilities;

import pathfinding.VitesseCourbure;
import pathfinding.astarCourbe.arcs.ArcCourbe;
import pathfinding.astarCourbe.arcs.ArcCourbeClotho;
import pathfinding.astarCourbe.arcs.ClothoidesComputer;
import robot.RobotReal;
import robot.Speed;
import serie.DataForSerialOutput;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Sleep;
import utils.Vec2;
import utils.permissions.ReadOnly;
import container.Container;
import container.ServiceNames;
import debug.AffichageDebug;
import debug.IncomingDataDebug;
import debug.IncomingDataDebugBuffer;
import exceptions.ContainerException;
import exceptions.PointSortieException;
import exceptions.UnableToMoveException;

/**
 * Debug l'asser en affichant les grandeurs
 * @author pf
 *
 */

public class DebugAsser
{
	public static void main(String[] args) throws ContainerException, InterruptedException, PointSortieException
	{
		Container container = new Container();
		Log log = (Log) container.getService(ServiceNames.LOG);
		container.getService(ServiceNames.ROBOT_REAL); // initialisation de l'odo
		DataForSerialOutput stm = (DataForSerialOutput) container.getService(ServiceNames.SERIAL_OUTPUT_BUFFER);
		ClothoidesComputer clotho = (ClothoidesComputer) container.getService(ServiceNames.CLOTHOIDES_COMPUTER);
		IncomingDataDebugBuffer buffer = (IncomingDataDebugBuffer) container.getService(ServiceNames.INCOMING_DATA_DEBUG_BUFFER);
		AffichageDebug aff = new AffichageDebug();
		RobotReal robot = (RobotReal) container.getService(ServiceNames.ROBOT_REAL);

		if(Config.debugAsser)
		{
			aff.pack();
			RefineryUtilities.centerFrameOnScreen(aff);
			aff.setVisible(true);
		}
		
//		Config config = (Config) container.getService(ServiceNames.CONFIG);
/*		synchronized(config)
		{
			config.set(ConfigInfo.MATCH_DEMARRE, true);
			config.set(ConfigInfo.DATE_DEBUT_MATCH, System.currentTimeMillis());
		}
	*/	
		if(Config.debugAsser)
			stm.activeDebugMode();

		stm.asserOff();
		
		double kpVitesseD = 5; // 11 pour 23V // 5 // 80 // 3
		double kiVitesseD = 0.2; // 1 // 1 // 0 // 1
		double kdVitesseD = 0.17; // 0.9 // 0.2 // 0.7 // 0.2

		double kpVitesseG = 8; // 11 pour 23V // 5 // 80 // 5
		double kiVitesseG = 0.2; // 1 // 1 // 0 // 1
		double kdVitesseG = 0.27; // 0.9 // 0.2 // 0.7 // 0.2

		stm.setPIDconstVitesseDroite(kpVitesseD, kiVitesseD, kdVitesseD);
		stm.setPIDconstVitesseGauche(kpVitesseG, kiVitesseG, kdVitesseG);

		double kpRot = 0.12;
		double kiRot = 0;
		double kdRot = 0.008;
		
		stm.setPIDconstRotation(kpRot, kiRot, kdRot);

		double kpTr = 0.04; // 0.04
		double kiTr = 0.0; // sur les conseils de Sylvain
		double kdTr = 0.006; // 0.006

		stm.setPIDconstTranslation(kpTr, kiTr, kdTr);

		double kpCourbure = 0.1;
		double kiCourbure = 0.00;
		double kdCourbure = 0.00;
		
		stm.setPIDconstCourbure(kpCourbure, kiCourbure, kdCourbure);
		
		double k1 = 0.0;
		double k2 = 0.2;
		
		stm.setConstSamson(k1, k2);
		/*
		try {
			robot.avancer(-1000, Speed.STANDARD);
		} catch (UnableToMoveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*//*
		try {
			robot.tourner(Math.PI/2, Speed.STANDARD);
		} catch (UnableToMoveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			robot.tourner(0, Speed.INTO_WALL);
		} catch (UnableToMoveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			robot.avancer(100, Speed.STANDARD);
		} catch (UnableToMoveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
//		stm.avancer(500, Speed.STANDARD);
//		stm.turn(-Math.PI/2, Speed.STANDARD);
/*
		stm.turn(-Math.PI/2, Speed.STANDARD);
		Sleep.sleep(600);
		stm.turn(Math.PI, Speed.STANDARD);
		Sleep.sleep(600);
		stm.avancer(1500, Speed.STANDARD);*/
/*		Sleep.sleep(600);
		stm.suspendMouvement();
		Sleep.sleep(1000);
		stm.reprendMouvement();*/
//		Sleep.sleep(1000);
//		stm.immobiliseUrgence();
//		stm.asserVitesse(50, 80);
//		stm.turn(Math.PI/2, Speed.STANDARD);
//		Sleep.sleep(1000);
//		stm.turn(Math.PI, Speed.STANDARD);
//		log.debug("On demande à avancer !");
//		stm.avancer(200, Speed.INTO_WALL);
//		Sleep.sleep(5000);
/*		stm.avancer(-200, Speed.STANDARD);
		Sleep.sleep(1000);
		stm.avancerMemeSens(200, Speed.STANDARD);
		Sleep.sleep(1000);
		stm.avancer(-200, Speed.STANDARD);
		Sleep.sleep(1000);
		stm.avancerMemeSens(-200, Speed.STANDARD);*/
//		stm.turn(0, Speed.STANDARD);
/*		stm.turn(-Math.PI/2);
		Sleep.sleep(5000);
		stm.turn(0);
		Sleep.sleep(5000);
		stm.turn(Math.PI/2);
		Sleep.sleep(5000);
		stm.turn(Math.PI);*/
//		stm.immobilise();

		Sleep.sleep(3000);
		
		int nbArc = 2;
		ArcCourbeClotho arc[] = new ArcCourbeClotho[nbArc];
		for(int i = 0; i < nbArc; i++)
			arc[i] = new ArcCourbeClotho();

		clotho.getTrajectoire(robot.getCinematique(), VitesseCourbure.COURBURE_IDENTIQUE, Speed.TEST, arc[0]);
		stm.envoieArcCourbe(arc[0]);

//		clotho.getTrajectoire(arc[0], VitesseCourbure.COURBURE_IDENTIQUE, Speed.INTO_WALL, arc[1]);
//		stm.envoieArcCourbe(arc[1]);
		
		for(int i = 0; i < arc[0].getNbPoints(); i++)
			log.debug(arc[0].arcselems[i]);


		if(Config.debugAsser)
			while(true)
			{
				synchronized(buffer)
				{
					if(buffer.isEmpty())
						buffer.wait();
					IncomingDataDebug in = buffer.poll();
					log.debug(in);
					aff.add(in);
	//				aff.add(new IncomingDataDebug((new Random()).nextInt(10), (new Random()).nextInt(7), 7, 2, 1, 2, 5, 6));
	//				Sleep.sleep(100);
				}
			}
	}
}
