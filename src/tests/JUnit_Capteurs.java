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

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import capteurs.CapteursProcess;
import capteurs.SensorsData;
import robot.Cinematique;

/**
 * Tests unitaires pour les capteurs
 * @author pf
 *
 */

public class JUnit_Capteurs extends JUnit_Test {

	private CapteursProcess capteurs;
	
	@Override
	@Before
    public void setUp() throws Exception {
        super.setUp();
        capteurs = container.getService(CapteursProcess.class);
    }
	
	@Test
	public void test_correction() throws Exception
	{
    	Field f = CapteursProcess.class.getDeclaredField("bufferCorrection");
    	f.setAccessible(true);
    	Cinematique[] buffer = (Cinematique[]) f.get(capteurs);
    	int[] mesures = {100,100,100,100,100,100,100,100,100,100,100,100};
    	Cinematique cinematique = new Cinematique(1280,500,Math.PI/2+0.03,true,0,0);
    	Assert.assertTrue(buffer[0] == null);
    	capteurs.updateObstaclesMobiles(new SensorsData(mesures, cinematique));
    	Assert.assertTrue(buffer[0] != null);
    	Assert.assertTrue(Math.abs(buffer[0].orientationReelle + 0.03) < 0.001);
    	Assert.assertTrue(Math.abs(buffer[0].getPosition().getX() - 18) < 0.001);
    	Assert.assertTrue(Math.abs(buffer[0].getPosition().getY()) < 0.001);
	}
	
	@Test
	public void test_capteurs() throws Exception
	{
    	int[] mesures = {100,100,100,100,100,100,100,100,100,100,100,100};
    	Cinematique cinematique = new Cinematique(0,0,0,true,0,0);
    	capteurs.updateObstaclesMobiles(new SensorsData(mesures, cinematique));
	}

}
