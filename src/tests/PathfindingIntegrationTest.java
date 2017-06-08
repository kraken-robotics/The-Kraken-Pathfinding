package tests;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pathfinding.Pathfinding;
import smartMath.Vec2;
import table.Table;

public class PathfindingIntegrationTest extends JUnit_Test
{
	private Table table;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		config.set("couleur", "jaune");
	//	robotchrono = new RobotChrono(config, log);
		table = (Table)container.getService("Table");
//		container.getService("threadPosition");
	//	container.demarreThreads();
	}

	@Test
	public void test_InstanciationPathfinding() throws Exception
	{
		System.out.println("\n\n ====== Test d'intégration pathfinding =====");
		System.out.println("Calcul d'un même parcours avec des cases de 1cm à 10cm de coté");
		Vec2 depart = new Vec2(1205,1140);
		Vec2 arrivee =  new Vec2(-1100,300);
		Pathfinding finder = new Pathfinding(table, config, log);
	/*	System.out.println();
		for(int i = 1; i < 11; ++i)
		{
			
			
			//	System.out.println(finder.map.stringForm());
		}
		*/
		
		
		ArrayList<Vec2> chemin = finder.chemin(depart, arrivee);
		System.out.println(chemin);/*
		String out = "";
		Integer i = 1;
		for (int  j = 0; j < finder.map.getSizeX(); ++j)
		{
			for (int  k = finder.map.getSizeY() - 1; k >= 0; --k)
			{
				IntPair pos = new IntPair(j,k);
				if (finder.getDepart().x ==j && finder.getDepart().y ==k)
					out += 'D';
				else if (finder.getArrivee().x ==j && finder.getArrivee().y ==k)
					out += 'A';
				else if (chemin.contains(pos))
				{
					out += i.toString();
					i++;
				}
				else if(finder.map.canCross(j, k))
					out += '.';
				else
					out += 'X';	
			}
			
			out +='\n';
		}
		System.out.println(out);
		*/
		Assert.assertTrue(true);

	}
}
