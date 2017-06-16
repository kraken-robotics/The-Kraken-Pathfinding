/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.robot;

import java.io.Serializable;

import pfg.kraken.memory.Memorizable;
import pfg.kraken.obstacles.types.ObstacleRobot;

/**
 * Une cinématique + un obstacle associé
 * 
 * @author pf
 *
 */

public class CinematiqueObs extends Cinematique implements Memorizable, Serializable
{
	private static final long serialVersionUID = 1L;
	public volatile ObstacleRobot obstacle;
	private int indiceMemory;

	public CinematiqueObs(int demieLargeurNonDeploye, int demieLongueurArriere, int demieLongueurAvant)
	{
		super();
		obstacle = new ObstacleRobot(demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant);
	}

	public void copy(CinematiqueObs autre)
	{
		super.copy(autre);
		obstacle.copy(autre.obstacle);
	}

	@Override
	public void setIndiceMemoryManager(int indice)
	{
		indiceMemory = indice;
	}

	@Override
	public int getIndiceMemoryManager()
	{
		return indiceMemory;
	}

	/**
	 * Met à jour les données
	 * 
	 * @param x
	 * @param y
	 * @param orientation
	 * @param enMarcheAvant
	 * @param courbure
	 * @param vitesseMax
	 * @param obstacle
	 * @param r
	 */
	@Override
	public void update(double x, double y, double orientationGeometrique, boolean enMarcheAvant, double courbure)
	{
		super.update(x, y, orientationGeometrique, enMarcheAvant, courbure);
		obstacle.update(position, orientationReelle);
	}
	
	@Override
	public void updateReel(double x, double y, double orientationReelle, boolean enMarcheAvant, double courbure)
	{
		super.updateReel(x, y, orientationReelle, enMarcheAvant, courbure);
		obstacle.update(position, orientationReelle);
	}

}
