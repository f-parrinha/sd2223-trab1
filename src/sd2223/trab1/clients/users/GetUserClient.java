package sd2223.trab1.clients.users;

import sd2223.trab1.discovery.Discovery;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Class GetUserClient - defines client for searching one user info
 *
 * @author Francisco Parrinha 	58360
 * @author Martin Magdalinchev 	58172
 */
public class GetUserClient {
    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    /** Constants */
    private static final Logger LOG = Logger.getLogger(CreateUserClient.class.getName());
    private static final String SPLITTER = "@";

    public static void main(String[] args) throws IOException {
        // Invalid arguments
        if(args.length != 2) {
            System.err.println( "Use: java aula2.clients.GetUserClient name password");
            System.exit(-2);
        }
        String name = args[0];
        String password = args[1];

        // Get server URI
        Discovery discovery = Discovery.getInstance();
        var uris = discovery.knownUrisOf("UsersService", 5);
        if(uris.length == 0) {
            LOG.severe("No URIs found.");
            System.exit(-1);
        }

        // Run
        for (URI uri : uris) {
            // Run
            LOG.info("Sending request to server.");

            String url = uri.toString().split(SPLITTER)[0];
            var result = new RestUsersClient(URI.create(url)).getUser(name, password);
            System.out.println("Result: " + result);

            if (result != null) {
                // Close
                System.out.println("Closing...");
                System.exit(1);
            }
        }

        // Close
        System.out.println("Closing...");
        System.exit(1);
    }
}
