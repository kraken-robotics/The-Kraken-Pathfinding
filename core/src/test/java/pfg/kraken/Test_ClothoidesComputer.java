/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.struct.Cinematique;
import pfg.kraken.struct.XY;
import pfg.kraken.astar.tentacles.ClothoidesComputer;
import pfg.kraken.astar.tentacles.StaticTentacle;
import pfg.kraken.astar.tentacles.types.ClothoTentacle;
import static pfg.kraken.astar.tentacles.Tentacle.*;

/**
 * Tests unitaires de la recherche de chemin courbe
 * 
 * @author pf
 *
 */

public class Test_ClothoidesComputer extends JUnit_Test
{
	private ClothoidesComputer clotho;

	@Before
	public void setUp() throws Exception
	{
		super.setUpStandard("default");
		clotho = injector.getService(ClothoidesComputer.class);
	}

	@Test
	public void test_clotho() throws Exception
	{
		int nbArc = 16;
		StaticTentacle arc[] = new StaticTentacle[nbArc];
		for(int i = 0; i < nbArc; i++)
			arc[i] = new StaticTentacle(injector.getService(RectangularObstacle.class));

		Cinematique c = new Cinematique(0, 1000, Math.PI / 2, false, 0, false);
		clotho.getTrajectoire(c, ClothoTentacle.COURBURE_IDENTIQUE, arc[0], 0);
		clotho.getTrajectoire(arc[0], ClothoTentacle.GAUCHE_2, arc[1], 0);
		clotho.getTrajectoire(arc[1], ClothoTentacle.COURBURE_IDENTIQUE, arc[2], 0);
		clotho.getTrajectoire(arc[2], ClothoTentacle.GAUCHE_1, arc[3], 0);
		clotho.getTrajectoire(arc[3], ClothoTentacle.COURBURE_IDENTIQUE, arc[4], 0);
		clotho.getTrajectoire(arc[4], ClothoTentacle.COURBURE_IDENTIQUE, arc[5], 0);
		clotho.getTrajectoire(arc[5], ClothoTentacle.COURBURE_IDENTIQUE, arc[6], 0);
		clotho.getTrajectoire(arc[6], ClothoTentacle.GAUCHE_1, arc[7], 0);
		clotho.getTrajectoire(arc[7], ClothoTentacle.GAUCHE_2, arc[8], 0);
		clotho.getTrajectoire(arc[8], ClothoTentacle.GAUCHE_2, arc[9], 0);
		clotho.getTrajectoire(arc[9], ClothoTentacle.DROITE_1, arc[10], 0);
		clotho.getTrajectoire(arc[10], ClothoTentacle.DROITE_1, arc[11], 0);
		clotho.getTrajectoire(arc[11], ClothoTentacle.DROITE_1, arc[12], 0);
		clotho.getTrajectoire(arc[12], ClothoTentacle.DROITE_1, arc[13], 0);
		clotho.getTrajectoire(arc[13], ClothoTentacle.DROITE_1, arc[14], 0);
		clotho.getTrajectoire(arc[14], ClothoTentacle.GAUCHE_2, arc[15], 0);

		for(int a = 0; a < nbArc; a++)
		{
			for(int i = 0; i < NB_POINTS; i++)
			{
				if(i > 0)
				{
					double distance = arc[a].arcselems[i-1].cinem.getPosition().distance(arc[a].arcselems[i].cinem.getPosition());
					assert distance <= PRECISION_TRACE_MM && distance >= PRECISION_TRACE_MM*0.97 : distance;
				}
				else if(a > 0)
				{
					double distance = arc[a-1].arcselems[NB_POINTS - 1].cinem.getPosition().distance(arc[a].arcselems[0].cinem.getPosition());
					assert distance <= PRECISION_TRACE_MM && distance >= PRECISION_TRACE_MM*0.97 : distance;
				}
			}
			if(a == 0)
			{
				Assert.assertEquals(arc[0].arcselems[NB_POINTS - 1].cinem.getX(), 0, 0.1);
				Assert.assertEquals(arc[0].arcselems[NB_POINTS - 1].cinem.getY(), 1000 + (int) DISTANCE_ARC_COURBE, 0.1);
			}
		}

		Assert.assertEquals(0, arc[nbArc - 1].arcselems[arc[nbArc - 1].arcselems.length - 1].cinem.getPosition().distance(new XY(-166.41,1335.34)), 0.1);
	}
}
