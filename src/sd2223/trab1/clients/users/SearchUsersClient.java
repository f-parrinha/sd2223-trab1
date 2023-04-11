package sd2223.trab1.clients.users;

import sd2223.trab1.discovery.Discovery;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Class SearchUsersClient - defines a client for searching users' display names by pattern
 *
 * @author Francisco Parrinha 	58360
 * @author Martin Magdalinchev 	58172
 */
public class SearchUsersClient {
	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	/** Constants */
	private static final Logger LOG = Logger.getLogger(CreateUserClient.class.getName());

	public static void main(String[] args) throws IOException {
		// Get server URI
		Discovery discovery = Discovery.getInstance();
		var uris = discovery.knownUrisOf("UsersService", 5);
		if(uris.length == 0) {
			LOG.severe("No URIs found.");
			System.exit(-1);
		}
		URI uri = uris[0];

		// Invalid arguments
		if (args.length != 1) {
			System.err.println("Use: java aula3.clients.SearchUsersClient userId ");
			System.exit(-2);
		}

		// Run
		String userId = args[0];

		System.out.println("Sending request to server.");
		var result = new RestUsersClient(URI.create(uri.toString())).searchUsers(userId);
		System.out.println("Success: (" + result.size() + " users)");
		result.forEach(System.out::println);

		// Close
		System.out.println("Closing...");
		System.exit(1);
	}
}
