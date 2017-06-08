package hook.sortes;

import hook.Executable;
import hook.Hook;
import hook.methodes.TakeFire;
import enums.Cote;
import robot.RobotVrai;
import robot.cartes.Capteurs;
import strategie.GameState;
import utils.Log;
import utils.Read_Ini;

/**
 * Classe des hook de capteur de feu, qui hérite de la classe hook
 * @author pf
 *
 */

class HookFeu extends Hook {

	private Capteurs capteur;
	Cote cote;
	
	public HookFeu(Read_Ini config, Log log, GameState<RobotVrai> real_state, Capteurs capteur)
	{
		super(config, log, real_state);
		this.capteur = capteur;
	}
	
	public boolean evaluate()
	{
	    // TODO: si le robot détecte un feu à gauche et que sa pince gauche est prise, alors il prend le feu à droite...
		// si on tient déjà un feu de ce côté...
		if(real_state.robot.isTient_feu(Cote.DROIT) && real_state.robot.isTient_feu(Cote.GAUCHE))
			return false;
		boolean gauche = capteur.isThereFireGauche();
		boolean milieu = capteur.isThereFireMilieu();
		boolean droit = capteur.isThereFireDroit();
		if(droit || milieu || gauche)
		{
		    log.warning("On a détecté un feu!", this);
		    Executable takefire = callbacks.get(0).methode;
		    if(takefire instanceof TakeFire)
		    {
		        if(gauche)
		            if(!real_state.robot.isTient_feu(Cote.GAUCHE))
		                ((TakeFire)takefire).setColour(Cote.GAUCHE, Cote.GAUCHE);
		            else
                        ((TakeFire)takefire).setColour(Cote.DROIT, Cote.GAUCHE);
		        else if(droit)
                    if(!real_state.robot.isTient_feu(Cote.DROIT))
                        ((TakeFire)takefire).setColour(Cote.DROIT, Cote.DROIT);
                    else
                        ((TakeFire)takefire).setColour(Cote.GAUCHE, Cote.DROIT);
		        else if(milieu)
                    if(!real_state.robot.isTient_feu(Cote.GAUCHE))
                        ((TakeFire)takefire).setColour(Cote.GAUCHE, Cote.MILIEU);
                    else
                        ((TakeFire)takefire).setColour(Cote.DROIT, Cote.MILIEU);
		    }
			return declencher();
		}
		return false;
	}
	
}
