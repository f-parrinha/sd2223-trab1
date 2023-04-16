package sd2223.trab1.servers.java;

import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.clients.UsersClientFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
    private final ConcurrentHashMap<String, List<Message>> feeds = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<String>> usersFollowers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<String>> usersSubscribers = new ConcurrentHashMap<>();
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
        var result = authorize(user, pwd);
        if (!result.isOK()) {
            return Result.error(result.error());
        }

        // Insert message in feed
        addToFeed(user, message);
        addPropagateToFollowers(message, usersFollowers.get(user));

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
        var result = authorize(user, "");
        if (!result.isOK() && result.error().equals(Result.ErrorCode.NOT_FOUND)) {
            return Result.error(result.error());
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
        removePropagateToFollowers(message, usersFollowers.get(user));

        return null;
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        LOG.info("getMessage : " + mid);

        /*@TODO Add domain propagation*/

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
        /*@TODO Add domain propagation*/

        // Check if user exists in domain or if password is correct
        var result = authorize(user, "");
        if (!result.isOK() && result.error().equals(Result.ErrorCode.NOT_FOUND)) {
            return Result.error(result.error());
        }

        var feedTmp = feeds.get(user);
        var feed = feedTmp == null ? new LinkedList<Message>() : feedTmp;

        return Result.ok(time == 0 ? feed : feed.stream().filter( m -> m.getCreationTime() < time).toList());
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
        var result = authorize(user, pwd);
        if (!result.isOK()) {
            return Result.error(result.error());
        }

        // Add subscription
        addAsFollower(user, userSub);
        addSubscriber(user, userSub);

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
        var result = authorize(user, pwd);
        if (!result.isOK()) {
            return Result.error(result.error());
        }

        // Unsubscribe
        removeAsFollower(user, userSub);
        removeSubscriber(user, userSub);

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
        List<String> subscribersListTmp = usersSubscribers.get(user);

        return Result.ok(subscribersListTmp == null ? new LinkedList<>() : subscribersListTmp);
    }

    /* ---- Propagation auxiliary methods ---- */

    /**
     * Adds message to all user's followers' feeds
     * @param message message to post
     * @param followers user's followers
     */
    private void addPropagateToFollowers(Message message, List<String> followers){
        if(followers == null) { return; }

        /*@TODO Add domain propagation*/
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

        /*@TODO Add domain propagation*/
        for (String u : followers){
            removeFromFeed(u, message);
        }
    }

    /* ---- Message post auxiliary methods ---- */

    /**
     * Adds a single message to a single feed
     * @param user user id@domain
     * @param message message to add
     */
    private void addToFeed(String user, Message message){
        List<Message> feed = feeds.get(user);
        if(feed == null){ feed = new LinkedList<>(); }

        if(feed.isEmpty() || !feed.contains(message)) {
            // Set unique ID
            long uniqueID = num_seq.get() * 256 + id;
            message.setId(uniqueID);

            feed.add(message);
            feeds.put(user, feed);
        }
    }

    /**
     * Removes a single message to a single feed
     * @param user user id@domain
     * @param message message to remove
     */
    private void removeFromFeed(String user, Message message){
        List<Message> feed = feeds.get(user);
        if(feed == null){ return; }

        feed.remove(message);
        feeds.put(user, feed);
    }

    /* ---- Subscription auxiliary methods ---- */

    /**
     * Adds a user as a follower to userSub
     * @param user user as follower
     * @param userSub user who gets a follow
     */
    private void addAsFollower(String user, String userSub){
        List<String> followersList= usersFollowers.get(userSub);
        if(followersList == null){ followersList = new LinkedList<>(); }

        if(followersList.isEmpty() || !followersList.contains(user)) {
            followersList.add(user);
        }

        usersFollowers.put(userSub, followersList);
    }

    /**
     * Removes user as a follower to userSub
     * @param user user as follower
     * @param userSub user who gets a follow
     */
    private void removeAsFollower(String user, String userSub){
        List<String> followersList = usersFollowers.get(userSub);
        if(followersList == null){ return; }

        followersList.remove(user);
        usersFollowers.put(userSub, followersList);
    }

    /**
     * Adds userSub to the user's subscribed list
     * @param user user
     * @param userSub subscribed user
     */
    private void addSubscriber(String user, String userSub){
        List<String> subscribersList= usersSubscribers.get(user);
        if(subscribersList == null){ subscribersList = new LinkedList<>(); }

        if(subscribersList.isEmpty() || !subscribersList.contains(user)) {
            subscribersList.add(userSub);
        }

        usersSubscribers.put(user, subscribersList);
    }

    /**
     * Removes userSub from the user's subscribed list
     * @param user user
     * @param userSub subscribed user
     */
    private void removeSubscriber(String user, String userSub){
        List<String> subscribersList = usersSubscribers.get(user);
        if(subscribersList == null){ return; }

        System.out.println("TEST LIST: "+subscribersList.remove(userSub));
        usersSubscribers.put(user, subscribersList);

        System.out.println("TEST RESULT: "+usersSubscribers.get(user));
    }

    /* ---- HTTP request auxiliary methods ---- */

    /**
     * Creates GET request to user server from same domain to get the user
     * @param user user id@domain
     * @param pwd password
     * @return Result
     */
    private Result<User> authorize(String user, String pwd) {
        String[] user_domain = user.split("@");
        var client = UsersClientFactory.get(domain);

        return client.getUser(user_domain[0], pwd);
    }
}
