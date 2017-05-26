/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package memory;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import container.Service;
import exceptions.ContainerException;
import exceptions.MemoryManagerException;
import utils.Log;

/**
 * Classe qui fournit des objets
 * Quand on a besoin de beaucoup d'objets, car l'instanciation d'un objet est
 * long.
 * Du coup on réutilise les mêmes objets sans devoir en créer tout le temps de
 * nouveaux.
 * 
 * @author pf
 *
 */

public abstract class MemoryManager<T extends Memorizable> implements Service
{

	private int initial_nb_instances;

	private List<T[]> nodes = new ArrayList<T[]>();
	private Class<T> classe;
	protected Log log;
	private int firstAvailable;
	private int tailleMax = 1 << 24;

	protected abstract T make();
	
	public MemoryManager(Class<T> classe, Log log) throws ContainerException
	{
		this.classe = classe;
		this.log = log;
	}
	
	@SuppressWarnings("unchecked")
	protected void init(int nb_instances)
	{
		initial_nb_instances = nb_instances;
		nodes.add((T[]) Array.newInstance(classe, nb_instances));
		firstAvailable = 0;
		// on instancie une fois pour toutes les objets
		log.debug("Instanciation de " + nb_instances + " " + classe.getSimpleName() + "…");

		for(int i = 0; i < nb_instances; i++)
		{
			nodes.get(0)[i] = make();
			nodes.get(0)[i].setIndiceMemoryManager(i);
		}
	}

	/**
	 * Donne un objet disponible
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unchecked")
	public synchronized T getNewNode() throws MemoryManagerException
	{
		// lève une exception s'il n'y a plus de place
		if(firstAvailable == initial_nb_instances * nodes.size())
		{
			if(initial_nb_instances * nodes.size() >= tailleMax) // pas trop
																	// d'objets
																	// (sert
																	// à
																	// empêcher
																	// les
																	// bugs
																	// de
																	// tout
																	// faire
																	// planter…
																	// cette
																	// condition
																	// est
																	// inutile
																	// en
																	// temps
																	// normal)
			{
				log.critical("Mémoire saturée pour " + classe.getSimpleName() + ", arrêt");
				throw new MemoryManagerException();
			}

			if(nodes.size() + 1 >= 20)
				log.warning("Mémoire trop petite pour les " + classe.getSimpleName() + ", extension (nouvelle taille : " + ((nodes.size() + 1) * initial_nb_instances) + ")");

			T[] newNodes = (T[]) Array.newInstance(classe, initial_nb_instances);

			for(int i = 0; i < initial_nb_instances; i++)
			{
				newNodes[i] = make();//container.make(classe, extra);
				newNodes[i].setIndiceMemoryManager(i + firstAvailable);
			}

			nodes.add(newNodes);
		}

		T out = nodes.get(firstAvailable / initial_nb_instances)[firstAvailable % initial_nb_instances];
		firstAvailable++;
		return out;
	}

	/**
	 * Signale que tous les objets sont disponibles. Très rapide.
	 * 
	 * @param id_astar
	 */
	public synchronized void empty()
	{
		firstAvailable = 0;
	}

	/**
	 * Signale qu'un objet est de nouveau disponible
	 * 
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
			log.critical("Objet déjà détruit ! " + indice_state + " > " + firstAvailable);
			new Exception().printStackTrace(log.getPrintWriter());
			return;
		}

		// On inverse dans le Vector les deux objets,
		// de manière à avoir toujours un Vector trié.
		firstAvailable--;

		if(indice_state != firstAvailable)
		{
			T tmp1 = nodes.get(indice_state / initial_nb_instances)[indice_state % initial_nb_instances];
			T tmp2 = nodes.get(firstAvailable / initial_nb_instances)[firstAvailable % initial_nb_instances];

			tmp1.setIndiceMemoryManager(firstAvailable);
			tmp2.setIndiceMemoryManager(indice_state);

			nodes.get(firstAvailable / initial_nb_instances)[firstAvailable % initial_nb_instances] = tmp1;
			nodes.get(indice_state / initial_nb_instances)[indice_state % initial_nb_instances] = tmp2;
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
