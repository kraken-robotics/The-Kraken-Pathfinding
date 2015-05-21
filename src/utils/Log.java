package utils;

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
	private GregorianCalendar calendar = new GregorianCalendar();
	FileWriter writer = null;

	private String 	couleurDebug 	= "\u001B[32m",
					couleurWarning 	= "\u001B[33m",
					couleurCritical = "\u001B[31m";

	// Ne pas afficher les messages de bug permet d'économiser du temps CPU
	private boolean affiche_debug = true;
	
	// Sauvegarder les logs dans un fichier
	private boolean sauvegarde_fichier = false;
	
	/**
	 * Affichage de debug, en vert
	 * @param message
	 * @param objet
	 */
	public void debug(Object message)
	{
		if(affiche_debug)
			ecrire(" DEBUG ", message, couleurDebug, System.out);
	}

	/**
	 * Affichage de warnings, en orange
	 * @param message
	 * @param objet
	 */
	public void warning(Object message)
	{
		ecrire(" WARNING ", message, couleurWarning, System.out);
	}
	
	/**
	 * Affichage d'erreurs critiques, en rouge
	 * @param message
	 * @param objet
	 */
	public void critical(Object message)
	{
		ecrire(" CRITICAL ", message, couleurCritical, System.err);
	}

	private void ecrire(String niveau, Object message, String couleur, PrintStream ou)
	{
		if(logClosed)
			System.out.println("WARNING * Log fermé! Message: "+message);
		else
		{
			StackTraceElement elem = Thread.currentThread().getStackTrace()[3];
			String heure = calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND)+","+calendar.get(Calendar.MILLISECOND);
			String affichage = heure+niveau+elem.getClassName()+"."+elem.getMethodName()+":"+elem.getLineNumber()+" "+message;//+"\u001B[0m";
			if(couleur != couleurDebug || affiche_debug)
				ou.println(affichage);
			if(sauvegarde_fichier)
				ecrireFichier(affichage);
		}
	}
	
	/**
	 * Ecrit dans un fichier. Utilisé pendant la coupe.
	 * @param message
	 */
	private void ecrireFichier(String message)
	{
		try{
		     writer.write(message+"\n");
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	/**
	 * Sorte de destructeur, dans lequel le fichier est sauvegardé.
	 */
	public void close()
	{
		warning("Fin du log");
		
		if(sauvegarde_fichier)
			try {
				debug("Sauvegarde du fichier de logs");
				if(writer != null)
					writer.close();
			}
			catch(Exception e)
			{
				System.out.println(e);
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
		if(!sauvegarde_fichier && writer != null)
			try {
				writer.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		if(sauvegarde_fichier)
			try {
				String heure = calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND);
				writer = new FileWriter("logs/LOG-"+heure+".txt", true); 
			}
			catch(Exception e)
			{
				critical(e);
			}
		warning("Service de log démarré");
	}

}
