package table;

import container.Service;
import smartMath.Vec2;
import utils.*;

public class Table implements Service
{
	// Dépendances
	private Log log;
	private Config config;
	
	// DEPENDS ON RULES
	
	// Les éléments de jeu de notre couleur.
	private GameElement[] total = new GameElement[22];
	private GameElement[] emplacementsTapis = new GameElement[2];
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
		
		total[0] = emplacementsTapis[0] = new GameElement(new Vec2(-50, 1500), -1);
		total[1] = emplacementsTapis[1] = new GameElement(new Vec2(450, 1500), -1);
		
		total[2] = plots[0] = new GameElement(new Vec2(1410, 150), 30);
		total[3] = plots[1] = new GameElement(new Vec2(1410, 250), 30);
		total[4] = plots[2] = new GameElement(new Vec2(1410, 1300), 30);
		total[5] = plots[3] = new GameElement(new Vec2(650, 1300), 30);
		total[6] = plots[4] = new GameElement(new Vec2(650, 1400), 30);
		total[7] = plots[5] = new GameElement(new Vec2(200, 600), 30);
		total[8] = plots[6] = new GameElement(new Vec2(630, 645), 30);
		total[9] = plots[7] = new GameElement(new Vec2(400, 230), 30);
		
		total[10] = claps[0] = new GameElement(new Vec2(650, 0), -1);
		total[11] = claps[1] = new GameElement(new Vec2(-950, 0), -1);
		total[12] = claps[2] = new GameElement(new Vec2(1250, 0), -1);
		
		total[13] = verres[0] = new GameElement(new Vec2(-1250, 250), 50);
		total[14] = verres[1] = new GameElement(new Vec2(1250, 250), 50);
		total[15] = verres[2] = new GameElement(new Vec2(-590, 1200), 50);
		total[16] = verres[3] = new GameElement(new Vec2(590, 1200), 50);
		total[17] = verres[4] = new GameElement(new Vec2(0, 350), 50);

		total[18] = distributeurs[0] = new GameElement(new Vec2(900, 1950), 25);
		total[19] = distributeurs[1] = new GameElement(new Vec2(1200, 1950), 25);
		total[20] = distributeurs[2] = new GameElement(new Vec2(-900, 1950), 25);
		total[21] = distributeurs[3] = new GameElement(new Vec2(-1200, 1950), 25);
	}
	
	public void setClapDone(int id)
	{
		indice++;
		hash = indice;
		claps[id].setDone();
	}

	public void setVerreDone(int id)
	{
		indice++;
		hash = indice;
		verres[id].setDone();
	}
	
	public void setDistributeurDone(int id)
	{
		indice++;
		hash = indice;
		distributeurs[id].setDone();
	}

	public void setPlotTaken(int id)
	{
		indice++;
		hash = indice;
		plots[id].setDone();
	}

	public void setTapisPut(int id)
	{
		indice++;
		hash = indice;
		emplacementsTapis[id].setDone();
	}
	
	/**
	 * La table en argument deviendra la copie de this (this reste inchangé)
	 * @param ct
	 */
	public void copy(Table ct)
	{
        if(!equals(ct))
		{
			for(int i = 0; i < 2; i++)	
			    emplacementsTapis[i].fastClone(ct.emplacementsTapis[i]);
			for(int i = 0; i < 8; i++)	
			    plots[i].fastClone(ct.plots[i]);
			for(int i = 0; i < 3; i++)	
			    claps[i].fastClone(ct.claps[i]);
			for(int i = 0; i < 5; i++)	
			    verres[i].fastClone(ct.verres[i]);
			for(int i = 0; i < 4; i++)	
			    distributeurs[i].fastClone(ct.distributeurs[i]);			
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
		return other.hash == hash; //TODO
 	}

	@Override
	public void updateConfig() {
		// TODO Auto-generated method stub
		
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