package sd2223.trab1.clients.users;

import sd2223.trab1.discovery.Discovery;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Class DeleteUserClient - defines a client for deleting users
 *
 * @author Francisco Parrinha 	58360
 * @author Martin Magdalinchev 	58172
 */
public class DeleteUserClient {
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
        if( args.length != 2) {
            System.err.println( "Use: java aula2.clients.DeleteUserClient userId password");
            System.exit(-2);
        }

        // Run
        String name = args[0];
        String password = args[1];

        LOG.info("Sending request to server.");

        var result = new RestUsersClient(URI.create(uri.toString())).deleteUser(name, password);
        System.out.println("Result: " + result);

        // Close
        System.out.println("Closing...");
        System.exit(1);
    }
}
