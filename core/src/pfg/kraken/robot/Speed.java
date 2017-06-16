/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.robot;

/**
 * Définition des vitesses possibles de déplacement du robot.
 * 
 * @author pf
 *
 */

public interface Speed
{
	public double getMaxForwardSpeed(double curvature);
	
	public double getMaxBackwardSpeed(double curvature);	
}
