package jsspec.extras;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class StartExtras extends Task {
	{
		System.out.println("load");
	}
	public StartExtras() {
		System.out.println("construct");
	}
	public void execute() throws BuildException {
		System.out.println("stopping extras");
		new Main().stop();
	}
}
