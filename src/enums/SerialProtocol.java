package enums;

/**
 * Protocole série entre le bas niveau et la Java
 * @author pf
 *
 */

public enum SerialProtocol {
	/**
	 * Protocole Java vers bas niveau
	 */
	OUT_ACTIONNEUR(0x06),
	OUT_STOP(0x07),

	OUT_DEBUG_MODE(0x0B),
	OUT_AVANCER(0x0C),
	OUT_AVANCER_NEG(0x0D),
	OUT_AVANCER_IDEM(0x0E),
	OUT_AVANCER_REVERSE(0x0F),

	OUT_ASSER_OFF(0x28),

	OUT_SEND_ARC(0x2E),
	OUT_SEND_ARC_ARRET(0x2F),

	OUT_PING(0x3F),

	/**
	 * Protocole bas niveau vers Java
	 */
	
	IN_ROBOT_ARRIVE(0x02),
	IN_PB_DEPLACEMENT(0x03),
	IN_DEBUT_MATCH(0x04),
	IN_MATCH_FINI(0x05),
	IN_COULEUR_ROBOT(0x06),
	IN_INFO_CAPTEURS(0x0A),

	MASK_LAST_BIT(0xFE);

	public final byte code;
	public final int codeInt;
	private SerialProtocol(int code)
	{
		codeInt = code;
		this.code = (byte) code;
	}
	
}
