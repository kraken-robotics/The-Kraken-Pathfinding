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
