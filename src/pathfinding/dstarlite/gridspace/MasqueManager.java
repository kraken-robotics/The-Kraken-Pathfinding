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

package pathfinding.dstarlite.gridspace;

import java.util.ArrayList;
import java.util.List;

import config.Config;
import config.ConfigInfo;
import container.Service;
import container.dependances.LowPFClass;
import graphic.PrintBuffer;
import utils.Log;
import utils.Vec2RO;

/**
 * Création de masque
 * @author pf
 *
 */

public class MasqueManager implements Service, LowPFClass
{
	private int centreMasqueEnnemi, centreMasqueCylindre;
	private PointGridSpaceManager pointManager;
	private PointDirigeManager pointDManager;
	private PrintBuffer buffer;
	protected Log log;
	private List<PointDirige> modelEnnemi = new ArrayList<PointDirige>();
	private List<PointDirige> modelCylindre = new ArrayList<PointDirige>();
	private boolean printObsCapteurs;
	
	public MasqueManager(Log log, PointGridSpaceManager pointManager, PointDirigeManager pointDManager, PrintBuffer buffer, Config config)
	{
		this.log = log;
		this.pointManager = pointManager;
		this.pointDManager = pointDManager;
		this.buffer = buffer;

		printObsCapteurs = config.getBoolean(ConfigInfo.GRAPHIC_D_STAR_LITE);
		int rayonRobot = config.getInt(ConfigInfo.DILATATION_ROBOT_ENNEMI_DSTARLITE); // l'obstacle du D* Lite doit être dilaté
		int rayonEnnemi = config.getInt(ConfigInfo.RAYON_ROBOT_ADVERSE);
		int rayonPointEnnemi = (int) Math.round((rayonEnnemi + rayonRobot) / PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS);
		int tailleMasqueEnnemi = 2*(rayonPointEnnemi+1)+1;
		int squaredRayonPointEnnemi = rayonPointEnnemi * rayonPointEnnemi;

		centreMasqueEnnemi = tailleMasqueEnnemi / 2;
		createMasque(centreMasqueEnnemi, tailleMasqueEnnemi, squaredRayonPointEnnemi, modelEnnemi);

		
		// la dilatation du robot est différente pour l'obstacle
		int rayonRobot2 = config.getInt(ConfigInfo.DILATATION_ROBOT_DSTARLITE);		
		int rayonCylindre = 32;
		int rayonPointCylindre = (int) Math.round((rayonRobot2 + rayonCylindre) / PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS);
		int tailleMasqueCylindre = 2*(rayonPointCylindre+1)+1;
		int squaredRayonPointCylindre = rayonPointCylindre * rayonPointCylindre;

		centreMasqueCylindre = tailleMasqueCylindre / 2;
		createMasque(centreMasqueCylindre, tailleMasqueCylindre, squaredRayonPointCylindre, modelCylindre);
	}
	
	private void createMasque(int centreMasque, int tailleMasque, int squaredRayonPoint, List<PointDirige> model)
	{
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

	/**
	 * Renvoie le masque de l'ennemi
	 * @param position
	 * @return
	 */
	public Masque getMasqueEnnemi(Vec2RO position)
	{
		return getMasque(position, modelEnnemi, centreMasqueEnnemi);
	}

	/**
	 * Renvoie le masque d'un cylindre
	 * @param position
	 * @return
	 */
	public Masque getMasqueCylindre(Vec2RO position)
	{
		return getMasque(position, modelCylindre, centreMasqueCylindre);
	}

	/**
	 * Renvoie un masque
	 * @param position
	 * @param model
	 * @param centreMasque
	 * @return
	 */
	private Masque getMasque(Vec2RO position, List<PointDirige> model, int centreMasque)
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
		
		Masque m = new Masque(pointManager, out);
		if(printObsCapteurs)
			buffer.addSupprimable(m);
		
		return m;
	}
}
