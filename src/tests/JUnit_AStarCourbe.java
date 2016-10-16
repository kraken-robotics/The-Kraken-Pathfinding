/*
Copyright (C) 2016 Pierre-François Gimenez

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

import obstacles.types.ObstacleCircular;
import obstacles.types.ObstacleRectangular;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import config.ConfigInfo;
import graphic.PrintBuffer;
import pathfinding.astarCourbe.AStarCourbe;
import pathfinding.astarCourbe.arcs.ArcCourbe;
import pathfinding.astarCourbe.arcs.ArcCourbeClotho;
import pathfinding.astarCourbe.arcs.ArcCourbeCubique;
import pathfinding.astarCourbe.arcs.ClothoidesComputer;
import pathfinding.astarCourbe.arcs.VitesseCourbure;
import pathfinding.chemin.IteratorCheminPathfinding;
import robot.Cinematique;
import robot.DirectionStrategy;
import robot.RobotChrono;
import robot.RobotReal;
import robot.Speed;
import utils.Vec2RO;

/**
 * Tests unitaires de la recherche de chemin courbe
 * @author pf
 *
 */

public class JUnit_AStarCourbe extends JUnit_Test {

	private AStarCourbe astar;
	private ClothoidesComputer clotho;
	private PrintBuffer buffer;
	private RobotReal robot;
	private IteratorCheminPathfinding iterator;
	private boolean graphicTrajectory;
	
	@Override
	@Before
    public void setUp() throws Exception {
        super.setUp();
		clotho = container.getService(ClothoidesComputer.class);
		buffer = container.getService(PrintBuffer.class);
		astar = container.getService(AStarCourbe.class);
		robot = container.getService(RobotReal.class);
		iterator = container.make(IteratorCheminPathfinding.class);
		graphicTrajectory = false;
	}

	@Test
    public void test_interpolation_cubique() throws Exception
    {		
		Cinematique c1 = new Cinematique(-1000, 1500, Math.PI/4, true, 0, Speed.STANDARD);
		Cinematique c2 = new Cinematique(0, 1000, 0, true, 0, Speed.STANDARD);
		
		ArcCourbeCubique arccubique = clotho.cubicInterpolation(container.make(RobotChrono.class), c1, c2, Speed.STANDARD, VitesseCourbure.DIRECT_COURBE);
	
		for(int i = 0; i < arccubique.arcs.size(); i++)
		{
			// Vérification de l'orientation
			if(i > 0)
				Assert.assertEquals(Math.atan2(
						arccubique.arcs.get(i).getPosition().getY() - arccubique.arcs.get(i-1).getPosition().getY(),
						arccubique.arcs.get(i).getPosition().getX() - arccubique.arcs.get(i-1).getPosition().getX()), arccubique.arcs.get(i).orientationGeometrique, 0.5);
			
			// Vérification de la courbure
/*			if(i > 1)
			{
				double x1 = arc.get(i).getPosition().x;
				double x2 = arc.get(i-1).getPosition().x;
				double x3 = arc.get(i-2).getPosition().x;
				double y1 = arc.get(i).getPosition().y;
				double y2 = arc.get(i-1).getPosition().y;
				double y3 = arc.get(i-2).getPosition().y;
				
				double xc = ((x3*x3 - x2*x2 + y3*y3 - y2*y2)/(2*(y3-y2)) - (x2*x2 - x1*x1 + y2*y2 - y1*y1) / (2*(y2 - y1)))
						/ ((x2 - x1) / (y2 - y1) - (x3 - x2) / (y3 - y2));
				double yc = -(x2-x1)/(y2-y1)*xc + (x2*x2 - x1*x1 + y2*y2 - y1*y1) / (2*(y2 - y1));
				log.debug(1000/Math.hypot(x1-xc, y1-yc)+" "+arc.get(i).courbure);
				Fenetre.getInstance().addObstacleEnBiais(new ObstacleRectangular(new Vec2RO(xc, yc), 30, 30, 0));
			}*/
			
			
			if(graphicTrajectory)
			{
				Thread.sleep(100);
				buffer.addSupprimable(new ObstacleRectangular(arccubique.arcs.get(i).getPosition(), 10, 10, 0));
			}
		}
		
    }
	
	@Test
    public void test_clotho() throws Exception
    {
		boolean graphicTrajectory = config.getBoolean(ConfigInfo.GRAPHIC_TRAJECTORY);
		ClothoidesComputer clotho = container.getService(ClothoidesComputer.class);
		RobotChrono r = container.make(RobotChrono.class);
		int nbArc = 16;
		ArcCourbeClotho arc[] = new ArcCourbeClotho[nbArc];
		for(int i = 0; i < nbArc; i++)
			arc[i] = new ArcCourbeClotho();

		Cinematique c = new Cinematique(0, 1000, Math.PI/2, false, 0, Speed.STANDARD);
		log.debug("Initial : "+c);
		clotho.getTrajectoire(r, c, VitesseCourbure.COURBURE_IDENTIQUE, Speed.STANDARD, arc[0]);
		clotho.getTrajectoire(r, arc[0], VitesseCourbure.GAUCHE_3, Speed.STANDARD, arc[1]);
		clotho.getTrajectoire(r, arc[1], VitesseCourbure.COURBURE_IDENTIQUE, Speed.STANDARD, arc[2]);
		clotho.getTrajectoire(r, arc[2], VitesseCourbure.GAUCHE_1, Speed.STANDARD, arc[3]);
		clotho.getTrajectoire(r, arc[3], VitesseCourbure.COURBURE_IDENTIQUE, Speed.STANDARD, arc[4]);
		clotho.getTrajectoire(r, arc[4], VitesseCourbure.COURBURE_IDENTIQUE, Speed.STANDARD, arc[5]);
		clotho.getTrajectoire(r, arc[5], VitesseCourbure.GAUCHE_3_REBROUSSE, Speed.STANDARD, arc[6]);
		clotho.getTrajectoire(r, arc[6], VitesseCourbure.GAUCHE_1, Speed.STANDARD, arc[7]);
		clotho.getTrajectoire(r, arc[7], VitesseCourbure.GAUCHE_3, Speed.STANDARD, arc[8]);
		clotho.getTrajectoire(r, arc[8], VitesseCourbure.DROITE_3_REBROUSSE, Speed.STANDARD, arc[9]);
		clotho.getTrajectoire(r, arc[9], VitesseCourbure.DROITE_1, Speed.STANDARD, arc[10]);
		clotho.getTrajectoire(r, arc[10], VitesseCourbure.DROITE_1, Speed.STANDARD, arc[11]);
		clotho.getTrajectoire(r, arc[11], VitesseCourbure.DROITE_1, Speed.STANDARD, arc[12]);
		clotho.getTrajectoire(r, arc[12], VitesseCourbure.DROITE_1, Speed.STANDARD, arc[13]);
		clotho.getTrajectoire(r, arc[13], VitesseCourbure.DROITE_1, Speed.STANDARD, arc[14]);
		clotho.getTrajectoire(r, arc[14], VitesseCourbure.GAUCHE_3, Speed.STANDARD, arc[15]);
	
		for(int a = 0; a < nbArc; a++)	
		{
//			System.out.println("arc "+arc[a].v+" avec "+arc[a].arcselems[0]);
			for(int i = 0; i < ClothoidesComputer.NB_POINTS; i++)
			{
/*				if(i > 0)
					System.out.println(arc[a].arcselems[i-1].point.distance(arc[a].arcselems[i].point));
				else if(a > 0)
					System.out.println(arc[a-1].arcselems[ClothoidesComputer.NB_POINTS - 1].point.distance(arc[a].arcselems[0].point));
	*/				
				System.out.println(arc[a].arcselems[i]);
				if(graphicTrajectory)
					for(int j = 0; j < arc[a].getNbPoints(); j++)
						buffer.addSupprimable(new ObstacleCircular(arc[a].getPoint(i).getPosition(), 4));
			}
			if(a == 0)
			{
				Assert.assertEquals(arc[0].arcselems[ClothoidesComputer.NB_POINTS - 1].getPositionEcriture().getX(), 0, 0.1);
				Assert.assertEquals(arc[0].arcselems[ClothoidesComputer.NB_POINTS - 1].getPositionEcriture().getY(), 1000+(int)ClothoidesComputer.DISTANCE_ARC_COURBE, 0.1);
			}
/*			else if(arc[a].arcselems[0].enMarcheAvant != arc[a-1].arcselems[0].enMarcheAvant)
				Assert.assertEquals(arc[a].vitesseCourbure.vitesse / 1000. * ClothoidesComputer.DISTANCE_ARC_COURBE, arc[a].arcselems[ClothoidesComputer.NB_POINTS-1].courbure, 0.1);
			else if(a > 0)
				Assert.assertEquals(arc[a].vitesseCourbure.vitesse / 1000. * ClothoidesComputer.DISTANCE_ARC_COURBE + arc[a-1].arcselems[ClothoidesComputer.NB_POINTS-1].courbure, arc[a].arcselems[ClothoidesComputer.NB_POINTS-1].courbure, 0.1);
*/		}
	
		
		Assert.assertEquals(arc[nbArc-1].arcselems[arc[nbArc-1].arcselems.length - 1].getPosition().distance(new Vec2RO(-22.769859459053365,1875.782736417656)), 0, 0.1);
    }
	
	@Test
    public void test_recherche() throws Exception
    {
		Cinematique depart = new Cinematique(100, 1200, 0, true, 3, Speed.STANDARD);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(0, 1200, Math.PI, false, 0, Speed.STANDARD);
		astar.computeNewPath(c, true, DirectionStrategy.FASTEST);
		iterator.reinit();
		ArcCourbe a = null;
		while(iterator.hasNext())
		{
			a = iterator.next();
			for(int i = 0; i < a.getNbPoints(); i++)
			{
//				log.debug(a.getPoint(i));
				robot.setCinematique(a.getPoint(i));
				Thread.sleep(100);
			}
		}
		Assert.assertTrue(a instanceof ArcCourbeCubique);
    }
	
}
