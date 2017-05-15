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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import config.ConfigInfo;
import container.Container;
import exceptions.ContainerException;
import graphic.Fenetre;
import graphic.PrintBuffer;
import graphic.printable.Layer;
import graphic.printable.Printable;
import robot.Cinematique;
import robot.RobotReal;
import utils.Log;

/**
 * Le client du serveur de fenêtre. Lit les infos depuis un socket et les
 * affiche
 * 
 * @author pf
 *
 */

public class ClientFenetre
{

	public static void main(String[] args) throws ContainerException, InterruptedException
	{
		Container container = null;
		try
		{
			try
			{
				// on force l'affichage non externe
				ConfigInfo.GRAPHIC_EXTERNAL.setDefaultValue(false);
				container = new Container();
				Fenetre f = container.getService(Fenetre.class);
				PrintBuffer buffer = container.getService(PrintBuffer.class);
				RobotReal robot = container.getService(RobotReal.class);
				Log log = container.getService(Log.class);
				InetAddress rpiAdresse = null;
				boolean loop = false;
				log.debug("Démarrage du client d'affichage");
				try
				{
					if(args.length != 0)
					{
						for(int i = 0; i < args.length; i++)
						{
							if(args[i].equals("-d"))
								loop = true;
							else if(!args[i].startsWith("-"))
							{
								String[] s = args[i].split("\\."); // on découpe
																	// avec les
																	// points
								if(s.length == 4) // une adresse ip,
													// probablement
								{
									log.debug("Recherche du serveur à partir de son adresse ip : " + args[i]);
									byte[] addr = new byte[4];
									for(int j = 0; j < 4; j++)
										addr[j] = Byte.parseByte(s[j]);
									rpiAdresse = InetAddress.getByAddress(addr);
								}
								else // le nom du serveur, probablement
								{
									log.debug("Recherche du serveur à partir de son nom : " + args[i]);
									rpiAdresse = InetAddress.getByName(args[i]);
								}
							}
							else
								log.warning("Paramètre inconnu : " + args[i]);
						}
					}

					if(rpiAdresse == null) // par défaut, la raspi (ip fixe)
					{
						rpiAdresse = InetAddress.getByAddress(new byte[] { (byte) 172, 24, 1, 1 });
						log.debug("Utilisation de l'adresse par défaut : " + rpiAdresse);
					}
				}
				catch(UnknownHostException e)
				{
					log.critical("La recherche du serveur a échoué ! " + e);
					return;
				}

				Socket socket = null;
				do
				{

					boolean ko;
					log.debug("Tentative de connexion…");

					do
					{
						try
						{
							socket = new Socket(rpiAdresse, 13370);
							ko = false;
						}
						catch(IOException e)
						{
							Thread.sleep(500); // on attend un peu avant de
												// réessayer
							ko = true;
						}
					} while(ko);

					log.debug("Connexion réussie !");
					ObjectInputStream in;
					try
					{
						in = new ObjectInputStream(socket.getInputStream());
					}
					catch(IOException e)
					{
						log.warning("Le serveur a coupé la connexion : " + e);
						continue; // on relance la recherche
					}

					try
					{
						while(true)
						{
							@SuppressWarnings("unchecked")
							List<Object> tab = (List<Object>) in.readObject();
							synchronized(buffer)
							{
								buffer.clearSupprimables();
								int i = 0;
								while(i < tab.size())
								{
									Object o = tab.get(i++);
									if(o instanceof Cinematique)
									{
										// log.debug("Cinématique !
										// "+((Cinematique)o).getPosition());
										robot.setCinematique((Cinematique) o);
									}
									else if(o instanceof Printable)
									{
										Layer l = (Layer) tab.get(i++);
										buffer.addSupprimable((Printable) o, l);
									}
									else
										log.critical("Erreur ! Objet non affichable : " + o.getClass());
								}
							}
						}
					}
					catch(IOException e)
					{
						log.warning("Le serveur a coupé la connexion : " + e);
						e.printStackTrace();
					}
					catch(ClassNotFoundException e)
					{
						e.printStackTrace();
					}
					finally
					{
						try
						{
							in.close();
						}
						catch(IOException e)
						{
							e.printStackTrace();
						}
					}

				} while(loop);

				try
				{
					socket.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
				if(f != null)
					f.waitUntilExit();
				log.debug("Arrêt du client d'affichage");
			}
			catch(ContainerException | InterruptedException e)
			{}

		}
		finally
		{
			if(container != null)
				container.destructor();
		}
	}

}
