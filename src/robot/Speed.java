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


package robot;

/**
 * Définition des vitesses possibles de déplacement du robot.
 * @author pf
 *
 */

public enum Speed
{
	// TODO fixer les valeurs

	// vitesse standard
    STANDARD(1.),
    
    TEST1(0.3),
    
    TEST2(0.5),
    
    TEST3(0.8),
    
    // pour décourager la marche arrière, on la met moins rapide
    MARCHE_ARRIERE(0.5),

    // Vitesse du robot lors d'une replanification, plus lent que la vitesse standard
    REPLANIF(0.7),

    REPLANIF_MARCHE_ARRIERE(0.3);

    // en millimètre par milliseconde = mètre par seconde
    public final double translationalSpeed;
    
    /**
     * @param vitesse
     */
    private Speed(double translationalSpeed)
    {
    	this.translationalSpeed = translationalSpeed;
    }
   
}
