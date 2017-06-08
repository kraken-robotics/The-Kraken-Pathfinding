package threads;

import smartMath.Vec2;
import strategie.Strategie;
import table.Table;
import utils.Sleep;

/**
 * Thread qui analyse le comportement de l'ennemi à partir de sa position
 * @author pf, krissprolls
 *
 */
public class ThreadAnalyseEnnemi extends AbstractThread  {

	private Table table;
	private Strategie strategie;
	
	private long[] date_freeze = new long[2];
	public Vec2[] positionsfreeze = new Vec2[2];
//	private int tolerance = 1000;
	
	public ThreadAnalyseEnnemi(Table table, Strategie strategie)
	{
		this.table = table;
		this.strategie = strategie;
		positionsfreeze = table.get_positions_ennemis();
		Thread.currentThread().setPriority(1);
	}
	
	@Override
	public void run()
	{
		log.debug("Lancement du thread d'analyse de l'ennemi", this);

		while(!ThreadTimer.match_demarre)
		{
			if(stop_threads)
			{
				log.debug("Arrêt du thread d'analyse de l'ennemi", this);
				return;
			}
			Sleep.sleep(100);
		}

		date_freeze[0] = System.currentTimeMillis();
		date_freeze[1] = System.currentTimeMillis();
		
		while(!ThreadTimer.fin_match)
		{
			if(stop_threads)
			{
				log.debug("Arrêt du thread d'analyse de l'ennemi", this);
				return;
			}

			Vec2[] positionsEnnemi = table.get_positions_ennemis();
			for(int i = 0; i < 2; i++)
				// C'est le cas d'un robot qui freeze pas
				if(positionsfreeze[i].SquaredDistance(positionsEnnemi[i]) > 100)	// 10cm de tolérance sur le freeze
				{
					// comme s'il allait refreezer  a sa nouvelle position tout de suite
					positionsfreeze[i] = positionsEnnemi[i];
					date_freeze[i] = System.currentTimeMillis();
				}
			
			strategie.analyse_ennemi(positionsfreeze, duree_freeze());
			
			Sleep.sleep(500); // le sleep peut être long, le robot adverse ne bouge de toute façon pas très vite...
		}
		log.debug("Arrêt du thread d'analyse de l'ennemi", this);

	}


	/**
	 * Donne à la stratégie les durées de freeze de chaque robot
	 * @return duree_freeze
	 */
	public int[] duree_freeze()
	{
		int[] duree_freeze = new int[2];
		duree_freeze[0] = (int)(System.currentTimeMillis() - date_freeze[0]);
		duree_freeze[1] = (int)(System.currentTimeMillis() - date_freeze[1]);
		return duree_freeze;
	}
	
}
