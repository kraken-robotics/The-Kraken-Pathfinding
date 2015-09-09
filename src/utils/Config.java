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
	public static final boolean debugSerie = false;
	// Permet de savoir si le match a démarré et quand
	public static final boolean graphicDStarLite = false;
	public static final boolean graphicThetaStar = false;
	public static final boolean graphicObstacles = true;

	private String name_config_file = "config.ini";
	private volatile Properties properties = new Properties();
	private Log log;
	
	private boolean configIniCharge = false;
	
	public Config()
	{
		try
		{
			properties.load(new FileInputStream(name_config_file));
			configIniCharge = true;
		}
		catch  (IOException e)
		{
			System.out.println("Erreur lors de l'ouverture de config.ini. Utilisation des valeurs par défaut.");
		}
		completeConfig();
	}

	/**
	 * A appeler après l'ouverture du log.
	 */
	public void init(Log log)
	{
		this.log = log;
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
	public synchronized String getString(ConfigInfo nom)
	{
		return properties.getProperty(nom.toString());
	}

	/**
	 * Méthode set privée
	 * @param nom
	 * @return
	 */
	private synchronized void set(ConfigInfo nom, String value)
	{
		// On notifie avant d'affecter la valeur.
		// En fait, ça n'a pas d'importance car le mutex sur la config est activé…
		if(value.compareTo(properties.getProperty(nom.toString())) != 0)
			notify();
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
	private synchronized void afficheTout()
	{
		log.debug("Configuration initiale");
		for(ConfigInfo info: ConfigInfo.values())
			log.warning(info+" = "+getString(info));
	}
	
	/**
	 * Complète avec les valeurs par défaut le fichier de configuration
	 */
	private synchronized void completeConfig()
	{
		if(configIniCharge)
		{
			for(ConfigInfo info: ConfigInfo.values())
			{
				if(!properties.containsKey(info.toString()))
					properties.setProperty(info.toString(), info.getDefaultValue());
				else if(!info.isConstant())
				{
					System.out.println(info+" NE peut PAS être surchargé par config.ini");
					properties.setProperty(info.toString(), info.getDefaultValue());
				}
				else
					System.out.println(info+" surchargé par config.ini");
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
					System.out.println(cle+" inutilisé. Veuillez le retirer de config.ini");
			}
		}
		else
		{
			for(ConfigInfo info: ConfigInfo.values())
				properties.setProperty(info.toString(), info.getDefaultValue());
		}
	}
		
	/**
	 * Récupère la symétrie
	 * @return
	 */
	public boolean getSymmetry()
	{
		return RobotColor.parse(getString(ConfigInfo.COULEUR)).symmetry;
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
}
