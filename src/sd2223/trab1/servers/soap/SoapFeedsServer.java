package sd2223.trab1.servers.soap;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.xml.ws.Endpoint;
import sd2223.trab1.discovery.Discovery;
import sd2223.trab1.util.Globals;

/**
 * Class SoapFeedsServer - A soap feeds server, receives http requests
 * <p>
 * Uses a discovery system to announce his URI
 *
 * @author Francisco Parrinha	58369
 * @author Martin Magdalinchev	58172
 */
public class SoapFeedsServer {

    public static final int PORT = 8081;
    public static String SERVER_BASE_URI = "http://%s:%s/soap";

    private static Logger Log = Logger.getLogger(SoapUsersServer.class.getName());

    public static void main(String[] args) throws Exception {

		System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
		System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
		System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
		System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");

        String domain = args[0];
        long id = Long.parseLong(args[1]);

        Log.setLevel(Level.INFO);
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_BASE_URI, ip, PORT);
            Discovery discovery = Discovery.getInstance();
            discovery.announce(domain, Globals.FEEDS_SERVICE_NAME, serverURI);
            Endpoint.publish(serverURI.replace(ip, "0.0.0.0"), new SoapFeedsWebService(domain, id));

            Log.info(String.format("%s Soap Server ready @ %s\n", Globals.FEEDS_SERVICE_NAME, serverURI));

        } catch (Exception e) {
            Log.severe(e.getMessage());
        }
    }
}
