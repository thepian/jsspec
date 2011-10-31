package jsspec.extras;

import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class StopExtras extends Task {
	protected int port;
	public String key;

	public StopExtras() {
	}
	public void execute() throws BuildException {
		System.out.println("stopping extras");

        try
        {
            if (port<=0)
                System.err.println("'port' property for StopExtras Ant Task must be specified");
            if (key==null)
            {
                key="";
                System.err.println("'key' property for StopExtras must be specified");
                System.err.println("Using empty key");
            }

            Socket s=new Socket(InetAddress.getByName("127.0.0.1"),port);
            OutputStream out=s.getOutputStream();
            out.write((key+"\r\nstop\r\n").getBytes());
            out.flush();
            s.close();
        }
        catch (ConnectException e)
        {
            System.err.println("ERROR: Not running!");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
}
