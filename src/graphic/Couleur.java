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

package graphic;

import java.awt.Color;

/**
 * Quelques couleurs prédéfinies
 * @author pf
 *
 */

public enum Couleur {
	BLANC(new Color(255, 255, 255, 255)),
	NOIR(new Color(0, 0, 0, 255)),
	GRIS(new Color(50, 50, 50, 200)),
	BLEU(new Color(0, 0, 200, 255)),
	JAUNE(new Color(200, 200, 0, 255)),
	ROUGE(new Color(200, 0, 0, 255)),
	VIOLET(new Color(200, 0, 200, 255));
	
	public final Color couleur;
	
	private Couleur(Color couleur)
	{
		this.couleur = couleur;
	}
}
