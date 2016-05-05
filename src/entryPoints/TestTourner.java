package entryPoints;

import robot.RobotReal;
import robot.Speed;
import robot.actuator.ActuatorOrder;
import serie.DataForSerialOutput;
import utils.Config;
import utils.ConfigInfo;
import utils.Sleep;
import container.Container;
import container.ServiceNames;
import exceptions.ContainerException;
import exceptions.PointSortieException;

/**
 * Test odo / asser
 * @author pf
 *
 */

public class TestTourner
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
		
		System.out.println("Match lancé ! Couleur : "+config.getString(ConfigInfo.COULEUR));

		Speed vitesse = Speed.SLOW;

		while(true)
		{
			robot.tournerB(2*Math.PI/3, vitesse);
			Sleep.sleep(1000);
			robot.tournerB(-2*Math.PI/3, vitesse);
			Sleep.sleep(1000);
		}
	}
}
