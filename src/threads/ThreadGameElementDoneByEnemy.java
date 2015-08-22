package threads;

import obstacles.ObstaclesIterator;
import obstacles.ObstaclesMemory;
import obstacles.types.ObstacleProximity;
import pathfinding.MoteurPhysique;
import table.GameElementNames;
import table.Table;
import utils.Config;
import utils.Log;
import container.Service;
import enums.Tribool;

/**
 * Supprime les éléments de jeux qui sont proches de la position des obstacles vus
 * @author pf
 *
 */

public class ThreadGameElementDoneByEnemy extends Thread implements Service
{
	protected Log log;
	private ObstaclesMemory memory;
	private Table table;
	private MoteurPhysique moteur;
	private ObstaclesIterator iterator;
	
	
	public ThreadGameElementDoneByEnemy(Log log, ObstaclesMemory memory, Table table, MoteurPhysique moteur)
	{
		this.log = log;
		this.memory = memory;
		this.table = table;
		this.moteur = moteur;
		iterator = new ObstaclesIterator(log, memory);
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			synchronized(memory)
			{
				try {
					while(iterator.hasNext())
					{
						ObstacleProximity o = iterator.next();
					    // On vérifie aussi ceux qui ont un rayon nul (distributeur, clap, ..)
					    for(GameElementNames g: GameElementNames.values)
					        if(table.isDone(g) == Tribool.FALSE && moteur.didTheEnemyTakeIt(g, o))
					        	table.setDone(g, Tribool.MAYBE);						
					}
					memory.wait();
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
			}
			
			
		}
//		log.debug("Fermeture de ThreadObstacleManager");
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}



