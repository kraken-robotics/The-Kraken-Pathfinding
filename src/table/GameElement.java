package table;

import smartMath.Vec2;

abstract class GameElement
{
	protected Vec2 position;
	
	public GameElement(Vec2 position)
	{
		this.position = position;
	}
	
	public Vec2 getPosition()
	{
		return position;
	}
}
