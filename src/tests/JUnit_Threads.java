package tests;

import obstacles.ObstacleManager;

import org.junit.Assert;
import org.junit.Test;

import enums.ServiceNames;
import robot.RobotReal;
import robot.cardsWrappers.LocomotionCardWrapper;
import smartMath.Vec2;

/**
 * Tests unitaires des threads
 * @author pf
 *
 */

public class JUnit_Threads extends JUnit_Test {

	@Test
	public void test_arret() throws Exception
	{
		LocomotionCardWrapper deplacements = (LocomotionCardWrapper)container.getService(ServiceNames.LOCOMOTION_CARD_WRAPPER);
		deplacements.setX(0);
		deplacements.setY(1500);
		deplacements.setOrientation(0);
		deplacements.setTranslationnalSpeed(80);
		RobotReal robotvrai = (RobotReal) container.getService(ServiceNames.ROBOT_REAL);
// TODO démarrer thread position
		container.startAllThreads();
		Thread.sleep(100);
		Assert.assertTrue(robotvrai.getPosition().equals(new Vec2(0,1500)));
		container.stopAllThreads();
		deplacements.setX(100);
		deplacements.setY(1400);
		Thread.sleep(100);
		Assert.assertTrue(robotvrai.getPosition().equals(new Vec2(0,1500)));
	}

	@Test
	public void test_detection_obstacle() throws Exception
	{
		RobotReal robotvrai = (RobotReal) container.getService(ServiceNames.ROBOT_REAL);
		robotvrai.setPosition(new Vec2(0, 900));
		robotvrai.setOrientation(0);
		ObstacleManager obstaclemanager = (ObstacleManager)container.getService(ServiceNames.OBSTACLE_MANAGER);
		
		Assert.assertTrue(obstaclemanager.nb_obstacles() == 0);
		
		container.getService(ServiceNames.THREAD_SENSOR);
		container.startAllThreads();
		Thread.sleep(300);
		Assert.assertTrue(obstaclemanager.nb_obstacles() >= 1);

	}
	
	@Test
	public void test_serie() throws Exception
	{
		RobotReal robotvrai = (RobotReal) container.getService(ServiceNames.ROBOT_REAL);
		robotvrai.setPosition(new Vec2(1000, 1400));
		robotvrai.setOrientation((float)Math.PI);
		container.startAllThreads();
		Thread.sleep(200);
		robotvrai.avancer(1000);
	}

	@Test
	public void test_fin_thread_avant_match() throws Exception
	{
		container.startAllThreads();
		container.stopAllThreads();
	}

	
}
