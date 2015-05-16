package utils;

import container.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import enums.RobotColor;
import exceptions.ConfigException;


/**
 * Gère le fichier de configuration externe.
 * @author pf, marsu
 *
 */
public class Config implements Service
{
	// Permet de savoir si le match a démarré et quand
	private static long dateDebutMatch = 0;	
	public static boolean matchDemarre = false;	
	public static boolean capteursOn = false;
	public static boolean stopThreads = false;
	public static boolean finMatch;

	private String name_local_file = "local.ini";
	private String name_config_file = "config.ini";
	private String path;
	private Properties config = new Properties();
	private Properties local = new Properties();
	private Log log;
	
	public Config(String path)
	{
		this.path = path;
	}
	
	public void setLog(Log log)
	{
		this.log = log;
	}
	
	public void init() throws ConfigException
	{
	//	log.debug("Loading config from current directory : " +  System.getProperty("user.dir"), this)
		try
		{
			this.config.load(new FileInputStream(this.path+this.name_config_file));
		}
		catch  (IOException e)
		{
			e.printStackTrace();
			throw new ConfigException("Erreur ouverture de config.ini");
		}
		
		try
		{
			this.config.load(new FileInputStream(this.path+this.name_local_file));
		}
		catch  (IOException e)
		{
			try
			{
				FileOutputStream fileOut = new FileOutputStream(this.path+this.name_local_file);
				this.local.store(fileOut, "Ce fichier est un fichier généré par le programme.\nVous pouvez redéfinir les variables de config.ini dans ce fichier dans un mode de votre choix.\nPS : SopalINT RULEZ !!!\n");
			}
			catch (IOException e2)
			{
				e2.printStackTrace();
				throw new ConfigException("Erreur création de local.ini");
			}	
			throw new ConfigException("Erreur ouverture de local.ini");
		}	
		affiche_tout();
	}
	
	/**
	 * Récupère un entier de la config
	 * @param nom
	 * @return
	 * @throws NumberFormatException 
	 */
	public int getInt(ConfigInfo nom) throws NumberFormatException
	{
		return Integer.parseInt(getString(nom));
	}
	
	/**
	 * Récupère un booléen de la config
	 * @param nom
	 * @return
	 */
	public boolean getBoolean(ConfigInfo nom)
	{
		return Boolean.parseBoolean(getString(nom));
	}
	
	/**
	 * Récupère un double de la config
	 * @param nom
	 * @return
	 * @throws NumberFormatException 
	 */	
	public double getDouble(ConfigInfo nom) throws NumberFormatException
	{
		return Double.parseDouble(getString(nom));
	}
	
	/**
	 * Méthode de récupération des paramètres de configuration
	 * @param nom
	 * @return
	 */
	public String getString(ConfigInfo nom)
	{
		String out = null;
		out = config.getProperty(nom.toString());
		if(out == null)
		{
			log.debug("Erreur config: "+nom+" introuvable.");
			return nom.getDefaultValue();
		}
		return out;
	}

	/**
	 * Méthode utilisée seulement par les tests
	 * @param nom
	 * @return
	 */
	private void set(ConfigInfo nom, String value)
	{
		boolean change = value.compareTo(config.getProperty(nom.toString())) != 0;
		log.debug(nom+" = "+value+" (ancienne valeur: "+config.getProperty(nom.toString())+")");
		config.setProperty(nom.toString(), value);
		if(change)
			synchronized(this)
			{
				notifyAll();
			}
	}
	
	/**
	 * Set en version user-friendly
	 * @param nom
	 * @param value
	 */
	public void set(ConfigInfo nom, Object value)
	{
		set(nom, value.toString());
	}

	/**
	 * Affiche toute la config.
	 * Appelé au début du match.
	 */
	private void affiche_tout()
	{
		if(Boolean.parseBoolean(config.getProperty("affiche_debug")))
		{
			log.debug("Configuration initiale");
			for(Object o: config.keySet())
				log.debug(o+": "+config.get(o));
		}
	}
	
	@Override
	public void updateConfig()
	{}
		
	/**
	 * Etablit la date de début du match
	 */
	public void setDateDebutMatch()
	{
		dateDebutMatch = System.currentTimeMillis();
	}
	
	/**
	 * Récupère la date de début du match
	 * @return
	 */
	public static long getDateDebutMatch()
	{
		// Si le match n'a pas encore commencé, on dit qu'il vient de commencer (sinon les calculs bug)
		if(dateDebutMatch == 0)
			return System.currentTimeMillis();
		return dateDebutMatch;
	}

	/**
	 * Récupère la symétrie
	 * @return
	 */
	public boolean getSymmetry()
	{
		return RobotColor.parse(getString(ConfigInfo.COULEUR)).isSymmetry();
	}
	
}
