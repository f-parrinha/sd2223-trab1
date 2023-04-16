package sd2223.trab1.servers.soap;


import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.xml.ws.Endpoint;
import sd2223.trab1.discovery.Discovery;

/**
 * Class SoapUsersServer - A soap users server, receives http requests
 * <p>
 * Uses a discovery system to announce his URI
 *
 * @author Francisco Parrinha	58369
 * @author Martin Magdalinchev	58172
 */
public class SoapUsersServer {

    public static final int PORT = 8081;
    public static final String SERVICE_NAME = "users";
    public static String SERVER_BASE_URI = "http://%s:%s/soap";

    private static Logger Log = Logger.getLogger(SoapUsersServer.class.getName());

    public static void main(String[] args) throws Exception {

//		System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
//		System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
//		System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
//		System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");

        String domain = args[0];

        Log.setLevel(Level.INFO);
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_BASE_URI, ip, PORT);

            Discovery discovery = Discovery.getInstance();
            discovery.announce(domain, SERVICE_NAME, serverURI);

            Endpoint.publish(serverURI, new SoapUsersWebService());

            Log.info(String.format("%s Soap Server ready @ %s\n", SERVICE_NAME, serverURI));
        } catch (Exception e) {
            Log.severe(e.getMessage());
        }
    }

}
