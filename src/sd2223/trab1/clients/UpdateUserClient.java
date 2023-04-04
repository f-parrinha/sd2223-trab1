package sd2223.trab1.clients;

import sd2223.trab1.api.User;
import sd2223.trab1.server.Discovery;
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
        if (args.length != 5) {
            System.err.println("Use: java aula2.clients.UpdateUserClient name oldPassword newDisplayName newDomain newPassword");
            System.exit(-2);
        }

        // Run
        String name = args[0];
        String oldPassword = args[1];
        String newDisplayName = args[2];
        String newDomain = args[3];
        String newPassword = args[4];

        var u = new User(name, newPassword, newDomain, newDisplayName);

        var result = new RestUsersClient(URI.create(uri.toString())).updateUser(name, oldPassword, u);
        System.out.println("Success : " + result);

        // Close
        System.out.println("Closing...");
        System.exit(1);
    }
}
