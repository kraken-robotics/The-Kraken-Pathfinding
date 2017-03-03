package tests;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

/**
 * From http://stackoverflow.com/questions/9288107/run-single-test-from-a-junit-class-using-command-line/9288513
 *
 */

public class SingleJUnitTestRunner {
    public static void main(String... args) throws ClassNotFoundException {
        String[] classAndMethod = args[0].split("#");
        Request request = Request.method(Class.forName(classAndMethod[0]),
                classAndMethod[1]);

        Result result = new JUnitCore().run(request);
        System.exit(result.wasSuccessful() ? 0 : 1);
    }
}