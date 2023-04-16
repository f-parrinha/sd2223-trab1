package sd2223.trab1.servers.rest;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import sd2223.trab1.discovery.Discovery;

public class RestUsersServer {

	private static Logger Log = Logger.getLogger(RestUsersServer.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	public static final int PORT = 8080;
	public static final String SERVICE = "UsersService";
	private static final String SERVER_URI_FMT = "http://%s:%s/rest";

	public static void main(String[] args) {
		// Get input
		String domain = args[0];

		try {
			// Use Discovery to announce the uri of this server
			String ip = InetAddress.getLocalHost().getHostAddress();
			String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
			Discovery discovery = Discovery.getInstance();
			discovery.announce(domain, SERVICE, serverURI);

			// Start server
			ResourceConfig config = new ResourceConfig();
			config.register(RestUsersResource.class);
			// config.register(CustomLoggingFilter.class);

			JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config);	// If it does not work add after URI.create(): .replace(ip, "0.0.0.0")

			Log.info(String.format("%s Server ready @ %s\n", SERVICE, serverURI));

			// More code can be executed here...
		} catch (Exception e) {
			Log.severe(e.getMessage());
		}
	}
}
