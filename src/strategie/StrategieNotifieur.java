package strategie;

import container.Service;

/**
 * Juste un objet notifié par Table, GridSpace, ... afin de dire à la stratégie que les choses ont changés.
 * @author pf
 *
 */

public class StrategieNotifieur implements Service
{
	@Override
	public void updateConfig()
	{}
}