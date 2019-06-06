/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles.clothoid;

import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.astar.tentacles.TentacleComputer;
import pfg.kraken.astar.tentacles.TentacleType;
import pfg.kraken.struct.Kinematic;
import static pfg.kraken.astar.tentacles.Tentacle.*;

import java.awt.Color;

/**
 * Arc de clothoïde de longueur constante
 * 
 * @author pf
 *
 */

public enum ClothoTentacle implements TentacleType
{
	SAME_CURVATURE(0),
	LEFT_0(1),
	RIGHT_0(-1),
	LEFT_1(4),
	RIGHT_1(-4),
	LEFT_2(9),
	RIGHT_2(-9),
	LEFT_3(16),
	RIGHT_3(-16),

	SAME_CURVATURE_AFTER_STOP(0),
	SAME_CURVATURE_L1_AFTER_STOP(1, 0),
	SAME_CURVATURE_R1_AFTER_STOP(-1, 0),
	SAME_CURVATURE_L3_AFTER_STOP(3, 0),
	SAME_CURVATURE_R3_AFTER_STOP(-3, 0),

	SAME_CURVATURE_CUSP(0, 0),
	SAME_CURVATURE_L1_CUSP(1, 0),
	SAME_CURVATURE_R1_CUSP(-1, 0),
	SAME_CURVATURE_L3_CUSP(3, 0),
	SAME_CURVATURE_R3_CUSP(-3, 0);

	public final int vitesse; // vitesse en (1/m)/m = 1/m^2
	public final int courbureInitiale; // courbure en m^-1
	public final int squaredRootVitesse; // sqrt(abs(vitesse))
	public final boolean positif; // calculé à la volée pour certaine vitesse
	public final boolean rebrousse;
	public final boolean arret;
	public double maxSpeed; // en m.s⁻¹
	public static ClothoidComputer computer;

	private ClothoTentacle(int vitesse)
	{
		this(0, vitesse);
	}

	private ClothoTentacle(int courbureInitiale, int vitesse)
	{
		this.courbureInitiale = courbureInitiale;
		rebrousse = toString().endsWith("_CUSP");
		arret = toString().endsWith("_AFTER_STOP") || rebrousse;
		this.vitesse = vitesse;
		positif = vitesse >= 0;
		this.squaredRootVitesse = (int) Math.sqrt(Math.abs(vitesse));
	}

	@Override
	public boolean isAcceptable(Kinematic c, DirectionStrategy directionstrategyactuelle, double courbureMax)
	{

		// il y a un problème si :
		// - on veut rebrousser chemin
		// ET
		// - si :
		// - on n'est pas en fast, donc pas d'autorisation
		// ET
		// - on est dans la bonne direction, donc pas d'autorisation
		// exceptionnelle de se retourner

		if(rebrousse && (directionstrategyactuelle != DirectionStrategy.FASTEST && directionstrategyactuelle.isPossible(c.enMarcheAvant)))
		{
			// log.debug(vitesse+" n'est pas acceptable (rebroussement
			// interdit");
			return false;
		}

		// Si on ne rebrousse pas chemin alors que c'est nécessaire
		if(!rebrousse && !directionstrategyactuelle.isPossible(c.enMarcheAvant))
		{
			// log.debug(vitesse+" n'est pas acceptable (rebroussement
			// nécessaire");
			return false;
		}

		double courbureFuture = c.courbureGeometrique + vitesse * DISTANCE_ARC_COURBE_M;
		if(!(courbureFuture >= -courbureMax && courbureFuture <= courbureMax))
		{
			// log.debug(vitesse+" n'est acceptable (courbure trop grande");
			return false;
		}

		return true;
	}

	@Override
	public int getNbArrets(boolean firstMove)
	{
		if(firstMove)
			return 0;
		if(arret)
			return 1;
		return 0;
	}

	@Override
	public Color getColor()
	{
		if(arret)
			return Color.GREEN;
		return Color.RED;
	}
	
	@Override
	public TentacleComputer getComputer()
	{
		return computer;
	}

	@Override
	public double getComputationalCost()
	{
		return 1;
	}

}
