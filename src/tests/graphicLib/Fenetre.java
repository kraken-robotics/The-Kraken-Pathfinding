package tests.graphicLib;

import javax.imageio.ImageIO;
import javax.swing.*;

import obstacles.Obstacle;
import obstacles.ObstacleCircular;
import obstacles.ObstacleProximity;
import obstacles.ObstacleRectangular;
import obstacles.ObstaclesFixes;
import permissions.ReadOnly;
import permissions.TestOnly;
import table.GameElementNames;
import utils.Vec2;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.AttributedString;
import java.util.ArrayList;

/**
 * Interface graphique écrite à l'arrache
 * @author pf
 *
 */

public class Fenetre extends JPanel {

	private static final long serialVersionUID = 1L;

	private int sizeX = 450, sizeY = 300;
	private ArrayList<Vec2<ReadOnly>> pointsADessiner = new ArrayList<Vec2<ReadOnly>>();
	private ArrayList<ObstacleProximity<ReadOnly>> listObstaclesMobiles;
	private ArrayList<Vec2<ReadOnly>[]> segments = new ArrayList<Vec2<ReadOnly>[]>();
	private Image image;
//	private AttributedString affichage = new AttributedString("");
	private ArrayList<ArrayList<Vec2<ReadOnly>>> paths = new ArrayList<ArrayList<Vec2<ReadOnly>>>();
	private ArrayList<Double> orientations = new ArrayList<Double>();
	private ArrayList<Color> couleurs = new ArrayList<Color>();
	
	private ArrayList<ObstacleRectangular<ReadOnly>> obstaclesEnBiais = new ArrayList<ObstacleRectangular<ReadOnly>>();

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

	public void paint(Graphics g)
	{
		g.drawImage(image, 0, 0, this);
		g.setColor(new Color(0, 0, 130, 40));

		for(int i = firstNotDead; i < listObstaclesMobiles.size(); i++)
			paintObstacle(listObstaclesMobiles.get(i).getTestOnly(), g, 0);
	}

	public void showOnFrame() {
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(sizeX+200,sizeY));
		JFrame frame = new JFrame("Test");
		frame.getContentPane().add(this);
		frame.pack();
		frame.setVisible(true);
	}

	public void updateFirstNotDead(int firstNotDead)
	{
		this.firstNotDead = firstNotDead;
	}
	
	public void setObstaclesMobiles(ArrayList<ObstacleProximity<ReadOnly>> listObstaclesMobiles)
	{
		this.firstNotDead  = 0;
		this.listObstaclesMobiles = listObstaclesMobiles;
	}

	public void paintObstacle(ObstacleCircular<TestOnly> o, Graphics g, int dilatationObstacle)
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
		for(ObstacleRectangular<ReadOnly> o: obstaclesEnBiais)
		{
			X = ObstacleRectangular.getXPositions(o.getTestOnly());
			Y = ObstacleRectangular.getYPositions(o.getTestOnly());
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
	
	public void addObstacleEnBiais(ObstacleRectangular<ReadOnly> obstacleEnBiais)
	{
		obstaclesEnBiais.add(obstacleEnBiais);
	}
	
}
