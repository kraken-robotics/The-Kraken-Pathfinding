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


package robot;

/**
 * Définition des vitesses possibles de déplacement du robot.
 * @author pf, marsu
 *
 */

public enum Speed
{
	// TODO fixer les valeurs
    STANDARD(3., 5./1000.),
    
    // On avance moins vite si l'on veut percuter un mur.
    INTO_WALL(0.5, .5/1000.),

    // On avance moins vite si l'on veut percuter un mur.
    TEST(0.1, .1/1000.),

    // Vitesse du robot lors d'une replanification, plus lent que la vitesse standard
    REPLANIF(2., 1./1000.),
   
    SLOW(1.2, 2./1000.),

    POISSON(.15, 2./1000.);

    public static final int translationStopDuration = 200; // le temps de s'arrêter en translation
    public static final int rotationStopDuration = 100; // le temps de s'arrêter en rotation
    
    /** en millisecondes par millimètre */
    public final double invertedTranslationalSpeed;
    
    /** en millisecondes par radian */
    public final double invertedRotationalSpeed;
        
    /** en millimètres par milliseconde = mètre par seconde */
    public final double translationalSpeed;
    
    /** en radians par milliseconde */
    public final double rotationalSpeed;

    /**
     * @param PWM_translation
     * @param PWM_rotation
     */
    private Speed(double translationalSpeed, double rotationalSpeed)
    {
    	this.translationalSpeed = translationalSpeed;
    	this.rotationalSpeed = rotationalSpeed;
    	invertedTranslationalSpeed = 1. / translationalSpeed;
    	invertedRotationalSpeed = 1. / rotationalSpeed;
    }
   
}
