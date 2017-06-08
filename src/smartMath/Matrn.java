package smartMath;

import exceptions.MatrixException;
import Jama.Matrix;
/**
 * Classe de calcul matriciel
 * @author pf
 * @author clément
 *
 */

public class Matrn {
	
	public double[][] matrice;
	public int[] taille;

	public Matrn(double[][] t)
	{
		matrice = t;
		taille = new int[2];
		taille[0] = t.length ;
		taille[1] = t[0].length;
	}
	
	public Matrn(int n)
	{
		matrice = new double[n][n];
		taille = new int[2];
		taille[0] = n;
		taille[1] = n;
	}
	
	/**
	 * @param p nombre de lignes
	 * @param n nombre de colonnes
	 */
	public Matrn(int p,int n)
	{
		matrice = new double[p][n];
		taille = new int[2];
		taille[0] = n;
		taille[1] = p;
	}
	
	/**
	 * la matrice aura une taille (p,n) et tous les éléments vaudront valeur
	 * @param p
	 * @param n
	 * @param valeur : la valeur par défaut
	 */
	public Matrn(int p,int n, int valeur)
	{
		matrice = new double[p][n];
		taille = new int[2];
		taille[0] = n;
		taille[1] = p;
		for(int i = 0; i< taille[0]; i++)
		{
			for(int j = 0; j < taille[1]; j++)
			{
				setCoeff(valeur ,i, j);
			}
		}		
	}
	
	/**
	 * Modifie le coeff en (i,j)
	 * @param coeff
	 * @param i la ligne
	 * @param j la colonne
	 */
	public void setCoeff(double coeff, int i, int j)
	{
		matrice[i][j] = coeff;
	}
	
	/**
	 * Récupère le coeff de (i,j)
	 * @param i la ligne
	 * @param j la colonne
	 */
	public double getCoeff(int i, int j)
	{
		return matrice[i][j];
	}
	
	public int getNbLignes()
	{
		return taille[1];
	}

	public int getNbColonnes()
	{
		return taille[0];
	}

	public void additionner_egal (Matrn A) throws MatrixException
	{	
		if(taille[0] != A.taille[0] || taille[1] != A.taille[1])
			throw new MatrixException();
		for(int i = 0; i < taille[0]; i++)
			for(int j = 0; j < taille[1]; j++)
				 matrice[j][i]= matrice[j][i]+A.matrice[j][i];
	}
	public Matrn additionner (Matrn A) throws MatrixException
	{
		Matrn a = new Matrn(taille[0],taille[1]);
		if(taille[0] != A.taille[0] || taille[1] != A.taille[1])
			throw new MatrixException();
		for(int i = 0; i < taille[0]; i++)
			for(int j = 0; j < taille[1]; j++)
				 a.matrice[j][i]= matrice[j][i]+A.matrice[j][i];
		return a;
	}
	public Matrn soustraire (Matrn A) throws MatrixException
	{	
		Matrn a = new Matrn(taille[0],taille[1]);
		if(taille[0] != A.taille[0] || taille[1] != A.taille[1])
			throw new MatrixException();
		for(int i = 0; i < taille[0]; i++)
			for(int j = 0; j < taille[1]; j++)
				 a.matrice[j][i]= matrice[j][i] - A.matrice[j][i];
		return a;
	}
	public void soustraire_egal (Matrn A) throws MatrixException
	{	
		if(taille[0] != A.taille[0] || taille[1] != A.taille[1])
			throw new MatrixException();
		for(int i = 0; i < taille[0]; i++)
			for(int j = 0; j < taille[1]; j++)
				 matrice[j][i]= matrice[j][i] - A.matrice[j][i];
	}
	public void multiplier_egal(Matrn A) throws MatrixException
	{//multiplier this. avec A
		if( this.taille[0] != A.taille[1])
			throw new MatrixException();
		Matrn m = new Matrn(taille[0], A.taille[1]);
		for(int i = 0; i< taille[0]; i++)
		{
			for(int j = 0;j < A.taille[1];j++)
			{
				m.matrice[i][j] = 0;
				for(int k = 0; k < taille[1];k++)
				{
					m.matrice[i][j] += matrice[i][k]*A.matrice[k][j];
				}
			}
		}
		this.matrice = m.matrice;
	}
	public Matrn multiplier(Matrn A) throws MatrixException
	{//multiplier this. avec A
		if( this.taille[0] != A.taille[1])
			throw new MatrixException();
		Matrn m = new Matrn(taille[0], A.taille[1]);
		for(int i = 0; i< taille[0]; i++)
		{
			for(int j = 0;j < A.taille[1];j++)
			{
				m.matrice[i][j] = 0;
				for(int k = 0; k < taille[1];k++)
				{
					m.matrice[i][j] += matrice[i][k]*A.matrice[k][j];
				}
			}
		}
		return m;
	}
	
	
	
	public void transpose_egal() throws MatrixException
	{
		if(taille[0] != taille[1])
			throw new MatrixException();
		for(int i = 0; i < taille[0]; i++)
			for(int j = 0; j < i; j++)
			{
				double tmp = matrice[j][i];
				matrice[j][i] = matrice[i][j];
				matrice[i][j] = tmp;
			}
	}

	public Matrn transpose() throws MatrixException
	{		
		if(taille[0] != taille[1])
			throw new MatrixException();
		Matrn a = new Matrn(taille[0], taille[1]);
		for(int i = 0; i < taille[0]; i++)
			for(int j = 0; j <= i; j++)
			{
				a.matrice[j][i] = matrice[i][j];
				a.matrice[i][j] = matrice[j][i];				
			}
		return a;
	}

	public Matrn inverser()
	{
		// TODO : check
		//Il faut impérativement que la matrice soit inversible !!! enfin je vais voir si je peux gérer le cas contraire
		Matrix a = new Matrix(matrice);
		a.inverse();
		Matrn b = new Matrn(a.getArrayCopy());
		return b;
	}
	static public Matrn identiter(int n)
	{
		// TODO : doc
		//nom de la méthode on français, mais ça explique qu'on construit une matice identité de taille n
		
		Matrn ident= new Matrn(n,n,0);
		for(int i = 0; i <n; i++)
		{
			ident.setCoeff(1, i, i);
		}
		return ident;
	}
	
	public void clone(Matrn m)
	{
		taille = m.taille.clone();
		matrice = m.matrice.clone();		
	}
	public void multiplier_scalaire(double a)
	{
		for(int i = 0; i < taille[0]; i++)
			for(int j = 0; j < taille[1]; j++)
				 matrice[j][i]= a*matrice[j][i];
	}
	
}
	
