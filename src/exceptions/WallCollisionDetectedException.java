package exceptions;

/**
 * Exception lancée si le robot détecte (en avance, avec des calculs de collisions) une prochaine rencontre impromptue avec un mur
 * @author pf, marsu
 *
 */
public class WallCollisionDetectedException extends Exception
{

	private static final long serialVersionUID = -8074280063169359572L;

	public WallCollisionDetectedException()
	{
		super();
	}
	
	public WallCollisionDetectedException(String m)
	{
		super(m);
	}

}
