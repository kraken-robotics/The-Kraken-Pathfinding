package entryPoints;
import container.Container;
import container.ServiceNames;
import exceptions.ContainerException;
import exceptions.FinMatchException;
import exceptions.PointSortieException;
import exceptions.SerialManagerException;
import exceptions.ThreadException;

public class PrintPointsSortie {

	public static void main(String[] args)
	{
			try {
				(new Container()).getService(ServiceNames.A_STAR_STRATEGY);
			} catch (ContainerException | ThreadException
					| SerialManagerException | FinMatchException
					| PointSortieException e) {
				e.printStackTrace();
			}
	}

}
