package sd2223.trab1.servers.rest;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import sd2223.trab1.discovery.Discovery;
import sd2223.trab1.util.Globals;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Class RestFeedsServer - A rest feeds server, receives http requests
 * <p>
 * Uses a discovery system to announce his URI
 *
 * @author Francisco Parrinha	58369
 * @author Martin Magdalinchev	58172
 */
public class RestFeedsServer {

    /** Constants */
    private static final Logger LOG = Logger.getLogger(RestUsersServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static final int PORT = 8080;
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";

    public static void main(String[] args) {
        // Get input
        String domain = args[0];
        long id = Long.parseLong(args[1]);

        // Init server
        try {
            // Use Discovery to announce the uri of this server
            String ip = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
            Discovery discovery = Discovery.getInstance();
            discovery.announce(domain, Globals.FEEDS_SERVICE_NAME, serverURI);

            ResourceConfig config = new ResourceConfig();
            config.register(new RestFeedsResource(domain, id));
            // config.register(CustomLoggingFilter.class);

            JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config);

            LOG.info(String.format("%s Server ready @ %s\n", Globals.FEEDS_SERVICE_NAME, serverURI));

            // More code can be executed here...
        } catch (Exception e) {
            LOG.severe(e.getMessage());
        }
    }
}
