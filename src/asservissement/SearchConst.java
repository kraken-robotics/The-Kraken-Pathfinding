package asservissement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import container.Container;
import enums.ServiceNames;
import robot.cardsWrappers.LocomotionCardWrapper;

/**
 * Application permettant de trouver les constantes d'asservissement en rotation
 * et en translation
 * 
 * Ce n'est pas un code utile en match, c'est une application a part
 * 
 * @author pf, kayou
 * 
 */
public class SearchConst
{
    private static float kp = 0, kd = 0;
    private static int pwm_max = 0;
    private static BufferedReader bufferRead = new BufferedReader( new InputStreamReader(System.in) );

    /**
     *  point d'entrée du programme
     * @param args
     */
    public static void main(String[] args)
    {
        Container container;
        LocomotionCardWrapper deplacements = null;
        int signe = 1;
        try
        {
            container = new Container();
            deplacements = (LocomotionCardWrapper) container.getService(ServiceNames.LOCOMOTION_CARD_WRAPPER);

            System.out.println("r ou t?");
            char asserv = (char) System.in.read();
            float distance = -1000;
            float angle = -(float) Math.PI;

            while (true)
            {
                if (asserv == 't')
                {
                    distance = -distance;

                    if (bufferRead.ready())
                        bufferRead.readLine();

                    set_kp_kd_pwm();

                    deplacements.changeTranslationnalFeedbackParameters(kp, kd, pwm_max);
                    System.out.println(distance);
                    deplacements.moveForward(distance);

                }
                else if (asserv == 'r')
                {
                    signe *= -1;
                    angle = (float) (Math.PI / 2 + signe * Math.PI / 2);
                    if (bufferRead.ready())
                        bufferRead.readLine();

                    set_kp_kd_pwm();

                    deplacements.changeRotationnalFeedbackParameters(kp, kd, pwm_max);
                    System.out.println(angle);
                    deplacements.turn(angle);

                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
	
	/**
	 *  demande à l'utilisateur de nouvelles valeurs de constantes
	 * @throws IOException
	 */
    public static void set_kp_kd_pwm() throws IOException
    {
        String s;
        System.out.println("kp ?");
        s = bufferRead.readLine();
        kp = Float.parseFloat(s);
        System.out.println(kp);

        System.out.println("kd ?");
        s = bufferRead.readLine();
        kd = Float.parseFloat(s);
        System.out.println(kd);

        System.out.println("pwm_max ?");
        s = bufferRead.readLine();
        pwm_max = Integer.parseInt(s);
        System.out.println(pwm_max);
    }
}
