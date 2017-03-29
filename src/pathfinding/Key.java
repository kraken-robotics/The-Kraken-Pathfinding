/*
Copyright (C) 2013-2017 Pierre-François Gimenez

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

package pathfinding;

import scripts.Script;

/**
 * Clé pour le cache du pathfinding
 * @author pf
 *
 */

public class Key
{
	public Integer cinem;
	public Script s;
	public ChronoGameState chrono;
	public boolean shoot;
	
	public Key(ChronoGameState chrono, Script s, boolean shoot)
	{
		this.chrono = chrono;
		this.cinem = chrono.robot.getCinematique().codeForPFCache();
		this.s = s;
		this.shoot = shoot;
	}
	
	@Override
	public int hashCode()
	{
		return (cinem + s.hashCode()) * 2 + (shoot ? 1 : 0);
	}
	
	@Override
	public boolean equals(Object o)
	{
		return o instanceof Key && ((Key)o).shoot == shoot && ((Key)o).s == s && ((Key)o).cinem == cinem;
	}
	
	@Override
	public String toString()
	{
		return s+"-"+cinem+"-"+shoot;
	}
}