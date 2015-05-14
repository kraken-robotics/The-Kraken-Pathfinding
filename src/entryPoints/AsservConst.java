package entryPoints;

import hook.Hook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import container.Container;
import container.ServiceNames;
import robot.stm.STMcard;

/**
 * Application permettant de trouver les constantes d'asservissement en rotation
 * et en translation
 * 
 * Ce n'est pas un code utile en match, c'est une application a part
 * 
 * @author pf, kayou
 * 
 */
public class AsservConst
{
    private static double kp = 0, kd = 0;
    private static int pwm_max = 0;
    private static BufferedReader bufferRead = new BufferedReader( new InputStreamReader(System.in) );

    /**
     *  point d'entrée du programme
     * @param args
     */
    public static void main(String[] args)
    {
        Container container;
        STMcard deplacements = null;
        int signe = 1;
        try
        {
            container = new Container();
            deplacements = (STMcard) container.getService(ServiceNames.STM_CARD);

            System.out.println("r ou t?");
            char asserv = (char) System.in.read();
            int distance = -1000;
            double angle = -Math.PI;

            while (true)
            {
                if (asserv == 't')
                {
                    distance = -distance;

                    if (bufferRead.ready())
                        bufferRead.readLine();

                    set_kp_kd_pwm();

                    deplacements.changeTranslationalFeedbackParameters(kp, kd, pwm_max);
                    System.out.println(distance);
                    deplacements.moveLengthwise(distance, new ArrayList<Hook>(), false);

                }
                else if (asserv == 'r')
                {
                    signe *= -1;
                    angle = (Math.PI / 2 + signe * Math.PI / 2);
                    if (bufferRead.ready())
                        bufferRead.readLine();

                    set_kp_kd_pwm();

                    deplacements.changeRotationalFeedbackParameters(kp, kd, pwm_max);
                    System.out.println(angle);
                    deplacements.turn(angle, new ArrayList<Hook>());

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
        kp = Double.parseDouble(s);
        System.out.println(kp);

        System.out.println("kd ?");
        s = bufferRead.readLine();
        kd = Double.parseDouble(s);
        System.out.println(kd);

        System.out.println("pwm_max ?");
        s = bufferRead.readLine();
        pwm_max = Integer.parseInt(s);
        System.out.println(pwm_max);
    }
}
