/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.autoreplanning;

import java.util.List;
import pfg.kraken.struct.ItineraryPoint;

/**
 * A path diff
 * @author pf
 *
 */

public final class PathDiff
{
	public final int firstDifferentPoint;
	public final List<ItineraryPoint> diff;
	public final boolean isComplete;
	
	public PathDiff(int firstDifferentPoint, List<ItineraryPoint> diff, boolean isComplete)
	{
		this.firstDifferentPoint = firstDifferentPoint;
		this.diff = diff;
		this.isComplete = isComplete;
	}
}