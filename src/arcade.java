
import utils.Sleep;

import java.awt.event.*;
				

public class arcade implements KeyListener {

	KeyEvent key;
	
	public arcade ()  {
	}

		
	@Override
	public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == 16)
        {
            lanceur_journee_gate.codeactionneurs = 0;
        }
        else if (e.getKeyCode() == 17)
        {
            lanceur_journee_gate.codeactionneurs = 1;
        }
        else if (e.getKeyCode() == 87)
        {
            lanceur_journee_gate.codeactionneurs = 2;
        }
        else if (e.getKeyCode() == 18)
        {
            lanceur_journee_gate.codeactionneurs = 3;
        }
        else if (e.getKeyChar() == 32)
        {
            lanceur_journee_gate.codeactionneurs = 4;
        }
        else if (e.getKeyChar() == 88)
        {
            lanceur_journee_gate.codeactionneurs = 5;
        }
		if (e.getKeyCode() == KeyEvent.VK_UP)
		{
	        lanceur_journee_gate.avance = true;
			if (lanceur_journee_gate.codetourne != 1)
			{
				lanceur_journee_gate.codetourne = 1;
				Sleep.sleep(1000);
			}
		}
		else if (e.getKeyCode() == KeyEvent.VK_DOWN)
		{
	        lanceur_journee_gate.avance = true;
			if (lanceur_journee_gate.codetourne != 3)
			{
			lanceur_journee_gate.codetourne = 3;
			Sleep.sleep(1000);
			}
		}
			
		else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
		{
	        lanceur_journee_gate.avance = true;
			if (lanceur_journee_gate.codetourne != 0)
			{
			lanceur_journee_gate.codetourne = 0;
			Sleep.sleep(1000);
			}
		}
		else if (e.getKeyCode() == KeyEvent.VK_LEFT)
		{
	        lanceur_journee_gate.avance = true;
			if (lanceur_journee_gate.codetourne != 2)
			{
    			lanceur_journee_gate.codetourne = 2;
    			Sleep.sleep(1000);
			}
		}

		}
	@Override
	public void keyReleased(KeyEvent e) {
		lanceur_journee_gate.avance = false;
	}
	@Override
	public void keyTyped(KeyEvent e) {
		
	}
}

