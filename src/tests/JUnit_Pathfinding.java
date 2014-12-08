package tests;

import org.junit.Before;
import org.junit.Test;

import pathfinding.GridSpace;
import pathfinding.Pathfinding;
import smartMath.Vec2;
import table.Table;
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
    }
	
	@Test
    public void test_element_jeu_disparu() throws Exception
    {
    	// une fois ce verre pris, le chemin est libre
    	table.setVerreDone(2);
    	pathfinding.computePath(PathfindingNodes.BAS_GAUCHE.getCoordonnees(), PathfindingNodes.COTE_MARCHE_GAUCHE, gridspace, false, false);
    }

	@Test(expected=PathfindingException.class)
    public void test_element_jeu_disparu_2() throws Exception
    {
    	pathfinding.computePath(PathfindingNodes.BAS_GAUCHE.getCoordonnees(), PathfindingNodes.COTE_MARCHE_GAUCHE, gridspace, false, false);
    }
	
	@Test
    public void test_element_jeu_disparu_3() throws Exception
    {
    	pathfinding.computePath(PathfindingNodes.BAS_GAUCHE.getCoordonnees(), PathfindingNodes.COTE_MARCHE_GAUCHE, gridspace, false, true);
    }

}
