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
	 * Donne un gamestate disponible
	 * @param id_astar
	 * @return
	 * @throws FinMatchException
	 */
	public T getNewNode()
	{
		// lève une exception s'il n'y a plus de place
		T out;
		out = nodes[firstAvailable];
		firstAvailable++;
		return out;
	}

	/**
	 * Signale que tous les objets sont disponibles. Très rapide.
	 * @param id_astar
	 */
	public void empty()
	{
		firstAvailable = 0;
	}
	
	/**
	 * Signale qu'un objet est de nouveau disponible
	 * @param state
	 * @param id_astar
	 * @throws MemoryManagerException
	 */
	public void destroyNode(T state)
	{
		
		int indice_state = state.getIndiceMemoryManager();

		/**
		 * S'il est déjà détruit, on lève une exception
		 */
		if(indice_state >= firstAvailable)
		{
			int z = 0;
			z = 1 / z;
		}

		// On inverse dans le Vector les deux gamestates,
		// de manière à avoir toujours un Vector trié.
		firstAvailable--;
		
		T tmp1 = nodes[indice_state];
		T tmp2 = nodes[firstAvailable];

		tmp1.setIndiceMemoryManager(firstAvailable);
		tmp2.setIndiceMemoryManager(indice_state);

		nodes[firstAvailable] = tmp1;
		nodes[indice_state] = tmp2;
	}

	/**
	 * Retourne le nombre d'élément utilisé
	 */
	public int getSize()
	{
		return firstAvailable;
	}
	
}
