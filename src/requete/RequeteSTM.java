package requete;

import utils.Config;
import utils.Log;
import container.Service;

/**
 * Une classe qui contient les informations en cas d'erreur soulevée par le bas niveau
 * @author pf
 *
 */

public class RequeteSTM implements Service {

	public RequeteType type;
	public int param;
	protected Log log;
	
	public RequeteSTM(Log log)
	{
		this.log = log;
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
}
