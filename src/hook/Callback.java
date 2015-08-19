package hook;

import java.util.ArrayList;

import permissions.ReadWrite;
import robot.RobotChrono;
import strategie.GameState;
import exceptions.ChangeDirectionException;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.WallCollisionDetectedException;

/**
 * Classe de callback. Contient la fonction et ses arguments à appeler.
 * @author pf
 */

public class Callback
{

	// L'évènement a-t-il été réalisé ?
	private boolean isDone = false;
	
	// L'évènement ne doit-t-il survenir qu'une unique fois ?
	private boolean isUnique;
	
	// le code à éxecuter lors de l'évènement
	private Executable method;
	
	/**
	 * Constructeur d'un callback avec 2 paramètres: la méthode et si elle doit être exécutée une seule fois
	 * @param methode
	 * @param unique
	 */
	public Callback(Executable methode, boolean unique)
	{
		this.method = methode;
		this.isUnique = unique;
	}
	
	/**
	 * Constructeur d'un callback avec 1 paramètre, la méthode. Par défaut, celle-ci est exécutée une seule fois.
	 * @param methode
	 */
	public Callback(Executable methode)
	{
		this.method = methode;
		isUnique = true;
	}
	
	/**
	 * Le callback appelle la méthode, si elle n'est pas unique ou si elle n'est pas déjà faite
	 * @return vrai si le robot a été déplacé/ tourné, faux sinon
	 * @throws FinMatchException 
	 * @throws ScriptHookException 
	 * @throws WallCollisionDetectedException 
	 * @throws ChangeDirectionException 
	 */
	public void call() throws FinMatchException, ScriptHookException, WallCollisionDetectedException, ChangeDirectionException
	{
		if(!(shouldBeDeleted()))
		{
            isDone = true;
			method.execute();
		}
	}
	
	/**
	 * Explique si le Callback devrait être détruit
	 * @return true si le Callback devrait être détruit
	 */
	public boolean shouldBeDeleted()
	{
	    return isUnique && isDone;
	}

	public void updateGameState(GameState<RobotChrono,ReadWrite> state)
	{
		method.updateGameState(state);
	}

	public ArrayList<String> toSerial()
	{
		ArrayList<String> out = new ArrayList<String>();
		out.add(String.valueOf(isUnique));
		out.addAll(method.toSerial());
		return out;
	}
}
