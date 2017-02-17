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

package tests;

import org.junit.Before;
import org.junit.Test;

import scripts.Strategie;

/**
 * Tests unitaires pour les capteurs
 * @author pf
 *
 */

public class JUnit_Strategie extends JUnit_Test {

	private Strategie strat;
	
	@Override
	@Before
    public void setUp() throws Exception {
        super.setUp();
        strat = container.getService(Strategie.class);
    }
	
	@Test
	public void test_strat() throws Exception
	{
		strat.doWinMatch();
	}

}
