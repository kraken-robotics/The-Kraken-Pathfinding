package robot;

/**
 * Définition des vitesses possibles de déplacement du robot.
 * @author pf, marsu
 *
 */

public enum Speed
{
	// TODO fixer les valeurs
    STANDARD(10, 10),
    
    // On avance moins vite si l'on veut percuter un mur.
    INTO_WALL(10, 10),
    
    // Vitesse du robot lors d'une replanification, plus lent que la vitesse standard
    REPLANIF(10,10),
    
    SLOW(10, 10);
    
    public static final int translationStopDuration = 200; // le temps de s'arrêter en translation
    public static final int rotationStopDuration = 100; // le temps de s'arrêter en rotation
    
    /** en millisecondes par millimètre */
    public final double invertedTranslationalSpeed;
    
    /** en millisecondes par radian */
    public final double invertedRotationalSpeed;
        
    /** en millimètres par milliseconde */
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
