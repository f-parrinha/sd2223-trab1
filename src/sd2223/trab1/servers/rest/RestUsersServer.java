package sd2223.trab1.servers.rest;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import sd2223.trab1.discovery.Discovery;
import sd2223.trab1.util.Globals;

/**
 * Class RestUsersServer - Rest users server
 * <p>
 * Uses a discovery system to announce his URI
 *
 * @author Francisco Parrinha	58369
 * @author Martin Magdalinchev	58172
 */
public class RestUsersServer {
	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	/** Constants */
	private static final Logger LOG = Logger.getLogger(RestUsersServer.class.getName());
	public static final int PORT = 8080;
	private static final String SERVER_URI_FMT = "http://%s:%s/rest";

	public static void main(String[] args) {
		// Get input
		String domain = args[0];

		try {
			// Use Discovery to announce the uri of this server
			String ip = InetAddress.getLocalHost().getHostAddress();
			String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
			Discovery discovery = Discovery.getInstance();
			discovery.announce(domain, Globals.USERS_SERVICE_NAME, serverURI);

			// Start server
			ResourceConfig config = new ResourceConfig();
			config.register(RestUsersResource.class);
			// config.register(CustomLoggingFilter.class);

			JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config);	// If it does not work add after URI.create(): .replace(ip, "0.0.0.0")

			LOG.info(String.format("%s Server ready @ %s\n", Globals.USERS_SERVICE_NAME, serverURI));

			// More code can be executed here...
		} catch (Exception e) {
			LOG.severe(e.getMessage());
		}
	}
}
