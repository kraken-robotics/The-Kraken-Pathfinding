/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.memory;

/**
 * The different state of the objects
 * @author Pierre-François Gimenez
 *
 */

public enum MemPoolState
{
	FREE, // free
	WAITING, // in open-set
	STANDBY, // in closed set
	CURRENT, // the current node
	NEXT; // the successors
}
