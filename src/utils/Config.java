package utils;

import container.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;


/**
 * 
 * @author pf, marsu
 *
 */
public class Config implements Service
{
	private String name_local_file = "local.ini";
	private String name_config_file = "config.ini";
	private String path;
	private Properties config = new Properties();
	private Properties local = new Properties();
	
    Enumeration<?> e = local.propertyNames();

	
	public Config(String path) throws Exception
	{
		this.path = path;
	//	log.debug("Loading config from current directory : " +  System.getProperty("user.dir"), this)
		try
		{
			this.config.load(new FileInputStream(this.path+this.name_config_file));
		}
		catch  (IOException e)
		{
			e.printStackTrace();
			throw new Exception("Erreur ouverture de config.ini");
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
				throw new Exception("Erreur création de local.ini");
			}	
			throw new Exception("Erreur ouverture de local.ini");
		}	
		affiche_tout();
	}
	
	/**
	 * Méthode de récupération des paramètres de configuration
	 * @param nom
	 * @return
	 * @throws ConfigException
	 */
	public String get(String nom)
	{
		String out = null;
		out = config.getProperty(nom);
		if(out == null)
		{
			System.out.println("Erreur config: "+nom+" introuvable.");
		}
		return out;
	}

	/**
	 * Méthode utilisée seulement par les tests
	 * @param nom
	 * @return
	 */
	private void set(String nom, String value)
	{
		System.out.println(nom+" = "+value+" (ancienne valeur: "+config.getProperty(nom)+")");
		config.setProperty(nom, value);
	}
	
	/**
	 * Set en version user-friendly
	 * @param nom
	 * @param value
	 */
	public void set(String nom, Object value)
	{
		System.out.println(nom+" = "+value.toString()+" (ancienne valeur: "+config.getProperty(nom)+")");
		set(nom, value.toString());
	}

	// TODO private
	private void affiche_tout()
	{
		if(Boolean.parseBoolean(config.getProperty("affiche_debug")))
		{
			System.out.println("Configuration initiale");
			for(Object o: config.keySet())
				System.out.println(o+": "+config.get(o));
		}
	}
	
	public void updateConfig()
	{
	}
	
}
