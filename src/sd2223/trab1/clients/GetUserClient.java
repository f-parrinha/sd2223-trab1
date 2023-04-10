package sd2223.trab1.clients;

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
        if(args.length != 2) {
            System.err.println( "Use: java aula2.clients.GetUserClient name password");
            System.exit(-2);
        }

        // Run
        String name = args[0];
        String password = args[1];

        System.out.println("Sending request to server.");

        var result = new RestUsersClient(URI.create(uri.toString())).getUser(name, password);
        System.out.println("Result : "+result);

        // Close
        System.out.println("Closing...");
        System.exit(1);
    }
}
