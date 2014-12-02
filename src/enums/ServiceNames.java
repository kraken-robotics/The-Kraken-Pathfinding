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
	 MEMORY_MANAGER,
	 HOOK_FACTORY,
	 SENSORS_CARD_WRAPPER,
	 ACTUATOR_CARD_WRAPPER,
	 PATHFINDING,
	 REAL_GAME_STATE,
	 EXECUTION,
	 SCRIPT_MANAGER,
	 SERIE_ASSERVISSEMENT(TypeService.SERIE, 0),
	 SERIE_CAPTEURS_ACTIONNEURS(TypeService.SERIE, 1),
	 THREAD_SENSOR,
	 THREAD_LASER,
	 THREAD_TIMER,
	 CHECK_UP,
	 OBSTACLE_MANAGER,
	 GRID_SPACE;
	
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
