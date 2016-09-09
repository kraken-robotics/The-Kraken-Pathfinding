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

package graphic;

import javax.imageio.ImageIO;
import javax.swing.*;

import container.Service;
import obstacles.ObstaclesFixes;
import obstacles.CapteursProcess;
import obstacles.memory.ObstaclesIteratorPresent;
import obstacles.types.Obstacle;
import obstacles.types.ObstacleCircular;
import obstacles.types.ObstacleRectangular;
import pathfinding.dstarlite.gridspace.GridSpace;
import pathfinding.dstarlite.gridspace.PointGridSpace;
import pathfinding.dstarlite.gridspace.PointGridSpaceManager;
import robot.RobotReal;
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
	
	private ArrayList<Printable> elementsAffichables = new ArrayList<Printable>();
	
	private ObstaclesIteratorPresent iterator;

	private boolean printObsFixes;
	private boolean printObsCapteurs;
    
	private GridSpace gs;
	private PointGridSpaceManager pm;
	private RobotReal robot;
	private boolean needInit = true;
	
	public Fenetre(Log log, ObstaclesIteratorPresent iterator, GridSpace gs, PointGridSpaceManager pm, RobotReal robot)
	{
		this.log = log;
		this.iterator = iterator;
		this.gs = gs;
		this.pm = pm;
		this.robot = robot;
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
			sizeX = image.getWidth(this);
			sizeY = image.getHeight(this);
		} catch (IOException e) {
			e.printStackTrace();
		}

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
				obs.getObstacle().print(g, this, robot);

		for(Printable p : elementsAffichables)
			p.print(g, this, robot);
		
		if(printObsCapteurs)
		{
			iterator.reinit();
			while(iterator.hasNext())				
				iterator.next().print(g, this, robot);
		}
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
		printObsCapteurs = config.getBoolean(ConfigInfo.GRAPHIC_OBSTACLES);
		printObsFixes = config.getBoolean(ConfigInfo.GRAPHIC_FIXED_OBSTACLES);
		afficheFond = config.getBoolean(ConfigInfo.GRAPHIC_BACKGROUND);
	}
	
	/**
	 * Supprime tous les obstacles
	 * @param c
	 */
	public void clear()
	{
		elementsAffichables.clear();
	}
	
	/**
	 * Ajoute un obstacle
	 * @param o
	 */
	public void add(Printable o)
	{
		elementsAffichables.add(o);
		if(needInit)
			init();
		repaint();
	}
	
}
