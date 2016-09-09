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

package utils;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import container.Service;

/**
 * Service de log, affiche à l'écran des informations avec différents niveaux de couleurs
 * @author pf
 *
 */

public class Log implements Service
{
	// Dépendances
	private boolean logClosed = false;
	private BufferedWriter writer = null;

	// Ne pas afficher les messages de bug permet d'économiser du temps CPU
	private boolean affiche_debug = true;
	
	// Sauvegarder les logs dans un fichier
	private boolean sauvegarde_fichier = false;
	
	// Ecriture plus rapide sans appel à la pile d'exécution
	private boolean fastLog = false;
	
	/**
	 * date du démarrage
	 */
	private long dateInitiale;
	
	public Log()
	{
		dateInitiale = System.currentTimeMillis();
	}
	
	/**
	 * Affichage de debug, en vert
	 * @param message
	 * @param objet
	 */
	public void debug(Object message)
	{
		if(fastLog)
			ecrireFast(message, true, System.out);
		else
			ecrire(" ", message, true, System.out);
	}

	/**
	 * Affichage de warnings, en orange
	 * @param message
	 * @param objet
	 */
	public void warning(Object message)
	{
		if(fastLog)
			ecrireFast(message, false, System.out);
		else
			ecrire(" WARNING ", message, false, System.out);
	}
	
	/**
	 * Affichage d'erreurs critiques, en rouge
	 * @param message
	 * @param objet
	 */
	public void critical(Object message)
	{
		if(fastLog)
			ecrireFast(message, false, System.err);
		else
			ecrire(" CRITICAL ", message, false, System.err);
	}

	/**
	 * Ce synchronized peut ralentir le programme, mais s'assure que les logs ne se chevauchent pas.
	 * @param niveau
	 * @param message
	 * @param couleur
	 * @param ou
	 */
	private synchronized void ecrire(String niveau, Object message, boolean debug, PrintStream ou)
	{
		if(logClosed)
			System.out.println("WARNING * Log fermé! Message: "+message);
		else if(!debug || affiche_debug || sauvegarde_fichier)
		{
			long date = System.currentTimeMillis() - dateInitiale;
			String affichage;
			StackTraceElement elem = Thread.currentThread().getStackTrace()[3];
			affichage = date+niveau+elem.getClassName().substring(elem.getClassName().lastIndexOf(".")+1)+":"+elem.getLineNumber()+" > "+message;//+"\u001B[0m";

			if(!debug || affiche_debug)
			{
				if(sauvegarde_fichier)
				{
					try{
					     writer.write(message+"\n");
					}
					catch(IOException e)
					{
						e.printStackTrace();
					}
				}
//				else
					ou.println(affichage);
			}
		}
	}
	
	/**
	 * Affichage rapide
	 * @param niveau
	 * @param message
	 * @param couleur
	 * @param ou
	 */
	private void ecrireFast(Object message, boolean debug, PrintStream ou)
	{
		if(logClosed)
			System.out.println("WARNING * Log fermé! Message: "+message);
		else if(!debug || affiche_debug || sauvegarde_fichier)
		{
			long date = System.currentTimeMillis() - dateInitiale;
			String affichage = date+" > "+message;
			if(!debug || affiche_debug)
			{
				if(sauvegarde_fichier)
				{
					try{
					     writer.write(message+"\n");
					}
					catch(IOException e)
					{
						e.printStackTrace();
					}					
				}
//				else
					ou.println(affichage);
			}
		}
	}
	
	/**
	 * Sorte de destructeur, dans lequel le fichier est sauvegardé.
	 */
	public void close()
	{
		if(sauvegarde_fichier)
			try {
				debug("Sauvegarde du fichier de logs");
				if(writer != null)
				{
					writer.flush();
					writer.close();
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}

		logClosed = true;
	}
	
	@Override
	public void updateConfig(Config config)
	{}
	
	@Override
	public void useConfig(Config config)
	{
		affiche_debug = config.getBoolean(ConfigInfo.AFFICHE_DEBUG);
		sauvegarde_fichier = config.getBoolean(ConfigInfo.SAUVEGARDE_FICHIER);
		fastLog = config.getBoolean(ConfigInfo.FAST_LOG);
		if(sauvegarde_fichier)
		{
			GregorianCalendar calendar = new GregorianCalendar();
			String heure = calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND);
			String file = "logs/LOG-"+heure+".txt";
			try {
				writer = new BufferedWriter(new FileWriter(file)); 
				debug("Un fichier de sauvegarde est utilisé: "+file);
			}
			catch(FileNotFoundException e)
			{
				try {
					Runtime.getRuntime().exec("mkdir logs");
					try {
						Thread.sleep(50);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					writer = new BufferedWriter(new FileWriter(file));
					debug("Un fichier de sauvegarde est utilisé: "+file);
				} catch (IOException e1) {
					critical("Erreur (1) lors de la création du fichier : "+e1);
					sauvegarde_fichier = false;
				}
			} catch (IOException e) {
				critical("Erreur (2) lors de la création du fichier : "+e);
				sauvegarde_fichier = false;
			}
		}
		debug("Service de log démarré");
	}

}
