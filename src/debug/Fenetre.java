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

package debug;

import javax.imageio.ImageIO;
import javax.swing.*;

import container.Service;
import obstacles.ObstaclesFixes;
import obstacles.Capteurs;
import obstacles.memory.ObstaclesIteratorPresent;
import obstacles.types.Obstacle;
import obstacles.types.ObstacleCircular;
import obstacles.types.ObstacleRectangular;
import pathfinding.dstarlite.gridspace.GridSpace;
import pathfinding.dstarlite.gridspace.PointGridSpace;
import pathfinding.dstarlite.gridspace.PointGridSpaceManager;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2RO;
import utils.Vec2RW;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Interface graphique
 * @author pf
 *
 */

public class Fenetre extends JPanel implements Service {

	private static final long serialVersionUID = 1L;
	protected Log log;
	
	private boolean afficheFond;
	private int sizeX = 450, sizeY = 300;
	private Image image;
	private JFrame frame;
	
	private HashMap<Class<?>, ArrayList<Printable>> elementsAffichables = new HashMap<Class<?>, ArrayList<Printable>>();
	
	private ObstaclesIteratorPresent iterator;

	protected Capteurs capteurs;
	private boolean printObsFixes;
//	private int firstNotDead = 0;
    
	private GridSpace gs;
	private PointGridSpaceManager pm;
	private boolean needInit = true;
	
	public Fenetre(Log log, ObstaclesIteratorPresent iterator, GridSpace gs, PointGridSpaceManager pm, Capteurs capteurs)
	{
		this.log = log;
		this.iterator = iterator;
		this.gs = gs;
		this.pm = pm;
		this.capteurs = capteurs;
	}
	
	public void destructor()
	{
		if(!needInit)
			frame.dispose();
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
		}
	}

	public int distanceXtoWindow(int dist)
	{
		return dist*sizeX/3000;
	}

	public int distanceYtoWindow(int dist)
	{
		return dist*sizeY/2000;
	}

	public int XGridPointtoWindow(int x)
	{
		return x*sizeX/(PointGridSpace.NB_POINTS_POUR_TROIS_METRES-1);
	}

	public int YGridPointtoWindow(int y)
	{
		return sizeY-y*sizeY/(PointGridSpace.NB_POINTS_POUR_DEUX_METRES-1);
	}

	public int XtoWindow(double x)
	{
		return (int)((x+1500)*sizeX/3000);
	}

	public int YtoWindow(double y)
	{
		return (int)((2000-y)*sizeY/2000);
	}
	
	@Override
	public void paint(Graphics g)
	{
		if(afficheFond)
			g.drawImage(image, 0, 0, this);

		if(printObsFixes)
			for(ObstaclesFixes obs : ObstaclesFixes.values)
				obs.getObstacle().print(g, this);

		for(ArrayList<Printable> l : elementsAffichables.values())
			for(Printable p : l)
				p.print(g,this);
		
		iterator.reinit();
		while(iterator.hasNext())				
			iterator.next().print(g, this);
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
				Vec2RW p1 = capteurs.positionsRelatives[i].plusNewVector(new Vec2RO(0, 1000));
				Vec2RW p2 = p1.plusNewVector(new Vec2RO(800, angleCone + Capteurs.orientationsRelatives[i], true));
				Vec2RW p3 = p1.plusNewVector(new Vec2RO(800, - angleCone + Capteurs.orientationsRelatives[i], true));
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
			for(Vec2RW v: point)
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

//		Vec2RO v = ObstaclesFixes.TEST.getObstacle().position;
//		int r = ((ObstacleCircular)ObstaclesFixes.TEST.getObstacle()).getRadius();
//		g.fillOval(XtoWindow(v.x-r), YtoWindow(v.y+r), distanceXtoWindow(r*2), distanceYtoWindow(r*2));
*/
	}

	public void showOnFrame() {
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(sizeX,sizeY));
        frame = new JFrame();
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.dispose();
            }
        });        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(this);
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		printObsFixes = config.getBoolean(ConfigInfo.GRAPHIC_FIXED_OBSTACLES);
		afficheFond = config.getBoolean(ConfigInfo.GRAPHIC_BACKGROUND);
	}
	
	/**
	 * Supprime des obstacles d'une même classe
	 * @param c
	 */
	public void clear(Class<? extends Printable> c)
	{
		elementsAffichables.remove(c);
	}
	
	/**
	 * Ajoute un obstacle
	 * @param o
	 */
	public void add(Printable o)
	{
		ArrayList<Printable> l = elementsAffichables.get(o.getClass());
		if(l == null)
		{
			l = new ArrayList<Printable>();
			elementsAffichables.put(o.getClass(), l);
		}
		l.add(o);
		if(needInit)
			init();
		repaint();
	}
	
}
