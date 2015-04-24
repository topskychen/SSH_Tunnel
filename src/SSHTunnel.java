import java.io.Console;
import java.util.Properties;
import java.util.Scanner;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;


/**
 * This class is for establishing ssh.
 * @author qian chen
 *
 */
public class SSHTunnel {

	final static JSch	jsch	= new JSch();
	static Session session = null;
	static String sshHost = "localhost";
	static String remoteHost = "localhost";
	static int localPort = 9001;
	static int remotePort = 8080;
	
	public static void connect(String strSshUser, String strSshPassword,
			String strSshHost, int nSshPort, String strRemoteHost,
			int nLocalPort, int nRemotePort) throws JSchException {
		
		session = jsch.getSession(strSshUser, strSshHost, 22);
		session.setPassword(strSshPassword);

		final Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);

		session.connect();
		session.setPortForwardingL(nLocalPort, strRemoteHost, nRemotePort);
	}

	public static void disconnect() {
		if (session != null) {
			session.disconnect();
		}
	}
	
	/**
	 * Establish ssh tunnel to connect the faith server
	 */
	public static boolean doSshTunnel(Scanner in) {
		try {
			String username = null;
			String password = null;
			
			System.out.println("Please input your username:");
			
	    	username = in.nextLine();
	    	
	    	/**
	    	 * hide the password if possible.
	    	 */
			Console console = System.console();
		    if (console == null) {
				System.out.println("Please input your password:");
				password = in.nextLine();
		    } else {
		    	char passwordArray[] = console.readPassword("Please input your password:\n");
		    	password = new String(passwordArray);
		    }
		    System.out.println("logining...");
			SSHTunnel.connect(username, password, sshHost, 22, remoteHost, localPort, remotePort);
			return true;
		} catch (JSchException e) {
			e.printStackTrace();
			SSHTunnel.disconnect();
		}
		return false;
	}
	
	public static void main(String[] args) {
		/**
	     * If you do not want to use ssh tunnel, you can remove following four lines.
	     */
		if (args.length == 2) {
			String[] tks = args[0].split(":");
			if (tks.length != 2) {
				System.out.println("localPort:sshHost");
				return;
			}
			localPort = Integer.parseInt(tks[0]);
			sshHost = tks[1];
			tks = args[1].split(":");
			if (tks.length != 2) {
				System.out.println("remoteHost:remotePort");
				return;
			}
			remoteHost = tks[0];
			remotePort = Integer.parseInt(tks[1]);
		} else {
			System.out.println("The args are localPort:sshHost remoteHost:remotePort.");
			return;
		}
		Scanner in = new Scanner(System.in);
		while (!doSshTunnel(in)) {
			System.out.println("Ssh tunnel is not established, please try again!");
		}
		System.out.println("SSH tunnel is established from localhost:" + localPort + " to " + remoteHost + ":" + remotePort + " via " + sshHost + "!");
		while (in.hasNext()) {
			String line = in.nextLine();
			if (line.equalsIgnoreCase("exit")) break;
		}
		disconnect();
		in.close();
	}
}
