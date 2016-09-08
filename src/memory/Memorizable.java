package memory;

import utils.Config;

/**
 * Interface de ce qui est mémorisable par le MemoryManager
 * @author pf
 *
 */

public interface Memorizable
{
	/**
	 * Permet de modifier son indice
	 * @param indice
	 */
	public void setIndiceMemoryManager(int indice);
	
	/**
	 * Renvoi l'indice précédemment fixé
	 * @return
	 */
	public int getIndiceMemoryManager();
		
	public void useConfig(Config config);
}
