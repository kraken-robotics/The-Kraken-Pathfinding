package strategie;

import container.Service;
import robot.Robot;
import table.Table;
import utils.Log;
import utils.Config;

/**
 * Le game state contient toutes les informations à connaître pour la stratégie. Il contient:
 * - Robot (real ou chrono), qui apporte des informations sur le robot (position, orientation, ...)
 * - Table, qui apporte des informations sur les obstacles et les éléments de jeux
 * @author pf
 *
 * @param <R>
 * R est soit un RobotReal, soit un RobotChrono
 */

public class GameState<R extends Robot> implements Service
{    
    /*
     * Les attributs public sont en "final". Cela signifie que les objets
     * peuvent être modifiés mais pas ces références.
     */
    public final Table table;
    public final R robot;

    // time contient le temps écoulé depuis le début du match en ms
    public long time_depuis_debut;
    public int pointsObtenus;	// points marqués depus le debut du match

    public GameState(Config config, Log log, Table table, R robot)
    {
        this.table = table;
        this.robot = robot;
        pointsObtenus = 0;
    }

    @Override
    public void updateConfig()
    {
        table.updateConfig();
        robot.updateConfig();
    }
    
}
