package sd2223.trab1.servers.java;

import sd2223.trab1.api.Message;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.clients.UsersClientFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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
    private final ConcurrentHashMap<String, List<String>> userFollowers = new ConcurrentHashMap<>();
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

        // Check if user exists in domain or if password is correct
        String[] user_domain = user.split("@");
        var client = UsersClientFactory.get(domain);
        var _user = client.getUser(user_domain[0], pwd);
        if (!_user.isOK()) {
            return Result.error(_user.error());
        }

        // Insert message in feed
        addToFeed(user, message);
        addPropagateToFollowers(message, userFollowers.get(user));

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

        // Check if user exists in domain or if password is correct
        String[] user_domain = user.split("@");
        var client = UsersClientFactory.get(domain);
        var _user = client.getUser(user_domain[0], pwd);
        if (!_user.isOK()) {
            return Result.error(_user.error());
        }

        List<Message> feed = feeds.get(user);
        Optional<Message> messageOptional = feed.stream().filter(m -> m.getId() == mid).findFirst();

        // Check if message exists
        if(messageOptional.isEmpty()) {
            LOG.info("Message does not exist");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        // Remove message from feed
        Message message = messageOptional.get();
        removeFromFeed(user, message);
        removePropagateToFollowers(message, userFollowers.get(user));

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
        LOG.info("from: " + user + "; subUser: " + userSub);

        // Check if user data is valid
        if(user == null || pwd == null) {
            LOG.info("Name or Password null.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        // Check if user exists in domain or if password is correct
        String[] user_domain = user.split("@");
        var client = UsersClientFactory.get(domain);
        var _user = client.getUser(user_domain[0], pwd);
        if (!_user.isOK()) {
            return Result.error(_user.error());
        }

        // Subscribe user
        List<String> subscribersListTmp = userFollowers.get(user);
        List<String> subscribersList = subscribersListTmp == null ? new LinkedList<>() : subscribersListTmp;
        subscribersList.add(userSub); userFollowers.put(user, subscribersList);

        return Result.ok(null);
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        LOG.info("from: " + user + "; unsubscribeUser: " + userSub);

        // Check if user data is valid
        if(user == null || pwd == null) {
            LOG.info("Name or Password null.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        // Check if user exists in domain or if password is correct
        String[] user_domain = user.split("@");
        var client = UsersClientFactory.get(domain);
        var _user = client.getUser(user_domain[0], pwd);
        if (!_user.isOK()) {
            return Result.error(_user.error());
        }

        List<String> subscribersListTmp = userFollowers.get(user);
        List<String> subscribersList = subscribersListTmp == null ? new LinkedList<>() : subscribersListTmp;
        subscribersList.remove(userSub); userFollowers.put(user, subscribersList);

        return Result.ok(null);
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        LOG.info("listSubs from user: " + user);

        // Check if user data is valid
        if(user == null) {
            LOG.info("Name null.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        // Gets subscribers
        List<String> subscribersListTmp = userFollowers.get(user);
        List<String> subscribersList = subscribersListTmp == null ? new LinkedList<>() : subscribersListTmp;

        return Result.ok(subscribersList);
    }

    /**
     * Adds message to all user's followers' feeds
     * @param message message to post
     * @param followers user's followers
     */
    private void addPropagateToFollowers(Message message, List<String> followers){
        if(followers == null) { return; }

        for (String u : followers){
            addToFeed(u, message);
        }
    }

    /**
     * Removes message from all user's followers' feeds
     * @param message message to remove
     * @param followers user's followers
     */
    private void removePropagateToFollowers(Message message, List<String> followers){
        if(followers == null) { return; }

        for (String u : followers){
            removeFromFeed(u, message);
        }
    }

    /**
     * Adds a single message to a single feed
     * @param user user id@domain
     * @param message message to add
     */
    private void addToFeed(String user, Message message){
        message.setId(id);  /*@TODO - Create unique IDs */
        List<Message> feedTmp = feeds.get(user);
        List<Message> feed = feedTmp == null ? new LinkedList<>() : feedTmp; feed.add(message);
        feeds.put(user, feed);
    }

    /**
     * Removes a single message to a single feed
     * @param user user id@domain
     * @param message message to remove
     */
    private void removeFromFeed(String user, Message message){
        List<Message> feed = feeds.get(user);
        feed.remove(message);
        feeds.put(user, feed);
    }


}
