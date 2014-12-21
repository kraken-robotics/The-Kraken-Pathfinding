import strategie.Execution;
import container.Container;
import enums.ServiceNames;

/**
 * Lanceur TechTheTroll
 * @author pf
 *
 */

/* TODO LIST
 * se renseigner sur la compilation sans bytecode (GCJ)
 * pouvoir arrêter une recherche avant la fin (hook de fin de match)
 * affichage graphique stratégie
 */

public class Lanceur {

	public static void main(String[] args) {

		try {
			Container container = new Container();
			container.startAllThreads();
			Execution execution = (Execution)container.getService(ServiceNames.EXECUTION);
			execution.boucleExecution();
			container.destructor();
		} catch (Exception e) {
			System.out.println("Abandon du lanceur.");
			e.printStackTrace();
			return;
		}
		
	}

}
