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
	public static final boolean debugSerie = true;
	// Permet de savoir si le match a démarré et quand
	public static final String path = "./config/";

	private String name_config_file = "config.ini";
	private Properties properties = new Properties();
	private Log log;
	
	private boolean needUpdate = false;
	
	public Config(Log log)
	{
		this.log = log;

		try
		{
			properties.load(new FileInputStream(path+name_config_file));
		}
		catch  (IOException e)
		{
			log.critical("Erreur lors de l'ouverture de config.ini. Utilisation des valeurs par défaut.");
		}
		
	}

	/**
	 * A appeler après l'ouverture du log.
	 */
	public void init()
	{
		completeConfig();
		if(getBoolean(ConfigInfo.AFFICHE_DEBUG))
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
	 * Récupère un entier long de la config
	 * @param nom
	 * @return
	 * @throws NumberFormatException 
	 */
	public long getLong(ConfigInfo nom) throws NumberFormatException
	{
		return Long.parseLong(getString(nom));
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
		return properties.getProperty(nom.toString());
	}

	/**
	 * Méthode set privée
	 * @param nom
	 * @return
	 */
	private void set(ConfigInfo nom, String value)
	{
		needUpdate |= value.compareTo(properties.getProperty(nom.toString())) != 0;
		log.debug(nom+" = "+value+" (ancienne valeur: "+properties.getProperty(nom.toString())+")");
		properties.setProperty(nom.toString(), value);
	}
	
	/**
	 * Méthode set publique avec protection
	 * @param nom
	 * @param value
	 */
	public void set(ConfigInfo nom, Object value)
	{
		if(nom.isConstant())
			log.critical("Demande d'affectation à une config constante: "+nom);
		else
			set(nom, value.toString());
	}

	/**
	 * Affiche toute la config.
	 * Appelé au début du match.
	 */
	private void afficheTout()
	{
		log.debug("Configuration initiale");
		for(ConfigInfo info: ConfigInfo.values())
			log.debug(info+": "+getString(info));
	}
	
	/**
	 * Complète avec les valeurs par défaut le fichier de configuration
	 */
	private void completeConfig()
	{
		for(ConfigInfo info: ConfigInfo.values())
		{
			if(!properties.containsKey(info.toString()))
				properties.setProperty(info.toString(), info.getDefaultValue());
			else
				log.debug(info+" surchargé par config.ini");
		}
		for(String cle: properties.stringPropertyNames())
		{
			if(cle.contains("#"))
			{
				properties.remove(cle);
				continue;
			}
			boolean found = false;
			for(ConfigInfo info: ConfigInfo.values())
				if(info.toString().compareTo(cle) == 0)
				{
					found = true;
					break;
				}
			if(!found)
				log.warning(cle+" inutilisé. Veuillez le retirer de config.ini");
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
	public void updateConfigServices()
	{
		synchronized(this)
		{
			if(needUpdate)
				notifyAll();
			needUpdate = false;
		}
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
}
