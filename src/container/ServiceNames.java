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
	 CAPTEURS,
	 ROBOT_REAL,
	 EXECUTION,
	 HOOK_FACTORY,
	 REAL_GAME_STATE,
	 SCRIPT_MANAGER,
	 STRATEGIE_INFO,
	 SERIE_STM,
	 STRATEGIE,
	 D_STAR_LITE,
	 THETA_STAR,
	 ARC_MANAGER,
	 CHEMIN_PATHFINDING,
	 STRATEGIE_NOTIFIEUR,
	 GRID_SPACE,
	 INCOMING_DATA_BUFFER,
	 INCOMING_HOOK_BUFFER,
	 MEMORY_MANAGER,
	 SERIAL_OUTPUT_BUFFER,
	 REQUETE_STM,
	 MOTEUR_PHYSIQUE,
	 OBSTACLES_MEMORY,
	 
	 // Les threads
	 THREAD_FIN_MATCH,
	 THREAD_SERIAL_INPUT,
	 THREAD_SERIAL_OUTPUT,
	 THREAD_CONFIG,
	 THREAD_GAME_ELEMENT_DONE_BY_ENEMY,
//	 THREAD_OBSTACLE_MANAGER,
//	 THREAD_STRATEGIE_INFO,
//	 THREAD_PATHFINDING,
//	 THREAD_STRATEGIE,
	 THREAD_GRID_SPACE,
//	 THREAD_GRID_SPACE2,
	 THREAD_CAPTEURS;

	 private boolean isThread = false;
	 
	 private ServiceNames()
	 {
		 isThread = name().startsWith("THREAD_");
	 }
	 
	 public boolean isThread()
	 {
		 return isThread;
	 }
	 
}
