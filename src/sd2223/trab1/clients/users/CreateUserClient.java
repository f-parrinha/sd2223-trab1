package sd2223.trab1.clients.users;

import sd2223.trab1.api.User;
import sd2223.trab1.clients.RestUsersClient;
import sd2223.trab1.discovery.Discovery;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Class CreateUserClient - defines client for creating users
 *
 * @author Francisco Parrinha 	58360
 * @author Martin Magdalinchev 	58172
 */
public class CreateUserClient {
	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	/** Constants */
	private static final Logger LOG = Logger.getLogger(CreateUserClient.class.getName());
	private static final String SPLITTER = "@";

	public static void main(String[] args) throws IOException {
		// Invalid arguments
		if (args.length != 4) {
			System.err.println("Use: java aula3.clients.CreateUserClient name pwd domain displayName");
			System.exit(-2);
		}

		String name = args[0];
		String pwd = args[1];
		String domain = args[2];
		String displayName = args[3];
		User u = new User(name, pwd, domain, displayName);

		// Get server URI
		var uris = Discovery.getInstance().knownUrisOf("UsersService", 5);
		if(uris.length == 0) {
			LOG.severe("No URIs found.");
			System.exit(-1);
		}
		String url = Arrays.stream(uris).toList().stream().filter(uri -> uri.toString().split(SPLITTER)[1].equals(domain)).toString().split(SPLITTER)[0];

		// Run
		LOG.info("Sending request to server.");
		var result = new RestUsersClient(URI.create(url)).createUser(u);
		System.out.println("Result: " + result);

		// Close
		System.out.println("Closing...");
		System.exit(1);
	}
}
