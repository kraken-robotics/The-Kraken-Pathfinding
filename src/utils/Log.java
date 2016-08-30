package utils;

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
	private GregorianCalendar calendar = new GregorianCalendar();
	private FileWriter writer = null;

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
		ecrire(" ", message, true, System.out);
	}

	/**
	 * Affichage de warnings, en orange
	 * @param message
	 * @param objet
	 */
	public void warning(Object message)
	{
		ecrire(" WARNING ", message, false, System.out);
	}
	
	/**
	 * Affichage d'erreurs critiques, en rouge
	 * @param message
	 * @param objet
	 */
	public void critical(Object message)
	{
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
//			String heure = calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND)+","+calendar.get(Calendar.MILLISECOND);
			if(fastLog)
				affichage = date+" > "+message;
			else
			{
				StackTraceElement elem = Thread.currentThread().getStackTrace()[3];
				affichage = date+niveau+elem.getClassName().substring(elem.getClassName().lastIndexOf(".")+1)+/*"."+elem.getMethodName()+*/":"+elem.getLineNumber()+" > "+message;//+"\u001B[0m";
			}
			if(!debug || affiche_debug)
			{
				if(sauvegarde_fichier)
					ecrireFichier(affichage);
//				else
					ou.println(affichage);
			}
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
		catch(IOException e)
		{
			e.printStackTrace();
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
					writer.close();
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
	public void useConfig(Config config) throws InterruptedException
	{
		affiche_debug = config.getBoolean(ConfigInfo.AFFICHE_DEBUG);
		sauvegarde_fichier = config.getBoolean(ConfigInfo.SAUVEGARDE_FICHIER);
		fastLog = config.getBoolean(ConfigInfo.FAST_LOG);
		if(sauvegarde_fichier)
		{
			String heure = calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND);
			String file = "logs/LOG-"+heure+".txt";
			try {
				writer = new FileWriter(file); 
				debug("Un fichier de sauvegarde est utilisé: "+file);
			}
			catch(FileNotFoundException e)
			{
				try {
					Runtime.getRuntime().exec("mkdir logs");
					Thread.sleep(500);
					writer = new FileWriter(file); 
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
