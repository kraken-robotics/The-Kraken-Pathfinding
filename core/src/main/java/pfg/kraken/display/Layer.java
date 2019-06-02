/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.display;

/**
 * Les couches d'affichage
 * 
 * @author pf
 *
 */

public enum Layer
{
	// L'affichage se fait du premier au dernier (l'ordre est donc important)
	IMAGE_BACKGROUND,
	BACKGROUND,
	MIDDLE,
	FOREGROUND;
	
	public final int layer = 100*ordinal();
}
