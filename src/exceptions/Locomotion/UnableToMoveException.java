package exceptions.Locomotion;

/**
 * Problème générique de déplacement du robot, que ce soit a cause d'un robot ennemi
 * (détecté par les capteurs) qui bloque le passage, ou d'un bloquage mécanique (type mur)
 * @author pf, marsu
 *
 */
public class UnableToMoveException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8139322860107594266L;

	public UnableToMoveException()
	{
		super();
	}
	
	public UnableToMoveException(String m)
	{
		super(m);
	}
	
}
 