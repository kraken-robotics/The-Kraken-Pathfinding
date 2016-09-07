package debug;

import javax.imageio.ImageIO;
import javax.swing.*;

import container.Container;
import exceptions.ContainerException;
import obstacles.ObstaclesFixes;
import obstacles.Capteurs;
import obstacles.memory.ObstaclesIteratorPresent;
import obstacles.memory.ObstaclesMemory;
import obstacles.types.Obstacle;
import obstacles.types.ObstacleCircular;
import obstacles.types.ObstacleRectangular;
import pathfinding.dstarlite.GridSpace;
import pathfinding.dstarlite.PointGridSpace;
import pathfinding.dstarlite.PointGridSpaceManager;
import table.Table;
import utils.Config;
import utils.Vec2;
import utils.permissions.ReadOnly;
import utils.permissions.ReadWrite;
import utils.Log;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Interface graphique écrite à l'arrache
 * @author pf
 *
 */

public class Fenetre extends JPanel {

	private static final long serialVersionUID = 1L;
	private static Fenetre instance;
	
	private static final boolean afficheFond = false;
	private int sizeX = 450, sizeY = 300;
//	private ArrayList<Vec2<ReadOnly>> pointsADessiner = new ArrayList<Vec2<ReadOnly>>();
//	private LinkedList<ObstacleProximity> listObstaclesMobiles;
	private ArrayList<Vec2<ReadOnly>[]> segments = new ArrayList<Vec2<ReadOnly>[]>();
	private Image image;
//	private Vec2<ReadWrite>[] point;
//	private AttributedString affichage = new AttributedString("");
	
	private ObstaclesIteratorPresent iterator;
	private ArrayList<ObstacleRectangular> obstaclesEnBiais = new ArrayList<ObstacleRectangular>();
	private ArrayList<ObstacleCircular> obstaclesCirulaires = new ArrayList<ObstacleCircular>();

	protected Capteurs capteurs;
	private boolean printObsFixes = false;
//	private int firstNotDead = 0;
    
	private GridSpace gs;
	private PointGridSpaceManager pm;
	public static boolean needInit = true;
	
	private Fenetre(Container container) throws InterruptedException
	{
		try {
			iterator = new ObstaclesIteratorPresent(container.getService(Log.class),
					container.getService(ObstaclesMemory.class));
			gs = container.getService(GridSpace.class);
			pm = container.getService(PointGridSpaceManager.class);
		} catch (ContainerException e) {
			e.printStackTrace();
		}
	}
	
	private void init()
	{
		needInit = false;
		try {
			image = ImageIO.read(new File("minitable2016.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		sizeX = image.getWidth(this);
		sizeY = image.getHeight(this);
		try {
	    	Method m = GridSpace.class.getDeclaredMethod("isTraversableStatique", PointGridSpace.class);
	    	m.setAccessible(true);

			for(int i = 0; i < PointGridSpace.NB_POINTS; i++)
				if((boolean) m.invoke(gs, pm.get(i)))
					grid[i] = Couleur.BLANC;
				else
					grid[i] = Couleur.NOIR;
		} catch (Exception e) {
			e.printStackTrace();
		}
		new MouseListener(this);
		showOnFrame();
	}
	
	public enum Couleur {
		BLANC(new Color(255, 255, 255, 0)),
		NOIR(new Color(0, 0, 0, 255)),
		BLEU(new Color(0, 0, 200, 255)),
		JAUNE(new Color(200, 200, 0, 255)),
		ROUGE(new Color(200, 0, 0, 255)),
		VIOLET(new Color(200, 0, 200, 255));
		
		public final Color couleur;
		
		private Couleur(Color couleur)
		{
			this.couleur = couleur;
		}
	}
	
	private Couleur[] grid = new Couleur[PointGridSpace.NB_POINTS];
	
	public void setColor(PointGridSpace gridpoint, Couleur couleur)
	{
		if(grid[gridpoint.hashCode()] != couleur)
		{
			grid[gridpoint.hashCode()] = couleur;
			if(needInit)
				init();
			repaint();
//			Sleep.sleep(50);
		}
	}
	
	public static Fenetre getInstance()
	{
		return instance;
	}

	public static void setInstance(Container container) throws InterruptedException
	{
		instance = new Fenetre(container);
	}

	private int distanceXtoWindow(int dist)
	{
		return dist*sizeX/3000;
	}

	private int distanceYtoWindow(int dist)
	{
		return dist*sizeY/2000;
	}
/*
	private int WindowToX(int x)
	{
		return x*3000/sizeX-1500;
	}

	private int WindowToY(int y)
	{
		return 2000-y*2000/sizeY;
	}
*/
	private int XGridPointtoWindow(int g)
	{
		return (g & (PointGridSpace.NB_POINTS_POUR_TROIS_METRES-1))*sizeX/(PointGridSpace.NB_POINTS_POUR_TROIS_METRES-1);
	}

	private int YGridPointtoWindow(int g)
	{
		return sizeY-(g >> (PointGridSpace.PRECISION))*sizeY/(PointGridSpace.NB_POINTS_POUR_DEUX_METRES-1);
	}

	private int XtoWindow(double x)
	{
		return (int)((x+1500)*sizeX/3000);
	}

	private int YtoWindow(double y)
	{
		return (int)((2000-y)*sizeY/2000);
	}

	public void setCapteurs(Capteurs capteurs)
	{
		this.capteurs = capteurs;
	}
	
	@Override
	@SuppressWarnings("unused")
	public void paint(Graphics g)
	{
		if(afficheFond)
			g.drawImage(image, 0, 0, this);
		if(Config.graphicDStarLite || Config.graphicThetaStar)
			for(int i = 0; i < PointGridSpace.NB_POINTS; i++)
			{
				g.setColor(grid[i].couleur);
				g.fillOval(XGridPointtoWindow(i)-distanceXtoWindow((int) PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS)/2,
						YGridPointtoWindow(i)-distanceYtoWindow((int) PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS)/2,
						distanceXtoWindow((int) PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS),
						distanceYtoWindow((int) PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS));
			}
		if(Config.graphicObstacles)
		{
			if(printObsFixes)
				for(ObstaclesFixes obs : ObstaclesFixes.values)
				{
					Obstacle o = obs.getObstacle();
					if(o instanceof ObstacleRectangular)
						paintObstacleRectangulaire((ObstacleRectangular)o, g, 0);
					else if(o instanceof ObstacleCircular)
						paintObstacleCirculaire((ObstacleCircular)o, g, 0);
				}

			paintObstacleEnBiais(g);
			paintObstaclesCirculaires(g);
			iterator.reinit();
			while(iterator.hasNext())				
				paintObstacleCirculaire(iterator.next(), g, 0);
		}
/*
		g.setColor(new Color(0, 0, 130, 40));

//		for(int i = firstNotDead; i < listObstaclesMobiles.size(); i++)
//			paintObstacle(listObstaclesMobiles.get(i).getTestOnly(), g, 0);
		int nbPoints = 4;
		for(int k = 0; k < nbPoints; k++)
			if(listObstaclesMobiles.size() > k)
				paintObstacle(listObstaclesMobiles.get(listObstaclesMobiles.size()-1-k), g, 0);
		
		if(capteurs != null)
		{
//			int nbCapteurs = 12;
			int nbCapteurs = 8;
			double angleCone;
			for(int i = 0; i < nbCapteurs; i++)
			{
//				if(i < 8)
					angleCone = 35.*Math.PI/180;
//				else
//					angleCone = 5.*Math.PI/180;
				Vec2<ReadWrite> p1 = capteurs.positionsRelatives[i].plusNewVector(new Vec2<ReadOnly>(0, 1000));
				Vec2<ReadWrite> p2 = p1.plusNewVector(new Vec2<ReadOnly>(800, angleCone + Capteurs.orientationsRelatives[i], true));
				Vec2<ReadWrite> p3 = p1.plusNewVector(new Vec2<ReadOnly>(800, - angleCone + Capteurs.orientationsRelatives[i], true));
				int[] x = new int[3];
				x[0] = XtoWindow(p1.x);
				x[1] = XtoWindow(p2.x);
				x[2] = XtoWindow(p3.x);
				int[] y = new int[3];
				y[0] = YtoWindow(p1.y);
				y[1] = YtoWindow(p2.y);
				y[2] = YtoWindow(p3.y);
				g.setColor(new Color(0, 130, 0, 50));
				g.fillArc(XtoWindow(p1.x-800), YtoWindow(p1.y+800), distanceXtoWindow(2*800), distanceYtoWindow(2*800), (int)(Capteurs.orientationsRelatives[i]*180/Math.PI-35.), 70);
//				g.fillPolygon(x, y, 3);
				g.setColor(new Color(0, 130, 0, 255));
				g.drawPolygon(x, y, 3);
			}
		}

		g.setColor(new Color(255, 0, 0, 30));

		if(point != null)
			for(Vec2<ReadWrite> v: point)
				g.fillOval(XtoWindow(v.x-200), YtoWindow(v.y+200), distanceXtoWindow(400), distanceYtoWindow(400));

		ObstacleRectangular o = (ObstacleRectangular)ObstaclesFixes.TEST_RECT.getObstacle();
		int[] X = ObstacleRectangular.getXPositions(o);
		int[] Y = ObstacleRectangular.getYPositions(o);
		for(int i = 0; i < 4; i++)
		{
			X[i] = XtoWindow(X[i]);
			Y[i] = YtoWindow(Y[i]);
		}
		g.fillPolygon(X, Y, 4);

//		Vec2<ReadOnly> v = ObstaclesFixes.TEST.getObstacle().position;
//		int r = ((ObstacleCircular)ObstaclesFixes.TEST.getObstacle()).getRadius();
//		g.fillOval(XtoWindow(v.x-r), YtoWindow(v.y+r), distanceXtoWindow(r*2), distanceYtoWindow(r*2));
*/
	}

	public void showOnFrame() {
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(sizeX,sizeY));
		JFrame frame = new JFrame("Test");
		frame.getContentPane().add(this);
		frame.pack();
		frame.setVisible(true);
	}

	@SuppressWarnings("unchecked")
	public void paintObstacleCirculaire(ObstacleCircular o, Graphics g, int dilatationObstacle)
	{
    	Field f;
		try {
			f = Obstacle.class.getDeclaredField("position");
	    	f.setAccessible(true);
	
			if(o.radius <= 0)
				g.fillOval(XtoWindow(((Vec2<ReadWrite>)f.get(o)).x)-5, YtoWindow(((Vec2<ReadWrite>)f.get(o)).y)-5, 10, 10);
			else
				g.fillOval(XtoWindow(((Vec2<ReadWrite>)f.get(o)).x-o.radius-dilatationObstacle), YtoWindow(((Vec2<ReadWrite>)f.get(o)).y+o.radius+dilatationObstacle), distanceXtoWindow((o.radius+dilatationObstacle)*2), distanceYtoWindow((o.radius+dilatationObstacle)*2));		

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void paintObstacleRectangulaire(ObstacleRectangular o, Graphics g, int dilatationObstacle)
	{/*// TODO
		if(dilatationObstacle != 0)
		{
			// les quatre coins
			g.fillOval(XtoWindow(o.getPosition().x-o.getSizeX()/2-dilatationObstacle), YtoWindow(o.getPosition().y+o.getSizeY()/2+dilatationObstacle), distanceXtoWindow(dilatationObstacle*2), distanceYtoWindow(dilatationObstacle*2));
			g.fillOval(XtoWindow(o.getPosition().x-o.getSizeX()/2-dilatationObstacle), YtoWindow(o.getPosition().y-o.getSizeY()/2+dilatationObstacle), distanceXtoWindow(dilatationObstacle*2), distanceYtoWindow(dilatationObstacle*2));		
			g.fillOval(XtoWindow(o.getPosition().x+o.getSizeX()/2-dilatationObstacle), YtoWindow(o.getPosition().y+o.getSizeY()/2+dilatationObstacle), distanceXtoWindow(dilatationObstacle*2), distanceYtoWindow(dilatationObstacle*2));		
			g.fillOval(XtoWindow(o.getPosition().x+o.getSizeX()/2-dilatationObstacle), YtoWindow(o.getPosition().y-o.getSizeY()/2+dilatationObstacle), distanceXtoWindow(dilatationObstacle*2), distanceYtoWindow(dilatationObstacle*2));		

			g.fillRect(XtoWindow(o.getPosition().x-o.getSizeX()/2-dilatationObstacle), YtoWindow(o.getPosition().y+o.getSizeY()/2), distanceXtoWindow(dilatationObstacle), distanceYtoWindow(o.getSizeY()));
			g.fillRect(XtoWindow(o.getPosition().x+o.getSizeX()/2), YtoWindow(o.getPosition().y+o.getSizeY()/2), distanceXtoWindow(dilatationObstacle), distanceYtoWindow(o.getSizeY()));
			g.fillRect(XtoWindow(o.getPosition().x-o.getSizeX()/2), YtoWindow(o.getPosition().y+o.getSizeY()/2+dilatationObstacle), distanceXtoWindow(o.getSizeX()), distanceYtoWindow(dilatationObstacle));
			g.fillRect(XtoWindow(o.getPosition().x-o.getSizeX()/2), YtoWindow(o.getPosition().y-o.getSizeY()/2), distanceXtoWindow(o.getSizeX()), distanceYtoWindow(dilatationObstacle));
		}
		g.fillRect(XtoWindow(o.getPosition().x-o.getSizeX()/2), YtoWindow(o.getPosition().y+o.getSizeY()/2), distanceXtoWindow(o.getSizeX()), distanceYtoWindow(o.getSizeY()));
*/	}
	
	// Impossible
//	public void paintObstacle(Obstacle<ReadOnly> o, Graphics g, int dilatationObstacle)
//	{
//		if(o instanceof ObstacleRectangular)
//			paintObstacle((ObstacleRectangular<ReadOnly>)o, g, dilatationObstacle);
//		else if(o instanceof ObstacleCircular)
//			paintObstacle((ObstacleCircular<ReadOnly>)o, g, dilatationObstacle);
//	}

/*	public void afficheCoordonnees(Point point)
	{
		affichage = new AttributedString("("+WindowToX((int)point.getX())+", "+WindowToY((int)point.getY())+")");
	}
	*/
	public void addSegment(Vec2<ReadOnly> a, Vec2<ReadOnly> b)
	{
		@SuppressWarnings("unchecked")
		Vec2<ReadOnly>[] v = new Vec2[2];
		v[0] = a;
		v[1] = b;
		segments.add(v);
	}
	
	public void paintSegments(Graphics g)
	{
		for(Vec2<ReadOnly>[] v : segments)
			g.drawLine(XtoWindow(v[0].x), YtoWindow(v[0].y), XtoWindow(v[1].x), YtoWindow(v[1].y));
	}
	public void paintObstaclesCirculaires(Graphics g)
	{
		g.setColor(Couleur.NOIR.couleur);		
		synchronized(obstaclesCirulaires)
		{
			for(ObstacleCircular o: obstaclesCirulaires)
				paintObstacleCirculaire(o, g, 0);
		}
	}
	@SuppressWarnings("unchecked")
	public void paintObstacleEnBiais(Graphics g)
	{
		try {
		g.setColor(Couleur.NOIR.couleur);
		int[] X, Y;
		synchronized(obstaclesEnBiais)
		{
			for(ObstacleRectangular o: obstaclesEnBiais)
			{
				Field f1 = ObstacleRectangular.class.getDeclaredField("coinBasDroiteRotate");
		    	f1.setAccessible(true);
		    	Field f2 = ObstacleRectangular.class.getDeclaredField("coinHautDroiteRotate");
		    	f2.setAccessible(true);
		    	Field f3 = ObstacleRectangular.class.getDeclaredField("coinHautGaucheRotate");
		    	f3.setAccessible(true);
		    	Field f4 = ObstacleRectangular.class.getDeclaredField("coinBasGaucheRotate");
		    	f4.setAccessible(true);

				
				X = new int[4];
				X[0] = (int) ((Vec2<ReadOnly>)f1.get(o)).x;
				X[1] = (int) ((Vec2<ReadOnly>)f2.get(o)).x;
				X[2] = (int) ((Vec2<ReadOnly>)f3.get(o)).x;
				X[3] = (int) ((Vec2<ReadOnly>)f4.get(o)).x;

				Y = new int[4];
				Y[0] = (int) ((Vec2<ReadOnly>)f1.get(o)).y;
				Y[1] = (int) ((Vec2<ReadOnly>)f2.get(o)).y;
				Y[2] = (int) ((Vec2<ReadOnly>)f3.get(o)).y;
				Y[3] = (int) ((Vec2<ReadOnly>)f4.get(o)).y;
				
				for(int i = 0; i < 4; i++)
				{
					X[i] = XtoWindow(X[i]);
					Y[i] = YtoWindow(Y[i]);
				}
				g.fillPolygon(X, Y, 4);
			}
		}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
//		g.fillRect(100, 100, 100, 100);
	}
	
	public void clearObstacleEnBiais()
	{
		synchronized(obstaclesEnBiais)
		{
			obstaclesEnBiais.clear();
		}
		if(needInit)
			init();
		repaint();
	}
	
	public void addObstacleEnBiais(ObstacleRectangular obstacleEnBiais)
	{
		synchronized(obstaclesEnBiais)
		{
			obstaclesEnBiais.add(obstacleEnBiais);
		}
		if(needInit)
			init();
		repaint();
	}
	
	public void printObsFixes()
	{
		printObsFixes = true;
        if(needInit)
            init();
        repaint();
	}
	
	public void addObstacleCirculaire(ObstacleCircular o)
	{
		obstaclesCirulaires.add(o);
		if(needInit)
			init();
		repaint();
	}
	
}
