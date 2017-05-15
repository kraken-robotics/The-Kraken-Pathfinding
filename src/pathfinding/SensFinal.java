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

package pathfinding;

/**
 * Le sens final (marche avant / marche arrière) souhaitée à la fin d'une
 * recherche de chemin
 * 
 * @author pf
 *
 */

public enum SensFinal
{
	MARCHE_AVANT(true, false),
	MARCHE_ARRIERE(false, true),
	AUCUNE_PREF(true, true);

	private boolean marcheAvantOK, marcheArriereOK;

	private SensFinal(boolean marcheAvantOK, boolean marcheArriereOK)
	{
		this.marcheAvantOK = marcheAvantOK;
		this.marcheArriereOK = marcheArriereOK;
	}

	/**
	 * Cette direction est-elle valide pour finir le chemin ?
	 * 
	 * @param marcheAvant
	 * @return
	 */
	public boolean isOK(boolean marcheAvant)
	{
		return (marcheAvant && marcheAvantOK) || (!marcheAvant && marcheArriereOK);
	}
}
