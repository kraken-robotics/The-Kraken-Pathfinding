package enums;

import serie.trame.Order;

/**
 * Protocole série entre le bas niveau et la Java
 * @author pf
 *
 */

public class SerialProtocol {
	
	public enum OutOrder
	{
		/**
		 * Protocole Java vers bas niveau
		 */
		ACTIONNEUR(0x06, Order.Type.LONG),
		STOP(0x07, Order.Type.SHORT),
	
		AVANCER(0x0C, Order.Type.LONG),
		AVANCER_NEG(0x0D, Order.Type.LONG),
		AVANCER_IDEM(0x0E, Order.Type.LONG),
		AVANCER_REVERSE(0x0F, Order.Type.LONG),
	
		ASSER_OFF(0x28, Order.Type.SHORT),
	
		SEND_ARC(0x2E, Order.Type.SHORT),
		SEND_ARC_ARRET(0x2C, Order.Type.SHORT),
	
		START_CAPTEURS(0x10, Order.Type.LONG),
		MATCH_BEGIN(0x11, Order.Type.LONG),
		MATCH_END(0x12, Order.Type.LONG),
		ASK_COLOR(0x13, Order.Type.SHORT),
	
		PING(0x5A, Order.Type.SHORT);
		
		public final byte code;
		public final Order.Type type;
		
		private OutOrder(int code, Order.Type type)
		{
			this.type = type;
			this.code = (byte) code;
		}
	}
	
	public enum InOrder
	{
		/**
		 * Protocole bas niveau vers Java
		 */
	
		ROBOT_ARRIVE(0x02),
		PB_DEPLACEMENT(0x03),
		MOUVEMENT_ANNULE(0x03),
		
		// Couleur
		COULEUR_ROBOT_GAUCHE(0x02),
		COULEUR_ROBOT_DROITE(0x01),
		COULEUR_ROBOT_INCONNU(0x03);

		public final int codeInt;
		
		private InOrder(int code)
		{
			codeInt = code;
		}
	}
	
}
