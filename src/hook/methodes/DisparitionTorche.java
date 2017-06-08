package hook.methodes;

import enums.Cote;
import hook.Executable;
import table.Table;

/**
 * Classe implémentant la méthode de disparition de torche, utilisée lors des déplacements
 * @author pf
 *
 */

public class DisparitionTorche implements Executable {

		private Cote cote;
		private Table table;
		
		public DisparitionTorche(Table table, Cote cote)
		{
			this.table = table;
			this.cote = cote;
		}
		
		@Override
		public boolean execute()
		{
            System.out.println("La torche "+cote+" a disparu pour de vrai!");
			table.torche_disparue(cote);
            return false; // ça n'affecte pas les mouvements du robot
		}

}
