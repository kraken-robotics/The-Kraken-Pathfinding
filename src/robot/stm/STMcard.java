package robot.stm;

import hook.Hook;
import hook.types.HookDemiPlan;

import java.util.ArrayList;

import permissions.ReadOnly;
import planification.astar.arc.PathfindingNodes;
import planification.astar.arc.SegmentTrajectoireCourbe;
import utils.Config;
import utils.Log;
import utils.Vec2;
import container.Service;
import enums.RobotColor;
import robot.DirectionStrategy;
import robot.Speed;
import serial.SerialConnexion;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.SerialConnexionException;
import exceptions.UnableToMoveException;

public class STMcard implements Service {

	protected Log log;
	protected Config config;
	private SerialConnexion serie;
	private boolean symetrie;

	public STMcard(Config config, Log log, SerialConnexion serie)
	{
		this.log = log;
		this.config = config;
		this.serie = serie;		
	}

	/**
	 * On envoie à la STM les noeuds du pathfinding.
	 * Ils sont utilisés par la STM pour suivre un chemin.
	 * @throws SerialConnexionException
	 * @throws FinMatchException
	 */
	public void sendPathfindingNodes() throws SerialConnexionException
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

	public void sendSpeed() throws SerialConnexionException
	{
		String[] message = new String[2+2*Speed.values().length];
		message[0] = "spd";
		message[1] = Integer.toString(Speed.values().length);
		for(Speed s: Speed.values())
		{
			message[2+2*s.ordinal()] = Integer.toString(s.PWMRotation);
			message[2+2*s.ordinal()+1] = Integer.toString(s.PWMTranslation);
		}
		serie.communiquer(message, 0);
	}

	/**
	 * Autorise les trajectoires courbes
	 * @throws SerialConnexionException
	 * @throws FinMatchException
	 */
	public void trajectoire_courbe_on() throws SerialConnexionException
	{
		serie.communiquer("tct", 0);		
	}

	/**
	 * Interdit les trajectoires courbes
	 * @throws SerialConnexionException
	 * @throws FinMatchException
	 */
	public void trajectoire_courbe_off() throws SerialConnexionException
	{
		serie.communiquer("tcf", 0);		
	}

	public void suit_chemin(ArrayList<PathfindingNodes> chemin) throws SerialConnexionException
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
	
	public RobotColor getRobotColor() throws SerialConnexionException
	{
		String[] color = serie.communiquer("clr", 1);
		return RobotColor.parse(color[0]);
	}
	
	// TODO: modifier le code des hooks afin de faciliter cette communication
	public void register_hook(Hook hook) throws SerialConnexionException
	{
		String[] message = new String[1];
		message[0] = "rh";
		// TODO
		serie.communiquer(message, 0);		
	}
	
    public void turn(double angle, ArrayList<Hook> hooks) throws UnableToMoveException
    {
    	// TODO
    }
    
    public void moveLengthwise(int distance, ArrayList<Hook> hooks, boolean mur) throws UnableToMoveException
    {
    	// TODO
    }

    public void followPath(ArrayList<SegmentTrajectoireCourbe> chemin, HookDemiPlan hookTrajectoireCourbe, ArrayList<Hook> hooks, DirectionStrategy directionstrategy) throws UnableToMoveException, ScriptHookException
    {
    	// TODO
    }
    
    public void readjust()
    {
    	// TODO
    }
    
    /**
     * Met à jour la position. A ne faire qu'en début de match.
     * @param position
     * @throws FinMatchException 
     */
    public void setPosition(Vec2<ReadOnly> position) {
    	// TODO
    }
    
    public void setOrientation(double orientation) {
    	// TODO
    }
    
    public Vec2<ReadOnly> getPosition()
    {
		return null;
    	// TODO
    }

    public double getOrientation()
    {
		return 0;
    	// TODO
    }

    
	/**
	 * Arrête le robot
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void immobilise() throws SerialConnexionException
	{
		// Je bourrine, tu bourrines, il bourrine, ...
        disableTranslationalFeedbackLoop();
        disableRotationalFeedbackLoop();
		serie.communiquer("stop", 0);
        enableTranslationalFeedbackLoop();
        enableRotationalFeedbackLoop();
	}
	
	/**
	 * Ecrase la position x du robot au niveau de la carte
	 * @param x la nouvelle abscisse que le robot doit considérer avoir sur la table
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void setX(int x) throws SerialConnexionException
	{
		String chaines[] = {"cx", Integer.toString(x)};
		serie.communiquer(chaines, 0);
	}

	/**
	 * Ecrase la position y du robot au niveau de la carte
	 * @param x la nouvelle ordonnée que le robot doit considérer avoir sur la table
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void setY(int y) throws SerialConnexionException
	{
		String chaines[] = {"cy", Integer.toString(y)};
		serie.communiquer(chaines, 0);	
	}
	
	/**
	 * Active l'asservissement en translation du robot
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void enableTranslationalFeedbackLoop() throws SerialConnexionException
	{
		serie.communiquer("ct1", 0);
	}

	/**
	 * Active l'asservissement en rotation du robot
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void enableRotationalFeedbackLoop() throws SerialConnexionException
	{
		serie.communiquer("cr1", 0);
	}

	/**
	 * Désactive l'asservissement en translation du robot
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void disableTranslationalFeedbackLoop() throws SerialConnexionException
	{
		serie.communiquer("ct0", 0);
	}

	/**
	 * Désactive l'asservissement en rotation du robot
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void disableRotationalFeedbackLoop() throws SerialConnexionException
	{
		serie.communiquer("cr0", 0);
	}

	/**
	 * Modifie la vitesse en translation du robot sur la table
	 * @param pwmMax la nouvelle valeur maximum que peut prenvent prendre les pwm des moteurs lors d'une translation
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void setTranslationalSpeed(Speed speed) throws SerialConnexionException
	{
		// envois a la carte d'asservissement les nouvelles valeurs des correcteurs et le nouveau maximum des pwm
		String chaines[] = {"ctv", Double.toString(speed.kp_trans), Double.toString(speed.kd_trans), Integer.toString(speed.PWMTranslation)};
		serie.communiquer(chaines, 0);			
	}

	/**
	 * Modifie la vitesse en rotation du robot sur la table
	 * @param pwmMax la nouvelle valeur maximum que peut prenvent prendre les pwm des moteurs lors d'une rotation
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void setRotationalSpeed(Speed speed) throws SerialConnexionException
	{
		// envois a la carte d'asservissement les nouvelles valeurs des correcteurs et le nouveau maximum des pwm
		String chaines[] = {"crv", Double.toString(speed.kp_rot), Double.toString(speed.kd_rot), Integer.toString(speed.PWMRotation)};
		serie.communiquer(chaines, 0);
	}
	
	/**
	 * envois a la carte d'asservissement de nouvelles valeurs pour les correcteurs et un nouveau maximum pour les pwm lors d'une translation
	 * @param kp nouvelle valeur du correcteur proportionnel
	 * @param kd nouvelle valeur du correcteur dérivé 
	 * @param pwm_max a nouvelle valeur maximum que peut prenvent prendre les pwm des moteurs lors d'une translation
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void changeTranslationalFeedbackParameters(double kp, double kd, int pwm_max) throws SerialConnexionException
	{
		String chaines[] = {"ctv", Double.toString(kp), Double.toString(kd), Integer.toString(pwm_max)};
		serie.communiquer(chaines, 0);
	}

	/**
	 * envois a la carte d'asservissement de nouvelles valeurs pour les correcteurs et un nouveau maximum pour les pwm lors d'une rotation
	 * @param kp nouvelle valeur du correcteur proportionnel
	 * @param kd nouvelle valeur du correcteur dérivé 
	 * @param pwm_max a nouvelle valeur maximum que peut prenvent prendre les pwm des moteurs lors d'une rotation
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void changeRotationalFeedbackParameters(double kp, double kd, int pwm_max) throws SerialConnexionException
	{
		String chaines[] = {"crv", Double.toString(kp), Double.toString(kd), Integer.toString(pwm_max)};
		serie.communiquer(chaines, 0);
	}

	/**
	 * Demande a la carte d'asservissement la position et l'orientation courrante du robot sur la table.
	 * Renvoie x, y et orientation du robot
	 * @return un tableau de 3 cases: [x, y, orientation]
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public double[] getCurrentPositionAndOrientation() throws SerialConnexionException
	{
		// on demande a la carte des information a jour
		// on envois "?infos" et on lis double (dans l'ordre : abscisse, ordonnée, orientation)
		String[] infosBuffer = serie.communiquer("?xyo", 3);
		double[] parsedInfos = new double[3];
		for(int i = 0; i < 3; i++)
		    parsedInfos[i] = Double.parseDouble(infosBuffer[i]);

		return parsedInfos;
	}

	public boolean isEnemyHere() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void updateConfig()
	{
        symetrie = config.getSymmetry();
	}
	
	/**
	 * Envoie un ordre à la série. Le protocole est défini dans l'enum ActuatorOrder
	 * @param order l'ordre à envoyer
	 * @throws SerialConnexionException en cas de problème de communication avec la carte actionneurs
	 * @throws FinMatchException 
	 */
	public void useActuator(ActuatorOrder order) throws SerialConnexionException
	{
		if(symetrie)
			order = order.getSymmetry();
		serie.communiquer(order.getSerialOrder(), 0);
	}

	public void close()
	{
		serie.close();
	}

	/**
	 * Méthode bloquante
	 * Renvoie la liste des positions des obstacles vus par les capteurs
	 * @return renvoie la position brute puis position de l'ennemi, pour chaque capteur
	 * @throws FinMatchException */
/*
	public Vec2<ReadOnly>[] mesurer() throws FinMatchException
	{
		@SuppressWarnings("unchecked")
		Vec2<ReadOnly>[] positions = (Vec2<ReadOnly>[]) new Vec2[2*nbCapteurs];
		if(!capteursOn)
    		return positions;
		String[] positionsString = new String[4*nbCapteurs];
		synchronized(serie)
		{
			try {
				serie.wait();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			try {
				positionsString = serie.read(4*nbCapteurs);
				for(int i = 0; i < 2*nbCapteurs; i++)
					positions[i] = new Vec2<ReadOnly>(Integer.parseInt(positionsString[2*i]), Integer.parseInt(positionsString[2*i+1]));
				return positions;
			} catch (IOException e) {
				e.printStackTrace();
				return positions;
			}
		}
	}
	*/

	/**
	 * Le match a-t-il démarré? Demande à la STM l'état du jumper.
	 * @return
	 * @throws SerialConnexionException
	 * @throws FinMatchException
	 */
    public boolean demarrageMatch() throws SerialConnexionException
    {
    	try {
    		return Integer.parseInt(serie.communiquer("j", 1)[0]) != 0;
    	}
    	catch(NumberFormatException e)
    	{
    		log.critical("Réponse du jumper non comprise");
    		return false;
    	}
    }
    
}
