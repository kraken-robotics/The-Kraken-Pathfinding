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
import graphic.PrintBufferInterface;
import obstacles.types.Obstacle;
import utils.Log;
import utils.Vec2RO;
import utils.Vec2RW;

/**
 * Création de masque
 * @author pf
 *
 */

public class MasqueManager implements Service, LowPFClass
{
	private int centreMasqueCylindre;
	private PointGridSpaceManager pointManager;
	private PointDirigeManager pointDManager;
	private PrintBufferInterface buffer;
	private int rayonRobot;
	protected Log log;
	private List<PointDirige> modelCylindre = new ArrayList<PointDirige>();
	private boolean printObsCapteurs;
	private boolean[][] dedans = new boolean[200][200];
	private Vec2RW pos = new Vec2RW(), coinbasgaucheVec2 = new Vec2RW();

	public MasqueManager(Log log, PointGridSpaceManager pointManager, PointDirigeManager pointDManager, PrintBufferInterface buffer, Config config)
	{
		this.log = log;
		this.pointManager = pointManager;
		this.pointDManager = pointDManager;
		this.buffer = buffer;

		printObsCapteurs = config.getBoolean(ConfigInfo.GRAPHIC_D_STAR_LITE);

		rayonRobot = config.getInt(ConfigInfo.DILATATION_ROBOT_DSTARLITE);		
		int rayonCylindre = 32;
		int rayonPointCylindre = (int) Math.round((rayonRobot + rayonCylindre) / PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS);
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
	 * Renvoie le masque d'un cylindre
	 * @param position
	 * @return
	 */
	public Masque getMasqueCylindre(Vec2RO position)
	{
		return getMasque(position, modelCylindre, centreMasqueCylindre, centreMasqueCylindre);
	}

	/**
	 * Renvoie un masque
	 * @param position
	 * @param model
	 * @param centreMasque
	 * @return
	 */
	private Masque getMasque(Vec2RO position, List<PointDirige> model, int centreMasqueX, int centreMasqueY)
	{
		PointGridSpace p = pointManager.get(position);
		List<PointDirige> out = new ArrayList<PointDirige>();
		
		for(PointDirige c : model)
		{
			PointDirige point = pointDManager.get(pointManager.get(c.point.x + p.x - centreMasqueX, c.point.y + p.y - centreMasqueY), c.dir);
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

	public Masque getMasqueEnnemi(Obstacle obstacle)
	{
		double xmin = obstacle.getLeftmostX() - rayonRobot;
		double xmax = obstacle.getRightmostX() + rayonRobot;
		double ymin = obstacle.getBottomY() - rayonRobot;
		double ymax = obstacle.getTopY() + rayonRobot;
		
		coinbasgaucheVec2.setX(xmin);
		coinbasgaucheVec2.setY(ymin);
		
		int y = (int) Math.round(ymin / PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS) - 1;
		int x = (int) Math.round((xmin + 1500) / PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS) - 1;

		int tailleMasqueX = (int) Math.round((xmax - xmin) / PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS) + 3;
		int tailleMasqueY = (int) Math.round((ymax - ymin) / PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS) + 3;
				
		List<PointDirige> model = new ArrayList<PointDirige>();
		
		for(int i = 0; i < tailleMasqueX; i++)
			for(int j = 0; j < tailleMasqueY; j++)
			{
				if(!pointManager.isValid(x+i, y+j)) // life is too short for this shit
					continue;

				PointGridSpace p = pointManager.get(x+i, y+j);
				p.computeVec2(pos);
				dedans[i][j] = obstacle.squaredDistance(pos) < rayonRobot * rayonRobot;
			}

		for(int i = 0; i < tailleMasqueX; i++)
			for(int j = 0; j < tailleMasqueY; j++)
				if(pointManager.isValid(x+i, y+j) && !dedans[i][j])
					for(Direction d : Direction.values)
					{
						int i2 = i + d.deltaX, j2 = j + d.deltaY;
						if(i2 >= 0 && i2 < tailleMasqueX && j2 >= 0 && j2 < tailleMasqueY && dedans[i2][j2])
							model.add(pointDManager.get(i+x,j+y,d));
					}

		Masque m = new Masque(pointManager, model);
		if(printObsCapteurs)
			buffer.addSupprimable(m);

		return m;
	}
}
