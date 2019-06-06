/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.struct;

import java.io.Serializable;

import pfg.kraken.memory.Memorizable;
import pfg.kraken.memory.MemoryPool.MemPoolState;
import pfg.kraken.obstacles.RectangularObstacle;

/**
 * Une cinématique + un obstacle associé
 * 
 * @author pf
 *
 */

public final class EmbodiedKinematic implements Memorizable, Serializable
{
	public final Kinematic cinem;
	private static final long serialVersionUID = 1L;
	public volatile RectangularObstacle obstacle;
	public volatile double maxSpeed; // in m/s

	private volatile int indiceMemory;
	
	public String toString()
	{
		return super.toString()+", maxSpeed = "+maxSpeed;
	}
	
	public EmbodiedKinematic(RectangularObstacle vehicleTemplate)
	{
		cinem = new Kinematic();
		obstacle = vehicleTemplate.clone();
		maxSpeed = -1;
	}

	public void copy(EmbodiedKinematic autre)
	{
		cinem.copy(autre.cinem);
		autre.maxSpeed = maxSpeed;
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
	public void update(double x, double y, double orientationGeometrique, boolean enMarcheAvant, double courbure, double maxSpeed, boolean stop)
	{
		cinem.update(x, y, orientationGeometrique, enMarcheAvant, courbure, stop);
		this.maxSpeed = maxSpeed;
		obstacle.update(cinem.position, cinem.orientationReelle);
	}

	private volatile MemPoolState state = MemPoolState.FREE;
	
	@Override
	public void setState(MemPoolState state)
	{
		this.state = state;
	}
	
	@Override
	public MemPoolState getState()
	{
		return state;
	}
	
	@Override
	public EmbodiedKinematic clone()
	{
		EmbodiedKinematic out = new EmbodiedKinematic(obstacle);
		copy(out);
		return out;
	}

	public void update(ItineraryPoint p)
	{
		cinem.update(p);
		maxSpeed = p.maxSpeed;
		obstacle.update(cinem.position, cinem.orientationReelle);
	}
}
