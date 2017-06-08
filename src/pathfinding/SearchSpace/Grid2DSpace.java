/**
 * 
 */
package pathfinding.SearchSpace;

import java.io.Serializable;
import java.util.ArrayList;

import smartMath.Vec2;
import table.obstacles.Obstacle;
import table.obstacles.ObstacleCirculaire;
import table.obstacles.ObstacleRectangulaire;
import utils.Log;
import utils.Read_Ini;

/**
 * @author Marsya, (Krissprolls), pf
 *	La classe espace de recherche
 *  Pour le robot, ce sera concr�tement la table
 *  Toutes les méthodes sont appelées dans les coordonnées de la grille (sauf les conversions)
 */

public class Grid2DSpace implements Serializable
{
	private static final long serialVersionUID = 1L;

	private boolean[][] datas;
	private static Grid2DPochoir[] pochoirs = null; // Non sérialisé car static
//	private int surface;
	
	// Taille de datas
	private int sizeX;
	private int sizeY;
	
	// Taille "normale" de la table
	private static int table_x = 3000;
	private static int table_y = 2000;
	private int marge = 20;
	private static int margeStatique = 20;
	
	private int num_pochoir; // 2^num_pochoir = reductionFactor
	private static int robotRadius;
	private static int rayon_robot_adverse = 200;
	
	
	/**
	 * Utilisé très rarement.
	 * @param config
	 * @param log
	 */
	public static void set_static_variables(Read_Ini config, Log log)
	{
		if(pochoirs == null)
		{
			table_x = Integer.parseInt(config.get("table_x"));
			table_y = Integer.parseInt(config.get("table_y"));
			rayon_robot_adverse = Integer.parseInt(config.get("rayon_robot_adverse"));
			robotRadius = Integer.parseInt(config.get("rayon_robot"));
			margeStatique = Integer.parseInt(config.get("marge"));

			pochoirs = new Grid2DPochoir[10];
			for(int i = 0; i < 10; i++)
				pochoirs[i] = new Grid2DPochoir(rayon_robot_adverse >> i);
		}
	}
	
	/**
	 * Ce constructeur est utilisé uniquement pour générer les caches.
	 * Construit un Grid2DSpace vide.
	 * @param reductionFactor
	 */
	public Grid2DSpace(int num_pochoir)
	{
		//Création du terrain avec obstacles fixes
		sizeX = table_x >> num_pochoir;
		sizeY = table_y >> num_pochoir;
		this.num_pochoir = num_pochoir;
//		surface = sizeX * sizeY;
		marge = margeStatique;
		datas = new boolean[sizeX+1][sizeY+1];
		// construit une map de sizeX * sizeY vide
		for(int i=0; i<sizeX; i++)
			for(int j=0; j<sizeY;j++)
				datas[i][j] = true;		
	}
	

	
	/**
	 * Transforme un chemin ou chaque pas est spécifié en un chemin lissé ou il ne reste que très peu de sommets
	 * @param le chemin non lissé (avec tout les pas)
	 * @return le chemin liss (avec typiquement une disaine de sommets grand maximum)
	 */
	public ArrayList<Vec2> lissage(ArrayList<Vec2> cheminFull)
	{
		if (cheminFull.size() < 2)
			return cheminFull;
		// Nettoie le chemin
		ArrayList<Vec2> chemin = new ArrayList<Vec2>();
		int 	lastXDelta = 0,
				lastYDelta = 0,
				xDelta = 0,
				yDelta = 0;
		
		// On doit rentrer les 2 premiers points du parcours
		//chemin.add(cheminFull.get(cheminFull.size()-1)); // ajoute la fin
		chemin.add(cheminFull.get(0));
		chemin.add(cheminFull.get(1));
		
		xDelta = (int)(cheminFull.get(1).x - cheminFull.get(0).x);
		yDelta = (int)(cheminFull.get(1).y - cheminFull.get(0).y);
		for (int i = 2; i < cheminFull.size(); ++i)	
		{
			lastXDelta = xDelta;
			lastYDelta = yDelta;
			xDelta = (int)(cheminFull.get(i).x - cheminFull.get(i-1).x);
			yDelta = (int)(cheminFull.get(i).y - cheminFull.get(i-1).y);
			
			if (xDelta != lastXDelta && yDelta != lastYDelta)	// Si virage, on garde le point, sinon non.
				chemin.add(cheminFull.get(i-1));
		}
		chemin.remove(1); // retire l'intermédiare de calcul
		chemin.add(cheminFull.get(cheminFull.size()-1)); // ajoute la fin
		
		
		// supprimes les points non nécéssaire.
		ArrayList<Vec2> out = new ArrayList<Vec2>();
		
		// saute les 2 derniers points, comme on ne pourra rien simplifier entre.
		for (int i = 0; i < chemin.size(); ++i)	
		{
			// regardes si un point plus loin peut �tre rejoint en ligne droite
			for (int j = chemin.size()-1; j > i; --j)
			{
				if (canCrossLine(chemin.get(i).x, chemin.get(i).y, chemin.get(j).x, chemin.get(j).y))
				{
					//System.out.println("Lissage loops parameters :  i = " + i + ";  j = " + j);
					//drawLine(chemin.get(i).x, chemin.get(i).y, chemin.get(j).x, chemin.get(j).y);
					// on a trouvé le point le plus loin que l'on peut rejoindre en ligne droite
					out.add(chemin.get(i));
					i = j-1;	// on continuras la recherche a partir de ce point.
					break;
				}
			}
		}
		// 	on ajoute le point d'arrivée au chemin final
		out.add(chemin.get(chemin.size()-1));
		
		return out;
	}
	
	

	/**
	 * Surcouche user-friendly d'ajout d'obstacle fixe
	 * @param obs
	 */
	public void appendObstacleFixe(Obstacle obs)
	{
		if(obs instanceof ObstacleRectangulaire)
			appendObstacleFixe((ObstacleRectangulaire)obs);
		else if(obs instanceof ObstacleCirculaire)
			appendObstacleFixe((ObstacleCirculaire)obs);
	}
	
	/**
	 * Ajout long d'un obstacle rectangulaire.
	 * Exécuté seulement lors de la génération du cache.
	 * @param obs
	 */
	private void appendObstacleFixe(ObstacleRectangulaire obs)
	{
		// Asumptions :  	obs.getPosition() returns the top left corner of the rectangle
		//					also, rectangle is Axis Aligned...
		for(int i = obs.getPosition().x - robotRadius - marge; i < obs.getPosition().x + obs.getLongueur_en_x() + robotRadius + marge; i++)
			for(int j = obs.getPosition().y - robotRadius - marge - obs.getLongueur_en_y(); j < obs.getPosition().y + robotRadius + marge; j++)
				if(i >= -table_x/2 && i < table_x/2 && j >= 0 && j < table_y && obs.distance(new Vec2(i,j)) < robotRadius + marge)
				{
					Vec2 posGrid = conversionTable2Grid(new Vec2(i,j));
					datas[posGrid.x][posGrid.y] = false;
				}
	}

	/**
	 * Ajout long d'un obstacle circulaire.
	 * Exécuté seulement lors de la génération du cache.
	 * @param obs
	 */
	private void appendObstacleFixe(ObstacleCirculaire obs)
	{
		int radius = obs.getRadius();
		for(int i = obs.getPosition().x - robotRadius - marge - radius; i < obs.getPosition().x + radius + robotRadius + marge; i++)	
			for(int j = obs.getPosition().y - robotRadius - marge - radius; j < obs.getPosition().y + radius + robotRadius + marge; j++)
				if(i >= -table_x/2 && i < table_x/2 && j >= 0 && j < table_y && obs.getPosition().distance(new Vec2(i,j)) < radius + robotRadius + marge)
				{
					Vec2 posGrid = conversionTable2Grid(new Vec2(i,j));
//					System.out.println("ij:"+ new Vec2(i,j));
//					System.out.println("posGrid:"+ posGrid);
					datas[posGrid.x][posGrid.y] = false;
				}
	}

	
	/**
	 * Ajout optimisé d'obstacle temporaires, de taille fixe.
	 * Utilise les pochoirs.
	 * Usage très courant.
	 * @param obs
	 */
	public void appendObstacleTemporaire(ObstacleCirculaire obs)
	{
		// Asumptions :  	obs.getPosition() returns the center of the circle (pretty obvious, but still...)
		
		Vec2 posPochoir = conversionTable2Grid(obs.getPosition());
		
		Grid2DPochoir pochoir = pochoirs[num_pochoir];

		int radius = pochoir.radius;

		// Recopie le pochoir
		for(int i = posPochoir.x - radius; i < posPochoir.x + radius; i++)
			for(int j = posPochoir.y - radius; j < posPochoir.y + radius; j++)
				if( i >= 0 && i < sizeX && j >=0 && j < sizeY)
					datas[i][j] = pochoir.datas[i-posPochoir.x+radius][j-posPochoir.y + radius] && datas[i][j];
					// Evaluation paresseuse: a priori, puisqu'on applique le pochoir, il y a plus de chances qu'il soit à false que datas
	}
	
	/**
	 * Constructeur privé de Grid2DSpace
	 * Utilisé seulement par makecopy
	 */
	private Grid2DSpace()
	{
	}
	
	/**
	 * Génère une copie. Utilise la méthode clone.
	 * @return
	 */
	public Grid2DSpace makeCopy()
	{
		Grid2DSpace output = new Grid2DSpace();
		copy(output);
		return output;
	}
	
	// renvois true si le tarrain est franchissable � la position donn�e, faux sinon
	// x est de droite a gauche et y de bas en haut
	public boolean canCross(int x, int y)
	{
	    try {
	        return datas[x][y];
	    }
	    catch(Exception e)
	    {
	        return false;
	    }
	}
	
	/**
	 * Implémentation user-friendly de canCross
	 * @param pos
	 * @return
	 */
	public boolean canCross(Vec2 pos)
	{
	    return canCross(pos.x, pos.y);
	}

	
	/**
     * Implémentation user-friendly de canCrossLine
	 * renvois true si le terrain est franchissable � en ligne droite entre les 2 positions donn�es, faux sinon
	 * @param a un point
	 * @param b un autre point
	 * @return
	 */
	public boolean canCrossLine(Vec2 a, Vec2 b)
	{
		if(a==null || b==null)
			return false;
	    return canCrossLine(a.x, a.y, b.x, b.y);
	}

	/**
	 * renvois true si le terrain est franchissable � en ligne droite entre les 2 positions donn�es, faux sinon
	 * @param a un point
	 * @param b un autre point
	 * @return
	 */
	public boolean canCrossLine(int x0, int y0, int x1, int y1)
	{
		 // Bresenham's line algorithm
		
		  int dx = Math.abs(x1-x0), sx = x0<x1 ? 1 : -1;
		  int dy = Math.abs(y1-y0), sy = y0<y1 ? 1 : -1; 
		  int err = (dx>dy ? dx : -dy)/2;
		  int e2;
		 
		  while(x0!=x1 || y0!=y1)
		  {
			    if (!canCross(x0,y0))
			    	return false;
				
				e2 = err;
				if (e2 >-dx) { err -= dy; x0 += sx; }
				if (e2 < dy) { err += dx; y0 += sy; }
		  }
		 return true;
	}
	
	/**
	 * Clone "this" into "other". "this" is not modified.
	 * @param other
	 */
	public void copy(Grid2DSpace other)
	{
		other.datas = new boolean[sizeX+1][sizeY+1];
		for (int i = 0; i < datas.length; i++) {
		    System.arraycopy(datas[i], 0, other.datas[i], 0, datas[0].length);
		}
//		other.surface = surface;
		other.sizeX = sizeX;
		other.sizeY = sizeY;
		other.num_pochoir = num_pochoir;
	}
	
	/**
	 * Convertit une longueur depuis les unités de la table dans les unités de la grille
	 * @param nb
	 * @return
	 */
	private int conversionTable2Grid(int nb)
	{
		return nb >> num_pochoir;
	}

	/**
	 * Convertit une longueur depuis les unités de la grille dans les unités de la tabme
	 * @param nb
	 * @return
	 */
	private int conversionGrid2Table(int nb)
	{
		return nb << num_pochoir;
	}

	/**
	 * Convertit un point depuis les unités de la table dans les unités de la grille
	 * @return
	 */
	public Vec2 conversionTable2Grid(Vec2 pos)
	{
		return new Vec2(conversionTable2Grid(pos.x + table_x/2),
						conversionTable2Grid(pos.y));
	}
	
	/**
	 * Convertit un point depuis les unités de la grille dans les unités de la table
	 * @return
	 */
	public Vec2 conversionGrid2Table(Vec2 pos)
	{
		return new Vec2(conversionGrid2Table(pos.x)-table_x/2,
						conversionGrid2Table(pos.y));
	}
	
	/**
	 * @return the sizeX
	 */
	public int getSizeX() {
		return sizeX;
	}

	/**
	 * @return the sizeY
	 */
	public int getSizeY() {
		return sizeY;
	}

	
	/**
	 * Surchage de la méthode toString de Object
	 * Permet de faire System.out.println(map) ou log.debug(map, this) pour afficher un Grid2DSpace
	 */
	public String toString()
	{
		String s = new String();
		for(int i = sizeY-1; i >= 0; i--)
		{
			for(int j = 0; j < sizeX; j++)
			{
				if(datas[j][i])
					s+=".";
				else
					s+="X";
			}
			s+="\n";
		}
		return s;
	}

	public void setMarge(int marge)
	{
	    this.marge = marge;
	}
	
}
