/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken.pathfinding.dstarlite.gridspace;

import java.util.ArrayList;
import java.util.List;
import config.Config;
import graphic.PrintBufferInterface;
import kraken.config.ConfigInfoKraken;
import kraken.obstacles.types.Obstacle;
import kraken.utils.Log;
import kraken.utils.Vec2RW;

/**
 * Création de masque
 * 
 * @author pf
 *
 */

public class MasqueManager
{
	private PointGridSpaceManager pointManager;
	private PointDirigeManager pointDManager;
	private PrintBufferInterface buffer;
	private int rayonRobot;
	protected Log log;
	private boolean printObsCapteurs;
	private boolean[][] dedans = new boolean[200][200];
	private Vec2RW pos = new Vec2RW(), coinbasgaucheVec2 = new Vec2RW();

	public MasqueManager(Log log, PointGridSpaceManager pointManager, PointDirigeManager pointDManager, PrintBufferInterface buffer, Config config)
	{
		this.log = log;
		this.pointManager = pointManager;
		this.pointDManager = pointDManager;
		this.buffer = buffer;

		printObsCapteurs = config.getBoolean(ConfigInfoKraken.GRAPHIC_D_STAR_LITE);

		rayonRobot = config.getInt(ConfigInfoKraken.DILATATION_ROBOT_DSTARLITE);
	}

	public Masque getMasque(Obstacle obstacle)
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
				if(!pointManager.isValid(x + i, y + j)) // life is too short for
														// this shit
					continue;

				PointGridSpace p = pointManager.get(x + i, y + j);
				p.computeVec2(pos);
				dedans[i][j] = obstacle.squaredDistance(pos) < rayonRobot * rayonRobot;
			}

		for(int i = 0; i < tailleMasqueX; i++)
			for(int j = 0; j < tailleMasqueY; j++)
				if(pointManager.isValid(x + i, y + j) && !dedans[i][j])
					for(Direction d : Direction.values)
					{
						int i2 = i + d.deltaX, j2 = j + d.deltaY;
						if(i2 >= 0 && i2 < tailleMasqueX && j2 >= 0 && j2 < tailleMasqueY && dedans[i2][j2])
							model.add(pointDManager.get(i + x, j + y, d));
					}

		Masque m = new Masque(pointManager, model);
		if(printObsCapteurs)
			buffer.addSupprimable(m);

		return m;
	}
}
