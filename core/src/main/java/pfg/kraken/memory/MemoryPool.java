/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


package pfg.kraken.memory;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import pfg.kraken.LogCategoryKraken;
import pfg.log.Log;


/**
 * A memory pool
 * @author pf
 *
 */

public abstract class MemoryPool<T extends Memorizable>
{

	private int initialNbInstances;

	private List<T[]> nodes = new ArrayList<T[]>();
	private final Class<T> classe;
	protected Log log;
	private volatile int firstAvailable;
	private static final int tailleMax = 1 << 24;

	protected abstract void make(T[] nodes);
	
	public MemoryPool(Class<T> classe, Log log)
	{
		this.classe = classe;
		this.log = log;
	}
	
	@SuppressWarnings("unchecked")
	protected void init(int nb_instances)
	{
		initialNbInstances = nb_instances;
		nodes.add((T[]) Array.newInstance(classe, nb_instances));
		firstAvailable = 0;
		// on instancie une fois pour toutes les objets
		log.write("Memory pool initialization (" + nb_instances + " instances of " + classe.getSimpleName() + ")", LogCategoryKraken.PF);

		make(nodes.get(0));
		for(int i = 0; i < nb_instances; i++)
			nodes.get(0)[i].setIndiceMemoryManager(i);
	}

	/**
	 * Donne un objet disponible
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public synchronized T getNewNode()
	{
		if(firstAvailable == initialNbInstances * nodes.size())
		{
			/**
			 * Probablement une erreur
			 */
			assert initialNbInstances * nodes.size() < tailleMax : "Mémoire saturée pour " + classe.getSimpleName();

			T[] newNodes = (T[]) Array.newInstance(classe, initialNbInstances);

			make(newNodes);
			
			for(int i = 0; i < initialNbInstances; i++)
				newNodes[i].setIndiceMemoryManager(i + firstAvailable);

			nodes.add(newNodes);
		}

		String s;
		T out = nodes.get(firstAvailable / initialNbInstances)[firstAvailable % initialNbInstances];
		assert (s = checkStateNew(out)) == null : s;
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
		assert checkEmpty();
		firstAvailable = 0;
	}

	private boolean checkEmpty()
	{
		MemPoolState s;
		int nbCurrent = 0;
		for(int i = 0; i < firstAvailable; i++)
		{
			s = nodes.get(i / initialNbInstances)[i % initialNbInstances].getState();
			assert s != MemPoolState.FREE : classe+" "+s + " "+i+"/"+firstAvailable;
			if(s == MemPoolState.CURRENT)
				nbCurrent++;
			// now they are free
			nodes.get(i / initialNbInstances)[i % initialNbInstances].setState(MemPoolState.FREE);
		}
		assert nbCurrent <= 1 : nbCurrent; // au plus un nœud "current"
		for(int i = firstAvailable; i < initialNbInstances * nodes.size() - 1; i++)
		{
			s = nodes.get(i / initialNbInstances)[i % initialNbInstances].getState();
			assert s == MemPoolState.FREE : classe+" "+s;
		}
		return true;
	}

	/**
	 * Signale qu'un objet est de nouveau disponible
	 * 
	 * @param objet
	 * @throws MemoryPoolException 
	 */
	public synchronized void destroyNode(T objet)
	{
		destroyNode(objet, true);
	}

	/**
	 * Dans certains cas, on sait que la suppression est redondante. Auquel cas, on désactive la vérification
	 * @param objet
	 * @param check
	 */
	synchronized void destroyNode(T objet, boolean check)
	{
		int indexObject = objet.getIndiceMemoryManager();

		assert indexObject >= 0;
		String s;
		assert (s = checkStateDestroy(objet)) == null : s;
		
		/**
		 * Invariant: l'objet ne doit pas être déjà détruit
		 */
		assert !check || indexObject < firstAvailable : "Instance of "+classe.getSimpleName()+" already destroyed ! " + indexObject + " >= " + firstAvailable;
		if(indexObject >= firstAvailable)
			return;

		// On inverse dans le Vector les deux objets,
		// de manière à avoir toujours un Vector trié.
		firstAvailable--;

		if(indexObject != firstAvailable)
		{
			T tmp1 = nodes.get(indexObject / initialNbInstances)[indexObject % initialNbInstances];
			T tmp2 = nodes.get(firstAvailable / initialNbInstances)[firstAvailable % initialNbInstances];

			tmp1.setIndiceMemoryManager(firstAvailable);
			tmp2.setIndiceMemoryManager(indexObject);

			nodes.get(firstAvailable / initialNbInstances)[firstAvailable % initialNbInstances] = tmp1;
			nodes.get(indexObject / initialNbInstances)[indexObject % initialNbInstances] = tmp2;
		}
	}

	private String checkStateDestroy(T objet)
	{
		String out = objet.getState() == MemPoolState.CURRENT || objet.getState() == MemPoolState.NEXT ? null : objet.getState().toString();
		objet.setState(MemPoolState.FREE);
		return out;
	}
	
	private String checkStateNew(T objet)
	{
		String out = objet.getState() == MemPoolState.FREE ? null : objet.getState().toString();
		objet.setState(MemPoolState.NEXT);
		return out;
	}

	public synchronized final void destroy(Iterable<T> c)
	{
		for(T o : c)
			destroyNode(o);
	}
	
	synchronized final void destroy(Iterable<T> c, boolean check)
	{
		for(T o : c)
			destroyNode(o, check);
	}
	
	/**
	 * Retourne le nombre d'élément utilisé
	 */
	public synchronized int getCurrentlyUsedObjectsNumber()
	{
		return firstAvailable;
	}

}
