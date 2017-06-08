package table;


import smartMath.Vec2;

public class Fresco extends Game_Element{
	private int length;
	/*
	 * |<-----longueur----->position<-----longueur----->|
	 * |<------------fresque_adverse------------------->|
	 */
	
	public Fresco(Vec2 position)
	{
		super(position);
		length = 50;//5 cm
	}
	public int getLength()
	{
		return length;
	}
}
