package debug;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * ABWABWA
 * @author pf
 *
 */

public class MouseListener extends MouseAdapter {

	private Fenetre fenetre;

	public MouseListener(Fenetre fenetre)
	{
		super();
		this.fenetre = fenetre;
		fenetre.addMouseListener(this);
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
//		Point point = e.getPoint();
//		fenetre.afficheCoordonnees(point);
		fenetre.repaint();
	}
}
