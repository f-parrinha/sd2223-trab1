package sd2223.trab1.clients.users;

import sd2223.trab1.api.User;
import sd2223.trab1.clients.RestUsersClient;
import sd2223.trab1.discovery.Discovery;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Class UpdateUserClient - defines a client for updating users' info
 *
 * @author Francisco Parrinha 	58360
 * @author Martin Magdalinchev 	58172
 */
public class UpdateUserClient {
    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    /** Constants */
    private static final Logger LOG = Logger.getLogger(CreateUserClient.class.getName());
    private static final String SPLITTER = "@";

    public static void main(String[] args) throws IOException {
        // Invalid arguments
        if (args.length != 5) {
            System.err.println("Use: java aula2.clients.UpdateUserClient name oldPassword newDisplayName newDomain newPassword");
            System.exit(-2);
        }
        String name = args[0];
        String oldPassword = args[1];
        String newDisplayName = args[2];
        String newDomain = args[3];
        String newPassword = args[4];
        var u = new User(name, newPassword, newDomain, newDisplayName);

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
            var result = new RestUsersClient(URI.create(url)).updateUser(name, oldPassword, u);
            System.out.println("Success: " + result);

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
