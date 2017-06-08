package tests;

//import java.util.ArrayList;
//import java.util.Random;

import org.junit.Test;

//import pathfinding.AStar;
//import pathfinding.Pathfinding;
//import pathfinding.SearchSpace.Grid2DSpace;
//import smartMath.IntPair;
//import smartMath.Vec2;
import table.Table;

/**
 * 
 */

/**
 * @author karton
 *
 */
public class JUnit_SimpleAStarTest extends JUnit_Test
{
	Table table;

	@Test
	public void testPerf() throws Exception
	{/*
		Table table = (Table)container.getService("Table");
		
		int mapSizeX = 150;
		int mapSizeY = 100;


		Grid2DSpace map = new Grid2DSpace(new IntPair(mapSizeX,mapSizeY),table,200,log, config);
		
		//String mapStr = map.stringForm();
	    Random randomGenerator = new Random();
		
	    
	    // g�n�re une deamnde de chemin ou les cases de d�part et d'arriv�e sont valides.
		IntPair depart = new IntPair(randomGenerator.nextInt(map.getSizeX()),randomGenerator.nextInt(map.getSizeY()));
		IntPair arrivee = new IntPair(randomGenerator.nextInt(map.getSizeX()),randomGenerator.nextInt(map.getSizeY()));

		System.out.println("Calculating path...");
		
		
		
		
		
		// calcule le chemin
		//AStar solver = new AStar(map, depart, arrivee);
		AStar solver = new AStar(map, depart, arrivee);

		ArrayList<IntPair> cheminliss = new ArrayList<IntPair>();
		long duration = 0;
		long Lissduration = 0;
		int testNb = 1000;
		int validityCount = 0;
		Pathfinding pathfinder = new Pathfinding(table, config, log, 2);
		for (int i = 0; i< testNb; ++i)
		{
			 // g�n�re une deamnde de chemin ou les cases de départ et d'arrivée sont valides.
			depart = new IntPair(randomGenerator.nextInt(map.getSizeX()),randomGenerator.nextInt(map.getSizeY()));
			while (map.canCross(depart.x, depart.y) == false)
			{
				depart.x = randomGenerator.nextInt(map.getSizeX());
				depart.y = randomGenerator.nextInt(map.getSizeY()); 
			}
			
			arrivee = new IntPair(randomGenerator.nextInt(map.getSizeX()),randomGenerator.nextInt(map.getSizeY()));
			while (map.canCross(arrivee.x, arrivee.y) == false)
			{
				arrivee.x = randomGenerator.nextInt(map.getSizeX());
				arrivee.y = randomGenerator.nextInt(map.getSizeY()); 
			}
			
			solver = new AStar(map, depart, arrivee);

			long startTime = System.nanoTime();
			solver.process();
			long endTime = System.nanoTime();
			

			if(solver.isValid())
				validityCount++;

			duration += (endTime - startTime)/1000000;

			startTime = System.nanoTime();
			cheminliss = pathfinder.lissage(solver.getChemin(), map);
			endTime = System.nanoTime();
			Lissduration += (endTime - startTime)/1000;
			
		}

		System.out.println("Path done in " + duration/testNb + "ms on average over " + testNb + " random tests.");
		System.out.println("Smoothing done in " + Lissduration/testNb + "µs on average over " + testNb + " random tests.");

		System.out.println("Calculating output...");
		System.out.println("chemin size :" + solver.getChemin().size());
		System.out.println("chemin validity :" + (solver.isValid()));
		
		
		
		

		ArrayList<IntPair> chemin = solver.getChemin();
		String out = "";
		for (int  j = 0; j < mapSizeX; ++j)
		{
			for (int  k = mapSizeY - 1; k >= 0; --k)
			{
				IntPair pos = new IntPair(j,k);
				if (depart.x ==j && depart.y ==k)
					out += 'D';
				else if (arrivee.x ==j && arrivee.y ==k)
					out += 'A';
				else if (cheminliss.contains(pos))
					out += 'O';
				else if (chemin.contains(pos))
					out += '|';
				else if(map.canCross(j, k))
					out += '.';
				else
					out += 'X';	
			}
			
			out +='\n';
		}
		System.out.println(validityCount + " valid path");
		System.out.println(out);
	//	System.out.println("Legend : A, Arrivée, D, départ, ., on peut passser, X, obstacle, |, chemin prévu et O, sommet du chemin lissé");
		*/
	}
	

	@Test
	public void testArchi() throws Exception
	{/*
		int cmParCase = 2;
		int testNb = 1000;
		Table table = (Table)container.getService("Table");
		
		Pathfinding pathfinder = new Pathfinding(table, config, log, cmParCase);
		Grid2DSpace map = pathfinder.getMap();		
	    Random randomGenerator = new Random();
		
	    
	    // g�n�re une deamnde de chemin ou les cases de d�part et d'arriv�e sont valides.
	    IntPair depart = new IntPair(randomGenerator.nextInt(map.getSizeX()),randomGenerator.nextInt(map.getSizeY()));
	    IntPair arrivee = new IntPair(randomGenerator.nextInt(map.getSizeX()),randomGenerator.nextInt(map.getSizeY()));

		System.out.println("Calculating " + testNb + " path...");
		
		
		
		
		
		// calcule le chemin
		int validityCount = 0;
		for (int i = 0; i< testNb; ++i)
		{
			
			 // g�n�re une deamnde de chemin ou les cases de départ et d'arrivée sont valides.	
			depart.x = randomGenerator.nextInt(3000)-1500;
			depart.y = randomGenerator.nextInt(2000); 
			while (map.canCross((int)((float)(depart.x + 1500) / cmParCase /10), (int)((float)(depart.y) / cmParCase /10)) == false)
			{
				depart.x = randomGenerator.nextInt(3000)-1500;
				depart.y = randomGenerator.nextInt(2000); 
			}

			arrivee.x = randomGenerator.nextInt(3000)-1500;
			arrivee.y = randomGenerator.nextInt(2000); 
			while (map.canCross((int)((float)(arrivee.x + 1500) / cmParCase /10), (int)((float)(arrivee.y) / cmParCase /10)) == false)
			{
				arrivee.x = randomGenerator.nextInt(3000)-1500;
				arrivee.y = randomGenerator.nextInt(2000); 
			}*/
		/*	AStar solver = new AStar(map, 
									new IntPair((int)((float)(depart.x + 1500) / cmParCase /10), (int)((float)(depart.y) / cmParCase /10)), 
									new IntPair((int)((float)(arrivee.x + 1500) / cmParCase /10), (int)((float)(arrivee.y) / cmParCase /10)));
			

			solver.setDepart(		new IntPair((int)((float)(depart.x + 1500) / cmParCase /10), (int)((float)(depart.y) / cmParCase /10)));
			solver.setArrivee(		new IntPair((int)((float)(arrivee.x + 1500) / cmParCase /10), (int)((float)(arrivee.y) / cmParCase /10)));*/
	/*
	if (pathfinder.chemin(new Vec2(depart.x, depart.y), new Vec2(arrivee.x, arrivee.y)) != null)
				validityCount++;
		//	solver.process();
		//	if(solver.isValid())
			//	validityCount++;
			//pathfinder.lissage(solver.getChemin(), map);
			
		}
		System.out.println(validityCount + " valid path");
		System.out.println("Sucess");*/
	}
	
	@Test
	public void newArchitectureTest() throws Exception
	{/*
		Table table = (Table)container.getService("Table");
		
		int cmParCase = 2;
		int testNb = 1000;

		Vec2 depart = new Vec2(1205,1140);
		Vec2 arrivee =  new Vec2(-1100,300);
	    Random randomGenerator = new Random();
		Pathfinding finder = new Pathfinding(table, config, log, 2);
		System.out.println("Calculating " + testNb + " path...");
		
		for (int i = 0; i< testNb; ++i)
		{
		    // g�n�re une deamnde de chemin ou les cases de d�part et d'arriv�e sont valides.

			depart.x = randomGenerator.nextInt(3000)-1500;
			depart.y = randomGenerator.nextInt(2000); 
			while (finder.map.canCross((int)((float)(depart.x + 1500) / cmParCase /10), (int)((float)(depart.y) / cmParCase /10)) == false)
			{
				depart.x = randomGenerator.nextInt(3000)-1500;
				depart.y = randomGenerator.nextInt(2000); 
			}

			arrivee.x = randomGenerator.nextInt(3000)-1500;
			arrivee.y = randomGenerator.nextInt(2000); 
			while (finder.map.canCross((int)((float)(arrivee.x + 1500) / cmParCase /10), (int)((float)(arrivee.y) / cmParCase /10)) == false)
			{
				arrivee.x = randomGenerator.nextInt(3000)-1500;
				arrivee.y = randomGenerator.nextInt(2000); 
			}
			finder.chemin(depart, arrivee);
		}
		System.out.println("done");
		*/
	}
		
		

}
