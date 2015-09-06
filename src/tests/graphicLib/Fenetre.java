package tests.graphicLib;

import javax.imageio.ImageIO;
import javax.swing.*;

import container.Container;
import container.ServiceNames;
import exceptions.ContainerException;
import exceptions.PointSortieException;
import obstacles.Capteurs;
import obstacles.types.ObstacleCircular;
import obstacles.types.ObstacleProximity;
import obstacles.types.ObstacleRectangular;
import pathfinding.dstarlite.GridSpace;
//import obstacles.ObstaclesFixes;
import permissions.ReadOnly;
import permissions.ReadWrite;
import utils.Sleep;
//import table.GameElementNames;
import utils.Vec2;

import java.awt.*;
import java.io.File;
import java.io.IOException;
//import java.text.AttributedString;
import java.util.ArrayList;

/**
 * Interface graphique écrite à l'arrache
 * @author pf
 *
 */

public class Fenetre extends JPanel {

	private static final long serialVersionUID = 1L;
	private static Fenetre instance;
	
	private int sizeX = 450, sizeY = 300;
	private ArrayList<Vec2<ReadOnly>> pointsADessiner = new ArrayList<Vec2<ReadOnly>>();
	private ArrayList<ObstacleProximity> listObstaclesMobiles;
	private ArrayList<Vec2<ReadOnly>[]> segments = new ArrayList<Vec2<ReadOnly>[]>();
	private Image image;
	private Vec2<ReadWrite>[] point;
//	private AttributedString affichage = new AttributedString("");
	private ArrayList<ArrayList<Vec2<ReadOnly>>> paths = new ArrayList<ArrayList<Vec2<ReadOnly>>>();
	private ArrayList<Double> orientations = new ArrayList<Double>();
	private ArrayList<Color> couleurs = new ArrayList<Color>();
	
	private ArrayList<ObstacleRectangular> obstaclesEnBiais = new ArrayList<ObstacleRectangular>();

	private Capteurs capteurs;
	
	private int firstNotDead = 0;
    
	private Fenetre(Container container)
	{
		try {
			GridSpace gs = (GridSpace)container.getService(ServiceNames.GRID_SPACE);
			image = ImageIO.read(new File("table.png"));
			sizeX = image.getWidth(this);
			sizeY = image.getHeight(this);
			for(int i = 0; i < GridSpace.NB_POINTS; i++)
				if(gs.isTraversable(i))
					grid[i] = Couleur.BLANC;
				else
					grid[i] = Couleur.NOIR;
		} catch (IOException | ContainerException | PointSortieException e) {
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
	
	private Couleur[] grid = new Couleur[GridSpace.NB_POINTS];
	
	public void setColor(int gridpoint, Couleur couleur)
	{
		grid[gridpoint] = couleur;
		repaint();
		Sleep.sleep(200);
	}
	
	public static Fenetre getInstance()
	{
		return instance;
	}

	public static void setInstance(Container container)
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
		return (g & (GridSpace.NB_POINTS_POUR_TROIS_METRES-1))*sizeX/GridSpace.NB_POINTS_POUR_TROIS_METRES;
	}

	private int YGridPointtoWindow(int g)
	{
		return sizeY-(g >> (GridSpace.PRECISION))*sizeY/GridSpace.NB_POINTS_POUR_DEUX_METRES;
	}

	private int XtoWindow(int x)
	{
		return (x+1500)*sizeX/3000;
	}

	private int YtoWindow(int y)
	{
		return (2000-y)*sizeY/2000;
	}

	public void setCapteurs(Capteurs capteurs)
	{
		this.capteurs = capteurs;
	}
	
	public void paint(Graphics g)
	{
//		g.drawImage(image, 0, 0, this);
		for(int i = 0; i < GridSpace.NB_POINTS; i++)
		{
			g.setColor(grid[i].couleur);
			g.fillOval(XGridPointtoWindow(i)-distanceXtoWindow((int) GridSpace.DISTANCE_ENTRE_DEUX_POINTS)/2,
					YGridPointtoWindow(i)-distanceYtoWindow((int) GridSpace.DISTANCE_ENTRE_DEUX_POINTS)/2,
					distanceXtoWindow((int) GridSpace.DISTANCE_ENTRE_DEUX_POINTS),
					distanceYtoWindow((int) GridSpace.DISTANCE_ENTRE_DEUX_POINTS));
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
		setPreferredSize(new Dimension(sizeX+200,sizeY));
		JFrame frame = new JFrame("Test");
		frame.getContentPane().add(this);
		frame.pack();
		frame.setVisible(true);
	}

	public void setPoint(Vec2<ReadWrite>[] point)
	{
		this.point = point;
	}
	
	public void updateFirstNotDead(int firstNotDead)
	{
		this.firstNotDead = firstNotDead;
	}
	
	public void setObstaclesMobiles(ArrayList<ObstacleProximity> listObstaclesMobiles)
	{
		this.firstNotDead = 0;
		this.listObstaclesMobiles = listObstaclesMobiles;
	}

	public void paintObstacle(ObstacleCircular o, Graphics g, int dilatationObstacle)
	{
		if(o.radius <= 0)
			g.fillOval(XtoWindow(o.position.x)-5, YtoWindow(o.position.y)-5, 10, 10);
		else
			g.fillOval(XtoWindow(o.position.x-o.radius-dilatationObstacle), YtoWindow(o.position.y+o.radius+dilatationObstacle), distanceXtoWindow((o.radius+dilatationObstacle)*2), distanceYtoWindow((o.radius+dilatationObstacle)*2));		
	}
/*	
	public void paintObstacle(ObstacleRectangular<TestOnly> o, Graphics g, int dilatationObstacle)
	{
		if(dilatationObstacle != 0)
		{
			// les quatre coins
			g.fillOval(XtoWindow(Obstacle.getPosition(o).x-o.getSizeX()/2-dilatationObstacle), YtoWindow(Obstacle.getPosition(o).y+o.getSizeY()/2+dilatationObstacle), distanceXtoWindow(dilatationObstacle*2), distanceYtoWindow(dilatationObstacle*2));
			g.fillOval(XtoWindow(Obstacle.getPosition(o).x-o.getSizeX()/2-dilatationObstacle), YtoWindow(Obstacle.getPosition(o).y-o.getSizeY()/2+dilatationObstacle), distanceXtoWindow(dilatationObstacle*2), distanceYtoWindow(dilatationObstacle*2));		
			g.fillOval(XtoWindow(Obstacle.getPosition(o).x+o.getSizeX()/2-dilatationObstacle), YtoWindow(Obstacle.getPosition(o).y+o.getSizeY()/2+dilatationObstacle), distanceXtoWindow(dilatationObstacle*2), distanceYtoWindow(dilatationObstacle*2));		
			g.fillOval(XtoWindow(Obstacle.getPosition(o).x+o.getSizeX()/2-dilatationObstacle), YtoWindow(Obstacle.getPosition(o).y-o.getSizeY()/2+dilatationObstacle), distanceXtoWindow(dilatationObstacle*2), distanceYtoWindow(dilatationObstacle*2));		

			g.fillRect(XtoWindow(Obstacle.getPosition(o).x-o.getSizeX()/2-dilatationObstacle), YtoWindow(Obstacle.getPosition(o).y+o.getSizeY()/2), distanceXtoWindow(dilatationObstacle), distanceYtoWindow(o.getSizeY()));
			g.fillRect(XtoWindow(Obstacle.getPosition(o).x+o.getSizeX()/2), YtoWindow(Obstacle.getPosition(o).y+o.getSizeY()/2), distanceXtoWindow(dilatationObstacle), distanceYtoWindow(o.getSizeY()));
			g.fillRect(XtoWindow(Obstacle.getPosition(o).x-o.getSizeX()/2), YtoWindow(Obstacle.getPosition(o).y+o.getSizeY()/2+dilatationObstacle), distanceXtoWindow(o.getSizeX()), distanceYtoWindow(dilatationObstacle));
			g.fillRect(XtoWindow(Obstacle.getPosition(o).x-o.getSizeX()/2), YtoWindow(Obstacle.getPosition(o).y-o.getSizeY()/2), distanceXtoWindow(o.getSizeX()), distanceYtoWindow(dilatationObstacle));
		}
		g.fillRect(XtoWindow(Obstacle.getPosition(o).x-o.getSizeX()/2), YtoWindow(Obstacle.getPosition(o).y+o.getSizeY()/2), distanceXtoWindow(o.getSizeX()), distanceYtoWindow(o.getSizeY()));
	}
	
	// Impossible
	public void paintObstacle(Obstacle<ReadOnly> o, Graphics g, int dilatationObstacle)
	{
		if(o instanceof ObstacleRectangular)
			paintObstacle((ObstacleRectangular<ReadOnly>)o, g, dilatationObstacle);
		else if(o instanceof ObstacleCircular)
			paintObstacle((ObstacleCircular<ReadOnly>)o, g, dilatationObstacle);
	}
*/
/*	public void afficheCoordonnees(Point point)
	{
		affichage = new AttributedString("("+WindowToX((int)point.getX())+", "+WindowToY((int)point.getY())+")");
	}
	*/
	public void addSegment(Vec2<ReadOnly> a, Vec2<ReadOnly> b)
	{
		@SuppressWarnings("unchecked")
		Vec2<ReadOnly>[] v = (Vec2<ReadOnly>[]) new Vec2[2];
		v[0] = a;
		v[1] = b;
		segments.add(v);
	}
	
	public void paintSegments(Graphics g)
	{
		for(Vec2<ReadOnly>[] v : segments)
			g.drawLine(XtoWindow(v[0].x), YtoWindow(v[0].y), XtoWindow(v[1].x), YtoWindow(v[1].y));
	}
	
	public void paintObstacleEnBiais(Graphics g)
	{
		int[] X, Y;
		for(ObstacleRectangular o: obstaclesEnBiais)
		{
			X = ObstacleRectangular.getXPositions(o);
			Y = ObstacleRectangular.getYPositions(o);
			for(int i = 0; i < 4; i++)
			{
				X[i] = XtoWindow(X[i]);
				Y[i] = YtoWindow(Y[i]);
			}
			g.fillPolygon(X, Y, 4);
		}
	}
	
	public void resetPath()
	{
		paths.clear();
		orientations.clear();
		couleurs.clear();
	}
	
	public void setPath(Double orientation, ArrayList<Vec2<ReadOnly>> chemin, Color couleur)
	{
		orientations.add(orientation);
		paths.add(chemin);
		couleurs.add(couleur);
	}
	
	public void addObstacleEnBiais(ObstacleRectangular obstacleEnBiais)
	{
		obstaclesEnBiais.add(obstacleEnBiais);
	}
	
}
