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
import graphic.printable.Layer;
import graphic.printable.Printable;
import robot.Cinematique;
import robot.RobotReal;
import utils.Log;

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
		
		for(int i = 0; i < args.length; i++)
		{
			if(args[i].equals("-s"))
				vitesse = Double.parseDouble(args[++i]);
			else if(args[i].equals("-d"))
				debug = true;
			else if(args[i].equals("-v"))
				filename = args[++i];
			else if(args[i].equals("-l"))
				logfile = args[++i];
		}
		
		if(filename == null)
		{
			System.out.println("Utilisation : VideoReader -v fichierVideoALire -l fichierLogALire -v speed -d");
			return;
		}
		
		Container container = null;
		try {
			// on force l'affichage non externe
			ConfigInfo.GRAPHIC_EXTERNAL.setDefaultValue(false);
			ConfigInfo.GRAPHIC_DIFFERENTIAL.setDefaultValue(false);
			container = new Container();
			PrintBuffer buffer = container.getService(PrintBuffer.class);
			RobotReal robot = container.getService(RobotReal.class);
			Log log = container.getService(Log.class);
			TimestampedList listes;
			
			System.out.println("Fichier : "+filename);
			System.out.println("Vitesse : "+vitesse);		
			if(debug)
				System.out.println("Debug activé");
			
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
	        
	        Thread.sleep(1000); // le temps que la fenêtre apparaisse
	        log.debug("Taille : "+listes.size());
	        
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
	        
		    while(nextVid != Long.MAX_VALUE || nextLog != Long.MAX_VALUE)
			{
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
					
					System.out.println(nextLine);
					
				    nextLine = br.readLine();
				    if(nextLine == null)
				    	nextLog = Long.MAX_VALUE;
				    else
				    	nextLog = getTimestampLog(nextLine);
		    	}
			}
	        System.out.println("Fin de l'enregistrement");
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
