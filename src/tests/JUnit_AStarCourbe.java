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
import obstacles.types.ObstacleRobot;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import config.ConfigInfo;
import graphic.PrintBuffer;
import pathfinding.DirectionStrategy;
import pathfinding.SensFinal;
import pathfinding.astar.AStarCourbe;
import pathfinding.astar.arcs.ArcCourbeStatique;
import pathfinding.astar.arcs.ArcCourbeDynamique;
import pathfinding.astar.arcs.ClothoidesComputer;
import pathfinding.astar.arcs.VitesseCourbure;
import pathfinding.chemin.CheminPathfinding;
import pathfinding.chemin.IteratorCheminPathfinding;
import pathfinding.dstarlite.gridspace.GridSpace;
import robot.Cinematique;
import robot.CinematiqueObs;
import robot.RobotReal;
import robot.Speed;
import threads.ThreadName;
import threads.ThreadPathfinding;
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
	private CheminPathfinding chemin;
	private GridSpace gridspace;
//	private DStarLite dstarlite;

	@Override
	@Before
    public void setUp() throws Exception {
        super.setUp();
		clotho = container.getService(ClothoidesComputer.class);
		buffer = container.getService(PrintBuffer.class);
		astar = container.getService(AStarCourbe.class);
//		dstarlite = container.getService(DStarLite.class);
		robot = container.getService(RobotReal.class);
		chemin = container.getService(CheminPathfinding.class);
		iterator = container.make(IteratorCheminPathfinding.class);
		gridspace = container.getService(GridSpace.class);
		graphicTrajectory = config.getBoolean(ConfigInfo.GRAPHIC_TRAJECTORY_FINAL);
	}

	@Test
    public void test_interpolation_cubique() throws Exception
    {		
		Cinematique c1 = new Cinematique(-1100, 300, Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		Cinematique c2 = new Cinematique(0, 1000, 0, true, 0, Speed.STANDARD.translationalSpeed);
		
		ArcCourbeDynamique arccubique = clotho.cubicInterpolation(c1, c2, Speed.STANDARD, VitesseCourbure.DIRECT_COURBE);
	
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
				buffer.addSupprimable(new ObstacleRectangular(arccubique.arcs.get(i).getPosition(), 20, 20, 0));
				log.debug(arccubique.arcs.get(i).getPosition());
			}
		}
		
    }
	
	@Test
    public void test_cubique() throws Exception
    {
		boolean graphicTrajectory = config.getBoolean(ConfigInfo.GRAPHIC_TRAJECTORY);
		ClothoidesComputer clotho = container.getService(ClothoidesComputer.class);
		ArcCourbeDynamique arc;
		Cinematique c, c2;
		
		c = new Cinematique(0, 1000, -Math.PI/4, true, 0, Speed.STANDARD.translationalSpeed);
		c2 = new Cinematique(1000, 700, 0, false, 0, Speed.STANDARD.translationalSpeed);
		arc = clotho.cubicInterpolation(c, c2, Speed.STANDARD, VitesseCourbure.DIRECT_COURBE);
		if(graphicTrajectory)
			for(int i = 0; i < arc.getNbPoints(); i++)
				buffer.addSupprimable(new ObstacleCircular(arc.getPoint(i).getPosition(), 4));
		Assert.assertTrue(arc.getLast().enMarcheAvant);

		c = new Cinematique(0, 1000, Math.PI, true, 0, Speed.STANDARD.translationalSpeed);
		c2 = new Cinematique(1000, 700, 0, false, 0, Speed.STANDARD.translationalSpeed);
		arc = clotho.cubicInterpolation(c, c2, Speed.STANDARD, VitesseCourbure.DIRECT_COURBE_REBROUSSE);
		if(graphicTrajectory)
			for(int i = 0; i < arc.getNbPoints(); i++)
				buffer.addSupprimable(new ObstacleCircular(arc.getPoint(i).getPosition(), 4));
		Assert.assertTrue(!arc.getLast().enMarcheAvant);

		c = new Cinematique(100, 1100, -Math.PI/4, false, 0, Speed.STANDARD.translationalSpeed);
		c2 = new Cinematique(1000, 700, 0, false, 0, Speed.STANDARD.translationalSpeed);
		arc = clotho.cubicInterpolation(c, c2, Speed.STANDARD, VitesseCourbure.DIRECT_COURBE);
		if(graphicTrajectory)
			for(int i = 0; i < arc.getNbPoints(); i++)
				buffer.addSupprimable(new ObstacleCircular(arc.getPoint(i).getPosition(), 4));
		Assert.assertTrue(!arc.getLast().enMarcheAvant);

		c = new Cinematique(100, 1100, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		c2 = new Cinematique(1000, 700, 0, false, 0, Speed.STANDARD.translationalSpeed);
		arc = clotho.cubicInterpolation(c, c2, Speed.STANDARD, VitesseCourbure.DIRECT_COURBE_REBROUSSE);
		if(graphicTrajectory)
			for(int i = 0; i < arc.getNbPoints(); i++)
				buffer.addSupprimable(new ObstacleCircular(arc.getPoint(i).getPosition(), 4));
		Assert.assertTrue(arc.getLast().enMarcheAvant);

		c = new Cinematique(503, 884, -0.29, true, 0, Speed.STANDARD.translationalSpeed);
		c2 = new Cinematique(1000, 700, 0, false, 0, Speed.STANDARD.translationalSpeed);
		Assert.assertTrue(clotho.cubicInterpolation(c, c2, Speed.STANDARD, VitesseCourbure.DIRECT_COURBE_REBROUSSE) == null);
    }
	
	@Test
    public void test_clotho() throws Exception
    {
		boolean graphicTrajectory = config.getBoolean(ConfigInfo.GRAPHIC_TRAJECTORY);
		ClothoidesComputer clotho = container.getService(ClothoidesComputer.class);
		RobotReal rr = container.getService(RobotReal.class);
		int nbArc = 16;
		ArcCourbeStatique arc[] = new ArcCourbeStatique[nbArc];
		for(int i = 0; i < nbArc; i++)
			arc[i] = new ArcCourbeStatique(rr);

		Cinematique c = new Cinematique(0, 1000, Math.PI/2, false, 0, Speed.STANDARD.translationalSpeed);
		log.debug("Initial : "+c);
		clotho.getTrajectoire(c, VitesseCourbure.COURBURE_IDENTIQUE, Speed.STANDARD, arc[0]);
		clotho.getTrajectoire(arc[0], VitesseCourbure.GAUCHE_3, Speed.STANDARD, arc[1]);
		clotho.getTrajectoire(arc[1], VitesseCourbure.COURBURE_IDENTIQUE, Speed.STANDARD, arc[2]);
		clotho.getTrajectoire(arc[2], VitesseCourbure.GAUCHE_1, Speed.STANDARD, arc[3]);
		clotho.getTrajectoire(arc[3], VitesseCourbure.COURBURE_IDENTIQUE, Speed.STANDARD, arc[4]);
		clotho.getTrajectoire(arc[4], VitesseCourbure.COURBURE_IDENTIQUE, Speed.STANDARD, arc[5]);
		clotho.getTrajectoire(arc[5], VitesseCourbure.GAUCHE_3_REBROUSSE, Speed.STANDARD, arc[6]);
		clotho.getTrajectoire(arc[6], VitesseCourbure.GAUCHE_1, Speed.STANDARD, arc[7]);
		clotho.getTrajectoire(arc[7], VitesseCourbure.GAUCHE_3, Speed.STANDARD, arc[8]);
		clotho.getTrajectoire(arc[8], VitesseCourbure.DROITE_3_REBROUSSE, Speed.STANDARD, arc[9]);
		clotho.getTrajectoire(arc[9], VitesseCourbure.DROITE_1, Speed.STANDARD, arc[10]);
		clotho.getTrajectoire(arc[10], VitesseCourbure.DROITE_1, Speed.STANDARD, arc[11]);
		clotho.getTrajectoire(arc[11], VitesseCourbure.DROITE_1, Speed.STANDARD, arc[12]);
		clotho.getTrajectoire(arc[12], VitesseCourbure.DROITE_1, Speed.STANDARD, arc[13]);
		clotho.getTrajectoire(arc[13], VitesseCourbure.DROITE_1, Speed.STANDARD, arc[14]);
		clotho.getTrajectoire(arc[14], VitesseCourbure.GAUCHE_3, Speed.STANDARD, arc[15]);

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
				System.out.println(a+" "+i+" "+arc[a].arcselems[i]);
				if(graphicTrajectory)
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
	
		
		Assert.assertEquals(0, arc[nbArc-1].arcselems[arc[nbArc-1].arcselems.length - 1].getPosition().distance(new Vec2RO(-204.49063476285147,1699.4301324023606)), 0.1);
    }
	/*
	@Test
    public void test_bench() throws Exception
    {
		int nbmax = 10000;
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-1100, 600, 0, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(0, 1200, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		for(int i = 0; i < nbmax; i++)
		{
			astar.computeNewPath(c, false);
			chemin.clear();
		}
		log.debug("Temps : "+(System.nanoTime() - avant) / (nbmax * 1000000.));
    }
*/
	@Test
    public void test_recherche_manoeuvre() throws Exception
    {
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-950, 650, 0, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(0, 1200, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.computeNewPath(c, false);
		log.debug("Temps : "+(System.nanoTime() - avant) / (1000000.));
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : "+i);
    }
	
	@Test
    public void test_recherche_shoot() throws Exception
    {
		Cinematique depart = new Cinematique(0, 1800, -Math.PI/3, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(1000, 1200, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.computeNewPath(c, true);
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : "+i);
    }
	
	@Test
    public void test_recherche_shoot_pas() throws Exception
    {
		Cinematique depart = new Cinematique(0, 1800, -Math.PI/3, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(1000, 1200, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.computeNewPath(c, false);
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : "+i);
    }
	
	@Test
    public void test_replanif() throws Exception
    {
		// Ce test impose l'arrêt du thread de pathfinding pour ne pas avoir d'interférence
		ThreadPathfinding thread = container.getService(ThreadPathfinding.class);
		thread.interrupt();
		thread.join(1000);
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-800, 300, Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(0, 1600, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.computeNewPath(c, false);
		log.debug("Temps : "+(System.nanoTime() - avant) / (1000000.));
		iterator.reinit();
		CinematiqueObs a = null;
		int n = 10;
		while(iterator.hasNext() && iterator.getIndex() < n)
		{
			a = iterator.next();
			chemin.setCurrentIndex(iterator.getIndex());
			log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		gridspace.addObstacleAndRemoveNearbyObstacles(new Vec2RO(-400, 1300));
//		gridspace.addObstacleAndRemoveNearbyObstacles(new Vec2RO(0, 1600)); // TODO
		chemin.checkColliding();
		avant = System.nanoTime();
		astar.updatePath(true);
		log.debug("Temps recalcul : "+(System.nanoTime() - avant) / (1000000.));
		iterator.reinit();
		while(iterator.hasNext())
		{
			a = iterator.next();
			chemin.setCurrentIndex(iterator.getIndex());
			log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		container.restartThread(ThreadName.PATHFINDING); // pour pas qu'il soit mécontent
	}
	
	@Test
    public void test_recherche_finit_en_arriere() throws Exception
    {
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-800, 350, Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(1000, 700, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.computeNewPath(c, SensFinal.MARCHE_ARRIERE, false);
		log.debug("Temps : "+(System.nanoTime() - avant) / (1000000.));
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : "+i);
	}
	
	@Test
    public void test_recherche_finit_en_arriere3() throws Exception
    {
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-800, 550, -Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(1000, 700, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.computeNewPath(c, SensFinal.MARCHE_ARRIERE, false);
		log.debug("Temps : "+(System.nanoTime() - avant) / (1000000.));
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : "+i);
	}
	
	@Test
    public void test_recherche_finit_en_arriere2() throws Exception
    {
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-800, 800, Math.PI/4, true, 0, Speed.STANDARD.translationalSpeed);
//		Cinematique depart = new Cinematique(1000, 1000, 0, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(1100, 400, -Math.PI/4, false, 0, Speed.STANDARD.translationalSpeed);
		astar.computeNewPath(c, SensFinal.MARCHE_ARRIERE, true);
		log.debug("Temps : "+(System.nanoTime() - avant) / (1000000.));
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : "+i);
	}

	@Test
    public void test_recherche_finit_en_avant() throws Exception
    {
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-400, 1200, Math.PI, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(300, 1200, 0, false, 0, Speed.STANDARD.translationalSpeed);
		astar.computeNewPath(c, SensFinal.MARCHE_AVANT, false);
		log.debug("Temps : "+(System.nanoTime() - avant) / (1000000.));
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : "+i);
	}
	
	@Test
    public void test_recherche_loin() throws Exception
    {
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-800, 350, 3*Math.PI/4, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(900, 600, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.computeNewPath(c, true);
		log.debug("Temps : "+(System.nanoTime() - avant) / (1000000.));
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : "+i);
	}

	@Test
    public void test_recherche_loin_arriere() throws Exception
    {
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-800, 400, -Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(800, 700, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.computeNewPath(c, true);
		log.debug("Temps : "+(System.nanoTime() - avant) / (1000000.));
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : "+i);
	}

}
