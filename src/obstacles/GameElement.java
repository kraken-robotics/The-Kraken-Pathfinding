package obstacles;

import smartMath.Vec2;

/**
 * Un élément de jeu.
 * On demande à ce qu'il soit circulaire afin de faciliter les calculs de collision.
 * @author pf
 *
 */

public class GameElement extends ObstacleCircular
{
	private boolean done = false; 
	
	public GameElement(Vec2 position, int rayon)
	{
		super(position, rayon);
		this.position = position;
	}
	
	public void setDone()
	{
		done = true;
	}
	
	public boolean isDone()
	{
		return done;
	}
	
	/**
	 * Attention, ce clone ne modifie que ce qui peut changer, c'est-à-dire done.
	 * Utilisé pour mettre à jour rapidement un objet.
	 * @param other
	 */
	public void fastClone(GameElement other)
	{
		other.done = done;
	}

	public void clone(GameElement other)
	{
		other.position = position.clone();
		other.done = done;
	}

}
