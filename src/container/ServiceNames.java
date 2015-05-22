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
	 EXECUTION,
	 HOOK_FACTORY,
	 REAL_GAME_STATE,
	 SCRIPT_MANAGER,
	 STRATEGIE_INFO,
	 SERIE_STM,
	 STRATEGIE,
	 PATHFINDING,
	 CHEMIN_ACTUEL,
	 STRATEGIE_NOTIFIEUR,
	 OBSTACLE_MANAGER,
	 GRID_SPACE,
	 INCOMING_DATA_BUFFER,
	 INCOMING_HOOK_BUFFER,
	 MEMORY_MANAGER,
	
	 // Les threads
	 THREAD_FIN_MATCH(true),
	 THREAD_PEREMPTION(true),
	 THREAD_SERIE(true),
	 THREAD_CONFIG(true),
	 THREAD_OBSTACLE_MANAGER(true),
	 THREAD_STRATEGIE_INFO(true),
	 THREAD_PATHFINDING(true),
	 THREAD_STRATEGIE(true),
	 THREAD_GRID_SPACE(true),
	 THREAD_GRID_SPACE2(true),
	 THREAD_TABLE(true);

	 private boolean isThread = false;
	 
	 private ServiceNames()
	 {}
	 
	 private ServiceNames(boolean isThread)
	 {
		 this.isThread = isThread;
	 }
	 
	 public boolean isThread()
	 {
		 return isThread;
	 }
	 
}
