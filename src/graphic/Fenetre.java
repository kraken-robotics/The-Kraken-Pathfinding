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
import obstacles.memory.ObstaclesIteratorPresent;
import robot.RobotReal;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

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
	
	private static final long serialVersionUID = 1L;
	protected Log log;
	private PrintBuffer buffer;
	
	private boolean afficheFond;
	private int sizeX = 450, sizeY = 300;
	private Image image;
	private JFrame frame;
	private WindowExit exit = new WindowExit();
		
	private ObstaclesIteratorPresent iterator;

	private boolean printObsCapteurs = false;
    
	private RobotReal robot;
	private boolean needInit = true;
	
	public Fenetre(Log log, ObstaclesIteratorPresent iterator, RobotReal robot, PrintBuffer buffer)
	{
		this.log = log;
		this.iterator = iterator;
		this.robot = robot;
		this.buffer = buffer;
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

		/**
		 * Affichage des obstacles de proximité en gris
		 */
		g.setColor(Couleur.GRIS.couleur);

		if(printObsCapteurs)
		{
			iterator.reinit();
			while(iterator.hasNext())				
				iterator.next().print(g, this, robot);
		}		

		g.setColor(Couleur.NOIR.couleur);

		buffer.print(g, this, robot);
	}

	/**
	 * Affiche la fenêtre
	 */
	private void showOnFrame()
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
		printObsCapteurs = config.getBoolean(ConfigInfo.GRAPHIC_PROXIMITY_OBSTACLES);
		afficheFond = config.getBoolean(ConfigInfo.GRAPHIC_BACKGROUND);
	}
		
	/**
	 * Réaffiche
	 */
	public synchronized void refresh()
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
				exit.wait(5000);
			}
		}
	}
	
}
