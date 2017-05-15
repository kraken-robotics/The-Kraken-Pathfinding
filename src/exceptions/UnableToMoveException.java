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

package exceptions;

/**
 * Problème générique de déplacement du robot, que ce soit a cause d'un robot
 * ennemi
 * (détecté par les capteurs) qui bloque le passage, ou d'un bloquage mécanique
 * (type mur)
 * 
 * @author pf, marsu
 *
 */
public class UnableToMoveException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8139322860107594266L;

	public UnableToMoveException()
	{
		super();
	}

	public UnableToMoveException(String m)
	{
		super(m);
	}

}
