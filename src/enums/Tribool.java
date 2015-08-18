package enums;

/**
 * Utilisé on veut qualifier un troisième état pour un booléen: "peut-être".
 * Utilisé surtout quand on interprète le comportement de l'ennemi.
 * @author pf
 *
 */

public enum Tribool {
	FALSE(0), // ces hashs sont utilisés dans la génération du hash de la table
	MAYBE(1), // ces valeurs ne sont pas choisies au hasard mais sont des masques logiques
	TRUE(3); // en effet, on modifie la valeur du hash par un OU logique
	
	private int hash;
	private static Tribool reversed[] = new Tribool[4];
	
	static
	{
		for(Tribool t: values())
			reversed[t.hash] = t;
	}
	
	private Tribool(int hash)
	{
		this.hash = hash;
	}
	
	public int getHash()
	{
		return hash;
	}

	public static Tribool parse(int hash)
	{
		return reversed[hash];
	}

}
