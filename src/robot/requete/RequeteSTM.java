package robot.requete;

import utils.Config;
import utils.Log;
import container.Service;

/**
 * Une classe qui contient les informations en cas d'erreur soulevée par le bas niveau
 * @author pf
 *
 */

public class RequeteSTM implements Service {

	private volatile RequeteType type;
	protected Log log;
	
	public synchronized RequeteType getAndClear()
	{
		RequeteType out = type;
		type = null;
		return out;
	}
	
	public synchronized boolean isEmpty()
	{
		return type == null;
	}
	
	public synchronized void set(RequeteType type)
	{
		this.type = type;
		notifyAll();
	}
	
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
