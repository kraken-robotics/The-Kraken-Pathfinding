package tests;

import org.junit.Before;
import org.junit.After;

import utils.Log;
import utils.Config;
import container.Container;
import enums.ServiceNames;

public abstract class JUnit_Test
{

	protected Container container;
	protected Config config;
	protected Log log;
	
	@Before
	public void setUp() throws Exception
	{
		container = new Container();
		config = (Config) container.getService(ServiceNames.CONFIG);
		log = (Log) container.getService(ServiceNames.LOG);
	}

	@After
	public void tearDown() throws Exception {
		container.destructor();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	
}
