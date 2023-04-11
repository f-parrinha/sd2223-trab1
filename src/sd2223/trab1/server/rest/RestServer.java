package sd2223.trab1.server.rest;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import sd2223.trab1.discovery.Discovery;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public abstract class RestServer {

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    protected static void init(String[] args, Logger log, String service, String uri, int port, Class resource) throws UnknownHostException {
        // Receive arguments
        String domain = args[0];

        // Use Discovery to announce the uri of this server
        String ip = InetAddress.getLocalHost().getHostAddress();
        String serverURI = String.format(uri, ip, port);
        Discovery discovery = Discovery.getInstance();
        discovery.announce(domain, service, serverURI);

        try {
            ResourceConfig config = new ResourceConfig();
            config.register(resource);
            // config.register(CustomLoggingFilter.class);

            JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config);

            log.info(String.format("%s Server ready @ %s\n", service, serverURI));

            // More code can be executed here...
        } catch (Exception e) {
            log.severe(e.getMessage());
        }
    }
}
