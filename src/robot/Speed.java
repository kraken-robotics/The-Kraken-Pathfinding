package robot;

/**
 * Définition des vitesses possibles de déplacement du robot.
 * @author pf, marsu
 *
 */

public enum Speed
{
	// vitesse en temps normal
//    BETWEEN_SCRIPTS(170, 0.52, 17.0, 160, 2.0, 50.0),
    BETWEEN_SCRIPTS(17, 0.52, 17.0, 16, 2.0, 50.0),
    
    // On avance moins vite si l'on veut percuter un mur.
    INTO_WALL(90, 0.45, 12.5, 160, 2.0, 50.0),
    
    // Le recalage nous fais percuter dans les murs, donc on avance pas trop vite
    READJUSTMENT(90, 0.45, 12.5, 90, 1.0, 15.0);
    
    
    // valeurs des PWM (Phase Wave Modulation)
    public int PWMTranslation;
    public int PWMRotation;
    
    public static final int translationStopDuration = 200; // le temps de s'arrêter en translation
    public static final int rotationStopDuration = 100; // le temps de s'arrêter en rotation
    
    /** en millisecondes par millimètre */
    public final int invertedTranslationnalSpeed;
    
    /** en millisecondes par radian */
    public final int invertedRotationnalSpeed;
        
    /** en millimètres par milliseconde */
    public final double translationnalSpeed;
    
    /** en radians par milliseconde */
    public final double rotationnalSpeed;
    
    /** coefficient utilisé pour l'asservissement */
    public final double kp_rot;
    
    /** coefficient utilisé pour l'asservissement */
    public final double kd_rot;

    /** coefficient utilisé pour l'asservissement */
    public final double kp_trans;
    
    /** coefficient utilisé pour l'asservissement */
    public final double kd_trans;

    /**
     * @param PWM_translation
     * @param PWM_rotation
     */
    private Speed(int PWM_translation, double kp_trans, double kd_trans, int PWM_rotation, double kp_rot, double kd_rot)
    {
        this.PWMTranslation = PWM_translation;
        this.PWMRotation = PWM_rotation;
        this.kp_trans = kp_trans;
        this.kd_trans = kd_trans;
        this.kp_rot = kp_rot;
        this.kd_rot = kd_rot;
        
        // TODO: faire des MESURES
        
        invertedTranslationnalSpeed = (int) (1./((2500.)/(613.52 * (Math.pow(PWM_translation,(-1.034))))/1000));
        invertedRotationnalSpeed = (int) (1./((Math.PI)/(277.85 * Math.pow(PWM_rotation,(-1.222)))/1000));
        translationnalSpeed = 1./(invertedTranslationnalSpeed);
        rotationnalSpeed = 1./(invertedRotationnalSpeed);
    }
    
    /**
     * Renvoie le rayon courbure en trajectoire courbe en mm.
     * @return
     */
    public int rayonCourbure()
    {
    	return (int)(translationnalSpeed/rotationnalSpeed);
    }
    
}
