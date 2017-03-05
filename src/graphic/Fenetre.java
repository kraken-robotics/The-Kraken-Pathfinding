/*
Copyright (C) 2013-2017 Pierre-François Gimenez

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
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;

import config.Config;
import config.ConfigInfo;
import container.Service;
import container.dependances.GUIClass;
import graphic.printable.BackgroundImage;
import robot.RobotReal;
import utils.Log;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Interface graphique
 * @author pf
 *
 */

public class Fenetre extends JPanel implements Service, GUIClass {

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
	private long tempsMinEntreAffichage;
	private long lastAffichage = 0;
	private JFrame frame;
	private WindowExit exit = new WindowExit();
	private ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
	private RobotReal robot;
	private boolean needInit = true;
	private String backgroundPath;
	
	public Fenetre(Log log, RobotReal robot, PrintBuffer buffer, Config config)
	{
		this.log = log;
		this.robot = robot;
		this.buffer = buffer;

		afficheFond = config.getBoolean(ConfigInfo.GRAPHIC_BACKGROUND);
		backgroundPath = config.getString(ConfigInfo.GRAPHIC_BACKGROUND_PATH);
		tempsMinEntreAffichage = 1000/config.getInt(ConfigInfo.GRAPHIC_FPS_MAX);
		sizeX = config.getInt(ConfigInfo.GRAPHIC_SIZE_X);
		sizeY = 2*sizeX/3;
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
				Image image = ImageIO.read(new File(backgroundPath));
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
		if(System.currentTimeMillis() - lastAffichage > tempsMinEntreAffichage)
		{
			lastAffichage = System.currentTimeMillis();
			super.paintComponent(g);
	
			g.clearRect(0, 0, sizeX, sizeY);
			
			buffer.print(g, this, robot);
		}
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
		lastAffichage = 0; // on force l'affichage de la dernière image
		repaint();
		synchronized(exit)
		{
			if(!needInit && !exit.alreadyExited)
			{
				log.debug("Attente de l'arrêt de la fenêtre…");
				exit.wait(5000);
			}
		}
	}

	/**
	 * Ajoute une image au gif final
	 */
	public void saveImage()
	{
		BufferedImage bi = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_RGB);
		paint(bi.getGraphics());
		images.add(bi);
	}
	
	/**
	 * @param file
	 * @param delay
	 */
	public void saveGif(String file, int delay)
	{
		if(!images.isEmpty())
		{
			log.debug("Sauvegarde du gif de "+images.size()+" images…");
		    ImageOutputStream output;
			try {
				try {
					output = new FileImageOutputStream(new File(file));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return;
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}  		 
				
			    GifSequenceWriter writer;
				try {
					writer = new GifSequenceWriter(output, images.get(0).getType(), delay, true);    
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					output.close();
					return;
				} catch (IOException e) {
					e.printStackTrace();
					output.close();
					return;
				}  		 

				try {
				    for(BufferedImage i : images)
				    	writer.writeToSequence(i);
				}
				finally
				{
				    writer.close();    
				    output.close();
				}
				log.debug("Sauvegarde finie !");
	
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}  
		}
	}
	
}
