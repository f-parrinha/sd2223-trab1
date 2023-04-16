package sd2223.trab1.servers.soap;

import jakarta.inject.Singleton;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.rest.FeedsService;
import sd2223.trab1.api.soap.UsersException;
import sd2223.trab1.servers.java.JavaFeeds;
import sd2223.trab1.servers.soap.SoapWebService;

import java.util.List;

@Singleton
public class SoapFeedsWebService extends SoapWebService<UsersException> implements FeedsService {
    final Feeds impl;


    public SoapFeedsWebService(String domain, long id) {
        super( (result)-> new UsersException( result.error().toString()));
        this.impl = new JavaFeeds(domain, id);
    }
    @Override
    public long postMessage(String user, String pwd, Message msg) throws UsersException {
        return super.fromJavaResult(impl.postMessage( user, pwd, msg));
    }

    @Override
    public void removeFromPersonalFeed(String user, long mid, String pwd) throws UsersException {
        super.fromJavaResult(impl.removeFromPersonalFeed(user, mid, pwd));
    }

    @Override
    public Message getMessage(String user, long mid) throws UsersException {
        return super.fromJavaResult(impl.getMessage(user, mid));
    }

    @Override
    public List<Message> getMessages(String user, long time) throws UsersException {
        return super.fromJavaResult(impl.getMessages(user, time));
    }

    @Override
    public void subUser(String user, String userSub, String pwd) throws UsersException {
        super.fromJavaResult(impl.subUser(user, userSub, pwd));
    }

    @Override
    public void unsubscribeUser(String user, String userSub, String pwd) throws UsersException {
        super.fromJavaResult(impl.unsubscribeUser(user, userSub, pwd));
    }

    @Override
    public List<String> listSubs(String user) throws UsersException {
        return super.fromJavaResult(impl.listSubs(user));
    }
}
