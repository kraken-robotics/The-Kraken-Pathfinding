/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.display;

import java.awt.Graphics;
import java.io.Serializable;

/**
 * Élément affichable
 * 
 * @author pf
 *
 */

public interface Printable extends Serializable
{
	/**
	 * Print that object
	 * 
	 * @param g
	 */
	public void print(Graphics g, Display f);
}
