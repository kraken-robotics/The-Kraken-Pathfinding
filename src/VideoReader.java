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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;

import config.ConfigInfo;
import container.Container;
import exceptions.ContainerException;
import graphic.Fenetre;
import graphic.PrintBuffer;
import graphic.TimestampedList;
import graphic.printable.Couleur;
import graphic.printable.Layer;
import graphic.printable.Printable;
import obstacles.types.ObstacleRectangular;
import robot.Cinematique;
import robot.RobotReal;
import utils.Log;
import utils.Log.Verbose;
import utils.Vec2RO;

/**
 * Un lecteur de vidéo enregistrée sur le rover
 * @author pf
 *
 */

public class VideoReader {


	public static void main(String[] args) throws ContainerException, InterruptedException
	{
		String filename = null, logfile = null;
		double vitesse = 1;
		boolean debug = false;
		ObstacleRectangular robotBof = null;
		long[] breakPoints = new long[0];
		int indexBP = 0;
		boolean stopOnWarning = false, stopOnCritical = false;
		
		for(int i = 0; i < args.length; i++)
		{
			if(args[i].equals("-s")) // speed
				vitesse = Double.parseDouble(args[++i]);
			else if(args[i].equals("-d")) // debug
				debug = true;
			else if(args[i].equals("-v")) // video
				filename = args[++i];
			else if(args[i].equals("-w")) // warning
				stopOnWarning = true;
			else if(args[i].equals("-c")) // critical
				stopOnCritical = true;
			else if(args[i].equals("-l")) // log
				logfile = args[++i];
			else if(args[i].equals("-b")) // bof
			{
				// Robot bof : (630, 1320), angle = 0
				
				double posX = Double.parseDouble(args[++i]);
				double posY = Double.parseDouble(args[++i]);
				double angle = Double.parseDouble(args[++i]);
				
				robotBof = new ObstacleRectangular(new Vec2RO(posX, posY), 300, 240, angle, Couleur.ROBOT_BOF);
			}
			else if(args[i].equals("-B")) // break
			{
				int nb = Integer.parseInt(args[++i]);
				breakPoints = new long[nb];
				for(int j = 0; j < nb; j++)
					breakPoints[j] = Long.parseLong(args[++i]);
			}
		}
		
		if(filename == null)
		{
			System.out.println("Utilisation : VideoReader -v fichierVideoALire -l fichierLogALire -v speed -d -b posX posY angle");
			return;
		}
		
		Container container = null;
		
		try {
			// on force l'affichage non externe
			ConfigInfo.GRAPHIC_ENABLE.setDefaultValue(true);
			ConfigInfo.GRAPHIC_EXTERNAL.setDefaultValue(false);
			ConfigInfo.GRAPHIC_DIFFERENTIAL.setDefaultValue(false);
			ConfigInfo.SIMULE_SERIE.setDefaultValue(true);
			
			container = new Container();
			PrintBuffer buffer = container.getService(PrintBuffer.class);
			RobotReal robot = container.getService(RobotReal.class);
			Log log = container.getService(Log.class);
			TimestampedList listes;
						
			special("Fichier vidéo : "+filename);
			special("Fichier log : "+logfile);
			special("Vitesse : "+vitesse);		
			if(debug)
				special("Debug activé");
			if(robotBof != null)
			{
				special("RobotBof ajouté");
				buffer.add(robotBof);
			}

			
	        try {
	            FileInputStream fichier = new FileInputStream(filename);
	            ObjectInputStream ois = new ObjectInputStream(fichier);
	            listes = (TimestampedList) ois.readObject();
	            ois.close();
	        }
	        catch(IOException | ClassNotFoundException e)
	        {
	        	log.critical("Chargement échoué !");
	        	return;
	        }
	        
	        long initialDate = System.currentTimeMillis();
	        
	        Thread.sleep(500); // le temps que la fenêtre apparaisse
	        
	        BufferedReader br = null;
	        long nextLog = Long.MAX_VALUE;
	        String nextLine = null;
	        
	        if(logfile != null)
	        {
	        	br = new BufferedReader(new FileReader(logfile));	        
			    nextLine = br.readLine();
			    nextLog = getTimestampLog(nextLine);
	        }
	        
		    long nextVid = listes.getTimestamp(0);
	        long firstTimestamp = Math.min(nextLog, nextVid);

		    int indexListe = 0;
	        boolean stop = false;
		    
		    while(nextVid != Long.MAX_VALUE || nextLog != Long.MAX_VALUE)
			{
	    		if(indexBP < breakPoints.length && breakPoints[indexBP] < Math.min(nextVid, nextLog))
	    		{
	    			indexBP++;
	    			stop = true;
	    		}
		    	
		    	if(stop || System.in.available() > 0)
		    	{
		    		if(stop)
		    			special("Auto-pause !");
		    		else
			    		special("Pause !");

		    		stop = false;
		    		while(System.in.available() > 0)
		    			System.in.read();

	    			long avant = System.currentTimeMillis();

	    			while(System.in.available() == 0)
	    				Thread.sleep(10);
	    			
		    		while(System.in.available() > 0)
		    			System.in.read(); 
	    			
	    			initialDate += (System.currentTimeMillis() - avant);
	    			special("Unpause");
		    	}
		    	
		    	if(nextVid < nextLog)
		    	{
					List<Serializable> tab = listes.getListe(indexListe);
					long deltaT = (long)((nextVid - firstTimestamp) / vitesse);
					long deltaP = System.currentTimeMillis() - initialDate;
					long delta = deltaT - deltaP;
		
					if(delta > 0)
						Thread.sleep(delta);
					
					synchronized(buffer)
					{
						buffer.clearSupprimables();
						int i = 0;
						while(i < tab.size())
						{
							Serializable o = tab.get(i++);
								if(o instanceof Cinematique)
								{
									if(debug)
										System.out.println("Cinématique robot : "+((Cinematique)o).getPosition());
									robot.setCinematique((Cinematique)o);
								}
								else if(o instanceof Printable)
								{
									if(debug)
										System.out.println("Ajout : "+o);
									Layer l = (Layer) tab.get(i++);
									buffer.addSupprimable((Printable)o, l);
								}
								else
									System.err.println("Erreur ! Objet non affichable : "+o.getClass());
							}
					}
					
					indexListe++;
					if(indexListe < listes.size())
						nextVid = listes.getTimestamp(indexListe);
					else
						nextVid = Long.MAX_VALUE;
		    	}
		    	else
		    	{
					long deltaT = (long)((nextLog - firstTimestamp) / vitesse);
					long deltaP = System.currentTimeMillis() - initialDate;
					long delta = deltaT - deltaP;
					
					if(delta > 0)
						Thread.sleep(delta);
					
					if((stopOnWarning && nextLine.contains("WARNING")) || stopOnCritical && nextLine.contains("CRITICAL"))
						stop = true;
					
					System.out.println(nextLine);
					
				    nextLine = getNextLine(br);
				    if(nextLine == null)
				    	nextLog = Long.MAX_VALUE;
				    else
				    	nextLog = getTimestampLog(nextLine);
		    	}
			}
	        special("Fin de l'enregistrement");
	        br.close();
			container.getExistingService(Fenetre.class).waitUntilExit();
		} catch(InterruptedException e) {
			
		} catch(Exception e) {
			System.out.println(e);
			e.printStackTrace();
		} finally {
			if(container != null)
				container.destructor();
		}
	}
	
	private static void special(Object o)
	{
		System.out.println("	\u001B[34m"+o+"\u001B[0m");
	}
	
	private static String getNextLine(BufferedReader br) throws IOException
	{
		String line;
		while((line = br.readLine()) != null)
		{
			if(Verbose.shouldPrint(extractMasque(line)))
    			return line.substring(line.indexOf(" ")+1);
		}
		return line;
	}
	
	private static int extractMasque(String line)
	{
		try
		{
			return Integer.parseInt(line.split(" ")[0]);
		}
		catch(NumberFormatException e)
		{
			return Verbose.all;
		}
	}
	
	private static long getTimestampLog(String line)
	{
    	String time = line.split(" ")[0];
    	try {
    		int first = -1;
    		if(time.startsWith("\u001B["))
    		{
    			first = time.indexOf("m");
    			time = time.substring(first+1);
    		}
    		return Long.parseLong(time);
    	}
    	catch(NumberFormatException e)
    	{
    		return -1;
    	}
	}
	
}
