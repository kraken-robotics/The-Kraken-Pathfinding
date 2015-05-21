package strategie;

import utils.Config;
import container.Service;

/**
 * Juste un objet notifié par Table, GridSpace, ... afin de dire à la stratégie que les choses ont changé.
 * @author pf
 *
 */

public class StrategieNotifieur implements Service
{
	@Override
	public void updateConfig(Config config)
	{}
	
	@Override
	public void useConfig(Config config)
	{}
}