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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


/**
 * Permet de rejouer les logs en respectant les temps
 * @author pf
 *
 */

public class Replay {

	public static void main(String[] args)
	{
		if(args.length == 0 || args.length > 1)
			System.out.println("Usage : Replay logfile");
		else
		{
			try {
			System.out.println("Replay de "+args[0]);
			BufferedReader br;
				br = new BufferedReader(new FileReader(args[0]));
		    String line;
		    long startDate = System.currentTimeMillis();
		    while((line = br.readLine()) != null)
		    {
		    	String time = line.split(" ")[0];
		    	try {
		    		long temps = Long.parseLong(time);
		    		Thread.sleep(Math.max(0, temps+startDate - System.currentTimeMillis()));
		    			System.out.println(line);
		    	}
		    	catch(NumberFormatException e)
		    	{
		    		System.out.println("Temps inconnu : "+line);
		    	}
		    }
		    br.close();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
