package sd2223.trab1.servers.java;

import sd2223.trab1.api.Feed;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.clients.UsersClientFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static sd2223.trab1.servers.rest.RestResource.num_seq;


/**
 * Class JavaFeeds - Handles feeds resource
 *
 * @author Francisco Parrinha   58360
 * @author Martin Magdalinchev  58172
 */
public class JavaFeeds implements Feeds {

    /** Constants */
    private final ConcurrentHashMap<String, Feed> feeds = new ConcurrentHashMap<>();
    private static final Logger LOG = Logger.getLogger(JavaUsers.class.getName());

    /** Variables */
    private final String domain;
    private final long base;

    /** Constructor */
    public JavaFeeds(String domain, long base) {
        this.domain = domain;
        this.base = base;
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
        var result = requestUser(user, pwd);
        if (!result.isOK()) {
            return Result.error(result.error());
        }

        // Insert message in feed
        aux_postMessage(user, message);
        post_propagateToFollowers(message, feeds.get(user).getFollowers());

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

        // Check if user exists in domain
        var result = requestUser(user, "");
        if (!result.isOK() && result.error().equals(Result.ErrorCode.NOT_FOUND)) {
            return Result.error(result.error());
        }

        // Check if message exists
        Message message = feeds.get(user).getMessage(mid);
        if(message == null) {
            LOG.info("Message does not exist");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        // Remove message from feed
        aux_removeFromPersonalFeed(user, message);
        remove_propagateToFollowers(message, feeds.get(user).getFollowers());

        return Result.ok(null);
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        LOG.info("getMessage : " + mid);

        /*@TODO Add domain propagation*/

        var result = requestUser(user, "");
        Message message = feeds.get(user).getMessage(mid);

        // Check if user or message exists
        if ((!result.isOK() && result.error().equals(Result.ErrorCode.NOT_FOUND)) || message == null) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        return Result.ok(message);
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        /*@TODO Add domain propagation*/

        // Check if user exists in domain or if password is correct
        var result = requestUser(user, "");
        if (!result.isOK() && result.error().equals(Result.ErrorCode.NOT_FOUND)) {
            return Result.error(result.error());
        }

        Feed feed = feeds.get(user);
        feeds.put(user, feed == null ? feed = new Feed() : feed);

        return Result.ok(feed.getMessages());
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
        var result = requestUser(user, pwd);
        if (!result.isOK()) {
            return Result.error(result.error());
        }

        // Add subscription
        aux_subUser(user, userSub);

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
        var result = requestUser(user, pwd);
        if (!result.isOK()) {
            return Result.error(result.error());
        }

        // Unsubscribe
        aux_unsubscribeUser(user, userSub);

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

        Feed feed = feeds.get(user);
        feeds.put(user, feed == null ? feed = new Feed() : feed);

        return Result.ok(feed.getSubscribers());
    }

    /** Auxiliary methods */

    private void aux_postMessage(String user, Message message){
        Feed feed = feeds.get(user);
        if(feed == null) { feed = new Feed(); }

        message.setId(num_seq.get() * 256 + base);
        feed.addMessage(message);
        feeds.put(user, feed);
    }


    private void aux_removeFromPersonalFeed(String user, Message message){
        Feed feed = feeds.get(user);
        if(feed == null) { return; }

        feed.removeMessage(message);
        feeds.put(user, feed);
    }

    private void aux_subUser(String user, String userSub) {
        // Gets feeds
        Feed feedUser = feeds.get(user);
        Feed feedUserSub = feeds.get(userSub);
        if(feedUser == null) { feedUser = new Feed(); }
        if(feedUserSub == null) { feedUserSub = new Feed(); }

        // Adds subscriber to user and follower to subscribed user
        feedUser.addSubscriber(userSub);
        feedUserSub.addFollower(user);

        // Merge
        feeds.put(user, feedUser);
        feeds.put(userSub, feedUserSub);
    }

    private void aux_unsubscribeUser(String user, String userSub) {
        // Gets feed
        Feed feedUser = feeds.get(user);
        Feed feedUserSub = feeds.get(userSub);

        // Removes subscribed user from user and removes follower from previously subscribed user
        feedUser.removeSubscriber(userSub);
        feedUserSub.removeFollower(user);

        // Merge
        feeds.put(user, feedUser);
        feeds.put(userSub, feedUserSub);
    }

    /**
     * Adds message to all user's followers' feeds
     * @param message message to post
     * @param followers user's followers
     */
    private void post_propagateToFollowers(Message message, List<String> followers){
        /*@TODO Add domain propagation*/

        for (String u : followers){
            aux_postMessage(u, message);
        }
    }

    /**
     * Removes message from all user's followers' feeds
     * @param message message to remove
     * @param followers user's followers
     */
    private void remove_propagateToFollowers(Message message, List<String> followers){
        /*@TODO Add domain propagation*/

        for (String u : followers){
            aux_removeFromPersonalFeed(u, message);
        }
    }

    /**
     * Creates GET request to user server from same domain to get the user
     * @param user user id@domain
     * @param pwd password
     * @return Result
     */
    private Result<User> requestUser(String user, String pwd) {
        String[] user_domain = user.split("@");
        var client = UsersClientFactory.get(domain);

        return client.getUser(user_domain[0], pwd);
    }
}
