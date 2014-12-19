package enums;

/**
 * Utilisé on veut qualifier un troisième état pour un booléen: "peut-être".
 * Utilisé surtout quand on interprète le comportement de l'ennemi.
 * @author pf
 *
 */

public enum Tribool {
	FALSE(0), // ces hashs sont utilisés dans la génération du hash de la table
	MAYBE(1),
	TRUE(3);
	
	public final int hash;
	
	private Tribool(int hash)
	{
		this.hash = hash;
	}
	
}
