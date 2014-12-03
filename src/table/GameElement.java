package table;

import smartMath.Vec2;

/**
 * Un élément de jeu.
 * Visibilité "friendly" afin qu'il ne puisse être manipulé directement
 * que par la table (afin de ne pas bypasser le mécanisme de hash).
 * @author pf
 *
 */

class GameElement
{
	private Vec2 position;
	private boolean done = false; 
	
	public GameElement(Vec2 position)
	{
		this.position = position;
	}
	
	public Vec2 getPosition()
	{
		return position;
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
