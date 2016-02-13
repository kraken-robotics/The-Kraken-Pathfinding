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
	 HOOK_FACTORY,
	 REAL_GAME_STATE,
	 SCRIPT_MANAGER,
	 STRATEGIE_INFO,
	 SERIE_STM,
//	 SERIE_XBEE,

	 D_STAR_LITE,
	 GRID_SPACE,

	 A_STAR_COURBE,
	 A_STAR_COURBE_MEMORY_MANAGER,
	 A_STAR_COURBE_ARC_MANAGER,

	 //	 THETA_STAR,
//	 THETA_STAR_ARC_MANAGER,

	 LPA_STAR,

	 A_STAR,
	 A_STAR_ARC_MANAGER,
	 A_STAR_MEMORY_MANAGER,
	 
	 GRID_SPACE_STRATEGIE,
	 OBSERVE_TABLE_ET_OBSTACLES,
	 CHEMIN_PATHFINDING,
	 STRATEGIE_NOTIFIEUR,
	 INCOMING_DATA_BUFFER,
//	 THETA_STAR_MEMORY_MANAGER,
	 SERIAL_OUTPUT_BUFFER,
	 REQUETE_STM,
	 MOTEUR_PHYSIQUE,
	 OBSTACLES_MEMORY,
	 CLOTHOIDES_COMPUTER,
	 
	 // Les threads
	 THREAD_SERIAL_INPUT,
	 THREAD_SERIAL_OUTPUT,
	 THREAD_CONFIG,
	 THREAD_PEREMPTION,
	 THREAD_EVITEMENT,
	 THREAD_PATHFINDING,
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
