package sd2223.trab1.servers.java;

import sd2223.trab1.api.Message;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.clients.UsersClientFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Logger;

/**
 * Class JavaFeeds - Handles feeds resource
 *
 * @author Francisco Parrinha   58360
 * @author Martin Magdalinchev  58172
 */
public class JavaFeeds implements Feeds {

    /** Constants */
    private final ConcurrentHashMap<String, List<Message>> feeds = new ConcurrentHashMap<>();
    private static final Logger LOG = Logger.getLogger(JavaUsers.class.getName());

    /** Variables */
    private final String domain;
    private final long id;

    /** Constructor */
    public JavaFeeds(String domain, long id) {
        this.domain = domain;
        this.id = id;
    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message message) {
        LOG.info("postMessage : " + message);

        // Check if user data is valid
        if(user == null || pwd == null) {
            LOG.info("Name or Password null.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        // Check if user is from server's domain
        String[] user_domain = user.split("@");
        if(!user_domain[1].equals(domain)) {
            LOG.info("Publisher does not exist in the current domain.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        /** @TODO Handle password authentication */
        var client = UsersClientFactory.get(domain);
        var _user = client.getUser(user_domain[0], pwd);
        if (!_user.isOK()) {
            return Result.error(_user.error());
        }

        // Insert message in feed
        message.setId(id);
        List<Message> feedTmp = feeds.get(user);
        List<Message> feed = feedTmp == null ? new LinkedList<>() : feedTmp;
        feed.add(message); feeds.put(user, feed);

        return Result.ok(message.getId());
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        LOG.info("removeFromPersonalFeed : " + mid);

        // Check if user data is valid
        if(user == null || pwd == null) {
            LOG.info("Name or Password null.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        /** @TODO Handle password authentication */
        var client = UsersClientFactory.get(domain);
        var _user = client.getUser(user, pwd);
        if (!_user.isOK()) {
            return Result.error(_user.error());
        }

        String userDomain = user.split("@")[1];
        List<Message> feed = feeds.get(user);
        Optional<Message> message = feed.stream().filter(m -> m.getId() == mid).findFirst();
        if(!userDomain.equals(domain) || message.isEmpty()) {
            LOG.info("Publisher does not exist in the current domain.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        // Remove message from feed and add (merge) it to the multimap
        feed.remove(message.get());
        feeds.put(user, feed);

        return null;
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        LOG.info("getMessage : " + mid);

        /** @TODO Propagation */

        List<Message> feed = feeds.get(user);
        Optional<Message> message = feed.stream().filter(m -> m.getId() == mid).findFirst();

        // Check if user data is valid
        if(!feeds.containsKey(user) || message.isEmpty()) {
            LOG.info("Name or message do not exist.");

            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        return Result.ok(message.get());
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        /** @TODO Propagation */

        var feed = feeds.get(user);
        // Check if user data is valid
        if(feed == null) {
            LOG.info("Name or message do not exist.");

            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        long firstTimestamp = feed.get(0).getCreationTime();
        return  Result.ok(feed.stream().filter(m -> firstTimestamp - m.getCreationTime() <= time).toList());
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        LOG.info("subUser: " + userSub);

        // Check if user data is valid
        if(user == null || pwd == null) {
            LOG.info("Name or Password null.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        // Get user client
        var client = UsersClientFactory.get(domain);
        var _user = client.getUser(user, pwd);

        // Get user related errors - 404 and 403
        if (!_user.isOK()) {
            return Result.error(_user.error());
        }



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
