/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles.types;

import java.awt.Color;

import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.robot.Cinematique;

/**
 * Les différentes vitesses de courbure qu'on peut suivre
 * 
 * @author pf
 *
 */

public interface TentacleType
{
	public boolean isAcceptable(Cinematique c, DirectionStrategy directionstrategyactuelle, double courbureMax);
//	public Tentacle compute();
	public int getNbArrets();
	public Color getColor();
}
