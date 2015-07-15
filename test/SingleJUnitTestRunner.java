import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class SingleJUnitTestRunner {
		
	public static void main(String... args) throws ClassNotFoundException {
		String[] classAndMethod = args[0].split("#");
		Request request = Request.method(Class.forName(classAndMethod[0]), classAndMethod[1]);

		Result result = new JUnitCore().run(request);
		System.out.print(result.wasSuccessful() ? "Success " : "Failure ");
		List<Failure> failures = result.getFailures();
		for (Failure failure : failures) {
			System.out.println(failure);
		}
		System.exit(result.wasSuccessful() ? 0 : 1);
	}
}