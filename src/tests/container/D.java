package tests.container;

import utils.Config;
import container.Service;

/**
 * Classe utilisée pour le test de l'appel de config
 * @author pf
 *
 */
public class D implements Service
{
	public boolean useConfigOk = false;
	public boolean updateConfigOk = false;
	
	public D()
	{}
	
	@Override
	public void updateConfig(Config config)
	{
		updateConfigOk = true;
	}

	@Override
	public void useConfig(Config config)
	{
		useConfigOk = true;
	}
}
