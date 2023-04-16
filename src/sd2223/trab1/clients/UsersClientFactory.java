package sd2223.trab1.clients;

import java.net.URI;

import sd2223.trab1.api.java.Users;
import sd2223.trab1.clients.rest.RestUsersClient;
import sd2223.trab1.clients.soap.SoapUsersClient;
import sd2223.trab1.discovery.Discovery;

/**
 * Class UsersClientFactory - Generates REST and SOAP clients based on url
 *
 * @author Francisco Parrinha	58369
 * @author Martin Magdalinchev	58172
 */
public class UsersClientFactory {

	/** Constants */
	private static final String REST = "/rest";
	private static final String SOAP = "/soap";
	private static final String SERVICE = "UsersService";

	public static Users get(String domain) {
		URI serverURI = Discovery.getInstance().knownUrisOf(SERVICE, domain);
		var uriString = serverURI.toString();

		if (uriString.endsWith(REST))
			return new RestUsersClient(serverURI);
		else if (uriString.endsWith(SOAP))
			return new SoapUsersClient(serverURI);
		else
			throw new RuntimeException("Unknown service type..." + uriString);
	}
}
