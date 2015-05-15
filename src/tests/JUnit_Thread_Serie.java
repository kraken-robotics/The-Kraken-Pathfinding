package tests;

import org.junit.Before;
import org.junit.Test;

import threads.RobotThread;
import threads.ThreadSerial;
import threads.ThreadTimer;
import utils.Sleep;
import container.ServiceNames;

public class JUnit_Thread_Serie extends JUnit_Test {

	private ThreadSerial thread;
	
    @Before
    public void setUp() throws Exception {
        super.setUp();
        thread = (ThreadSerial) container.getService(ServiceNames.THREAD_SERIE);
        ((ThreadTimer) container.getService(ServiceNames.THREAD_TIMER)).start();
    }

    @Test
    public void test_reveil() throws Exception
    {
    	thread.start();
    	Sleep.sleep(20000);
    	RobotThread.stopAllThread();
    }

	
}
