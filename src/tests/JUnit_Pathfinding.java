package tests;

import org.junit.Before;
import org.junit.Test;

import pathfinding.GridSpace;
import pathfinding.Pathfinding;
import smartMath.Vec2;
import table.Table;
import enums.GameElementNames;
import enums.PathfindingNodes;
import enums.ServiceNames;
import exceptions.PathfindingException;
import exceptions.PathfindingRobotInObstacleException;

/**
 * Tests unitaires de la recherche de chemin.
 * @author pf
 *
 */

public class JUnit_Pathfinding extends JUnit_Test {

	private Pathfinding pathfinding;
	private GridSpace gridspace;
	private Table table;
	
	@Before
    public void setUp() throws Exception {
        super.setUp();
        pathfinding = (Pathfinding) container.getService(ServiceNames.PATHFINDING);
		gridspace = (GridSpace) container.getService(ServiceNames.GRID_SPACE);
		table = (Table) container.getService(ServiceNames.TABLE);
    }

	@Test(expected=PathfindingRobotInObstacleException.class)
    public void test_robot_dans_obstacle() throws Exception
    {
    	gridspace.creer_obstacle(new Vec2(80, 80));
    	pathfinding.computePath(new Vec2(80, 80), PathfindingNodes.values()[0], gridspace, false, false);
    }

	@Test(expected=PathfindingException.class)
    public void test_obstacle() throws Exception
    {
    	gridspace.creer_obstacle(PathfindingNodes.values()[0].getCoordonnees());
    	pathfinding.computePath(new Vec2(80, 80), PathfindingNodes.values()[0], gridspace, false, false);
    }

	@Test
    public void test_brute_force() throws Exception
    {
    	for(PathfindingNodes i: PathfindingNodes.values())
        	for(PathfindingNodes j: PathfindingNodes.values())
        		if(!j.is_an_emergency_point()) // on n'arrive jamais dans un emergency point
        		{
        			pathfinding.computePath(i.getCoordonnees(), j, gridspace, false, true);
        			pathfinding.computePath(i.getCoordonnees(), j, gridspace, true, true);
        		}
		for(PathfindingNodes n: PathfindingNodes.values())
			log.debug("Nous sommes passé "+n.getNbUse()+" fois par "+n+" "+n.getCoordonnees(), this);
    }
	
	@Test
    public void test_element_jeu_disparu() throws Exception
    {
    	// une fois ce verre pris, le chemin est libre
    	table.setDone(GameElementNames.VERRE_3);
    	pathfinding.computePath(PathfindingNodes.BAS_GAUCHE.getCoordonnees(), PathfindingNodes.COTE_MARCHE_GAUCHE, gridspace, false, false);
    }

	@Test(expected=PathfindingException.class)
    public void test_element_jeu_disparu_2() throws Exception
    {
		// Exception car il y a un verre sur le passage
    	pathfinding.computePath(PathfindingNodes.BAS_GAUCHE.getCoordonnees(), PathfindingNodes.COTE_MARCHE_GAUCHE, gridspace, false, false);
    }
	
	@Test
    public void test_element_jeu_disparu_3() throws Exception
    {
		// Pas d'exception car on demande au pathfinding de passer sur les éléments de jeux.
    	pathfinding.computePath(PathfindingNodes.BAS_GAUCHE.getCoordonnees(), PathfindingNodes.COTE_MARCHE_GAUCHE, gridspace, false, true);
    }

}
