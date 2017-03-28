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

import container.Service;
import graphic.printable.Layer;
import graphic.printable.Printable;

/**
 * Une interface qui permet de regrouper le print buffer déporté et celui qui ne l'est pas
 * @author pf
 *
 */

public interface PrintBufferInterface extends Service {

	public void clearSupprimables();
	public void addSupprimable(Printable o);
	public void addSupprimable(Printable o, Layer l);
	public void add(Printable o);
	public void removeSupprimable(Printable o);

}
