package pathfinding;

import java.util.ArrayList;

import exceptions.strategie.PathfindingException;
import pathfinding.SearchSpace.Grid2DSpace;
import smartMath.Vec2;

/**
 * Pathfinding simple utilisé quelques points de passages
 * @author pf
 *
 */

public class SimplePathfinding
{

    public final Grid2DSpace mapObstaclesTemporaires;
    public final Grid2DSpace mapObstaclesFixes;
    private static Vec2[] points;
    private Vec2[] points_convertis;
    private boolean[][] canCross = new boolean[6][6];
    
    // canCrossTorches sert de base à canCross en prenant en compte les torches
    private static boolean[][][] canCrossTorches = new boolean[4][6][6];
    
    /*
     * Numérotation des points
     *   1 2
     * 0     3
     *   5 4
     */

    static
    {
        points = new Vec2[6];
        points[0] = new Vec2(-1100, 900);
        points[1] = new Vec2(-600, 1400);
        points[2] = new Vec2(600, 1400);
        points[3] = new Vec2(1100, 900);
        points[4] = new Vec2(600, 500);
        points[5] = new Vec2(-600, 500);
        
        for(int t = 0; t < 4; t++)
            for(int i = 0; i < 6; i++)
                for(int j = 0; j < 6; j++)
                    // Si les cases sont adjacentes, c'est bon
                    canCrossTorches[t][i][j] = i==j || (j-i+6)%6 == 1 || (i-j+6)%6 == 1;
        // Cas particuliers
        canCrossTorches[0][5][1] = true;
        canCrossTorches[0][1][5] = true;
        canCrossTorches[0][2][4] = true;
        canCrossTorches[0][4][2] = true;

        canCrossTorches[1][2][4] = true;
        canCrossTorches[1][4][2] = true;

        canCrossTorches[2][5][1] = true;
        canCrossTorches[2][1][5] = true;

       /* 
        * 3: les deux torches sont là
        * 2: la torche de gauche a disparue
        * 1: la torche de droite a disparue
        * 0: les deux torches sont absentes
        */

    }
    
    public SimplePathfinding(Grid2DSpace mapObstaclesFixes, Grid2DSpace mapObstaclesTemporaires)
    {
        this.mapObstaclesFixes = mapObstaclesFixes;
        this.mapObstaclesTemporaires = mapObstaclesTemporaires;
        points_convertis = new Vec2[6];
        for(int i = 0; i < 6; i++)
            points_convertis[i] = mapObstaclesFixes.conversionTable2Grid(points[i]);
        mapObstaclesFixes.setMarge(0);
    }
    
    public ArrayList<Vec2> chemin(Vec2 depart, Vec2 arrivee) throws PathfindingException
    {
        ArrayList<Vec2> chemin = new ArrayList<Vec2>();
        chemin.add(depart);
        int procheDepart = 0;
        int procheArrivee = 0;
        
        // On calcule l'entrée la plus proche
        for(int i = 1; i < 6; i++)
        {
            // Pouvoir un canCross et pas un canCrossLine? Pour deux raisons
            // - c'est plus rapide à exécuter
            // - si on est dans un obstacle, canCrossLine va pleurer
            if(depart.SquaredDistance(points[i]) < depart.SquaredDistance(points[procheDepart]) && mapObstaclesTemporaires.canCross(points_convertis[i]))
                procheDepart = i;
            if(arrivee.SquaredDistance(points[i]) < arrivee.SquaredDistance(points[procheArrivee]) && mapObstaclesTemporaires.canCross(points_convertis[i]))
                procheArrivee = i;
        }
        
        // Calcul du parcours dans le sens trigo
        int pointActuel;
        int prochainPoint = procheDepart;
        int distanceTrigo = 0;
        boolean successTrigo = false;
        do {
            distanceTrigo++;
            pointActuel = prochainPoint;
            prochainPoint = (pointActuel+5)%6;
            successTrigo = pointActuel == procheArrivee;
        } while(!successTrigo && canCross[pointActuel][prochainPoint]);
        
        if(successTrigo)
        {
            int i = procheDepart;
            while(i != prochainPoint)
            {
                chemin.add(points[i]);
                i = (i+5)%6;
            } 
            chemin.add(arrivee);
            if(distanceTrigo <= 4)
                return lissage(chemin); // si on a trouvé un chemin pas plus long que la moitié: jackpot
        }
        
        // Calcul du parcours dans le sens horaire
        prochainPoint = procheDepart;
        int distanceHoraire = 0;
        boolean successHoraire = false;
        do {
            distanceHoraire++;
            pointActuel = prochainPoint;
            prochainPoint = (pointActuel+1)%6;
            successHoraire = pointActuel == procheArrivee;
        } while(!successHoraire && canCross[pointActuel][prochainPoint]);

        // Personne n'a trouvé la solution
        if(!successTrigo && !successHoraire)
            throw new PathfindingException();
                
        if(!successHoraire || distanceTrigo <= distanceHoraire)
            return chemin; // la solution trouvée dans le sens trigo

        // Sinon, on retourne le chemin en sens horaire
        chemin.clear();
        chemin.add(depart);
        int i = procheDepart;
        while(i != prochainPoint)
        {
            chemin.add(points[i]);
            i = (i+1)%6;
        } 
        chemin.add(arrivee);
        return lissage(chemin);
    }
    
    /**
     * Met à jour canCross
     */
    public void updateCanCross(int codeTorches)
    {
        // On recopie le canCross associé à ce code torche
        for(int i = 0; i < 6; i++)
            System.arraycopy(canCrossTorches[codeTorches][i], 0, canCross[i], 0, 6);

        // Puis on ajoute les obstacles mobiles
        for(int i = 0; i < 6; i++)
            for(int j = 0; j < 6; j++)
                if(i != j)
                    // évaluation paresseuse
                    canCross[i][j] &= mapObstaclesTemporaires.canCrossLine(points_convertis[i].clone(), points_convertis[j].clone()); // les clones sont nécessaires
        
/*        for(int i = 0; i < 6; i++)
            for(int j = 0; j < 6; j++)
                System.out.println(i+" "+j+": "+canCross[i][j]);*/
    }
    
    /**
     * Lissage très simple: on regarde si on peut sauter le premier point et l'avant-dernier.
     * @param chemin
     * @return
     */
    public ArrayList<Vec2> lissage(ArrayList<Vec2> chemin)
    {
/*        System.out.println("Avant lissage");
        for(Vec2 point: chemin)
            System.out.println(point);*/
            
        while(chemin.size() >= 3 && mapObstaclesFixes.canCrossLine(mapObstaclesFixes.conversionTable2Grid(chemin.get(0)), mapObstaclesFixes.conversionTable2Grid(chemin.get(2)))
                    && mapObstaclesTemporaires.canCrossLine(mapObstaclesTemporaires.conversionTable2Grid(chemin.get(0)), mapObstaclesTemporaires.conversionTable2Grid(chemin.get(2))))
                chemin.remove(1);
        
        int max = chemin.size();
        while(max >= 3 && mapObstaclesFixes.canCrossLine(mapObstaclesFixes.conversionTable2Grid(chemin.get(max-1)), mapObstaclesFixes.conversionTable2Grid(chemin.get(max-3)))
                    && mapObstaclesTemporaires.canCrossLine(mapObstaclesTemporaires.conversionTable2Grid(chemin.get(max-1)), mapObstaclesTemporaires.conversionTable2Grid(chemin.get(max-3))))
        {
                chemin.remove(max-2);
                max = chemin.size();
        }

/*        System.out.println("Après lissage");
        for(Vec2 point: chemin)
            System.out.println(point);*/

        chemin.remove(0); // on retire le point de départ

        return chemin;
    }
}
