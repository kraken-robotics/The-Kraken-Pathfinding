/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import pfg.log.Severity;

/**
 * The severity category of Kraken
 * @author pf
 *
 */

public enum SeverityCategoryKraken implements Severity
{
	INFO(false), WARNING(true), CRITICAL(true);

	public final boolean always;
	
	private SeverityCategoryKraken(boolean always)
	{
		this.always = always;
	}
	
	@Override
	public boolean alwaysPrint() {
		return always;
	}
}
