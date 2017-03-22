/*
Copyright (C) 2013-2017 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package graphic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import container.Container;
import exceptions.ContainerException;
import utils.Log;

/**
 * Le client du serveur de fenêtre. Lit les infos depuis un socket et les affiche
 * @author pf
 *
 */

public class ClientFenetre
{

	public static void main(String[] args) throws ContainerException, InterruptedException
	{
		Container container = new Container();
		Log log = container.getService(Log.class);
		InetAddress rpiAdresse = null;
		boolean loop = false;
		log.debug("Démarrage du client d'affichage");
		try {
			if(args.length != 0)
			{
				for(int i = 0; i < args.length; i++)
				{
					if(args[i].equals("-d"))
						loop = true;
					else if(!args[i].startsWith("-"))
					{
						String[] s = args[0].split(".");
						if(s.length == 4) // une adresse ip, probablement
						{
							byte[] addr = new byte[4];
							for(int j = 0; j < 4; j++)
								addr[j] = Byte.parseByte(s[j]);
							rpiAdresse = InetAddress.getByAddress(addr);
						}
						else // le nom du serveur, probablement
							rpiAdresse = InetAddress.getByName(args[0]);
					}
					else
						log.warning("Paramètre inconnu : "+args[i]);
				}
			}
			
			if(rpiAdresse == null) // par défaut, la raspi (ip fixe)
				rpiAdresse = InetAddress.getByAddress(new byte[]{(byte)172,24,1,1});
		} catch (UnknownHostException e) {
			log.critical("La recherche du serveur a échoué ! "+e);
			return;
		}
		
		Socket socket = null;
		do {
			
			boolean ko;
			log.debug("Tentative de connexion…");
			
			do {
				try {
					socket = new Socket(rpiAdresse, 133742);
					ko = false;
				} catch (IOException e) {
					Thread.sleep(500); // on attend un peu avant de réessayer
					ko = true;
				}
			} while(ko);
			
			log.debug("Connexion réussie !");
			BufferedReader in;
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} catch (IOException e) {
				log.warning("Le serveur a coupé la connexion : "+e);
				continue; // on relance la recherche
			}
			try {
				log.debug(in.readLine());
			} catch (IOException e) {
				log.warning("Le serveur a coupé la connexion : "+e);
			}
			finally {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		
		} while(loop);
		
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.debug("Arrêt du client d'affichage");
	}

	
}
