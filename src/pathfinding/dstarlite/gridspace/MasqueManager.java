/*
Copyright (C) 2016 Pierre-François Gimenez

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

package pathfinding.dstarlite.gridspace;

import java.util.ArrayList;
import java.util.List;

import config.Config;
import config.ConfigInfo;
import config.Configurable;
import container.Container;
import container.Service;
import exceptions.ContainerException;
import graphic.PrintBuffer;
import utils.Log;
import utils.Vec2RO;

/**
 * Création de masque
 * @author pf
 *
 */

public class MasqueManager implements Service, Configurable
{
	private int centreMasque;
	private PointGridSpaceManager pointManager;
	private PointDirigeManager pointDManager;
	private PrintBuffer buffer;
	private Container container;
	protected Log log;
	private List<PointDirige> model = new ArrayList<PointDirige>();
	private boolean printObsCapteurs;
	
	public MasqueManager(Log log, PointGridSpaceManager pointManager, PointDirigeManager pointDManager, PrintBuffer buffer, Container container)
	{
		this.log = log;
		this.pointManager = pointManager;
		this.pointDManager = pointDManager;
		this.buffer = buffer;
		this.container = container;
	}
	
	@Override
	public void useConfig(Config config)
	{
		printObsCapteurs = config.getBoolean(ConfigInfo.GRAPHIC_D_STAR_LITE);
		int rayonRobot = config.getInt(ConfigInfo.DILATATION_ROBOT_ENNEMI_DSTARLITE); // l'obstacle du D* Lite doit être dilaté
		int rayonEnnemi = config.getInt(ConfigInfo.RAYON_ROBOT_ADVERSE);
		int rayonPoint = (int) Math.round((rayonEnnemi + rayonRobot) / PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS);
		int tailleMasque = 2*(rayonPoint+1)+1;
		int squaredRayonPoint = rayonPoint * rayonPoint;

		centreMasque = tailleMasque / 2;
		for(int i = 0; i < tailleMasque; i++)
			for(int j = 0; j < tailleMasque; j++)
				if((i-centreMasque) * (i-centreMasque) + (j-centreMasque) * (j-centreMasque) > squaredRayonPoint)
					for(Direction d : Direction.values)
					{
						int i2 = i + d.deltaX, j2 = j + d.deltaY;
						if((i2-centreMasque) * (i2-centreMasque) + (j2-centreMasque) * (j2-centreMasque) <= squaredRayonPoint)
							model.add(pointDManager.get(i,j,d));
					}
	}
	
	public Masque getMasque(Vec2RO position)
	{
		PointGridSpace p = pointManager.get(position);
		List<PointDirige> out = new ArrayList<PointDirige>();
		
		for(PointDirige c : model)
		{
			PointDirige point = pointDManager.get(pointManager.get(c.point.x + p.x - centreMasque, c.point.y + p.y - centreMasque), c.dir);
			if(point != null)
			{
				PointGridSpace voisin = pointManager.getGridPointVoisin(point);
				if(voisin != null) // on vérifie que les deux points sont bien dans la table
					out.add(point);
			}
		}
		
		Masque m = null;
		try {
			m = container.make(Masque.class, out);
		} catch (ContainerException e) {
			log.critical(e);
		}

		if(printObsCapteurs)
			buffer.addSupprimable(m);
		
		return m;
	}

}
