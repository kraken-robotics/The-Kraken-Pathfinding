package pathfinding;

import pathfinding.astarCourbe.ArcCourbe;
import utils.Config;
import utils.Log;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import container.Service;

/**
 * S'occupe de la trajectoire actuelle.
 * Notifie dès qu'un chemin (partiel ou complet) est disponible
 * @author pf
 *
 */

public class CheminPlanif implements Service, Collection<ArcCourbe>
{
	protected Log log;
	
	private volatile LinkedList<ArcCourbe> chemin = new LinkedList<ArcCourbe>();
	
	public CheminPlanif(Log log)
	{
		this.log = log;
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
	public ArcCourbe poll()
	{
		return chemin.poll();
	}

	public boolean isEmpty()
	{
		return chemin.isEmpty();
	}

	@Override
	public boolean add(ArcCourbe arg0) {
		return chemin.add(arg0);
	}

	@Override
	public boolean addAll(Collection<? extends ArcCourbe> arg0) {
		return chemin.addAll(arg0);
	}

	@Override
	public void clear() {
		chemin.clear();
	}

	@Override
	public boolean contains(Object arg0) {
		return chemin.contains(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		return chemin.containsAll(arg0);
	}

	@Override
	public Iterator<ArcCourbe> iterator() {
		return chemin.iterator();
	}

	@Override
	public boolean remove(Object arg0) {
		return chemin.remove(arg0);
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		return chemin.removeAll(arg0);
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		return chemin.retainAll(arg0);
	}

	@Override
	public int size() {
		return chemin.size();
	}

	@Override
	public Object[] toArray() {
		return chemin.toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return chemin.toArray(arg0);
	}

}
