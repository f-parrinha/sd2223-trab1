package sd2223.trab1.servers.java;

import sd2223.trab1.api.Feed;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.clients.FeedsClientFactory;
import sd2223.trab1.clients.UsersClientFactory;
import sd2223.trab1.util.Globals;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Class JavaFeeds - Handles feeds resource
 *
 * @author Francisco Parrinha   58360
 * @author Martin Magdalinchev  58172
 */
public class JavaFeeds implements Feeds {

    /**
     * Class Propagator - handles propagations within and to outer domains
     *
     * @author Francisco Parrinha   58360
     * @author Martin Magdalinchev  58172
     */
    public final class Propagator {

        /**
         * Creates GET request to user server from same domain to get the user
         * @param user user id@domain
         * @param pwd password
         * @return Result
         */
        public Result<User> requestUser(String user, String pwd) {
            String[] user_domain = user.split("@");
            var client = UsersClientFactory.get(domain);

            return client.getUser(user_domain[0], pwd);
        }

        /**
         * Finds a message in subscribers' messages
         *
         * @param mid message id
         * @param subs subscribers list
         */
        public Message propagateSingle(long mid, List<String> subs){
            Message message = null;

            for (String u : subs) {
                String[] user_domain = u.split("@");

                if(user_domain[1].equals(domain)) {
                    // Inside domain propagation
                    message = feeds.get(u).getMessage(mid);

                } else {
                    var client = FeedsClientFactory.get(user_domain[1]);
                    message = client.getMessage(u, mid).value();
                }

                // Quit if found..
                if (message != null){ break; }
            }

            return message;
        }

        /**
         * Gets messages from subscribers
         *
         * @param time newest time
         * @param subs user subs
         */
        public List<Message> propagateList(long time, List<String> subs){
            List<Message> messages = new LinkedList<>();

            for (String u : subs) {
                String[] user_domain = u.split("@");

                if(user_domain[1].equals(domain)) {
                    // Inside domain propagation
                    messages.addAll(new LinkedList<>(feeds.get(u).getMessages(time)));
                } else {
                    var client = FeedsClientFactory.get(user_domain[1]);

                    // This getMessages also creates propagation, and we don't want that..
                    // We must filter by the name of the owner
                    var unfilteredMessages = new LinkedList<>(client.getMessages(u, time).value());
                    var filteredMessages = unfilteredMessages.stream().filter(m -> m.getUser().equals(user_domain[0])).toList();
                    messages.addAll(filteredMessages);
                }
            }

            return messages;
        }
    }

    /** Constants */
    private final ConcurrentHashMap<String, Feed> feeds = new ConcurrentHashMap<>();
    private static final Logger LOG = Logger.getLogger(JavaUsers.class.getName());
    private static final String UPDATE_FEED_ADD = "ADD";
    private static final String UPDATE_FEED_DELETE = "DELETE";

    /** Variables */
    private final String domain;
    private final long base;
    private final Propagator propagator;

    /** Constructor */
    public JavaFeeds(String domain, long base) {
        this.domain = domain;
        this.base = base;
        propagator = new Propagator();
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
        var result = propagator.requestUser(user, pwd);
        if (!result.isOK()) {
            return Result.error(result.error());
        }

        // Insert message in feed
        feeds.put(user, updateFeedMessage(message, feeds.get(user), UPDATE_FEED_ADD));

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
        var result = propagator.requestUser(user, "");
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
        feeds.put(user, updateFeedMessage(message, feeds.get(user), UPDATE_FEED_DELETE));

        return Result.ok(null);
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        LOG.info("getMessage : " + mid);

        /*@TODO Add domain propagation*/
        /* Inner propagation in done */

        var result = propagator.requestUser(user, "");
        Feed feed = feeds.get(user);
        Message message = feed.getMessage(mid);
        if(message == null) { message = propagator.propagateSingle(mid, feeds.get(user).getSubscribers()); }  // Propagate to get message..

        // Check if user or message exists
        if ((!result.isOK() && result.error().equals(Result.ErrorCode.NOT_FOUND)) || message == null) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        return Result.ok(message);
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        LOG.info("getMessages : " + user + "; time: " +time);
        /*@TODO Add domain propagation*/
        /* Inner propagation in done */

        // Check if user exists in domain or if password is correct
        var result = propagator.requestUser(user, "");
        if (!result.isOK() && result.error().equals(Result.ErrorCode.NOT_FOUND)) {
            return Result.error(result.error());
        }

        Feed feed = feeds.get(user);
        if(feed == null) { return Result.ok(new LinkedList<>()); }  // If there are no messages, return immediately..

        // Concatenate messages
        var ownMessages = feed.getMessages(time).stream();
        var propagatedMessages = propagator.propagateList(time, feed.getSubscribers()).stream();
        var merged = Stream.concat(ownMessages, propagatedMessages).toList();

        return Result.ok(merged);
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
        var result = propagator.requestUser(user, pwd);
        if (!result.isOK()) {
            return Result.error(result.error());
        }

        // Add subscription
        feeds.put(user, updateFeedSubscriber(user, userSub, UPDATE_FEED_ADD));

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
        var result = propagator.requestUser(user, pwd);
        if (!result.isOK()) {
            return Result.error(result.error());
        }

        // Unsubscribe
        feeds.put(user, updateFeedSubscriber(user, userSub, UPDATE_FEED_DELETE));

        return Result.ok(null);
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        LOG.info("listSubs from user: " + user);

        // Check if user data is valid
        var result = propagator.requestUser(user, "");
        if (!result.isOK() && result.error().equals(Result.ErrorCode.NOT_FOUND)) {
            return Result.error(result.error());
        }

        Feed feed = feeds.get(user);
        if(feed == null) { feed = new Feed(); }

        return Result.ok(feed.getSubscribers());
    }

    private Feed updateFeedMessage(Message message, Feed oldFeed, String updateOp){
        Feed feed = oldFeed;
        if(feed == null) { feed = new Feed(); }

        if(updateOp.equals(UPDATE_FEED_ADD)) {
            // Add message
            message.setId(Globals.NUM_SEQ.get() * 256 + base);
            feed.addMessage(message);
        } else if (updateOp.equals(UPDATE_FEED_DELETE)){
            // Remove message
            feed.removeMessage(message);
        }

        return feed;
    }

    private Feed updateFeedSubscriber(String user, String userSub, String updateOp) {
        // Gets feeds
        Feed feed = feeds.get(user);
        if(feed == null) { feed = new Feed(); }

        if(updateOp.equals(UPDATE_FEED_ADD)) {
            // Add subscriber
            feed.addSubscriber(userSub);
        } else if (updateOp.equals(UPDATE_FEED_DELETE)){
            // Remove message
            feed.removeSubscriber(userSub);
        }

        // Merge
        return feed;
    }
}
