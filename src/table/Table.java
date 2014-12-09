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
	private GameElement[] plots = new GameElement[8];
	private GameElement[] claps = new GameElement[3];
	private GameElement[] verres = new GameElement[5];
	private GameElement[] distributeurs = new GameElement[4];
	// Et potentiellement les balles de tennis
	
	private static int indice = 0;
	private int hash = 0;
	
	public Table(Log log, Config config)
	{
		this.log = log;
		this.config = config;	
		
		total[0] = plots[0] = new GameElement(new Vec2(1410, 150), 30, GameElementNames.PLOT_1);
		total[1] = plots[1] = new GameElement(new Vec2(1410, 250), 30, GameElementNames.PLOT_2);
		total[2] = plots[2] = new GameElement(new Vec2(1410, 1300), 30, GameElementNames.PLOT_3);
		total[3] = plots[3] = new GameElement(new Vec2(650, 1300), 30, GameElementNames.PLOT_4);
		total[4] = plots[4] = new GameElement(new Vec2(650, 1400), 30, GameElementNames.PLOT_5);
		total[5] = plots[5] = new GameElement(new Vec2(200, 600), 30, GameElementNames.PLOT_6);
		total[6] = plots[6] = new GameElement(new Vec2(630, 645), 30, GameElementNames.PLOT_7);
		total[7] = plots[7] = new GameElement(new Vec2(400, 230), 30, GameElementNames.PLOT_8);
		
		total[8] = claps[0] = new GameElement(new Vec2(650, 0), -1, GameElementNames.CLAP_1);
		total[9] = claps[1] = new GameElement(new Vec2(-950, 0), -1, GameElementNames.CLAP_2);
		total[10] = claps[2] = new GameElement(new Vec2(1250, 0), -1, GameElementNames.CLAP_3);
		
		total[11] = verres[0] = new GameElement(new Vec2(-1250, 250), 50, GameElementNames.VERRE_1);
		total[12] = verres[1] = new GameElement(new Vec2(1250, 250), 50, GameElementNames.VERRE_2);
		total[13] = verres[2] = new GameElement(new Vec2(-590, 1200), 50, GameElementNames.VERRE_3);
		total[14] = verres[3] = new GameElement(new Vec2(590, 1200), 50, GameElementNames.VERRE_4);
		total[15] = verres[4] = new GameElement(new Vec2(0, 350), 50, GameElementNames.VERRE_5);

		total[16] = distributeurs[0] = new GameElement(new Vec2(900, 1950), 25, GameElementNames.DISTRIB_1);
		total[17] = distributeurs[1] = new GameElement(new Vec2(1200, 1950), 25, GameElementNames.DISTRIB_2);
		total[18] = distributeurs[2] = new GameElement(new Vec2(-900, 1950), 25, GameElementNames.DISTRIB_3);
		total[19] = distributeurs[3] = new GameElement(new Vec2(-1200, 1950), 25, GameElementNames.DISTRIB_4);
	}
	
	/**
	 * On a pris l'objet ou on est passé dessus.
	 * @param id
	 */
	public void setDone(GameElementNames id)
	{
		indice++;
		hash = indice;
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
        if(!equals(ct))
		{
        	for(int i = 0; i < 20; i++)
        		total[i].fastClone(ct.total[i]);
        	ct.hash = hash;
		}
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
		return other.hash == hash;
 	}

	@Override
	public void updateConfig()
	{
	}
	
	public GameElement[] getObstacles()
	{
		return total;
	}
	
	/**
	 * Utilisé par les tests
	 * @return
	 */
	public int getHash()
	{
		return hash;
	}

}