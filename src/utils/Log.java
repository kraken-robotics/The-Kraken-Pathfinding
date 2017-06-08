package utils;

import java.io.FileWriter;
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
	private Config config;

	FileWriter writer = null;

	private String 	couleurDebug 	= "\u001B[32m",
					couleurWarning 	= "\u001B[33m",
					couleurCritical = "\u001B[31m";

	// Ne pas afficher les messages de bug permet d'économiser du temps CPU
	private boolean affiche_debug = true;
	
	// Sauvegarder les logs dans un fichier
	private boolean sauvegarde_fichier = false;
	
	public Log(Config config)
	{
		this.config = config;
		
		updateConfig();
		
		if(sauvegarde_fichier)
			try {
				java.util.GregorianCalendar calendar = new GregorianCalendar();
				String heure = calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND);
				writer = new FileWriter("logs/LOG-"+heure+".txt", true); 
			}
			catch(Exception e)
			{
				critical(e, this);
			}
		warning("Service de log démarré", this);
	
	}
	
	/**
	 * Méthode à appeler uniquement depuis une méthode statique. User-friendly
	 * @param message
	 */
	public void appel_static(Object message)
	{
		appel_static(message.toString());
	}
		
	/**
	 * Méthode à appeler uniquement depuis une méthode statique
	 * @param message
	 */
	public void appel_static(String message)
	{
		ecrire("Lanceur: "+message, couleurDebug, System.out);
	}
	
	
	/**
	 * Affichage de debug, en vert. User-friendly
	 * @param message
	 * @param objet
	 */
	public void debug(Object message, Object objet)
	{
		debug(message.toString(), objet);
	}
	
	/**
	 * Affichage de debug, en vert
	 * @param message
	 * @param objet
	 */
	public void debug(String message, Object objet)
	{
		if(affiche_debug)
			ecrire(objet.getClass().getName()+": "+message, couleurDebug, System.out);
	}

	/**
	 * Affichage de warnings, en orange. User-friendly
	 * @param message
	 * @param objet
	 */
	public void warning(Object message, Object objet)
	{
		warning(message.toString(), objet);
	}

	/**
	 * Affichage de warnings, en orange
	 * @param message
	 * @param objet
	 */
	public void warning(String message, Object objet)
	{
		ecrire(objet.getClass().getName()+": "+message, couleurWarning, System.out);
	}

	/**
	 * Affichage d'erreurs critiques, en rouge. User-friendly
	 * @param message
	 * @param objet
	 */
	public void critical(Object message, Object objet)
	{
		critical(message.toString(), objet);
	}
	
	/**
	 * Affichage d'erreurs critiques, en rouge
	 * @param message
	 * @param objet
	 */
	public void critical(String message, Object objet)
	{
		ecrire(objet.getClass().getName()+": "+message, couleurCritical, System.err);
	}

	private void ecrire(String message, String couleur, PrintStream ou)
	{
		java.util.GregorianCalendar calendar = new GregorianCalendar();
		String heure = calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND)+","+calendar.get(Calendar.MILLISECOND);
		if(couleur != couleurDebug || affiche_debug)
			ou.println(couleur+heure+" "+message+"\u001B[0m");
		if(sauvegarde_fichier)
			ecrireFichier(couleur+heure+" "+message+"\u001B[0m");
	}
	
	private void ecrireFichier(String message)
	{
		message += "\n";
		try{
		     writer.write(message,0,message.length());
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	/**
	 * Sorte de destructeur, dans lequel le fichier est sauvegardé.
	 * // TODO: refuser les demande d'écriture sur le log si l'initialisation n'a pas été faite, ou si le destructeur a été appellé 
	 */
	public void close()
	{
		warning("Fin du log",this);
		
		if(sauvegarde_fichier)
			try {
				debug("Sauvegarde du fichier de logs", this);
				if(writer != null)
					writer.close();
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
	}
	
	@Override
	public void updateConfig()
	{
		try {
			affiche_debug = Boolean.parseBoolean(this.config.get("affiche_debug"));
		}
		catch(Exception e)
		{
			critical(e, this);
		}
		try {
			sauvegarde_fichier = Boolean.parseBoolean(this.config.get("sauvegarde_fichier"));
		}
		catch(Exception e)
		{
			critical(e, this);
		}
	}

}
