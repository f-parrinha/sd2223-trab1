package sd2223.trab1.clients.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.rest.FeedsService;
import sd2223.trab1.api.rest.UsersService;

import java.net.URI;
import java.util.List;

public class RestFeedsClient extends RestClient implements Feeds {

    /** Variables */
    final WebTarget target;

    /** Constructor */
    public RestFeedsClient( URI serverURI ) {
        super( serverURI );
        target = client.target( serverURI ).path( FeedsService.PATH );
    }

    private Result<Long> clt_postMessage(String user, String pwd, Message message) {

        Response r = target.path(user)
                .queryParam(FeedsService.PWD, pwd).request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(message, MediaType.APPLICATION_JSON));

        return super.toJavaResult(r, Long.class);
    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message message) {
        return super.reTry(() -> clt_postMessage(user, pwd, message));
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        return null;
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        return null;
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        return null;
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        return null;
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        return null;
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        return null;
    }
}
