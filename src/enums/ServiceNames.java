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
	 PATHFINDING,
	 LASER,
	 LASER_FILTRATION,
	 GAME_STATE,
	 SCRIPT_MANAGER,
	 SERIE_ASSERVISSEMENT(TypeService.SERIE),
	 SERIE_CAPTEURS_ACTIONNEURS(TypeService.SERIE),
	 SERIE_LASER(TypeService.SERIE),
	 CARTE_TEST(TypeService.SERIE), // utilisée pour la recherche de la série uniquement
	 THREAD_SENSOR,
	 THREAD_LASER,
	 THREAD_TIMER,
	 CHECK_UP;
	
	 private TypeService type = TypeService.RIEN;

	 private ServiceNames()
	 {}
	 
	 private ServiceNames(TypeService type)
	 {
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
