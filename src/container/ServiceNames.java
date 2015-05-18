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
	 STM_CARD,
	 A_STAR_STRATEGY,
	 A_STAR_PATHFINDING,
	 REAL_GAME_STATE,
	 SCRIPT_MANAGER,
	 STRATEGIE_INFO,
	 SERIE_STM,
	
	 THREAD_TIMER(true),
	 THREAD_SERIE(true),
	 THREAD_OBSTACLE_MANAGER(true),
	 THREAD_STRATEGIE_INFO(true),
	 THREAD_PATHFINDING(true),
	 THREAD_STRATEGIE(true),
	 THREAD_GRID_SPACE(true),
	 
	 STRATEGIE_PATHFINDING,
	 TABLE_PATHFINDING,
	 STRATEGIE_ACTUELLE,
	 CHEMIN_ACTUEL,
	 
	 SERIAL_MANAGER,
	 OBSTACLE_MANAGER,
	 GRID_SPACE,
	 PATHFINDING_ARC_MANAGER,
	 STRATEGY_ARC_MANAGER,
	 INCOMING_DATA_BUFFER,
	 MEMORY_MANAGER;

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
