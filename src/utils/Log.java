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

package utils;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import config.Config;
import config.ConfigInfo;
import config.DynamicConfigurable;
import container.Service;

/**
 * Service de log, affiche à l'écran des informations avec différents niveaux de
 * couleurs
 * 
 * @author pf
 *
 */

public class Log implements Service, DynamicConfigurable
{
	public enum Verbose
	{
		SERIE(ConfigInfo.DEBUG_SERIE, true),
		CORRECTION(ConfigInfo.DEBUG_CORRECTION, false),
		TRAME(ConfigInfo.DEBUG_SERIE_TRAME, false),
		CAPTEURS(ConfigInfo.DEBUG_CAPTEURS, true),
		ASSER(ConfigInfo.DEBUG_ASSER, true),
		REPLANIF(ConfigInfo.DEBUG_REPLANIF, true),
		SCRIPTS(ConfigInfo.DEBUG_SCRIPTS, true),
		PF(ConfigInfo.DEBUG_PF, true),
		DEBUG(ConfigInfo.DEBUG_DEBUG, true);

		public final int masque;
		public final ConfigInfo c;
		protected boolean status;
		public boolean printInFile;

		public static final int all = (1 << (Verbose.values().length + 1)) - 1;
		private static Verbose[] values = values();

		private Verbose(ConfigInfo c, boolean printInFile)
		{
			masque = 1 << ordinal();
			this.c = c;
			this.printInFile = printInFile;
		}

		public static boolean shouldPrintInFile(int value)
		{
			if(value == all)
				return true;
			for(Verbose v : values)
				if(v.printInFile && (value & v.masque) != 0)
					return true;
			return false;
		}

		public static boolean shouldPrint(int value)
		{
			if(value == all)
				return true;
			for(Verbose v : values)
				if(v.status && (value & v.masque) != 0)
					return true;
			return false;
		}
	}

	private enum Niveau
	{
		DEBUG(" ", "\u001B[0m", System.out),
		WARNING(" WARNING ", "\u001B[33m", System.out),
		CRITICAL(" CRITICAL ", "\u001B[31m", System.err);

		public String entete, couleur;
		public PrintStream stream;

		private Niveau(String entete, String couleur, PrintStream stream)
		{
			this.entete = entete;
			this.couleur = couleur;
			this.stream = stream;
		}
	}

	// Dépendances
	private boolean logClosed = false;
	private BufferedWriter writer = null;

	// Sauvegarder les logs dans un fichier
	private boolean sauvegarde_fichier = false;

	// Ecriture plus rapide sans appel à la pile d'exécution
	private boolean fastLog = false;

	private boolean useColor = false;

	private String couleurDefault = "\u001B[0m";

	/**
	 * date du démarrage
	 */
	private long dateInitiale = System.currentTimeMillis();
	private long dateDebutMatch = -1;

	public long getDateInitiale()
	{
		return dateInitiale;
	}

	/**
	 * Affichage de debug, en vert
	 * 
	 * @param message
	 * @param objet
	 */
	public void debug(Object message)
	{
		ecrire(message.toString(), Niveau.DEBUG, Verbose.DEBUG.masque);
	}

	/**
	 * Affichage de debug, en vert
	 * 
	 * @param message
	 * @param objet
	 */
	public void debug(Object message, int masque)
	{
		if(masque == 0)
			masque = Verbose.DEBUG.masque;
		ecrire(message.toString(), Niveau.DEBUG, masque);
	}

	/**
	 * Affichage de warnings, en orange
	 * 
	 * @param message
	 * @param objet
	 */
	public void warning(Object message, int masque)
	{
		ecrire(message.toString(), Niveau.WARNING, masque);
	}

	/**
	 * Affichage de warnings, en orange
	 * 
	 * @param message
	 * @param objet
	 */
	public void warning(Object message)
	{
		ecrire(message.toString(), Niveau.WARNING, Verbose.all);
	}

	/**
	 * Affichage d'erreurs critiques, en rouge
	 * 
	 * @param message
	 * @param objet
	 */
	public void critical(Object message)
	{
		ecrire(message.toString(), Niveau.CRITICAL, Verbose.all);
	}

	/**
	 * Ce synchronized peut ralentir le programme, mais s'assure que les logs ne
	 * se chevauchent pas.
	 * 
	 * @param niveau
	 * @param message
	 * @param couleur
	 * @param ou
	 */
	private synchronized void ecrire(String message, Niveau niveau, int masque)
	{
		if(logClosed)
			System.out.println("WARNING * Log fermé! Message: " + message);
		else
		{
			long date = System.currentTimeMillis() - dateInitiale;
			String tempsMatch = "";
			if(dateDebutMatch != -1)
				tempsMatch = " T+" + (System.currentTimeMillis() - dateDebutMatch);

			String affichage;
			if(fastLog)
				affichage = date + tempsMatch + " > " + message;
			else
			{
				StackTraceElement elem = Thread.currentThread().getStackTrace()[3];
				affichage = date + tempsMatch + niveau.entete + elem.getClassName().substring(elem.getClassName().lastIndexOf(".") + 1) + ":" + elem.getLineNumber() + " (" + Thread.currentThread().getName() + ") > " + message;
			}

			if(sauvegarde_fichier && writer != null && Verbose.shouldPrintInFile(masque))
			{
				try
				{
					// On met la couleur dans le fichier
					if(useColor)
						writer.write(masque + " " + niveau.couleur + affichage + couleurDefault + "\n");
					else
						writer.write(masque + " " + affichage + "\n");
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
			if(Verbose.shouldPrint(masque))
				niveau.stream.println(affichage);
		}
	}

	/**
	 * Sorte de destructeur, dans lequel le fichier est sauvegardé.
	 */
	public void close()
	{
		if(sauvegarde_fichier)
			try
			{
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

	public void useConfig(Config config)
	{
		sauvegarde_fichier = config.getBoolean(ConfigInfo.SAUVEGARDE_LOG);
		useColor = config.getBoolean(ConfigInfo.COLORED_LOG);
		fastLog = config.getBoolean(ConfigInfo.FAST_LOG);

		for(Verbose v : Verbose.values())
		{
			v.status = config.getBoolean(v.c);
			v.printInFile |= v.status;
		}

		if(sauvegarde_fichier)
		{
			String file = "logs/" + new SimpleDateFormat("dd-MM.HH:mm").format(new Date()) + ".txt";
			try
			{
				writer = new BufferedWriter(new FileWriter(file));
				debug("Un fichier de sauvegarde est utilisé: " + file);
			}
			catch(FileNotFoundException e)
			{
				try
				{
					Runtime.getRuntime().exec("mkdir logs");
					try
					{
						Thread.sleep(50);
					}
					catch(InterruptedException e1)
					{
						e1.printStackTrace();
					}
					writer = new BufferedWriter(new FileWriter(file));
					debug("Un fichier de sauvegarde est utilisé: " + file);
				}
				catch(IOException e1)
				{
					critical("Erreur (1) lors de la création du fichier : " + e1);
					sauvegarde_fichier = false;
				}
			}
			catch(IOException e)
			{
				critical("Erreur (2) lors de la création du fichier : " + e);
				sauvegarde_fichier = false;
			}
		}
		debug("Service de log démarré");
	}

	@Override
	public void updateConfig(Config config)
	{
		dateDebutMatch = config.getLong(ConfigInfo.DATE_DEBUT_MATCH);
	}

	public PrintWriter getPrintWriter()
	{
		if(sauvegarde_fichier)
			return new PrintWriter(writer);
		return new PrintWriter(System.err);
	}

}
