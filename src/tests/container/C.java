package tests.container;

import utils.Config;
import container.Service;

/**
 * Classe utilisée pour le test de plusieurs constructeurs
 * @author pf
 *
 */
public class C implements Service
{
	public C(B b)
	{}

	public C(A a)
	{}
	
	@Override
	public void updateConfig(Config config) {}

	@Override
	public void useConfig(Config config) {}
}
