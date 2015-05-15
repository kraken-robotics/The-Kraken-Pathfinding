package tests;

import org.junit.Before;
import org.junit.Test;

import threads.ThreadSerial;
import utils.Sleep;
import container.ServiceNames;

public class JUnit_Thread_Serie extends JUnit_Test {

	private ThreadSerial thread;
	
    @Before
    public void setUp() throws Exception {
        super.setUp();
        thread = (ThreadSerial) container.getService(ServiceNames.THREAD_SERIE);
        container.startAllThreads();
    }

    @Test
    public void test_reveil() throws Exception
    {
    	Sleep.sleep(20000);
    }

	
}
