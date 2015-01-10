package robot.cardsWrappers;

import hook.Hook;

import java.util.ArrayList;

import robot.serial.SerialConnexion;
import utils.Config;
import utils.Log;
import container.Service;
import enums.PathfindingNodes;
import enums.RobotColor;
import enums.Speed;
import exceptions.FinMatchException;
import exceptions.serial.SerialConnexionException;

public class STMcardWrapper implements Service {

	protected Log log;
	private SerialConnexion serie;

	public STMcardWrapper(Config config, Log log, SerialConnexion serie)
	{
		this.log = log;
		this.serie = serie;		
	}

	/**
	 * On envoie à la STM les noeuds du pathfinding.
	 * Ils sont utilisés par la STM pour suivre un chemin.
	 * @throws SerialConnexionException
	 * @throws FinMatchException
	 */
	public void sendPathfindingNodes() throws SerialConnexionException, FinMatchException
	{
		String[] message = new String[2+2*PathfindingNodes.values().length];
		message[0] = "nds";
		message[1] = Integer.toString(PathfindingNodes.values().length);
		for(PathfindingNodes n: PathfindingNodes.values())
		{
			message[2+2*n.ordinal()] = Integer.toString(n.getCoordonnees().x);
			message[2+2*n.ordinal()+1] = Integer.toString(n.getCoordonnees().y);
		}
		serie.communiquer(message, 0);
	}

	public void sendSpeed() throws SerialConnexionException, FinMatchException
	{
		String[] message = new String[2+2*Speed.values().length];
		message[0] = "spd";
		message[1] = Integer.toString(Speed.values().length);
		for(Speed s: Speed.values())
		{
			message[2+2*s.ordinal()] = Integer.toString(s.PWMTotation);
			message[2+2*s.ordinal()+1] = Integer.toString(s.PWMTranslation);
		}
		serie.communiquer(message, 0);
	}

	/**
	 * Autorise les trajectoires courbes
	 * @throws SerialConnexionException
	 * @throws FinMatchException
	 */
	public void trajectoire_courbe_on() throws SerialConnexionException, FinMatchException
	{
		serie.communiquer("tct", 0);		
	}

	/**
	 * Interdit les trajectoires courbes
	 * @throws SerialConnexionException
	 * @throws FinMatchException
	 */
	public void trajectoire_courbe_off() throws SerialConnexionException, FinMatchException
	{
		serie.communiquer("tcf", 0);		
	}

	public void suit_chemin(ArrayList<PathfindingNodes> chemin) throws SerialConnexionException, FinMatchException
	{
		String[] message = new String[2+chemin.size()];
		message[0] = "sch";
		message[1] = Integer.toString(chemin.size());
		int i = 2;
		for(PathfindingNodes n: chemin)
		{
			message[i] = Integer.toString(n.ordinal());
			i++;
		}
		serie.communiquer(message, 0);
		// TODO: gérer réponse de la STM
	}
	
	public RobotColor getRobotColor() throws SerialConnexionException, FinMatchException
	{
		String[] color = serie.communiquer("clr", 1);
		return RobotColor.parse(color[0]);
	}
	
	// TODO: modifier le code des hooks afin de faciliter cette communication
	public void register_hook(Hook hook) throws SerialConnexionException, FinMatchException
	{
		String[] message = new String[1];
		message[0] = "rh";
		// TODO
		serie.communiquer(message, 0);		
	}
	
	@Override
	public void updateConfig() {
		// TODO Auto-generated method stub
		
	}

	// TODO: recopier le contenu de locomotioncardwrapper: avancer, envoi de constantes, ...
	
}
