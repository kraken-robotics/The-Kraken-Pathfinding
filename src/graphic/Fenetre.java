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
import obstacles.memory.ObstaclesIteratorPresent;
import pathfinding.dstarlite.gridspace.PointGridSpace;
import pathfinding.dstarlite.gridspace.PointGridSpaceManager;
import robot.RobotReal;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Interface graphique
 * @author pf
 *
 */

public class Fenetre extends JPanel implements Service {

	/**
	 * Couleurs surtout utilisées pour le dstarlite
	 * @author pf
	 *
	 */
	public enum Couleur {
		BLANC(new Color(255, 255, 255, 255)),
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
	
	private static final long serialVersionUID = 1L;
	protected Log log;
	private PointGridSpaceManager pointm;
	
	private boolean afficheFond;
	private int sizeX = 450, sizeY = 300;
	private Image image;
	private JFrame frame;
	private WindowExit exit = new WindowExit();
	
	private ArrayList<Printable> elementsAffichables = new ArrayList<Printable>();
	private Couleur[] grid = new Couleur[PointGridSpace.NB_POINTS];
	
	private ObstaclesIteratorPresent iterator;

	private boolean printObsFixes = false;
	private boolean printObsCapteurs = false;
    private boolean printDStarLite = false;
    
	private RobotReal robot;
	private boolean needInit = true;
	
	public Fenetre(Log log, ObstaclesIteratorPresent iterator, RobotReal robot, PointGridSpaceManager pointm)
	{
		this.log = log;
		this.iterator = iterator;
		this.robot = robot;
		this.pointm = pointm;
	}
	
	private class WindowExit extends WindowAdapter
	{		
		public boolean alreadyExited = false;
        @Override
        public synchronized void windowClosing(WindowEvent e) {
            notify();
            alreadyExited = true;
            frame.dispose();
        }
	}
	
	/**
	 * Initialisation
	 */
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

		showOnFrame();
	}
			
	/**
	 * Met à jour la couleur d'un nœud
	 * @param gridpoint
	 * @param couleur
	 */
	public void setColor(PointGridSpace gridpoint, Couleur couleur)
	{
		if(printDStarLite && grid[gridpoint.hashCode()] != couleur)
		{
			grid[gridpoint.hashCode()] = couleur;
			affiche();
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

	public int XtoWindow(double x)
	{
		return (int)((x+1500)*sizeX/3000);
	}

	public int YtoWindow(double y)
	{
		return (int)((2000-y)*sizeY/2000);
	}
	
	@Override
	public synchronized void paint(Graphics g)
	{
		if(afficheFond)
			g.drawImage(image, 0, 0, this);
		else
			g.clearRect(0, 0, sizeX, sizeY);

		g.setColor(Couleur.NOIR.couleur);

		if(printObsFixes)
			for(ObstaclesFixes obs : ObstaclesFixes.values())
				obs.getObstacle().print(g, this, robot);

		for(Printable p : elementsAffichables)
			p.print(g, this, robot);
		
		if(printObsCapteurs)
		{
			iterator.reinit();
			while(iterator.hasNext())				
				iterator.next().print(g, this, robot);
		}
		
		if(printDStarLite)
			for(int i = 0; i < PointGridSpace.NB_POINTS; i++)
				if(grid[i] != null)
				{
					g.setColor(grid[i].couleur);
					pointm.get(i).print(g, this, robot);
				}
	
	}

	/**
	 * Affiche la fenêtre
	 */
	public void showOnFrame()
	{
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(sizeX,sizeY));
        frame = new JFrame();
        
        /*
         * Fermeture de la fenêtre quand on clique sur la croix
         */
        frame.addWindowListener(exit);
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
		printDStarLite = config.getBoolean(ConfigInfo.GRAPHIC_D_STAR_LITE);
		printObsCapteurs = config.getBoolean(ConfigInfo.GRAPHIC_OBSTACLES);
		printObsFixes = config.getBoolean(ConfigInfo.GRAPHIC_FIXED_OBSTACLES);
		afficheFond = config.getBoolean(ConfigInfo.GRAPHIC_BACKGROUND);
	}
	
	public synchronized void clearGrid()
	{
		for(int i = 0; i < grid.length; i++)
			grid[i] = null;
	}
	
	/**
	 * Supprime tous les obstacles
	 * @param c
	 */
	public synchronized void clear()
	{
		elementsAffichables.clear();
	}
	
	/**
	 * Ajoute un obstacle
	 * @param o
	 */
	public synchronized void add(Printable o)
	{
		elementsAffichables.add(o);
		affiche();
	}
	
	/**
	 * Réaffiche
	 */
	public void affiche()
	{
		if(needInit)
			init();
		repaint();		
	}

	/**
	 * Attend que la fenêtre soit fermée
	 * @throws InterruptedException
	 */
	public synchronized void waitUntilExit() throws InterruptedException
	{
		synchronized(exit)
		{
			if(!needInit && !exit.alreadyExited)
			{
				log.debug("Attente de l'arrêt de la fenêtre…");
				exit.wait();
			}
		}
	}
	
}
