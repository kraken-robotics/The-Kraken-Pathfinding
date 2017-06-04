/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


package kraken.memory;

/**
 * Interface de ce qui est mémorisable par le MemoryManager
 * 
 * @author pf
 *
 */

public interface Memorizable
{
	/**
	 * Permet de modifier son indice
	 * 
	 * @param indice
	 */
	public void setIndiceMemoryManager(int indice);

	/**
	 * Renvoi l'indice précédemment fixé
	 * 
	 * @return
	 */
	public int getIndiceMemoryManager();
}
