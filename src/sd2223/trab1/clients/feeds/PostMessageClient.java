package sd2223.trab1.clients.feeds;

import sd2223.trab1.api.Message;
import sd2223.trab1.clients.users.CreateUserClient;
import sd2223.trab1.discovery.Discovery;

import java.net.URI;
import java.util.logging.Logger;

public class PostMessageClient {
    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    /** Constants */
    private static final Logger LOG = Logger.getLogger(PostMessageClient.class.getName());

    public static void main(String[] args) {
        // Check args
        if(args.length != 3){
            System.exit(-1);
        }
        String user = args[0];
        String pwd = args[1];
        
        // Get server URI
        var uris = Discovery.getInstance().knownUrisOf("FeedsService", 5);
        if(uris.length == 0) {
            LOG.severe("No URIs found.");
            System.exit(-1);
        }
        URI uri = uris[0];
    }
}
