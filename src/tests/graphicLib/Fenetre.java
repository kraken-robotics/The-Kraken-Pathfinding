package tests.graphicLib;

import javax.imageio.ImageIO;
import javax.swing.*;

import obstacles.Obstacle;
import obstacles.ObstacleCircular;
import obstacles.ObstacleProximity;
import obstacles.ObstacleRectangular;
//import obstacles.ObstaclesFixes;
import permissions.ReadOnly;
import permissions.ReadWrite;
import table.Capteurs;
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

	private int sizeX = 450, sizeY = 300;
//	private ArrayList<Vec2<ReadOnly>> pointsADessiner = new ArrayList<Vec2<ReadOnly>>();
	private ArrayList<ObstacleProximity> listObstaclesMobiles;
	private ArrayList<Vec2<ReadOnly>[]> segments = new ArrayList<Vec2<ReadOnly>[]>();
	private Image image;
	private Vec2<ReadOnly> point;
//	private AttributedString affichage = new AttributedString("");
	private ArrayList<ArrayList<Vec2<ReadOnly>>> paths = new ArrayList<ArrayList<Vec2<ReadOnly>>>();
	private ArrayList<Double> orientations = new ArrayList<Double>();
	private ArrayList<Color> couleurs = new ArrayList<Color>();
	
	private ArrayList<ObstacleRectangular> obstaclesEnBiais = new ArrayList<ObstacleRectangular>();

	private Capteurs capteurs;
	
	private int firstNotDead = 0;
    
	public Fenetre()
	{
		try {
			image = ImageIO.read(new File("table.png"));
			sizeX = image.getWidth(this);
			sizeY = image.getHeight(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new MouseListener(this);
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
		g.drawImage(image, 0, 0, this);
		g.setColor(new Color(0, 0, 130, 40));

//		for(int i = firstNotDead; i < listObstaclesMobiles.size(); i++)
//			paintObstacle(listObstaclesMobiles.get(i).getTestOnly(), g, 0);
		if(listObstaclesMobiles.size() > 0)
			paintObstacle(listObstaclesMobiles.get(listObstaclesMobiles.size()-1), g, 0);
		
		if(capteurs != null)
		{
//			int nbCapteurs = 12;
			int nbCapteurs = 2;
			double angleCone;
			for(int i = 0; i < nbCapteurs; i++)
			{
//				if(i < 8)
					angleCone = 35.*Math.PI/180;
//				else
//					angleCone = 5.*Math.PI/180;
				Vec2<ReadWrite> p1 = capteurs.positionsRelatives[i].plusNewVector(new Vec2<ReadOnly>(0, 1000));
				Vec2<ReadWrite> p2 = p1.plusNewVector(new Vec2<ReadOnly>(800, angleCone + Capteurs.orientationsRelatives[i]));
				Vec2<ReadWrite> p3 = p1.plusNewVector(new Vec2<ReadOnly>(800, - angleCone + Capteurs.orientationsRelatives[i]));
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
			g.fillOval(XtoWindow(point.x-200), YtoWindow(point.y+200), distanceXtoWindow(400), distanceYtoWindow(400));

	}

	public void showOnFrame() {
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(sizeX+200,sizeY));
		JFrame frame = new JFrame("Test");
		frame.getContentPane().add(this);
		frame.pack();
		frame.setVisible(true);
	}

	public void setPoint(Vec2<ReadOnly> point)
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
		if(o.getRadius() <= 0)
			g.fillOval(XtoWindow(Obstacle.getPosition(o).x)-5, YtoWindow(Obstacle.getPosition(o).y)-5, 10, 10);
		else
			g.fillOval(XtoWindow(Obstacle.getPosition(o).x-o.getRadius()-dilatationObstacle), YtoWindow(Obstacle.getPosition(o).y+o.getRadius()+dilatationObstacle), distanceXtoWindow((o.getRadius()+dilatationObstacle)*2), distanceYtoWindow((o.getRadius()+dilatationObstacle)*2));		
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
