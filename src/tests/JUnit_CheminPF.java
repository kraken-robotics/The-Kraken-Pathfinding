/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package tests;

import java.util.LinkedList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import config.ConfigInfo;
import pathfinding.PFInstruction;
import pathfinding.chemin.CheminPathfinding;
import pathfinding.chemin.IteratorCheminPathfinding;
import robot.CinematiqueObs;

/**
 * Tests unitaires pour le chemin pathfinding
 * 
 * @author pf
 *
 */

public class JUnit_CheminPF extends JUnit_Test
{

	private CheminPathfinding chemin;
	private IteratorCheminPathfinding iterator;
	private PFInstruction inst;

	@Override
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		chemin = container.getService(CheminPathfinding.class);
		iterator = new IteratorCheminPathfinding(chemin);
		inst = container.getService(PFInstruction.class);
	}

	@Test
	public void test_iterator() throws Exception
	{
		iterator.reinit();
		Assert.assertEquals(255, iterator.getIndex());
		Assert.assertFalse(iterator.hasNext());
		LinkedList<CinematiqueObs> l = new LinkedList<CinematiqueObs>();
		int demieLargeurNonDeploye = config.getInt(ConfigInfo.LARGEUR_NON_DEPLOYE) / 2;
		int demieLongueurArriere = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE);
		int demieLongueurAvant = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_AVANT);
		int marge = config.getInt(ConfigInfo.DILATATION_OBSTACLE_ROBOT);

		Assert.assertTrue(!iterator.hasNext());
		l.add(new CinematiqueObs(demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant, marge));
		chemin.addToEnd(l);
		Assert.assertTrue(iterator.hasNext());
		chemin.clear();
		Assert.assertTrue(!iterator.hasNext());
	}

	@Test
	public void pathfinding_instruction() throws Exception
	{
		synchronized(inst)
		{
			Assert.assertTrue(!inst.isDone());
			Assert.assertTrue(!inst.isSearching());
			Assert.assertTrue(!inst.hasRequest());

			inst.searchRequest();
			Assert.assertTrue(!inst.isDone());
			Assert.assertTrue(!inst.isSearching());
			Assert.assertTrue(inst.hasRequest());
			
			inst.beginSearch();
			Assert.assertTrue(!inst.isDone());
			Assert.assertTrue(inst.isSearching());
			Assert.assertTrue(!inst.hasRequest());
			
			inst.setDone();
			Assert.assertTrue(inst.isDone());
			Assert.assertTrue(!inst.isSearching());
			Assert.assertTrue(!inst.hasRequest());
			
			inst.throwException();
			Assert.assertTrue(!inst.isDone());
			Assert.assertTrue(!inst.isSearching());
			Assert.assertTrue(!inst.hasRequest());	
		}
	}
	
}
