package enums;

/**
 * Enumération des différents services. Plus d'informations sur les services dans Container.
 * @author pf
 *
 */
public enum ServiceNames {
	 LOG,
	 CONFIG,
	 TABLE,
	 LOCOMOTION,
	 LOCOMOTION_CARD_WRAPPER,
	 ROBOT_REAL,
	 HOOK_FACTORY,
	 SENSORS_CARD_WRAPPER,
	 ACTUATOR_CARD_WRAPPER,
	 A_STAR_STRATEGY,
	 A_STAR_PATHFINDING,
	 REAL_GAME_STATE,
	 EXECUTION,
	 SCRIPT_MANAGER,
	 SERIE_ASSERVISSEMENT(TypeService.SERIE, 0),
	 SERIE_CAPTEURS_ACTIONNEURS(TypeService.SERIE, 1),
	 THREAD_SENSOR,
	 THREAD_TIMER,
	 THREAD_STRATEGY,
	 SERIAL_MANAGER,
	 OBSTACLE_MANAGER,
	 GRID_SPACE,
	 PATHFINDING_ARC_MANAGER,
	 STRATEGY_ARC_MANAGER,
	 MEMORY_MANAGER;
	
	 private TypeService type = TypeService.RIEN;
	 private int nbSerie = 0;

	 private ServiceNames()
	 {}
	 
	 public int getNbSerie()
	 {
		 return nbSerie;
	 }
	 
	 private ServiceNames(TypeService type, int nbSerie)
	 {
		 this.nbSerie = nbSerie;
		 this.type = type;
	 }
	 
	 public TypeService getType()
	 {
		 return type;
	 }
	 
	 public enum TypeService {
		 RIEN,
		 SERIE;		 
	 }
	 
}
