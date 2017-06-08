package enums;

/**
 * Définition des vitesses possibles de déplacement du robot.
 * @author pf, marsu
 *
 */

public enum Speed
{
	// vitesse en temps normal
    BETWEEN_SCRIPTS(170, 160),
    
    // On avance moins vite si l'on veut percuter un mur.
    INTO_WALL(90, 160),
    
    // Le recalage nous fais percuter dans les murs, donc on avance pas trop vite
    READJUSTMENT(90, 90);
    
    
    // valeurs des PWM (Phase Wave Modulation) // TODO: utilité ?
    public int PWMTranslation;
    public int PWMTotation;
    
    // en milimètre par milisecondes ? //TODO: check this
    public int invertedTranslationnalSpeed;
    
    // en radians par milisecondes ? // TODO: check this
    public int invertedRotationnalSpeed;
        
    /**
     * //TODO faire la doc de ce contructeur 
     * @param PWM_translation
     * @param PWM_rotation
     */
    private Speed(int PWM_translation, int PWM_rotation)
    {
        this.PWMTranslation = PWM_translation;
        this.PWMTotation = PWM_rotation;
        
        invertedTranslationnalSpeed = (int) (1./(((float)2500)/((float)613.52 * (float)(Math.pow((double)PWM_translation,(double)(-1.034))))/1000));
        invertedRotationnalSpeed = (int) (1./(((float)Math.PI)/((float)277.85 * (float)Math.pow(PWM_rotation,(-1.222)))/1000));
    }

    
}
