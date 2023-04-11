package sd2223.trab1.clients;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.rest.FeedsService;
import sd2223.trab1.api.rest.UsersService;

import java.net.URI;
import java.util.List;

public class RestFeedsClient extends RestClient implements FeedsService {
    /** Variables */
    private final WebTarget target;

    /** Constructor */
    public RestFeedsClient(URI serverURI) {
        super(serverURI);
        target = client.target(serverURI).path(UsersService.PATH);
    }

    @Override
    public long postMessage(String user, String pwd, Message msg) {
        return super.reTry(() -> clt_postMessage(user, pwd, msg));
    }

    @Override
    public void removeFromPersonalFeed(String user, long mid, String pwd) {

    }

    @Override
    public Message getMessage(String user, long mid) {
        return null;
    }

    @Override
    public List<Message> getMessages(String user, long time) {
        return null;
    }

    @Override
    public void subUser(String user, String userSub, String pwd) {

    }

    @Override
    public void unsubscribeUser(String user, String userSub, String pwd) {

    }

    @Override
    public List<String> listSubs(String user) {
        return null;
    }


    private long clt_postMessage(String user, String pwd, Message msg) {
        Response r = target.path(user)
                .queryParam(FeedsService.PWD, pwd).request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(msg, MediaType.APPLICATION_JSON));

        if (r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity()) {
            return msg.getId();
        }

        System.out.println("Error, HTTP error status: " + r.getStatus());
        return -1;
    }
}
