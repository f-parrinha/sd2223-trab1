package sd2223.trab1.server.rest;

import sd2223.trab1.server.resources.FeedsResource;

import java.net.UnknownHostException;
import java.util.logging.Logger;

public class FeedsServer extends RestServer {
    public static final int PORT = 8080;
    public static final String SERVICE = "FeedsService";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";
    private static final Logger LOG = Logger.getLogger(FeedsServer.class.getName());

    public static void main(String[] args) throws UnknownHostException {
        init(args, LOG, SERVICE, SERVER_URI_FMT, PORT, FeedsResource.class);
    }
}
