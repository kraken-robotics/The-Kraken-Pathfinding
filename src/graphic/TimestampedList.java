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

package graphic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Une liste de listes d'objets timestampées
 * 
 * @author pf
 *
 */

public class TimestampedList implements Serializable
{
	private static final long serialVersionUID = -5167892162649965305L;
	private List<Long> timestamps = new ArrayList<Long>();
	private List<List<Serializable>> listes = new ArrayList<List<Serializable>>();
	private long dateInitiale;

	public TimestampedList(long dateInitiale)
	{
		this.dateInitiale = dateInitiale;
	}

	public void add(List<Serializable> o)
	{
		timestamps.add(System.currentTimeMillis() - dateInitiale);
		listes.add(o);
	}

	public long getTimestamp(int index)
	{
		return timestamps.get(index);
	}

	public List<Serializable> getListe(int index)
	{
		return listes.get(index);
	}

	public int size()
	{
		return timestamps.size();
	}

}
