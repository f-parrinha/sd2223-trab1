package sd2223.trab1.servers.rest;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import sd2223.trab1.discovery.Discovery;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

public class RestFeedsServer {
    private static Logger Log = Logger.getLogger(RestUsersServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static final int PORT = 8080;
    public static final String SERVICE = "FeedsService";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";

    public static void main(String[] args) {
        // Get input
        String domain = args[0];
        long id = Long.parseLong(args[1]);
        System.out.println(id);

        // Init server
        try {
            // Use Discovery to announce the uri of this server
            String ip = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
            Discovery discovery = Discovery.getInstance();
            discovery.announce(domain, SERVICE, serverURI);

            ResourceConfig config = new ResourceConfig();
            config.register(new RestFeedsResource(domain, id));
            // config.register(CustomLoggingFilter.class);

            JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config);

            Log.info(String.format("%s Server ready @ %s\n", SERVICE, serverURI));

            // More code can be executed here...
        } catch (Exception e) {
            Log.severe(e.getMessage());
        }
    }
}
