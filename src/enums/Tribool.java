package enums;

/**
 * Utilisé on veut qualifier un troisième état pour un booléen: "peut-être".
 * Utilisé quand on interprète le comportement de l'ennemi.
 * @author pf
 *
 */

public enum Tribool {
	FALSE(0), // ces hashs sont utilisés dans la génération du hash de la table
	MAYBE(1), // ces valeurs ne sont pas choisies au hasard mais sont des masques logiques
	TRUE(3); // en effet, on modifie la valeur du hash par un OU logique
	// De plus, on a l'ordre FALSE < MAYBE < TRUE
	
	public final int hash;
	public final int hashBool;
	private static Tribool reversed[] = new Tribool[4];
	
	static
	{
		for(Tribool t: values())
			reversed[t.hash] = t;
	}
	
	private Tribool(int hash)
	{
		this.hash = hash;
		hashBool = hash >> 1;
	}

	public static Tribool parse(int hash)
	{
		return reversed[hash];
	}

}
