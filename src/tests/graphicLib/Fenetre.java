package tests.graphicLib;

import javax.imageio.ImageIO;
import javax.swing.*;

import obstacles.Obstacle;
import obstacles.ObstacleCircular;
import obstacles.ObstacleProximity;
import obstacles.ObstacleRectangular;
import obstacles.ObstacleRectangularAligned;
import obstacles.gameElement.GameElement;
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
	private ArrayList<Vec2> pointsADessiner = new ArrayList<Vec2>();
	private GameElement[] listGameElement;
	private ArrayList<ObstacleRectangular> listObstaclesFixes;
	private ArrayList<ObstacleProximity> listObstaclesMobiles;
	private ArrayList<Vec2[]> segments = new ArrayList<Vec2[]>();
	private Image image;
	private int dilatationObstacle;
	private AttributedString affichage = new AttributedString("");
	private ArrayList<ArrayList<Vec2>> paths = new ArrayList<ArrayList<Vec2>>();
	private ArrayList<Double> orientations = new ArrayList<Double>();
	private ArrayList<Color> couleurs = new ArrayList<Color>();
	
	private ObstacleRectangular obstacleEnBiais;

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
	
	public void setDilatationObstacle(int dilatationObstacle)
	{
		this.dilatationObstacle = dilatationObstacle;
	}
	
	public void addPoint(Vec2 point)
	{
		pointsADessiner.add(point);
	}
	
	private int distanceXtoWindow(int dist)
	{
		return dist*sizeX/3000;
	}

	private int distanceYtoWindow(int dist)
	{
		return dist*sizeY/2000;
	}

	private int WindowToX(int x)
	{
		return x*3000/sizeX-1500;
	}

	private int WindowToY(int y)
	{
		return 2000-y*2000/sizeY;
	}

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

		g.setColor(Color.RED.darker().darker().darker());
		for(Obstacle o : listObstaclesFixes)
			paintObstacle(o,g,dilatationObstacle);

		g.setColor(Color.RED.darker().darker());
		for(Obstacle o : listObstaclesFixes)
			paintObstacle(o, g,0);

		g.setColor(Color.YELLOW);
		for(Vec2 pos : pointsADessiner)
			g.fillOval(XtoWindow(pos.x)-5, YtoWindow(pos.y)-5, 10, 10);

		// vert transparent
		g.setColor(new Color(0, 130, 0, 40));
		for(GameElement o : listGameElement)
			paintObstacle(o,g,dilatationObstacle);

		g.setColor(new Color(0, 130, 0, 255));
		for(GameElement o : listGameElement)
			paintObstacle(o,g,0);
		
		g.setColor(new Color(0, 0, 130, 40));
		for(int i = firstNotDead; i < listObstaclesMobiles.size(); i++)
			paintObstacle(listObstaclesMobiles.get(i), g, 0);
		
		g.setColor(Color.WHITE);
		g.fillRect(sizeX, 0, 200, sizeY);		

		g.setColor(Color.BLACK);
	    g.drawString(affichage.getIterator(), sizeX+50, 30);

	    g.setColor(Color.PINK);
//	    paintSegments(g);
	    paintObstacleEnBiais(g);
		

	    g.setColor(Color.BLUE);
	    for(int j = 0; j < paths.size(); j++)
	    {
	    	ArrayList<Vec2> path = paths.get(j);
	    	Double orientation = orientations.get(j);
		    if(path.size() >= 1)
		    {
		    	if(orientation != null)
		    	{
		    		g.setColor(Color.RED);
			    	g.fillOval(XtoWindow(path.get(0).x)-3+(int)(30*Math.cos(orientation)), YtoWindow(path.get(0).y)-3-(int)(30*Math.sin(orientation)), 6, 6);
		    	}
			    g.setColor(couleurs.get(j));
		    	g.fillOval(XtoWindow(path.get(0).x)-5, YtoWindow(path.get(0).y)-5, 10, 10);
		    	g.fillOval(XtoWindow(path.get(path.size()-1).x)-5, YtoWindow(path.get(path.size()-1).y)-5, 10, 10);
			    for(int i = 0; i < path.size()-1; i++)
			    {
			    	g.drawLine(XtoWindow(path.get(i).x), YtoWindow(path.get(i).y), XtoWindow(path.get(i+1).x), YtoWindow(path.get(i+1).y));
			    }
		    }
	    }
	}

	public void showOnFrame() {
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(sizeX+200,sizeY));
		JFrame frame = new JFrame("Test");
		frame.getContentPane().add(this);
		frame.pack();
		frame.setVisible(true);
	}

	public void setGameElement(GameElement[] listGameElement)
	{
		this.listGameElement = listGameElement;
	}

	public void setObstaclesFixes(ArrayList<ObstacleRectangular> listObstaclesFixes)
	{
		this.listObstaclesFixes = listObstaclesFixes;
	}

	public void setObstaclesMobiles(ArrayList<ObstacleProximity> listObstaclesMobiles, int firstNotDead)
	{
		this.firstNotDead  = firstNotDead;
		this.listObstaclesMobiles = listObstaclesMobiles;
	}

	public void paintObstacle(ObstacleCircular o, Graphics g, int dilatationObstacle)
	{
		if(o.getRadius() <= 0)
			g.fillOval(XtoWindow(o.getPosition().x)-5, YtoWindow(o.getPosition().y)-5, 10, 10);
		else
			g.fillOval(XtoWindow(o.getPosition().x-o.getRadius()-dilatationObstacle), YtoWindow(o.getPosition().y+o.getRadius()+dilatationObstacle), distanceXtoWindow((o.getRadius()+dilatationObstacle)*2), distanceYtoWindow((o.getRadius()+dilatationObstacle)*2));		
	}
	
	public void paintObstacle(ObstacleRectangularAligned o, Graphics g, int dilatationObstacle)
	{
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
	}
	
	// Impossible
	public void paintObstacle(Obstacle o, Graphics g, int dilatationObstacle)
	{
		if(o instanceof ObstacleRectangularAligned)
			paintObstacle((ObstacleRectangularAligned)o, g, dilatationObstacle);
		else if(o instanceof ObstacleCircular)
			paintObstacle((ObstacleCircular)o, g, dilatationObstacle);
	}

	public void afficheCoordonnees(Point point)
	{
		affichage = new AttributedString("("+WindowToX((int)point.getX())+", "+WindowToY((int)point.getY())+")");
	}
	
	public void addSegment(Vec2 a, Vec2 b)
	{
		Vec2[] v = new Vec2[2];
		v[0] = a.clone();
		v[1] = b.clone();
		segments.add(v);
	}
	
	public void paintSegments(Graphics g)
	{
		for(Vec2[] v : segments)
			g.drawLine(XtoWindow(v[0].x), YtoWindow(v[0].y), XtoWindow(v[1].x), YtoWindow(v[1].y));
	}
	
	public void paintObstacleEnBiais(Graphics g)
	{
		g.fillPolygon(obstacleEnBiais.getXPositions(), obstacleEnBiais.getYPositions(), 4);
	}
	
	public void resetPath()
	{
		paths.clear();
		orientations.clear();
		couleurs.clear();
	}
	
	public void setPath(Double orientation, ArrayList<Vec2> chemin, Color couleur)
	{
		orientations.add(orientation);
		paths.add(chemin);
		couleurs.add(couleur);
	}
	
	public void setObstacleEnBiais(ObstacleRectangular obstacleEnBiais)
	{
		this.obstacleEnBiais = obstacleEnBiais;
	}
	
}
