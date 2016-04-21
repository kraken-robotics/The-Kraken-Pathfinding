package robot;

import utils.Vec2;
import utils.permissions.ReadOnly;
import utils.permissions.ReadWrite;

/**
 * Une structure qui regroupe des infos de cinématique, sauf la vitesse
 * @author pf
 *
 */

public class CinematiqueSansVitesse
{
	protected final Vec2<ReadWrite> position = new Vec2<ReadWrite>();
	public volatile double orientation;
	public volatile boolean enMarcheAvant;
	public volatile double courbure;

	/**
	 * Constructeur par copie
	 * @param c
	 */
	public CinematiqueSansVitesse(CinematiqueSansVitesse c)
	{
		c.copy(this);
	}

	public CinematiqueSansVitesse(int x, int y, double orientation, boolean enMarcheAvant, double courbure)
	{
		position.x = x;
		position.y = y;
		this.orientation = orientation;
		this.enMarcheAvant = enMarcheAvant;
		this.courbure = courbure;
	}

	public final Vec2<ReadOnly> getPosition()
	{
		return position.getReadOnly();
	}

	public final Vec2<ReadWrite> getPositionEcriture()
	{
		return position;
	}

	/**
	 * Renvoie vrai si this est proche de autre
	 * @param autre
	 * @return
	 */
	public boolean estProche(CinematiqueSansVitesse autre)
	{
		System.out.println("Distance : "+position.squaredDistance(autre.position));
		return position.squaredDistance(autre.position) < 50*50; // TODO écrire
	}

	/**
	 * Renvoie vrai si this est proche de autre. Plus laxiste que "estProche"
	 * @param autre
	 * @return
	 */
	public boolean estProcheUrgence(CinematiqueSansVitesse autre)
	{
		return position.squaredDistance(autre.position) < 50*50; // TODO écrire
	}

	/**
	 * Copie this dans autre
	 * @param autre
	 */
	public void copy(CinematiqueSansVitesse autre)
	{
    	Vec2.copy(position.getReadOnly(), autre.position);
    	autre.orientation = orientation;
    	autre.enMarcheAvant = enMarcheAvant;
    	autre.courbure = courbure;
	}
	
	@Override
	public String toString()
	{
		return position+", "+orientation;
	}
}
