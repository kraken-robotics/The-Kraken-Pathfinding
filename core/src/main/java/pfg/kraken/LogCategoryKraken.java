/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import pfg.graphic.log.LogCategory;

/**
 * The log categories of Kraken
 * @author pf
 *
 */

public enum LogCategoryKraken implements LogCategory
{
	PF,
	REPLANIF,
	TEST;

	@Override
	public int getMask()
	{
		return 1 << ordinal();
	}
	
	
}
