package sd2223.trab1.clients;


import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.clients.rest.RestFeedsClient;
import sd2223.trab1.clients.rest.RestUsersClient;
import sd2223.trab1.clients.soap.SoapFeedsClient;
import sd2223.trab1.clients.soap.SoapUsersClient;
import sd2223.trab1.discovery.Discovery;

import java.net.URI;

/**
 * Class FeedsClientFactory - Generates REST and SOAP clients based on url
 *
 * @author Francisco Parrinha	58369
 * @author Martin Magdalinchev	58172
 */
public class FeedsClientFactory {

    /** Constants */
    private static final String REST = "/rest";
    private static final String SOAP = "/soap";
    private static final String SERVICE = "FeedsService";

    public static Feeds get(String domain) {
        URI serverURI = Discovery.getInstance().knownUrisOf(SERVICE, domain);
        var uriString = serverURI.toString();

        if (uriString.endsWith(REST))
            return new RestFeedsClient(serverURI);
        else if (uriString.endsWith(SOAP))
            return new SoapFeedsClient(serverURI);
        else
            throw new RuntimeException("Unknown service type..." + uriString);
    }
}