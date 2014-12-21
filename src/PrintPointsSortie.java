import container.Container;
import enums.ServiceNames;

public class PrintPointsSortie {

	public static void main(String[] args)
	{
		try {
			(new Container()).getService(ServiceNames.A_STAR_STRATEGY);
		} catch (Exception e) {}
	}

}
