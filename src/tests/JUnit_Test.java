package tests;

import org.junit.Before;
import org.junit.After;

import utils.Log;
import utils.Config;
import container.Container;

public abstract class JUnit_Test
{

	protected Container container;
	protected Config config;
	protected Log log;
	
	@Before
	public void setUp() throws Exception
	{
		container = new Container();
		config = (Config) container.getService("Read_Ini");
		log = (Log) container.getService("Log");
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
