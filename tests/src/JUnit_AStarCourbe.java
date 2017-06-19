/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

import java.util.Random;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import graphic.AbstractPrintBuffer;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.exceptions.PathfindingException;
import pfg.kraken.obstacles.types.CircularObstacle;
import pfg.kraken.astar.TentacularAStar;
import pfg.kraken.astar.tentacles.ArcCourbe;
import pfg.kraken.astar.tentacles.ArcCourbeDynamique;
import pfg.kraken.astar.tentacles.ArcCourbeStatique;
import pfg.kraken.astar.tentacles.BezierComputer;
import pfg.kraken.astar.tentacles.ClothoidesComputer;
import pfg.kraken.astar.tentacles.types.VitesseClotho;
import pfg.kraken.astar.tentacles.types.VitesseDemiTour;
import pfg.kraken.astar.tentacles.types.VitesseRameneVolant;
import pfg.kraken.chemin.DefaultCheminPathfinding;
import pfg.kraken.dstarlite.gridspace.GridSpace;
import pfg.kraken.robot.Cinematique;
import pfg.kraken.robot.CinematiqueObs;
import pfg.kraken.robot.Speed;
import pfg.kraken.utils.XY;

/**
 * Tests unitaires de la recherche de chemin courbe
 * 
 * @author pf
 *
 */

public class JUnit_TentacularAStar extends JUnit_Test
{

	private TentacularAStar astar;
	private ClothoidesComputer clotho;
	protected BezierComputer bezier;
	private AbstractPrintBuffer buffer;
	private boolean graphicTrajectory;
	private DefaultCheminPathfinding fakeChemin;
	private GridSpace gridspace;
	private CercleArrivee cercle;
	// private PrecomputedPaths prepaths;
	// private ArcManager arcmanager;
	// private DStarLite dstarlite;

	@Override
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		clotho = injector.getService(ClothoidesComputer.class);
		buffer = injector.getService(AbstractPrintBuffer.class);
		astar = injector.getService(TentacularAStar.class);
		// dstarlite = injector.getService(DStarLite.class);
		gridspace = injector.getService(GridSpace.class);
		bezier = injector.getService(BezierComputer.class);
		cercle = injector.getService(CercleArrivee.class);
		// arcmanager = injector.getService(ArcManager.class);
		graphicTrajectory = config.getBoolean(ConfigInfoKraken.GRAPHIC_TRAJECTORY_FINAL);
		fakeChemin = injector.getService(DefaultCheminPathfinding.class);
	}

	@Test
	public void test_clotho() throws Exception
	{
		int demieLargeurNonDeploye = config.getInt(ConfigInfoKraken.LARGEUR_NON_DEPLOYE) / 2;
		int demieLongueurArriere = config.getInt(ConfigInfoKraken.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE);
		int demieLongueurAvant = config.getInt(ConfigInfoKraken.DEMI_LONGUEUR_NON_DEPLOYE_AVANT);

		boolean graphicTrajectory = config.getBoolean(ConfigInfoKraken.GRAPHIC_TRAJECTORY);
		int nbArc = 16;
		ArcCourbeStatique arc[] = new ArcCourbeStatique[nbArc];
		for(int i = 0; i < nbArc; i++)
			arc[i] = new ArcCourbeStatique(demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant);

		Cinematique c = new Cinematique(0, 1000, Math.PI / 2, false, 0);
		log.debug("Initial : " + c);
		clotho.getTrajectoire(c, VitesseClotho.COURBURE_IDENTIQUE, arc[0]);
		clotho.getTrajectoire(arc[0], VitesseClotho.GAUCHE_2, arc[1]);
		clotho.getTrajectoire(arc[1], VitesseClotho.COURBURE_IDENTIQUE, arc[2]);
		clotho.getTrajectoire(arc[2], VitesseClotho.GAUCHE_1, arc[3]);
		clotho.getTrajectoire(arc[3], VitesseClotho.COURBURE_IDENTIQUE, arc[4]);
		clotho.getTrajectoire(arc[4], VitesseClotho.COURBURE_IDENTIQUE, arc[5]);
		clotho.getTrajectoire(arc[5], VitesseClotho.COURBURE_IDENTIQUE, arc[6]);
		clotho.getTrajectoire(arc[6], VitesseClotho.GAUCHE_1, arc[7]);
		clotho.getTrajectoire(arc[7], VitesseClotho.GAUCHE_2, arc[8]);
		clotho.getTrajectoire(arc[8], VitesseClotho.GAUCHE_2, arc[9]);
		clotho.getTrajectoire(arc[9], VitesseClotho.DROITE_1, arc[10]);
		clotho.getTrajectoire(arc[10], VitesseClotho.DROITE_1, arc[11]);
		clotho.getTrajectoire(arc[11], VitesseClotho.DROITE_1, arc[12]);
		clotho.getTrajectoire(arc[12], VitesseClotho.DROITE_1, arc[13]);
		clotho.getTrajectoire(arc[13], VitesseClotho.DROITE_1, arc[14]);
		clotho.getTrajectoire(arc[14], VitesseClotho.GAUCHE_2, arc[15]);

		for(int a = 0; a < nbArc; a++)
		{
			// System.out.println("arc "+arc[a].v+" avec "+arc[a].arcselems[0]);
			for(int i = 0; i < ClothoidesComputer.NB_POINTS; i++)
			{
				/*
				 * if(i > 0)
				 * System.out.println(arc[a].arcselems[i-1].point.distance(arc[a
				 * ].arcselems[i].point));
				 * else if(a > 0)
				 * System.out.println(arc[a-1].arcselems[ClothoidesComputer.
				 * NB_POINTS - 1].point.distance(arc[a].arcselems[0].point));
				 */
				System.out.println(a + " " + i + " " + arc[a].arcselems[i]);
				if(graphicTrajectory)
					buffer.addSupprimable(new CircularObstacle(arc[a].getPoint(i).getPosition(), 4));
			}
			if(a == 0)
			{
				Assert.assertEquals(arc[0].arcselems[ClothoidesComputer.NB_POINTS - 1].getPositionEcriture().getX(), 0, 0.1);
				Assert.assertEquals(arc[0].arcselems[ClothoidesComputer.NB_POINTS - 1].getPositionEcriture().getY(), 1000 + (int) ClothoidesComputer.DISTANCE_ARC_COURBE, 0.1);
			}
			/*
			 * else if(arc[a].arcselems[0].enMarcheAvant !=
			 * arc[a-1].arcselems[0].enMarcheAvant)
			 * Assert.assertEquals(arc[a].vitesseCourbure.vitesse / 1000. *
			 * ClothoidesComputer.DISTANCE_ARC_COURBE,
			 * arc[a].arcselems[ClothoidesComputer.NB_POINTS-1].courbure, 0.1);
			 * else if(a > 0)
			 * Assert.assertEquals(arc[a].vitesseCourbure.vitesse / 1000. *
			 * ClothoidesComputer.DISTANCE_ARC_COURBE +
			 * arc[a-1].arcselems[ClothoidesComputer.NB_POINTS-1].courbure,
			 * arc[a].arcselems[ClothoidesComputer.NB_POINTS-1].courbure, 0.1);
			 */ }

		log.debug(arc[nbArc - 1].arcselems[arc[nbArc - 1].arcselems.length - 1].getPosition());
		Assert.assertEquals(0, arc[nbArc - 1].arcselems[arc[nbArc - 1].arcselems.length - 1].getPosition().distance(new XY(-469.90, 1643.03)), 0.1);
	}

	@Test
	public void test_demi_tour() throws Exception
	{
		boolean graphicTrajectory = config.getBoolean(ConfigInfoKraken.GRAPHIC_TRAJECTORY);

		int nbArc = 2;
		ArcCourbeDynamique arc[] = new ArcCourbeDynamique[nbArc];

		Cinematique c = new Cinematique(0, 1000, Math.PI / 2, false, 0);
		log.debug("Initial : " + c);
		arc[0] = clotho.getTrajectoireDemiTour(c, VitesseDemiTour.DEMI_TOUR_GAUCHE);
		arc[1] = clotho.getTrajectoireDemiTour(arc[0].getLast(), VitesseDemiTour.DEMI_TOUR_DROITE);

		for(int a = 0; a < nbArc; a++)
		{
			for(int i = 0; i < arc[a].getNbPoints(); i++)
			{
				System.out.println(a + " " + i + " " + arc[a].getPoint(i));
				if(graphicTrajectory)
					buffer.addSupprimable(new CircularObstacle(arc[a].getPoint(i).getPosition(), 4));
			}
		}
	}

	@Test
	public void test_ramene() throws Exception
	{
		boolean graphicTrajectory = config.getBoolean(ConfigInfoKraken.GRAPHIC_TRAJECTORY);

		int nbArc = 2;
		ArcCourbe arc[] = new ArcCourbe[nbArc];

		Cinematique c = new Cinematique(0, 1000, Math.PI / 2, false, 0);
		log.debug("Initial : " + c);
		arc[0] = clotho.getTrajectoireDemiTour(c, VitesseDemiTour.DEMI_TOUR_GAUCHE);
		// arc[0] = new
		// ArcCourbeStatique(injector.getService(RobotReal.class));
		// clotho.getTrajectoire(c, VitesseCourbure.DROITE_5,
		// (ArcCourbeStatique)arc[0]);
		arc[1] = clotho.getTrajectoireRamene(arc[0].getLast(), VitesseRameneVolant.RAMENE_VOLANT);

		for(int a = 0; a < nbArc; a++)
		{
			for(int i = 0; i < arc[a].getNbPoints(); i++)
			{
				System.out.println(a + " " + i + " " + arc[a].getPoint(i));
				if(graphicTrajectory)
					buffer.addSupprimable(new CircularObstacle(arc[a].getPoint(i).getPosition(), 4));
			}
		}
	}

	@Test
	public void test_bezier_quad() throws Exception
	{
		boolean graphicTrajectory = config.getBoolean(ConfigInfoKraken.GRAPHIC_TRAJECTORY);

		int nbArc = 1;
		ArcCourbeDynamique arc[] = new ArcCourbeDynamique[nbArc];

		Cinematique c = new Cinematique(0, 1000, Math.PI / 2, true, -1);
		Cinematique arrivee = new Cinematique(400, 1400, Math.PI / 2, false, 0);
		log.debug("Initial : " + c);
		arc[0] = bezier.interpolationQuadratique(c, arrivee.getPosition());

		Assert.assertTrue(arc[0] != null);

		for(int a = 0; a < nbArc; a++)
		{
			for(int i = 0; i < arc[a].getNbPoints(); i++)
			{
				System.out.println(a + " " + i + " " + arc[a].getPoint(i));
				if(graphicTrajectory)
					buffer.addSupprimable(new CircularObstacle(arc[a].getPoint(i).getPosition(), 4));
			}
		}
	}
	
	// TODO déplacer dans le fichier approprié
	@Test(expected = AssertionError.class)
	public void test_assert() throws Exception
	{
		assert false;
	}

	/*
	 * @Test
	 * public void test_bezier_quad_cercle() throws Exception
	 * {
	 * boolean graphicTrajectory =
	 * config.getBoolean(ConfigInfo.GRAPHIC_TRAJECTORY);
	 * int nbArc = 1;
	 * ArcCourbeDynamique arc[] = new ArcCourbeDynamique[nbArc];
	 * Cinematique c = new Cinematique(-200, 1000, Math.PI, true, -1);
	 * cercle.set(GameElementNames.MINERAI_CRATERE_HAUT_GAUCHE, 250);
	 * log.debug("Initial : "+c);
	 * arc[0] = bezier.interpolationQuadratiqueCercle(c);
	 * Assert.assertTrue(arc[0] != null);
	 * for(int a = 0; a < nbArc; a++)
	 * {
	 * for(int i = 0; i < arc[a].getNbPoints(); i++)
	 * {
	 * System.out.println(a+" "+i+" "+arc[a].getPoint(i));
	 * if(graphicTrajectory)
	 * buffer.addSupprimable(new
	 * CircularObstacle(arc[a].getPoint(i).getPosition(), 4));
	 * }
	 * }
	 * Assert.assertTrue(cercle.isArrived(arc[0].getLast()));
	 * }
	 */

	/*
	 * @Test
	 * public void test_bench() throws Exception
	 * {
	 * int nbmax = 10000;
	 * long avant = System.nanoTime();
	 * Cinematique depart = new Cinematique(-1100, 600, 0, true, 0);
	 * robot.setCinematique(depart);
	 * Cinematique c = new Cinematique(0, 1200, Math.PI, false, 0);
	 * for(int i = 0; i < nbmax; i++)
	 * {
	 * astar.computeNewPath(c, false);
	 * chemin.clear();
	 * }
	 * log.debug("Temps : "+(System.nanoTime() - avant) / (nbmax * 1000000.));
	 * }
	 */
/*	@Test
	public void test_recherche_manoeuvre() throws Exception
	{
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-900, 450, -Math.PI / 6, true, 0);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(-400, 1200, Math.PI, false, 0);
		astar.initializeNewSearch(c, false, state);
		astar.process(chemin,false);
		log.debug("Temps : " + (System.nanoTime() - avant) / (1000000.));
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
		log.debug("Nb points : " + i);
	}

	@Test
	public void test_recherche_shoot_avec_ennemi() throws Exception
	{
		Cinematique depart = new Cinematique(0, 1800, -Math.PI / 3 + 0.1, true, 0);
		robot.setCinematique(depart);
		int[] data = { 0, 200, 0, 200, 0, 0, 0, 0, 0, 0, 0, 0 };
		sensors.add(new SensorsData(0, 0, data, robot.getCinematique()));

		Cinematique c = new Cinematique(1000, 1200, Math.PI, false, 0);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin,false);
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : " + i);
	}

	@Test
	public void test_recherche_debut_dans_obstacle_fixe() throws Exception
	{
		Cinematique depart = new Cinematique(-500, 1800, -Math.PI / 3, true, 0);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(1000, 1200, Math.PI, false, 0);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin,false);
		iterator.reinit();
		CinematiqueObs a = null, b = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(b != null)
				log.debug(a.getPosition().distance(b.getPosition()));
			b = a;
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : " + i);
	}

	@Test
	public void test_recherche_shoot_avec_ennemi_difficile() throws Exception
	{
		Cinematique depart = new Cinematique(0, 1800, -Math.PI / 3, true, 0);
		robot.setCinematique(depart);
		int[] data = { 0, 400, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		Cinematique c = new Cinematique(1000, 1200, Math.PI, false, 0);
		pathcache.prepareNewPath(new KeyPathCache(state, c, true));
		pathcache.waitPathfinding();
		chemin.addToEnd(fakeChemin.getPath());
		// astar.initializeNewSearch(c, true, state);
		// astar.process(chemin,false);
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		Random r = new Random();
		while(iterator.hasNext())
		{
			if(r.nextInt(10) == 0)
				sensors.add(new SensorsData(0, 0, data, robot.getCinematique()));

			i++;
			a = iterator.next();
			log.debug("Robot en " + iterator.getIndex() + " : " + a);
			robot.setCinematique(a);
			chemin.setCurrentIndex(iterator.getIndex());
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : " + i);
	}

	@Test
	public void test_recherche_shoot_avec_ennemi_difficile_encore() throws Exception
	{
		Cinematique depart = new Cinematique(0, 1800, -Math.PI / 3, true, 0);
		robot.setCinematique(depart);
		int[] data = { 0, 400, 0, 0, 0, 0, 0, 400, 300, 0, 0, 0 };

		Cinematique c = new Cinematique(800, 500, Math.PI, false, 0);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin,false);
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		Random r = new Random();
		while(iterator.hasNext())
		{
			if(r.nextInt(10) == 0)
				sensors.add(new SensorsData(0, 0, data, robot.getCinematique()));

			i++;
			a = iterator.next();
			log.debug("Robot en " + iterator.getIndex() + " : " + a);
			robot.setCinematique(a);
			chemin.setCurrentIndex(iterator.getIndex());
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : " + i);
		cercle.set(GameElementNames.MINERAI_CRATERE_HAUT_GAUCHE, 250, 30, -30, 10, -10);
		astar.initializeNewSearchToCircle(false, state);
		astar.process(chemin,false);
		iterator.reinit();
		a = null;
		i = 0;
		while(iterator.hasNext())
		{
			if(r.nextInt(10) == 0)
				sensors.add(new SensorsData(0, 0, data, robot.getCinematique()));

			i++;
			a = iterator.next();
			log.debug("Robot en " + iterator.getIndex() + " : " + a);
			robot.setCinematique(a);
			chemin.setCurrentIndex(iterator.getIndex());
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : " + i);
	}

	@Test
	public void test_recherche_depose() throws Exception
	{
		Cinematique depart = new Cinematique(200, 1400, Math.PI/2, true, 0);
		robot.setCinematique(depart);
		ScriptNames.SCRIPT_DEPOSE_MINERAI_DROITE.s.setUpCercleArrivee();
		astar.initializeNewSearchToCircle(true, state);
		astar.process(chemin,false);
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug("Robot en " + iterator.getIndex() + " : " + a);
			robot.setCinematique(a);
			chemin.setCurrentIndex(iterator.getIndex());
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : " + i);
	}
	
	@Test
	public void test_recherche_shoot_cercle_avec_ennemi_difficile() throws Exception
	{
		Cinematique depart = new Cinematique(0, 1900, -Math.PI / 3, true, 0);
		robot.setCinematique(depart);
		int[] data = { 0, 400, 0, 0, 0, 0, 0, 400, 0, 0, 0, 0 };
		cercle.set(GameElementNames.MINERAI_CRATERE_HAUT_GAUCHE, 300, 30, -30, 10, -10);
		astar.initializeNewSearchToCircle(false, state);
		astar.process(chemin,false);
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		Random r = new Random();
		while(iterator.hasNext())
		{
			if(r.nextInt(10) == 0)
				sensors.add(new SensorsData(0, 0, data, robot.getCinematique()));
			else
				sensors.add(new SensorsData(robot.getCinematique()));

			i++;
			a = iterator.next();
			log.debug("Robot en " + iterator.getIndex() + " : " + a);
			robot.setCinematique(a);
			chemin.setCurrentIndex(iterator.getIndex());
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : " + i);
	}

	@Test
	public void test_recherche_shoot_cercle_avec_ennemi_difficile2() throws Exception
	{
		Cinematique depart = new Cinematique(-800, 350, Math.PI / 2, true, 0);
		robot.setCinematique(depart);
		int[] data = { 0, 400, 0, 0, 0, 0, 0, 400, 0, 0, 0, 0 };
		cercle.set(GameElementNames.MINERAI_CRATERE_HAUT_DROITE, 250, 30, -30, 10, -10);
		astar.initializeNewSearchToCircle(false, state);
		astar.process(chemin,false);
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		Random r = new Random();
		while(iterator.hasNext())
		{
			if(r.nextInt(10) == 0)
				sensors.add(new SensorsData(0, 0, data, robot.getCinematique()));
			else
				sensors.add(new SensorsData(robot.getCinematique()));

			i++;
			a = iterator.next();
			log.debug("Robot en " + iterator.getIndex() + " : " + a);
			robot.setCinematique(a);
			chemin.setCurrentIndex(iterator.getIndex());
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : " + i);
	}

	@Test
	public void test_recherche_shoot() throws Exception
	{
		Cinematique depart = new Cinematique(0, 1800, -Math.PI / 3, true, 0);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(1000, 1200, Math.PI, false, 0);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin,false);
		iterator.reinit();
		CinematiqueObs a = null, b = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(b != null)
				log.debug(a.getPosition().distance(b.getPosition()));
			b = a;
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : " + i);
	}

	@Test
	public void test_recherche_shoot_depuis_pos_depart() throws Exception
	{
		Cinematique depart = new Cinematique(550, 1905, -Math.PI / 2, true, 0);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(1000, 1200, Math.PI, false, 0);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin,false);
		iterator.reinit();
		CinematiqueObs a = null, b = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(b != null)
				log.debug(a.getPosition().distance(b.getPosition()));
			b = a;
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : " + i);
	}

	@Test
	public void test_recherche_shoot2() throws Exception
	{
		Cinematique depart = new Cinematique(-200, 1600, -Math.PI / 3 + Math.PI, true, 0);
		robot.setCinematique(depart);
		Thread.sleep(100);
		Cinematique c = new Cinematique(1000, 1200, Math.PI, false, 0);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin,false);
		iterator.reinit();
		CinematiqueObs a = null, b = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(b != null)
				log.debug(a.getPosition().distance(b.getPosition()));
			b = a;
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : " + i);
	}

	@Test
	public void test_pathcache() throws Exception
	{
		Cinematique depart = new Cinematique(0, 1800, -Math.PI / 3, true, 0);
		Cinematique c = new Cinematique(1000, 1200, Math.PI, false, 0);
		robot.setCinematique(depart);
		KeyPathCache k = new KeyPathCache(state, c, false);
		pathcache.prepareNewPath(k);
		pathcache.follow(k, Speed.STANDARD);
	}

	@Test(expected = PathfindingException.class)
	public void test_pathcache_exception() throws Exception
	{
		Cinematique depart = new Cinematique(0, 1800, -Math.PI / 3, true, 0);
		Cinematique c = new Cinematique(100, 200, Math.PI, false, 0);
		robot.setCinematique(depart);
		KeyPathCache k = new KeyPathCache(state, c, false);
		pathcache.prepareNewPath(k);
		pathcache.follow(k, Speed.STANDARD);
	}

	@Test
	public void test_recherche_shoot_pas() throws Exception
	{
		Cinematique depart = new Cinematique(0, 1800, -Math.PI / 3, true, 0);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(1000, 1200, Math.PI, false, 0);
		astar.initializeNewSearch(c, false, state);
		astar.process(chemin,false);
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
		log.debug("Nb points : " + i);
	}
	
	@Test
	public void test_recherche_depose2() throws Exception
	{
		Cinematique depart = new Cinematique(0, 1600, Math.PI, true, 0);
		robot.setCinematique(depart);
		astar.initializeNewSearch(ScriptNames.SCRIPT_DEPOSE_MINERAI_DROITE.s.getPointEntree(), false, state);
		astar.process(chemin,false);
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
		log.debug("Nb points : " + i);
	}

	@Test
	public void test_prepathsf() throws Exception
	{
		injector.getService(PathCache.class);
	}

	@Test
	public void test_replanif() throws Exception
	{
		// Ce test impose l'arrêt du thread de pathfinding pour ne pas avoir
		// d'interférence
		ThreadUpdatePathfinding thread = injector.getService(ThreadUpdatePathfinding.class);
		thread.interrupt();
		thread.join(1000);
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-800, 300, Math.PI / 2, true, 0);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(0, 1600, Math.PI, false, 0);
		astar.initializeNewSearch(c, false, state);
		astar.process(chemin,false);
		log.debug("Temps : " + (System.nanoTime() - avant) / (1000000.));
		iterator.reinit();
		CinematiqueObs a = null;
		int n = 10;
		while(iterator.hasNext() && iterator.getIndex() < n)
		{
			a = iterator.next();
			chemin.setCurrentIndex(iterator.getIndex());
			// log.debug("Robot en "+iterator.getIndex());
			// log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		gridspace.addObstacleAndRemoveNearbyObstacles(new CircularObstacle(new Vec2RO(-400, 1300), 200));
		chemin.checkColliding(true);
		avant = System.nanoTime();
		astar.updatePath(chemin.getLastValidCinematique());
		log.debug("Temps recalcul : " + (System.nanoTime() - avant) / (1000000.));
		iterator.reinit();
		while(iterator.hasNext())
		{
			a = iterator.next();
			chemin.setCurrentIndex(iterator.getIndex());
			// log.debug("Robot en "+iterator.getIndex());
			// log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		injector.restartThread(ThreadName.UPDATE_PATHFINDING); // pour pas
																// qu'il soit
																// mécontent
	}

	@Test(expected = PathfindingException.class)
	public void test_replanif_trop_court() throws Exception
	{
		// Ce test impose l'arrêt du thread de pathfinding pour ne pas avoir
		// d'interférence
		ThreadUpdatePathfinding thread = injector.getService(ThreadUpdatePathfinding.class);
		thread.interrupt();
		thread.join(1000);
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-800, 300, Math.PI / 2, true, 0);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(0, 1600, Math.PI, false, 0);
		astar.initializeNewSearch(c, false, state);
		astar.process(chemin,false);
		log.debug("Temps : " + (System.nanoTime() - avant) / (1000000.));
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
		// gridspace.addObstacleAndRemoveNearbyObstacles(new
		// CircularObstacle(new Vec2RO(-600, 800), 200));
		gridspace.addObstacleAndRemoveNearbyObstacles(new CircularObstacle(new Vec2RO(0, 1600), 200));
		chemin.checkColliding(true);
		avant = System.nanoTime();
		try
		{
			astar.updatePath(robot.getCinematique());
		}
		catch(Exception e)
		{
			injector.restartThread(ThreadName.UPDATE_PATHFINDING); // pour pas
																	// qu'il
																	// soit
																	// mécontent
			throw e;
		}
	}

	@Test
	public void test_recherche_finit_en_arriere() throws Exception
	{
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-800, 350, Math.PI / 2, true, 0);
		robot.setCinematique(depart);
		cercle.set(GameElementNames.MINERAI_CRATERE_HAUT_GAUCHE, 200, 30, -30, 10, -10);
		astar.initializeNewSearchToCircle(true, state);
		astar.process(chemin,false);
		log.debug("Temps : " + (System.nanoTime() - avant) / (1000000.));
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
		log.debug("Nb points : " + i);
	}

	@Test
	public void test_recherche_finit_en_arriere2() throws Exception
	{
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-800, 350, Math.PI / 2, true, 0);
		robot.setCinematique(depart);
		cercle.set(GameElementNames.MINERAI_CRATERE_HAUT_DROITE, 250, 30, -30, 10, -10);
		astar.initializeNewSearchToCircle(false, state);
		astar.process(chemin,false);
		log.debug("Temps : " + (System.nanoTime() - avant) / (1000000.));
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
		log.debug("Nb points : " + i);
	}

	@Test
	public void test_recherche_finit_en_arriere3() throws Exception
	{
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-800, 350, Math.PI / 2, true, 0);
		robot.setCinematique(depart);
		cercle.set(GameElementNames.MINERAI_CRATERE_BAS_DROITE, 250, 30, -30, 10, -10);
		astar.initializeNewSearchToCircle(true, state);
		astar.process(chemin,false);
		log.debug("Temps : " + (System.nanoTime() - avant) / (1000000.));
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
		log.debug("Nb points : " + i);
	}

	@Test
	public void test_recherche_finit_en_avant() throws Exception
	{
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-400, 1200, Math.PI, true, 0);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(300, 1200, 0, false, 0);
		astar.initializeNewSearch(c, SensFinal.MARCHE_AVANT, false, state);
		astar.process(chemin,false);
		log.debug("Temps : " + (System.nanoTime() - avant) / (1000000.));
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
		log.debug("Nb points : " + i);
	}

	@Test
	public void test_trajectoire_avec_arret() throws Exception
	{
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-800, 350, 3 * Math.PI / 4, true, 0);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(-300, 800, Math.PI, false, 0);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin,false);
		log.debug("Temps : " + (System.nanoTime() - avant) / (1000000.));
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
		log.debug("Nb points : " + i);
	}

	@Test
	public void test_recherche_loin() throws Exception
	{
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-800, 350, 3 * Math.PI / 4, true, 0);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(800, 700, Math.PI, false, 0);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin,false);
		log.debug("Temps : " + (System.nanoTime() - avant) / (1000000.));
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
		log.debug("Nb points : " + i);
	}

	@Test
	public void test_recherche_loin_arriere() throws Exception
	{
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-800, 400, -Math.PI / 2, true, 0);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(800, 700, Math.PI, false, 0);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin,false);
		log.debug("Temps : " + (System.nanoTime() - avant) / (1000000.));
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
		log.debug("Nb points : " + i);
	}
*/
}
