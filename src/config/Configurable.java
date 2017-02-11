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

package config;

/**
 * Tout ce qui utilite la config
 * @author pf
 *
 */

public interface Configurable
{
	/**
	 * Affecte les valeurs de config. N'est appelé qu'une fois, juste après le constructeur.
	 * @param config
	 * @throws InterruptedException 
	 */
	public void useConfig(Config config);
}
