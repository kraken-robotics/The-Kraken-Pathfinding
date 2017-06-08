package hook.methods;

import hook.Executable;
import robot.Locomotion;
import smartMath.Vec2;

/**
 * Classe implémentant la méthode changement de consigne, utilisée pour avoir une trajectoire courbe.
 * @author pf
 *
 */

public class ChangeConsigne implements Executable
{

        private Vec2 newConsigne;
        private Locomotion robot;
        
        public ChangeConsigne(Vec2 nouvelle_consigne, Locomotion robot)
        {
            this.robot = robot;
            this.newConsigne = nouvelle_consigne;
        }
        
        @Override
        public boolean execute()
        {
            robot.setConsigne(newConsigne);
            return true; // le robot doit bouger
        }
        
}
