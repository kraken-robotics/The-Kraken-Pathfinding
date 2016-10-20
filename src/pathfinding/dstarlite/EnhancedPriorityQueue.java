/*
Copyright (C) 2016 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package pathfinding.dstarlite;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import pathfinding.dstarlite.gridspace.PointGridSpace;

/**
 * Une priority queue qui permet de faire increaseKey et decreaseKey
 * @author pf
 *
 */

public class EnhancedPriorityQueue
{
	private DStarLiteNode[] tab = new DStarLiteNode[PointGridSpace.NB_POINTS];
	private int firstAvailable = 0;
	
	/**
	 * Renvoie la racine et la supprime
	 * @return
	 */
	public DStarLiteNode poll()
	{
		DStarLiteNode out = tab[0];
		tab[0] = tab[--firstAvailable];
		tab[0].indexPriorityQueue = 0;
		percolateDown(tab[0]);
		return out;
	}
	
	/**
	 * Renvoie la racine
	 * @return
	 */
	public DStarLiteNode peek()
	{
		return tab[0];
	}
	
	/**
	 * Vide la file
	 */
	public void clear()
	{
		firstAvailable = 0;
	}
	
	/**
	 * Échange deux nœuds
	 * @param index1
	 * @param index2
	 */
	private final void swap(int index1, int index2)
	{
		DStarLiteNode tmp = tab[index1];
		tab[index1] = tab[index2];
		tab[index2] = tmp;
		tab[index1].indexPriorityQueue = index1;
		tab[index2].indexPriorityQueue = index2;
	}
	
	/**
	 * Récupère l'indice du fils gauche
	 * @param index
	 * @return
	 */
	private final int filsGauche(int index)
	{
		return 2*index+1;
	}
	
	/**
	 * Récupère l'indice du fils droit
	 * @param index
	 * @return
	 */
	private final int filsDroit(int index)
	{
		return 2*index+2;
	}
	
	/**
	 * Récupère l'indice du père
	 * @param index
	 * @return
	 */
	private final int pere(int index)
	{
		return (index-1) / 2;
	}
	
	/**
	 * Ajoute un nœuds au tas
	 * @param node
	 */
	public void add(DStarLiteNode node)
	{
		tab[firstAvailable] = node;
		node.indexPriorityQueue = firstAvailable;
		firstAvailable++;
		percolateUp(node);
	}
	
	/**
	 * Percolate-down ce nœud
	 * @param node
	 */
	public void percolateDown(DStarLiteNode node)
	{
		DStarLiteNode n = node;
		int fg, diff;
		while((diff = (firstAvailable - 1 - (fg = 2*n.indexPriorityQueue+1))) >= 0)
		{
			if(diff > 0 && tab[fg].cle.compare(tab[fg+1].cle) > 0)
				fg++;
			if(n.cle.compare(tab[fg].cle) > 0)
				swap(fg, n.indexPriorityQueue);
			else
				return;
		}
	}
	
	/**
	 * Supprime un nœud qui n'est pas forcément la racine
	 * @param node
	 */
	public void remove(DStarLiteNode node)
	{
		DStarLiteNode last =  tab[--firstAvailable];
		tab[node.indexPriorityQueue] = last;
		last.indexPriorityQueue = node.indexPriorityQueue;
		
		if(node.cle.compare(last.cle) < 0) // ce qui a remplacé node est plus grand : on descend
			percolateDown(last);
		else
			percolateUp(last);
	}
	
	/**
	 * Percolate-up ce nœud
	 * @param node
	 */
	public void percolateUp(DStarLiteNode node)
	{
		int p;
		// si ce nœud n'est pas la racine et que son père est plus grand : on inverse
		while(node.indexPriorityQueue > 0 && tab[p = pere(node.indexPriorityQueue)].cle.compare(tab[node.indexPriorityQueue].cle) > 0)
			swap(node.indexPriorityQueue, p);
	}

	/**
	 * Renvoie "true" si la file est vide
	 * @return
	 */
	public boolean isEmpty()
	{
		return firstAvailable == 0;
	}
	
	/**
	 * Sauvegarde dans un fichier la priority queue sous format graphique
	 */
	public void print(int nb)
	{
		FileWriter fw;
		try {
			fw = new FileWriter(new File("priority-queue-"+nb+".dot"));
			try {
				fw.write("digraph priorityqueue {\n");
				
				for(int i = 0; i < firstAvailable; i++)
				{
						fw.write(i+"[label=\""+tab[i].cle+"\"];\n");
						if(filsGauche(i) < firstAvailable)
							fw.write(i+" -> "+filsGauche(i)+" [label=\"g\"];\n");
						if(filsDroit(i) < firstAvailable)
							fw.write(i+" -> "+filsDroit(i)+" [label=\"g\"];\n");
				}
				fw.write("}\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				fw.close();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}
}
