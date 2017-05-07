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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;

import config.ConfigInfo;
import container.Container;
import exceptions.ContainerException;
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
		String filename = null;
		double vitesse = 1;
		
		for(int i = 0; i < args.length; i++)
		{
			if(args[i].equals("-v"))
				vitesse = Double.parseDouble(args[++i]);
			else
				filename = args[i];
		}
		
		if(filename == null)
		{
			System.out.println("Utilisation : VideoReader fichierALire -v vitesse");
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
			
			log.debug("Fichier : "+filename);
			log.debug("Vitesse : "+vitesse);			
			
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
	        
	        long firstTimestamp = listes.getTimestamp(0);
	        long initialDate = System.currentTimeMillis();
	        
	        log.debug("Taille : "+listes.size());
	        
	        for(int j = 0; j < listes.size(); j++)
			{
				List<Serializable> tab = listes.getListe(j);
				long deltaT = (long)((listes.getTimestamp(j) - firstTimestamp) / vitesse);
				long deltaP = System.currentTimeMillis() - initialDate;
				long delta = deltaT - deltaP;
	        	log.debug("Timestamp : "+deltaT);
	
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
		//						log.debug("Cinématique ! "+((Cinematique)o).getPosition());
								robot.setCinematique((Cinematique)o);
							}
							else if(o instanceof Printable)
							{
								Layer l = (Layer) tab.get(i++);
								buffer.addSupprimable((Printable)o, l);
							}
							else
								log.critical("Erreur ! Objet non affichable : "+o.getClass());
						}
				}
			}
	        while(true)
	        	Thread.sleep(1000);
		} catch(InterruptedException e) {
			
		} catch(Exception e) {
			System.out.println(e);
			e.printStackTrace();
		} finally {
			if(container != null)
				container.destructor();
		}
	}
}
