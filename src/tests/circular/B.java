package tests.circular;

import utils.Config;
import container.Service;

/**
 * Classe utilisée pour le test de dépendance circulaire
 * @author pf
 *
 */
public class B implements Service
{
	public B(A a)
	{}
	
	@Override
	public void updateConfig(Config config) {}

	@Override
	public void useConfig(Config config) throws InterruptedException {}
}
