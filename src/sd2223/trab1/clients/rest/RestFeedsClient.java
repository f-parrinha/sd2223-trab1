package sd2223.trab1.clients.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.rest.FeedsService;

import java.net.URI;
import java.util.List;

/**
 * Class RestFeedsClient - A rest feeds client, generates http requests
 *
 * @author Francisco Parrinha	58369
 * @author Martin Magdalinchev	58172
 */
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

    private Result<Void> clt_removeFromPersonalFeed(String user, long mid, String pwd) {
        Response r = target.path(user).path(Long.toString(mid))
                .queryParam(FeedsService.PWD, pwd).request()
                .accept(MediaType.APPLICATION_JSON)
                .delete();

        return super.toJavaResult(r, Void.class);
    }

    private Result<Message> clt_getMessage(String user, long mid) {
        Response r = target.path(user).path(Long.toString(mid)).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        return super.toJavaResult(r, Message.class);
    }

    @SuppressWarnings("unchecked")
    private Result<List<Message>> clt_getMessages(String user, long time) {
        Response r = target.path(user)
                .queryParam(FeedsService.TIME, time).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        return super.toJavaResult(r, (Class<List<Message>>)(Object)List.class);
    }

    @SuppressWarnings("unchecked")
    private Result<List<Message>> clt_getMessagesFromRemote(String user, String originalDomain, long time) {
        Response r = target.path(user).path(originalDomain)
                .path(Long.toString(time)).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        return super.toJavaResult(r, (Class<List<Message>>)(Object)List.class);
    }

    private Result<Void> clt_subUser(String user, String userSub, String pwd) {
        Response r = target.path(user).path(userSub)
                .queryParam(FeedsService.PWD, pwd).request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(userSub, MediaType.APPLICATION_JSON));

        return super.toJavaResult(r, Void.class);
    }

    private Result<Void> clt_unsubscribeUser(String user, String userSub, String pwd) {
        Response r = target.path(user).path(userSub)
                .queryParam(FeedsService.PWD, pwd).request()
                .accept(MediaType.APPLICATION_JSON)
                .delete();

        return super.toJavaResult(r, Void.class);
    }

    @SuppressWarnings("unchecked")
    private Result<List<String>> clt_listSubs(String user) {
        Response r = target.path(user).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        return super.toJavaResult(r, (Class<List<String>>)(Object)List.class);
    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message message) {
        return super.reTry(() -> clt_postMessage(user, pwd, message));
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        return super.reTry(() -> clt_removeFromPersonalFeed(user, mid, pwd));
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        return super.reTry(() -> clt_getMessage(user, mid));
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        return super.reTry(() -> clt_getMessages(user, time));
    }

    @Override
    public Result<List<Message>> getMessagesFromRemote(String user, String originalDomain, long time) {
        return super.reTry(() -> clt_getMessagesFromRemote(user, originalDomain, time));
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        return super.reTry(() -> clt_subUser(user, userSub, pwd));
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        return super.reTry(() -> clt_unsubscribeUser(user, userSub, pwd));
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        return super.reTry(() -> clt_listSubs(user));
    }
}
