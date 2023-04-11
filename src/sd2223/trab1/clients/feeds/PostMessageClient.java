package sd2223.trab1.clients.feeds;

import sd2223.trab1.api.Message;
import sd2223.trab1.clients.RestFeedsClient;
import sd2223.trab1.clients.RestUsersClient;
import sd2223.trab1.clients.users.CreateUserClient;
import sd2223.trab1.discovery.Discovery;

import java.net.URI;
import java.util.Arrays;
import java.util.logging.Logger;

public class PostMessageClient {
    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    /** Constants */
    private static final Logger LOG = Logger.getLogger(PostMessageClient.class.getName());
    private static final String SPLITTER = "@";

    public static void main(String[] args) {
        // Check args
        if(args.length != 6){
            System.exit(-1);
        }
        String[] user_domain = args[0].split("@");
        String user = user_domain[0];
        String domain = user_domain[1];
        String pwd = args[1];
        long mId = Long.parseLong(args[2]);
        String mText = args[3];
        Message message = new Message(mId, user, domain, mText);

        // Get server URI
        Discovery discovery = Discovery.getInstance();
        var uris = discovery.knownUrisOf("FeedsService", 5);
        if(uris.length == 0) {
            LOG.severe("No URIs found.");
            System.exit(-1);
        }
        String url = Arrays.stream(uris).toList().stream().filter(uri -> uri.toString().split(SPLITTER)[1].equals(domain)).toString().split(SPLITTER)[0];

        // Run
        LOG.info("Sending request to server.");

        var result = new RestFeedsClient(URI.create(url)).postMessage(user, pwd, message);
        System.out.println("Result: " + result);

        // Close
        System.out.println("Closing...");
        System.exit(1);
    }
}
