package container;

/**
 * Enumération des différents services. Plus d'informations sur les services dans Container.
 * @author pf
 *
 */
public enum ServiceNames {
	 LOG,
	 CONFIG,
	 TABLE,
	 ROBOT_REAL,
	 HOOK_FACTORY,
	 SENSORS_CARD_WRAPPER,
	 ACTUATOR_CARD_WRAPPER,
	 STM_CARD_WRAPPER,
	 A_STAR_STRATEGY,
	 A_STAR_PATHFINDING,
	 REAL_GAME_STATE,
	 EXECUTION,
	 SCRIPT_MANAGER,
	 SERIE_ASSERVISSEMENT(TypeService.SERIE, 0, 9600),
	 SERIE_CAPTEURS_ACTIONNEURS(TypeService.SERIE, 3, 9600),
	 SERIE_POSITION(TypeService.SERIE, 2, 9600),
	 SERIE_LOCOMOTION(TypeService.SERIE, 1, 9600),
	 THREAD_SENSOR,
	 THREAD_TIMER,
	 THREAD_POSITION,
	 THREAD_STRATEGY,
	 SERIAL_MANAGER,
	 OBSTACLE_MANAGER,
	 GRID_SPACE,
	 PATHFINDING_ARC_MANAGER,
	 STRATEGY_ARC_MANAGER,
	 MEMORY_MANAGER;
	
	 private TypeService type = TypeService.RIEN;
	 private int nbSerie = 0;
	 private int baudrate;

	 private ServiceNames()
	 {}
	 
	 public int getNbSerie()
	 {
		 return nbSerie;
	 }
	 
	 private ServiceNames(TypeService type, int nbSerie, int baudrate)
	 {
		 this.nbSerie = nbSerie;
		 this.type = type;
		 this.baudrate = baudrate;
	 }
	 
	 public TypeService getType()
	 {
		 return type;
	 }
	 
	 public int getBaudrate()
	 {
		 return baudrate;
	 }
	 
	 public enum TypeService {
		 RIEN,
		 SERIE;		 
	 }
	 
}
