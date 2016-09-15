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
import graphic.printable.BackgroundImage;
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
	private int sizeX, sizeY;
	private JFrame frame;
	private WindowExit exit = new WindowExit();
		
	private RobotReal robot;
	private boolean needInit = true;
	
	public Fenetre(Log log, RobotReal robot, PrintBuffer buffer)
	{
		this.log = log;
		this.robot = robot;
		this.buffer = buffer;
	}
	
	private class WindowExit extends WindowAdapter
	{		
		public volatile boolean alreadyExited = false;
		
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
		
		if(afficheFond)
		{
			try {
				Image image = ImageIO.read(new File("minitable2016.png"));
				sizeX = image.getWidth(this); // on ajuste la taille de la fenêtre à l'image
				sizeY = image.getHeight(this);
				buffer.add(new BackgroundImage(image));
			} catch (IOException e) {
				e.printStackTrace();
			}
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
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		g.clearRect(0, 0, sizeX, sizeY);

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
		afficheFond = config.getBoolean(ConfigInfo.GRAPHIC_BACKGROUND);
		sizeX = config.getInt(ConfigInfo.GRAPHIC_SIZE_X);
		sizeY = 2*sizeX/3;
	}
		
	/**
	 * Réaffiche
	 */
	public void refresh()
	{
		if(needInit)
			init();
		repaint();		
	}

	/**
	 * Attend que la fenêtre soit fermée
	 * @throws InterruptedException
	 */
	public void waitUntilExit() throws InterruptedException
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
