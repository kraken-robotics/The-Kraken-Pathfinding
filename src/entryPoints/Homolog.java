package entryPoints;

import hook.Hook;
import hook.methods.UtiliseActionneur;
import hook.types.HookDemiPlan;

import java.util.ArrayList;

import robot.RobotReal;
import robot.Speed;
import robot.actuator.ActuatorOrder;
import serie.DataForSerialOutput;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
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

public class Homolog
{
	public static void main(String[] args) throws ContainerException, InterruptedException, PointSortieException
	{
		Container container = new Container();
		Log log = (Log) container.getService(ServiceNames.LOG);
		Config config = (Config) container.getService(ServiceNames.CONFIG);
		container.getService(ServiceNames.ROBOT_REAL); // initialisation de l'odo
		DataForSerialOutput stm = (DataForSerialOutput) container.getService(ServiceNames.SERIAL_OUTPUT_BUFFER);
		RobotReal robot = (RobotReal) container.getService(ServiceNames.ROBOT_REAL);

		ArrayList<Hook> hooks = new ArrayList<Hook>();

		System.out.println("Attente du début du match");

		while(!config.getBoolean(ConfigInfo.MATCH_DEMARRE))
			Sleep.sleep(1);

		boolean symetrie = config.getSymmetry();
		
		Speed vitesse = Speed.SLOW;

		robot.vaAuPointB(new Vec2<ReadOnly>(1080, 1610), vitesse, true);

		stm.utiliseActionneurs(ActuatorOrder.AX12_ARRIERE_GAUCHE_OUVERT1);
		stm.utiliseActionneurs(ActuatorOrder.AX12_ARRIERE_DROIT_OUVERT1);
		robot.tournerB(-Math.PI/2, vitesse);		
		robot.avancerB(-380, true, vitesse);

		stm.utiliseActionneurs(ActuatorOrder.AX12_ARRIERE_DROIT_VERR2);
		stm.utiliseActionneurs(ActuatorOrder.AX12_ARRIERE_GAUCHE_VERR2);
		robot.avancerB(100, true, vitesse);
		
		hooks.clear();
		Hook hookBras = new HookDemiPlan(log, new Vec2<ReadOnly>(0, 1600), new Vec2<ReadOnly>(0, -10), symetrie);

		hookBras.ajouter_callback(new UtiliseActionneur(ActuatorOrder.AX12_AVANT_DROIT_OUVERT2));
		hookBras.ajouter_callback(new UtiliseActionneur(ActuatorOrder.AX12_AVANT_GAUCHE_OUVERT2));

		hooks.add(hookBras);

		robot.vaAuPointB(new Vec2<ReadOnly>(1200, 1000), hooks, vitesse, true);
		robot.tournerB(Math.PI, vitesse);
		robot.avancerB(900, false, vitesse);
		
	}
}
