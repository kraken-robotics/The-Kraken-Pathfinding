/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Scanner;
import config.ConfigInfo;
import container.Container;
import graphic.Fenetre;
import graphic.PrintBuffer;
import graphic.TimestampedList;
import graphic.printable.Couleur;
import graphic.printable.Layer;
import graphic.printable.Printable;
import graphic.printable.Vector;
import obstacles.types.ObstacleRectangular;
import robot.AnglesRoues;
import robot.Cinematique;
import robot.RobotReal;
import utils.Log;
import utils.Log.Verbose;
import utils.Vec2RO;

/**
 * Un lecteur de vidéo enregistrée sur le rover
 * 
 * @author pf
 *
 */

public class VideoReader
{

	public static void main(String[] args)
	{
		String filename = null, logfile = null;
		double vitesse = 1;
		boolean debug = false;
		ObstacleRectangular robotBof = null;
		long[] breakPoints = new long[0];
		int indexBP = 0;
		boolean stopOnWarning = false, stopOnCritical = false;
		boolean frameToFrame = false;
		long dateSkip = -1;
		boolean skipdone = false;
		long nextStopFTF = 0;
		
		// on force l'affichage non externe
		ConfigInfo.GRAPHIC_ENABLE.setDefaultValue(true);
		ConfigInfo.GRAPHIC_EXTERNAL.setDefaultValue(false);
		ConfigInfo.GRAPHIC_DIFFERENTIAL.setDefaultValue(false);
		ConfigInfo.GRAPHIC_ROBOT_AND_SENSORS.setDefaultValue(false);
		ConfigInfo.GRAPHIC_PRODUCE_GIF.setDefaultValue(false);
		ConfigInfo.GRAPHIC_ZOOM.setDefaultValue(0);

		ConfigInfo.SIMULE_SERIE.setDefaultValue(true);
		
		ConfigInfo.DEBUG_CAPTEURS.setDefaultValue(false);
		ConfigInfo.DEBUG_SCRIPTS.setDefaultValue(false);
		ConfigInfo.DEBUG_ASSER.setDefaultValue(false);
		ConfigInfo.DEBUG_DEBUG.setDefaultValue(false);
		ConfigInfo.DEBUG_PF.setDefaultValue(false);
		ConfigInfo.DEBUG_CORRECTION.setDefaultValue(false);		
		ConfigInfo.DEBUG_REPLANIF.setDefaultValue(false);
		ConfigInfo.DEBUG_SERIE.setDefaultValue(false);
		ConfigInfo.DEBUG_SERIE_TRAME.setDefaultValue(false);
		
		for(int i = 0; i < args.length; i++)
		{
			if(args[i].equals("-s")) // speed
				vitesse = Double.parseDouble(args[++i]);
			else if(args[i].equals("-S")) // skip
				dateSkip = Long.parseLong(args[++i]);
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
			else if(args[i].equals("-withsprite")) // pas de sprite du robot
				ConfigInfo.GRAPHIC_ROBOT_AND_SENSORS.setDefaultValue(true);
			else if(args[i].equals("-vcapt")) // verbose capteurs
				ConfigInfo.DEBUG_CAPTEURS.setDefaultValue(true);
			else if(args[i].equals("-vscripts")) // verbose scripts
				ConfigInfo.DEBUG_SCRIPTS.setDefaultValue(true);
			else if(args[i].equals("-vasser")) // verbose asser
				ConfigInfo.DEBUG_ASSER.setDefaultValue(true);
			else if(args[i].equals("-vdebug")) // verbose capteurs
				ConfigInfo.DEBUG_DEBUG.setDefaultValue(true);
			else if(args[i].equals("-vpf")) // verbose pf
				ConfigInfo.DEBUG_PF.setDefaultValue(true);
			else if(args[i].equals("-vreplanif")) // verbose replanif
				ConfigInfo.DEBUG_REPLANIF.setDefaultValue(true);
			else if(args[i].equals("-vserie")) // verbose série
				ConfigInfo.DEBUG_SERIE.setDefaultValue(true);
			else if(args[i].equals("-vcorr")) // verbose correction
				ConfigInfo.DEBUG_CORRECTION.setDefaultValue(true);
			else if(args[i].equals("-gif")) // génération d'un gif
			{
				ConfigInfo.GRAPHIC_PRODUCE_GIF.setDefaultValue(true);
				ConfigInfo.GIF_FILENAME.setDefaultValue(args[++i]);
			}
			else if(args[i].equals("-zoom")) // récupère le zoom
				ConfigInfo.GRAPHIC_ZOOM.setDefaultValue(Double.parseDouble(args[++i]));
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
			else
				System.err.println("Option inconnue ! " + args[i]);
		}

		if(filename == null && logfile == null)
		{
			System.out.println("Utilisation : VideoReader -v videoFile -l logFile [-s speed] [-w] [-c] [-b posX posY angle] [-B n ...]");
			System.out.println("-w : autostop on warning ");
			System.out.println("-c : autostop on critical ");
			System.out.println("-S date : start at this date");
			System.out.println("-b : add robot bof© ");
			System.out.println("-B n t1 t2 … tn: add n breakpoints at timestamps t1,… tn ");
			System.out.println("-s speed : set reading speed. 2 is twice as fast, 0.5 twice as slow");
			System.out.println("-withsprite : affiche le sprite du robot et des capteurs");
			System.out.println("-vcapt : verbose capteurs");
			System.out.println("-vscripts : verbose scripts");
			System.out.println("-vasser : verbose pour asser");
			System.out.println("-vdebug : verbose debug général");
			System.out.println("-vpf : verbose pathfinding");
			System.out.println("-vreplanif : verbose de la replanification à la volée");
			System.out.println("-vserie : verbose de la série");
			System.out.println("-vcorr : verbose de la correction d'odométrie");
			System.out.println("-gif filename : produce a gif");
			return;
		}

		Scanner sc = new Scanner(System.in);
		Container container = null;
		if(filename == null)
			ConfigInfo.GRAPHIC_ENABLE.setDefaultValue(false);

		try
		{
			container = new Container();
			
			PrintBuffer buffer = null;
			if(filename != null)
				buffer = container.getService(PrintBuffer.class);
			RobotReal robot = container.getService(RobotReal.class);
			Log log = container.getService(Log.class);
			TimestampedList listes = null;

			special("Fichier vidéo : " + filename);
			special("Fichier log : " + logfile);
			special("Vitesse : " + vitesse);
			if(dateSkip != -1)
				special("Skip to : " + dateSkip);
			if(debug)
				special("Debug activé");
			if(robotBof != null)
			{
				if(filename != null)
					special("RobotBof pas ajoutable : pas de vidéo");
				else
				{
					special("RobotBof ajouté");
					buffer.add(robotBof);
				}
			}

			if(filename != null)
			{
				try
				{
					FileInputStream fichier = new FileInputStream(filename);
					ObjectInputStream ois = new ObjectInputStream(fichier);
					listes = (TimestampedList) ois.readObject();
					ois.close();
				}
				catch(IOException | ClassNotFoundException e)
				{
					log.critical("Chargement échoué ! "+e);
					return;
				}
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

			long nextVid;

			if(listes == null)
				nextVid = Long.MAX_VALUE;
			else
				nextVid = listes.getTimestamp(0);

			long firstTimestamp = Math.min(nextLog, nextVid);

			int indexListe = 0;
			boolean stop = false;

			special("At any point, type \"stop\" to stop the VideoReader.");
			
			while(nextVid != Long.MAX_VALUE || nextLog != Long.MAX_VALUE)
			{
				if(indexBP < breakPoints.length && breakPoints[indexBP] < Math.min(nextVid, nextLog))
				{
					if(!frameToFrame)
						special("Breakpoint : "+breakPoints[indexBP]);
					indexBP++;
					stop = true;
				}
				
				if(frameToFrame && nextStopFTF < Math.min(nextVid, nextLog))
					stop = true;

				if(stop || System.in.available() > 0)
				{
					if(!frameToFrame)
					{						
						if(stop)
							special("Auto-pause !");
						else
							special("Pause ! Enter \"ftf\" to enter the frame-to-frame mode");
					}
					
					stop = false;
					while(System.in.available() > 0)
						System.in.read();

					long avant = System.currentTimeMillis();
					nextStopFTF = Math.min(nextVid, nextLog) + 5;

					String l = sc.nextLine();
					if(!frameToFrame && l.equals("ftf"))
					{
						frameToFrame = true;
						nextStopFTF = Math.min(nextVid, nextLog) + 5;
						special("Entre \"normal\" to resume the normal (non-frame-to-frame) mode");
					}
					else if(l.equals("stop"))
					{
						br.close();
						throw new InterruptedException();
					}
					else if(frameToFrame && l.equals("normal"))
					{
						special("Normal mode resumed");
						frameToFrame = false;
					}
					
/*					while(System.in.available() == 0)
						Thread.sleep(10);

					while(System.in.available() > 0)
						System.in.read();
*/
					initialDate += (System.currentTimeMillis() - avant);
					if(!frameToFrame)
						special("Unpause");
				}

				if(!skipdone && Math.min(nextVid, nextLog) > dateSkip)
				{
					stop = true;
					skipdone = true;
					initialDate -= dateSkip;
				}
				
				if(nextVid < nextLog)
				{
					List<Serializable> tab = listes.getListe(indexListe);
					long deltaT = (long) ((nextVid - firstTimestamp) / vitesse);
					long deltaP = System.currentTimeMillis() - initialDate;
					long delta = deltaT - deltaP;

					if(delta > 0 && dateSkip < nextVid)
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
									System.out.println("Cinématique robot : " + ((Cinematique) o).getPosition());
								robot.setCinematique((Cinematique) o);
							}
							else if(o instanceof AnglesRoues)
							{
								if(debug)
									System.out.println("Angles des roues du robot : " + ((AnglesRoues) o).angleRoueGauche + ", " + ((AnglesRoues) o).angleRoueDroite);
								robot.setAngleRoues(((AnglesRoues) o).angleRoueGauche, ((AnglesRoues) o).angleRoueDroite);
							}
							else if(o instanceof Vector)
							{
								robot.setVector((Vector) o);
							}
							else if(o instanceof Printable)
							{
								if(debug)
									System.out.println("Ajout : " + o);
								Layer l = (Layer) tab.get(i++);
								buffer.addSupprimable((Printable) o, l);
							}
							else
								System.err.println("Erreur ! Objet non affichable : " + o.getClass());
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
					long deltaT = (long) ((nextLog - firstTimestamp) / vitesse);
					long deltaP = System.currentTimeMillis() - initialDate;
					long delta = deltaT - deltaP;

					if(delta > 0 && dateSkip < nextLog)
						Thread.sleep(delta);

					if(skipdone && ((stopOnWarning && nextLine.contains("WARNING")) || stopOnCritical && nextLine.contains("CRITICAL")))
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
		}
		catch(Exception e)
		{}
		finally
		{
			try
			{
				sc.close();
				System.exit(container.destructor().code);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private static void special(Object o)
	{
		System.out.println("	\u001B[34m" + o + "\u001B[0m");
	}

	private static String getNextLine(BufferedReader br) throws IOException
	{
		String line;
		while((line = br.readLine()) != null)
			if(Verbose.shouldPrint(extractMasque(line)))
				return line.substring(line.indexOf(" ") + 1);

		return null;
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
		try
		{
			int first = -1;
			if(time.startsWith("\u001B["))
			{
				first = time.indexOf("m");
				time = time.substring(first + 1);
			}
			return Long.parseLong(time);
		}
		catch(NumberFormatException e)
		{
			return -1;
		}
	}

}
