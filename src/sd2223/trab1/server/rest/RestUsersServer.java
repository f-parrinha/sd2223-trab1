package sd2223.trab1.server.rest;

import java.net.UnknownHostException;
import java.util.logging.Logger;

public class RestUsersServer extends RestServer{
	public static final int PORT = 8080;
	public static final String SERVICE = "UsersService";
	private static final String SERVER_URI_FMT = "http://%s:%s/rest";
	private static final Logger LOG = Logger.getLogger(RestUsersServer.class.getName());

	public static void main(String[] args) throws UnknownHostException {
		init(args, LOG, SERVICE, SERVER_URI_FMT, PORT, RestUsersResource.class);
	}
}
