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
 * Debug l'asser en affichant les grandeurs
 * @author pf
 *
 */

public class Match
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
		
//		stm.avancer(1000, Speed.STANDARD);
		
		Speed vitesse = Speed.SLOW;
/*
		robot.avancerB(100, false, vitesse);
		
		
		if(true)
			while(true)
				Sleep.sleep(1000);*/
/*
		robot.avancerB(1400, false, vitesse);
		stm.utiliseActionneurs(ActuatorOrder.AX12_ARRIERE_DROIT_VERR1);
		stm.utiliseActionneurs(ActuatorOrder.AX12_ARRIERE_GAUCHE_VERR1);
		robot.vaAuPointB(new Vec2<ReadOnly>(-380, 1750), vitesse);
		stm.utiliseActionneurs(ActuatorOrder.AX12_AVANT_DROIT_OUVERT1);
		stm.utiliseActionneurs(ActuatorOrder.AX12_AVANT_GAUCHE_OUVERT1);
		robot.vaAuPointB(new Vec2<ReadOnly>(-280, 1800), vitesse);
		robot.tournerB(Math.PI, vitesse);
		robot.avancerB(500, false, vitesse);*/
		/*
		ArrayList<Hook> hooks2 = new ArrayList<Hook>();
		Hook hooktest = new HookDemiPlan(log, new Vec2<ReadOnly>(1400, 1000), new Vec2<ReadOnly>(-10, 0), symetrie);
		hooktest.ajouter_callback(new UtiliseActionneur(ActuatorOrder.AX12_POISSON_BAS));

		hooks2.add(hooktest);
		robot.avancerB(300, hooks2, Speed.INTO_WALL);
*/
		
//		robot.vaAuPointB(new Vec2<ReadOnly>(1080, 1310), vitesse, true);
		robot.vaAuPointB(new Vec2<ReadOnly>(1080, 1610), vitesse, true);

		stm.utiliseActionneurs(ActuatorOrder.AX12_ARRIERE_GAUCHE_OUVERT1);
		stm.utiliseActionneurs(ActuatorOrder.AX12_ARRIERE_DROIT_OUVERT1);
		robot.tournerB(-Math.PI/2, vitesse);		
//		robot.avancerB(-680, true, vitesse);
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
		robot.avancerB(-400, false, vitesse);

		
		robot.vaAuPointB(new Vec2<ReadOnly>(1080, 240), vitesse, true);
		robot.vaAuPointB(new Vec2<ReadOnly>(1000, 195), vitesse, !symetrie);
		robot.vaAuPointB(new Vec2<ReadOnly>(1080, 150), vitesse, symetrie);
		
		if(symetrie)
			robot.tournerB(0, vitesse);
		else
			robot.tournerB(Math.PI, vitesse);

		hooks.clear();
		Hook hookBaisse = new HookDemiPlan(log, new Vec2<ReadOnly>(1000, 120), new Vec2<ReadOnly>(-10, 0), symetrie);
		Hook hookMilieu = new HookDemiPlan(log, new Vec2<ReadOnly>(700, 120), new Vec2<ReadOnly>(-10, 0), symetrie);
		Hook hookBaisse2 = new HookDemiPlan(log, new Vec2<ReadOnly>(500, 120), new Vec2<ReadOnly>(-10, 0), symetrie);
		Hook hookOuvre = new HookDemiPlan(log, new Vec2<ReadOnly>(450, 120), new Vec2<ReadOnly>(-10, 0), symetrie);
		Hook hookFerme = new HookDemiPlan(log, new Vec2<ReadOnly>(400, 120), new Vec2<ReadOnly>(-10, 0), symetrie);
		Hook hookLeve = new HookDemiPlan(log, new Vec2<ReadOnly>(350, 120), new Vec2<ReadOnly>(-10, 0), symetrie);

		hookBaisse.ajouter_callback(new UtiliseActionneur(ActuatorOrder.AX12_POISSON_BAS));
		hookMilieu.ajouter_callback(new UtiliseActionneur(ActuatorOrder.AX12_POISSON_MILIEU));
		hookBaisse2.ajouter_callback(new UtiliseActionneur(ActuatorOrder.AX12_POISSON_BAS));
		hookOuvre.ajouter_callback(new UtiliseActionneur(ActuatorOrder.AX12_POISSON_OUVRE));
		hookFerme.ajouter_callback(new UtiliseActionneur(ActuatorOrder.AX12_POISSON_FERME));
		hookLeve.ajouter_callback(new UtiliseActionneur(ActuatorOrder.AX12_POISSON_HAUT));

		hooks.add(hookBaisse);
		hooks.add(hookMilieu);
		hooks.add(hookBaisse2);
		hooks.add(hookOuvre);
		hooks.add(hookFerme);
		hooks.add(hookLeve);
		
		robot.vaAuPointB(new Vec2<ReadOnly>(300, 150), hooks, Speed.POISSON, !symetrie);
		
	}
}
