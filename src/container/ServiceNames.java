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
	 REAL_GAME_STATE,
	 SERIE_COUCHE_PHYSIQUE,
//	 SERIE_XBEE,
	 D_STAR_LITE,
	 GRID_SPACE,

	 A_STAR_COURBE,
	 MEMORY_MANAGER,
	 ARC_MANAGER,

	 //	 THETA_STAR,
//	 THETA_STAR_ARC_MANAGER,

	 BUFFER_INCOMING_BYTES,
	 CHEMIN_PATHFINDING,
	 INCOMING_DATA_BUFFER,
	 INCOMING_ORDER_BUFFER,
//	 THETA_STAR_MEMORY_MANAGER,
	 SERIE_COUCHE_TRAME,
	 OUTGOING_ORDER_BUFFER,
	 MOTEUR_PHYSIQUE,
	 OBSTACLES_MEMORY,
	 CLOTHOIDES_COMPUTER,
	 
	 // Les threads
	 THREAD_SERIAL_INPUT_COUCHE_ORDRE,
	 THREAD_SERIAL_INPUT_COUCHE_TRAME,
	 THREAD_SERIAL_OUTPUT,
	 THREAD_SERIAL_OUTPUT_TIMEOUT,
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
