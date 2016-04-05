package entryPoints;

import org.jfree.ui.RefineryUtilities;

import pathfinding.VitesseCourbure;
import pathfinding.astarCourbe.ArcCourbe;
import pathfinding.astarCourbe.ClothoidesComputer;
import permissions.ReadOnly;
import buffer.DataForSerialOutput;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2;
import container.Container;
import container.ServiceNames;
import debug.AffichageDebug;
import debug.IncomingDataDebug;
import debug.IncomingDataDebugBuffer;
import exceptions.ContainerException;
import exceptions.PointSortieException;

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
		
		aff.pack();
		RefineryUtilities.centerFrameOnScreen(aff);
		aff.setVisible(true);
		
//		Config config = (Config) container.getService(ServiceNames.CONFIG);
/*		synchronized(config)
		{
			config.set(ConfigInfo.MATCH_DEMARRE, true);
			config.set(ConfigInfo.DATE_DEBUT_MATCH, System.currentTimeMillis());
		}
	*/	
		stm.activeDebugMode();
		
		double kpVitesse = 0.4; // 11 pour 23V
		double kiVitesse = 0; // 1
		double kdVitesse = 0; // 0.9
		
		stm.setPIDconstVitesseDroite(kpVitesse, kiVitesse, kdVitesse);
		stm.setPIDconstVitesseGauche(kpVitesse, kiVitesse, kdVitesse);

		double kpRot = 0.01;
		double kiRot = 0;
		double kdRot = 0;
		
		stm.setPIDconstRotation(kpRot, kiRot, kdRot);

		double kpTr = 10; // 0.4
		double kiTr = 0.001; // 0.003
		double kdTr = 0; // 0.04
		
		stm.setPIDconstTranslation(kpTr, kiTr, kdTr);

		double kpVitLin = 0.3;
		double kdVitLin = 0;
		
		stm.setPIDconstVitesseLineaire(kpVitLin, 0, kdVitLin);
		
		double kpCourbure = 0.3;
		double kdCourbure = 0;
		
		stm.setPIDconstCourbure(kpCourbure, 0, kdCourbure);
		
		double k1 = 0.3;
		double k2 = 0.3;
		
		stm.setConstSamson(k1, k2);
		
//		stm.avancer(100);
//		stm.turn(Math.PI/2);
//		stm.immobilise();
/*
		int nbArc = 2;
		ArcCourbe arc[] = new ArcCourbe[nbArc];
		for(int i = 0; i < nbArc; i++)
			arc[i] = new ArcCourbe();

		clotho.getTrajectoire(new Vec2<ReadOnly>(0, 1000), true, Math.PI, 0, VitesseCourbure.COURBURE_IDENTIQUE, arc[0]);
		stm.envoieArcCourbe(arc[0]);

		clotho.getTrajectoire(arc[0], VitesseCourbure.DROITE_0, arc[1]);
		stm.envoieArcCourbe(arc[1]);
*/

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
