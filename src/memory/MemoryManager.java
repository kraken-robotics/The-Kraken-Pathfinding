/*
Copyright (C) 2016 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package memory;

import java.lang.reflect.Array;

import container.Container;
import container.Service;
import exceptions.ContainerException;
import utils.Config;
import utils.Log;

/**
 * Classe qui fournit des objets
 * Quand on a besoin de beaucoup d'objets, car l'instanciation d'un objet est long.
 * Du coup on réutilise les mêmes objets sans devoir en créer tout le temps de nouveaux.
 * @author pf
 *
 */

public class MemoryManager<T extends Memorizable> implements Service {

	protected int nb_instances;

	private final T[] nodes;
	protected Log log;
	
	private int firstAvailable;
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		for(int i = 0; i < nb_instances; i++)
			nodes[i].useConfig(config);
	}

	@SuppressWarnings("unchecked")
	public MemoryManager(Class<T> classe, Log log, Container container, int nb_instances) throws ContainerException
	{	
		this.log = log;
		this.nb_instances = nb_instances;
		nodes = (T[]) Array.newInstance(classe, nb_instances);
		firstAvailable = 0;

		// on instancie une fois pour toutes les objets
		log.debug("Instanciation de "+nb_instances+" "+classe.getSimpleName());

		for(int i = 0; i < nb_instances; i++)
		{
			nodes[i] = container.make(classe);
			nodes[i].setIndiceMemoryManager(i);
		}
		log.debug("Instanciation finie");
	}
	
	/**
	 * Donne un objet disponible
	 * @return
	 */
	public synchronized T getNewNode()
	{
		// lève une exception s'il n'y a plus de place
		return nodes[firstAvailable++];
	}

	/**
	 * Signale que tous les objets sont disponibles. Très rapide.
	 * @param id_astar
	 */
	public synchronized void empty()
	{
		firstAvailable = 0;
	}
	
	/**
	 * Signale qu'un objet est de nouveau disponible
	 * @param objet
	 */
	public synchronized void destroyNode(T objet)
	{
		
		int indice_state = objet.getIndiceMemoryManager();

		/**
		 * S'il est déjà détruit, on lève une exception
		 */
		if(indice_state >= firstAvailable)
		{
			int z = 0;
			z = 1 / z;
		}

		// On inverse dans le Vector les deux objets,
		// de manière à avoir toujours un Vector trié.
		firstAvailable--;
	
		if(indice_state != firstAvailable)
		{
			T tmp1 = nodes[indice_state];
			T tmp2 = nodes[firstAvailable];
	
			tmp1.setIndiceMemoryManager(firstAvailable);
			tmp2.setIndiceMemoryManager(indice_state);
	
			nodes[firstAvailable] = tmp1;
			nodes[indice_state] = tmp2;
		}
	}

	/**
	 * Retourne le nombre d'élément utilisé
	 */
	public synchronized int getSize()
	{
		return firstAvailable;
	}
	
}
