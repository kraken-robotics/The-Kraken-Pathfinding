package hook.methods;

import enums.GameElementNames;
import table.Table;
import hook.Executable;

/**
 * 
 * @author pf
 *
 */

public class GameElementDone implements Executable {

	private GameElementNames id_element;
	private Table table;
	
	public GameElementDone(GameElementNames id_element, Table table)
	{
		this.id_element = id_element;
		this.table = table;
	}
	
	@Override
	public boolean execute() {
		table.setDone(id_element);
		return false;
	}

}
