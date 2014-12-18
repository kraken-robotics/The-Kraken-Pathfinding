package table;

import obstacles.GameElement;
import container.Service;
import enums.GameElementNames;
import enums.Tribool;
import smartMath.Vec2;
import utils.*;

public class Table implements Service
{
	// Dépendances
	private Log log;
	private Config config;
	
	// DEPENDS ON RULES
	
	// Les éléments de jeu de notre couleur.
	private GameElement[] total = new GameElement[20];
	// Et potentiellement les balles de tennis
	
	public Table(Log log, Config config)
	{
		this.log = log;
		this.config = config;	
		
		total[0] = new GameElement(log, new Vec2(1410, 150), 30, GameElementNames.PLOT_1);
		total[1] = new GameElement(log, new Vec2(1410, 250), 30, GameElementNames.PLOT_2);
		total[2] = new GameElement(log, new Vec2(1410, 1300), 30, GameElementNames.PLOT_3);
		total[3] = new GameElement(log, new Vec2(650, 1300), 30, GameElementNames.PLOT_4);
		total[4] = new GameElement(log, new Vec2(650, 1400), 30, GameElementNames.PLOT_5);
		total[5] = new GameElement(log, new Vec2(200, 600), 30, GameElementNames.PLOT_6);
		total[6] = new GameElement(log, new Vec2(630, 645), 30, GameElementNames.PLOT_7);
		total[7] = new GameElement(log, new Vec2(400, 230), 30, GameElementNames.PLOT_8);
		
		total[8] = new GameElement(log, new Vec2(650, 0), -1, GameElementNames.CLAP_1);
		total[9] = new GameElement(log, new Vec2(-950, 0), -1, GameElementNames.CLAP_2);
		total[10] = new GameElement(log, new Vec2(1250, 0), -1, GameElementNames.CLAP_3);
		
		total[11] = new GameElement(log, new Vec2(-1250, 250), 50, GameElementNames.VERRE_1);
		total[12] = new GameElement(log, new Vec2(1250, 250), 50, GameElementNames.VERRE_2);
		total[13] = new GameElement(log, new Vec2(-590, 1200), 50, GameElementNames.VERRE_3);
		total[14] = new GameElement(log, new Vec2(590, 1200), 50, GameElementNames.VERRE_4);
		total[15] = new GameElement(log, new Vec2(0, 350), 50, GameElementNames.VERRE_5);

		total[16] = new GameElement(log, new Vec2(900, 1950), 25, GameElementNames.DISTRIB_1);
		total[17] = new GameElement(log, new Vec2(1200, 1950), 25, GameElementNames.DISTRIB_2);
		total[18] = new GameElement(log, new Vec2(-900, 1950), 25, GameElementNames.DISTRIB_3);
		total[19] = new GameElement(log, new Vec2(-1200, 1950), 25, GameElementNames.DISTRIB_4);
	}
	
	/**
	 * On a pris l'objet ou on est passé dessus.
	 * @param id
	 */
	public void setDone(GameElementNames id)
	{
		total[id.ordinal()].setDone(Tribool.TRUE);
	}

	/**
	 * Cet objet est-il présent ou non?
	 * @param id
	 */
	public Tribool isDone(GameElementNames id)
	{
		return total[id.ordinal()].isDone();
	}	

	/**
	 * La table en argument deviendra la copie de this (this reste inchangé)
	 * @param ct
	 */
	public void copy(Table ct)
	{
		for(int i = 0; i < 20; i++)
			total[i].fastClone(ct.total[i]);
	}
	
	public Table clone()
	{
		Table cloned_table = new Table(log, config);
		copy(cloned_table);
		return cloned_table;
	}

	/**
	 * Utilisé pour les tests
	 * @param other
	 * @return
	 */
	public boolean equals(Table other)
	{
		for(int i = 0; i < 20; i++)
			if(total[i].isDone() != other.total[i].isDone())
				return false;
		return true;
 	}

	@Override
	public void updateConfig()
	{
	}
	
	/**
	 * Utilisé par l'obstacle manager
	 * @return
	 */
	public GameElement[] getObstacles()
	{
		return total;
	}

}