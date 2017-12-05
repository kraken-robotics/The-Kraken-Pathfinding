/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar;

/**
 * The different research modes
 * @author pgimenez
 *
 */

public enum ResearchMode
{
	XYO2XY, // Departure : position and orientation. Arrival : position.
	XYO2XYO; // Departure : position and orientation. Arrival : position and orientation.
}
