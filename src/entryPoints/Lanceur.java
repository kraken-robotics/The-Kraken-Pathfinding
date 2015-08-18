package entryPoints;
import strategie.Execution;
import container.Container;
import container.ServiceNames;

/**
 * Lanceur TechTheTroll
 * @author pf
 *
 */

/**
 * TODO LIST
 * Avoir un asservissement en vitesse lorsqu'on est loin de la consigne en position (nécessaire à la trajectoire courbe)
 * FreeRTOS sur ARM?
 * Obtenir la vitesse instantanée par une commande série
 * Avoir les capteurs par paire et faire de la trigonalisation
 * 
 *
 */

public class Lanceur {

	public static void main(String[] args) {

		try {
			Container container = new Container();
			Execution execution = (Execution)container.getService(ServiceNames.EXECUTION);

			/**
			 * Initialisation du robot
			 */
			// maintenant faite directement par la STM
			
			/**
			 * Attente du début du match
			 */
			execution.waitDebutMatch();
			
			/**
			 * Exécution du match
			 */
			execution.boucleExecution();
			
			/**
			 * Finalisation
			 */
			container.destructor();
		} catch (Exception e) {
			System.out.println("Abandon du lanceur.");
			e.printStackTrace();
			return;
		}
		
	}

}
