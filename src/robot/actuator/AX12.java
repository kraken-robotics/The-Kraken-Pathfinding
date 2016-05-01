package robot.actuator;

/**
 * Enumération des AX12 du robot avec leur ID
 * @author pf
 *
 */

public enum AX12
{
	AX12_AVANT_GAUCHE(4),
	AX12_AVANT_DROIT(3),
	AX12_ARRIERE_GAUCHE(1),
	AX12_ARRIERE_DROIT(2),
	AX12_PORTE_CANNE(5),
	AX12_LACHE_POISSON(6),

	AX12_GROS_1(0),
	AX12_GROS_2(7),
	AX12_GROS_3(8);
	
	public final int id;
	
	private AX12(int id)
	{
		this.id = id;
	}
}