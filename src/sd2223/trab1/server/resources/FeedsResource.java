package sd2223.trab1.server.resources;

import jakarta.inject.Singleton;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.rest.FeedsService;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Class FeedsResource - handles feeds' management
 *
 * @author Francisco Parrinha 	58360
 * @author Martin Magdalinchev 	58172
 */
@Singleton
public class FeedsResource implements FeedsService {

    /** Constants */
    private static final String DOMAIN = "@%s";
    private static final Logger LOG = Logger.getLogger(FeedsResource.class.getName());

    /** Variables */
    private final ConcurrentHashMap<User, List<Message>> feeds = new ConcurrentHashMap<>();

    /** Constructor */
    public FeedsResource() {
    }

    @Override
    public long postMessage(String user, String pwd, Message msg) {
        return 0;
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
}
