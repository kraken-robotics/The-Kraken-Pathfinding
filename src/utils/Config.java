package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import container.Service;
import enums.RobotColor;


/**
 * Gère le fichier de configuration externe.
 * @author pf, marsu
 *
 */
public class Config implements Service
{
	// Permet de savoir si le match a démarré et quand
	public static final String dossierconfig = "./config/";

	private String name_config_file = "config.ini";
	private String path;
	private Properties config = new Properties();
	private Log log;
	
	public Config(Log log, String path)
	{
		this.log = log;
		this.path = path;

		//	log.debug("Loading config from current directory : " +  System.getProperty("user.dir"), this)
		try
		{
			this.config.load(new FileInputStream(this.path+this.name_config_file));
		}
		catch  (IOException e)
		{
			log.critical("Erreur lors de l'ouverture de config.ini. Utilisation des valeurs par défaut.");
		}	
		afficheTout();
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
		if(!config.containsKey(nom.toString()))
		{
//			log.debug("Erreur config: "+nom+" introuvable. Utilisation de la valeur par défaut.");
			config.setProperty(nom.toString(), nom.getDefaultValue());			
		}
		return config.getProperty(nom.toString());
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
	private void afficheTout()
	{
		if(Boolean.parseBoolean(config.getProperty(ConfigInfo.AFFICHE_DEBUG.toString())))
		{
			log.debug("Configuration initiale");
			for(Object o: config.keySet())
				log.debug(o+": "+config.get(o));
		}
	}
		
	/**
	 * Récupère la symétrie
	 * @return
	 */
	public boolean getSymmetry()
	{
		return RobotColor.parse(getString(ConfigInfo.COULEUR)).isSymmetry();
	}

	/**
	 * Met à jour les config de tous les services
	 */
	@Override
	public void updateConfig(Config config)
	{
		synchronized(this)
		{
			notifyAll();
		}
	}
	
	@Override
	public void useConfig(Config config)
	{}
	
}
