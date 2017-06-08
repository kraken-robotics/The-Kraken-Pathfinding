/**
 * 
 */
package pathfinding;

import java.io.Serializable;

import exceptions.strategie.PathfindingException;
import smartMath.Vec2;

/**
 * Classe contenant l'ensemble des données brutes du cache
 * C'est elle qui sera très grosse en mémoire.
 * 
 * Le cache contient toutes les distances entre tous les couples de points de la table avec obstacles fixes
 * On stocke les distances de mannathan, en int.
 * Il y a besoin de 2 paramètres pour le point de départ, et 2 paramètres pour le point d'arrivée.
 * Du coup on bosses sur une classe qui sera grosso modo un gros tableau d'int à 4 dimentions.
 * @author Marsu, pf
 *
 */

class CacheHolder implements Serializable
{
	private static final long serialVersionUID = 1L;
	private int log_reduction;
	private int log_mm_per_unit;
	/* 8 bits non signés (grâce à byte2int et int2byte), avec une précision par défaut de 15 mm par unité, soit une distance maximale 254*15 = 3810mm
	 * 255 est la valeur infinie pour chemin impossible
	 * Taille de la classe: 
	 * - reduction 0: 2000²*3000² = 36To
	 * - reduction 1: 2 To
	 * - reduction 2: 141 Go
	 * - reduction 3: 9 Go
	 * - reduction 4: 550 Mo
	 * - reduction 5: 30 Mo
	 * - reduction 6: 2 Mo
	 */
	private byte[][][][] data;
	private int table_x;
	private int distance;
	
	public CacheHolder(int sizeX, int sizeY, int log_reduction, int log_mm_per_unit, int table_x)
	{
		this.log_mm_per_unit = log_mm_per_unit;
		this.log_reduction = log_reduction;
		this.table_x = table_x;
		data = new byte[sizeX][sizeY][sizeX][sizeY];
	}

	/**
	 * Donne la distance mémorisée entre depart et arrivée
	 * @param depart
	 * @param arrivee
	 * @return
	 * @throws PathfindingException
	 */
	public int getDistance(Vec2 depart, Vec2 arrivee) throws PathfindingException
	{
		distance = byte2int(data[(depart.x+table_x/2) >> log_reduction][depart.y >> log_reduction][(arrivee.x+table_x/2) >> log_reduction][arrivee.y >> log_reduction]);
		
	//	if(distance == 255)							// WARING : Ce if ralentit considérablement la stratégie
	//		throw new PathfindingException();
	//	else
		
		return distance << log_mm_per_unit;
	}
	
	/**
	 * Insère dans le CacheHolder une distance en mm
	 * @param depart
	 * @param arrivee
	 * @param distance
	 */
	public void setDistance(Vec2 depart, Vec2 arrivee, int distance)
	{
		data[(depart.x+table_x/2) >> log_reduction][depart.y >> log_reduction][(arrivee.x+table_x/2) >> log_reduction][arrivee.y >> log_reduction] = int2byte(distance >> log_mm_per_unit);
	}

	/**
	 * Insère dans le CacheHolder un chemin impossible
	 * @param depart
	 * @param arrivee
	 */
	public void setImpossible(Vec2 depart, Vec2 arrivee)
	{
		data[(depart.x+table_x/2) >> log_reduction][depart.y >> log_reduction][(arrivee.x+table_x/2) >> log_reduction][arrivee.y >> log_reduction] = int2byte(255);
	}

	/** 
	 * Conversion de int à byt afin avoir des byte signés.
	 * @param b
	 * @return
	 */
	private byte int2byte(int b)
	{
		return (byte)(b-128);
	}
	
	/**
	 * Conversion de byte à in tafin d'avoir des byte signés.
	 * @param b
	 * @return
	 */
	private int byte2int(byte b)
	{
		return (int)(b+128);
	}
	
}
