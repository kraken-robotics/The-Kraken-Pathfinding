package entryPoints;

import robot.RobotReal;
import robot.Speed;
import robot.actuator.ActuatorOrder;
import serie.DataForSerialOutput;
import utils.Config;
import utils.ConfigInfo;
import utils.Sleep;
import utils.Vec2;
import utils.permissions.ReadOnly;
import container.Container;
import container.ServiceNames;
import exceptions.ContainerException;
import exceptions.PointSortieException;

/**
 * Script d'homologation
 * @author pf
 *
 */

public class HomologWithBig
{
	public static void main(String[] args) throws ContainerException, InterruptedException, PointSortieException
	{
		Container container = new Container();
//		Log log = (Log) container.getService(ServiceNames.LOG);
		Config config = (Config) container.getService(ServiceNames.CONFIG);
		container.getService(ServiceNames.ROBOT_REAL); // initialisation de l'odo
		DataForSerialOutput stm = (DataForSerialOutput) container.getService(ServiceNames.SERIAL_OUTPUT_BUFFER);
		RobotReal robot = (RobotReal) container.getService(ServiceNames.ROBOT_REAL);

		System.out.println("Attente du début du match");

		while(!config.getBoolean(ConfigInfo.MATCH_DEMARRE))
			Sleep.sleep(1);

		Speed vitesse = Speed.SLOW;

		robot.vaAuPointB(new Vec2<ReadOnly>(1080, 1610), vitesse, true);

		stm.utiliseActionneurs(ActuatorOrder.AX12_ARRIERE_GAUCHE_OUVERT1);
		stm.utiliseActionneurs(ActuatorOrder.AX12_ARRIERE_DROIT_OUVERT1);
		robot.tournerB(-Math.PI/2, vitesse);		
		robot.avancerB(-380, true, vitesse);

		robot.avancerB(100, false, vitesse);
		
	}
}
