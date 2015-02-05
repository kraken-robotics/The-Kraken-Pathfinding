package threads;

import java.io.IOException;

import robot.Locomotion;
import robot.serial.SerialConnexion;
import utils.Config;
import utils.Log;
import utils.Vec2;
import container.Service;
import exceptions.FinMatchException;

/**
 * Thread qui récupère inlassablement la position du robot
 * @author pf
 *
 */

public class ThreadPosition extends AbstractThread implements Service
{

	protected Log log;
	protected Config config;
	private SerialConnexion serie;
	private Locomotion locomotion;
	
	public ThreadPosition(Log log, Config config, SerialConnexion serie, Locomotion locomotion)
	{
		this.log = log;
		this.config = config;
		this.serie = serie;
		this.locomotion = locomotion;
		
		Thread.currentThread().setPriority(2);
		updateConfig();
	}

	@Override
	public void run()
	{
		log.debug("Lancement du thread de position", this);
		String[] xyo;
		while(true)
		{
			synchronized(serie)
			{
				try {
					serie.wait();
				} catch (InterruptedException e) {
					continue;
				}
				try {
					xyo = serie.read(3);
				} catch (IOException e) {
					continue;
				}
				try {
					locomotion.setPosition(new Vec2(Integer.parseInt(xyo[0]),Integer.parseInt(xyo[1])));
				} catch (NumberFormatException | FinMatchException e) {
				}
				try {
					locomotion.setOrientation(Integer.parseInt(xyo[2])/1000);
				} catch (NumberFormatException | FinMatchException e) {
				}
				synchronized(locomotion)
				{
					locomotion.notify();
				}
			}
		}
	}

	@Override
	public void updateConfig()
	{}
	
}
