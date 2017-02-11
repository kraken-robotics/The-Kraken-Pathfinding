/*
Copyright (C) 2013-2017 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package tests.lowlevel;

import org.junit.Before;
import org.junit.Test;

import robot.RobotReal;
import tests.JUnit_Test;

/**
 * Tests unitaires des actionneurs
 * @author pf
 *
 */

public class JUnit_Actionneurs extends JUnit_Test {

	private RobotReal robot;
	
	@Override
	@Before
    public void setUp() throws Exception {
        super.setUp();
        robot = container.getService(RobotReal.class);
	}
	
	@Test
	public void test_actionneurs() throws Exception
	{
		robot.ejectBalles();
		robot.baisseFilet();
		robot.ouvreFilet();
	}
}
