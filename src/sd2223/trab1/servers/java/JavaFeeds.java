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
    public final class Remote {
        /**
         * Class Entry - An entity that assigns a key to a value
         * @param <K> key
         * @param <V> value
         */
        private class Entry<K, V> {
            private final K key;
            private final V value;

            public Entry(K key, V value) {
                this.key = key;
                this.value = value;
            }
        }

        /** Constants */
        private static final int MAX_CACHE_TIME = 2000;

        /** Variables */
        private final ConcurrentHashMap<String, Entry<Long, List<Message>>> messagesCache = new ConcurrentHashMap<>();

        /**
         * Creates GET request to user server from same domain to get the user
         * @param user user id@domain
         * @param pwd password
         * @return Result
         */
        public Result<User> requestUser(String user, String pwd, String domain) {
            var client = UsersClientFactory.get(domain);

            return client.getUser(user.split("@")[0], pwd);
        }

        /**
         * Finds a message in subscribers' messages
         *
         * @param mid message id
         * @param subs subscribers list
         */
        public Message getMessage(long mid, List<String> subs){
            Message message = null;

            for (String u : subs) {
                String[] user_domain = u.split("@");

                // Internal domain propagation
                if(user_domain[1].equals(domain)) {
                    message = feeds.get(u).getMessage(mid);

                } else { // Remote domain propagation
                    var client = FeedsClientFactory.get(user_domain[1]);
                    var result = client.getMessage(u, mid);

                    if(!result.isOK()) { continue; }
                    message = result.value();
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
        public List<Message> getMessages(long time, List<String> subs, String originalDomain){
            List<Message> messages = new LinkedList<>();

            for (String u : subs) {
                String[] user_domain = u.split("@");

                // Internal domain propagation
                if(user_domain[1].equals(domain)) {
                    var feed = feeds.get(u);
                    messages.addAll(new LinkedList<>(feed == null ? new LinkedList<>() : feed.getMessages(time)));
                    continue;
                }

                // Remote domain propagation
                var result = getFromRemote(u, originalDomain, user_domain[1], time);

                if(!result.isOK()) { continue; }
                messages.addAll(result.value());
            }

            return messages;
        }

        private Result<List<Message>> getFromRemote(String user, String originalDomain, String remoteDomain, long time){
            Result<List<Message>> result;
            var inCache = messagesCache.get(user);
            long timestamp = System.currentTimeMillis();

            if(inCache == null || inCache.key + MAX_CACHE_TIME < timestamp) {
                var client = FeedsClientFactory.get(remoteDomain);
                result = client.getMessagesFromRemote(user, originalDomain, time);

                if (result.isOK()) { messagesCache.put(user, new Entry<>(timestamp, result.value())); }   // Update cache...
            } else {
                result = Result.ok(inCache.value);
            }
            System.out.println("OK first");

            return result;
        }
    }

    /** Constants */
    private final ConcurrentHashMap<String, Feed> feeds = new ConcurrentHashMap<>();
    private static final String UPDATE_ADD = "ADD";
    private static final String UPDATE_DELETE = "DELETE";

    /** Variables */
    private final String domain;
    private final long base;
    private final Remote remote;

    /** Constructor */
    public JavaFeeds(String domain, long base) {
        this.domain = domain;
        this.base = base;
        remote = new Remote();
    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message message) {
        // Check if user data is valid
        if(user == null || pwd == null || !user.split("@")[1].equals(domain)) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        // Check if user exists in domain or if password is correct
        var result = remote.requestUser(user, pwd, domain);
        if (!result.isOK()) {
            return Result.error(result.error());
        }

        // Insert message in feed
        feeds.put(user, updateFeedMessage(message, feeds.get(user), UPDATE_ADD));

        return Result.ok(message.getId());
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        // Check if user data is valid
        if(user == null || pwd == null) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        // Check if user exists in domain
        var result = remote.requestUser(user, "", domain);
        if (!result.isOK() && result.error().equals(Result.ErrorCode.NOT_FOUND)) {
            return Result.error(result.error());
        }

        // Check if message exists
        var feed = feeds.get(user);
        Message message = feed == null ? null : feed.getMessage(mid);
        if(message == null) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        // Remove message from feed
        feeds.put(user, updateFeedMessage(message, feeds.get(user), UPDATE_DELETE));

        return Result.ok(null);
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        // Check user is remote
        String domain = user.split("@")[1];
        if(!domain.equals(this.domain)) {
            var client = FeedsClientFactory.get(domain);
            return client.getMessage(user, mid);
        }

        // User is local
        var result = remote.requestUser(user, "", domain);
        Feed feed = feeds.get(user);

        if ((!result.isOK() && result.error().equals(Result.ErrorCode.NOT_FOUND)) || feed == null) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        // Get message
        Message message = feed.getMessage(mid);
        if(message == null) { message = remote.getMessage(mid, feeds.get(user).getSubscribers()); }  // Propagate to get message...

        // Check if message exists
        if (message == null) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        return Result.ok(message);
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        // Check if user is remote
        String domain = user.split("@")[1];
        if(!domain.equals(this.domain)) {
            var client = FeedsClientFactory.get(domain);
            return client.getMessages(user, time);
        }

        // User is local. Check if user exists
        var result = remote.requestUser(user, "", domain);
        if (!result.isOK() && result.error().equals(Result.ErrorCode.NOT_FOUND)) {
            return Result.error(result.error());
        }

        Feed feed = feeds.get(user);
        if(feed == null) { return Result.ok(new LinkedList<>()); }  // If there are no messages, return immediately..

        // Concatenate messages
        var ownMessages = new LinkedList<>(feed.getMessages(time));
        var propagatedMessages = remote.getMessages(time, feed.getSubscribers(), user.split("@")[1]);

        ownMessages.addAll(propagatedMessages);

        System.out.println("OK FINAL");
        return Result.ok(ownMessages);
    }

    @Override
    public Result<List<Message>> getMessagesFromRemote(String user, String originalDomain, long time) {
        // Check if user exists in domain or if password is correct
        var result = remote.requestUser(user, "", domain);
        if (!result.isOK() && result.error().equals(Result.ErrorCode.NOT_FOUND)) {
            System.out.println(result.error());
            return Result.error(result.error());
        }

        // Check if not in a remote domain
        if(user.split("@")[1].equals(originalDomain)) {
            System.out.println(Result.ErrorCode.BAD_REQUEST);
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        Feed feed = feeds.get(user);
        if(feed == null) { return Result.ok(new LinkedList<>()); }  // If there are no messages, return immediately..

        // Gets own messages
        var messages = feed.getMessages(time);
        System.out.println("OKK");
        return Result.ok(messages);
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        // Check if user data is valid
        if(user == null || pwd == null) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        // Check if user exists in domain or if password is correct
        var result = remote.requestUser(user, pwd, domain);
        if (!result.isOK()) {
            return Result.error(result.error());
        }

        // Add subscription
        feeds.put(user, updateFeedSubscriber(user, userSub, UPDATE_ADD));

        return Result.ok(null);
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        // Check if user data is valid
        if(user == null || pwd == null) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        // Check if user exists in domain or if password is correct
        var result = remote.requestUser(user, pwd, domain);
        if (!result.isOK()) {
            return Result.error(result.error());
        }

        // Unsubscribe
        feeds.put(user, updateFeedSubscriber(user, userSub, UPDATE_DELETE));

        return Result.ok(null);
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        // Check if user data is valid
        var result = remote.requestUser(user, "", domain);
        if (!result.isOK() && result.error().equals(Result.ErrorCode.NOT_FOUND)) {
            return Result.error(result.error());
        }

        Feed feed = feeds.get(user);
        if(feed == null) { feed = new Feed(); }

        return Result.ok(feed.getSubscribers());
    }

    @Override
    public Result<Void> deleteFeed(String user) {
        var result = remote.requestUser(user, "", domain);
        if (!result.isOK() && result.error().equals(Result.ErrorCode.NOT_FOUND)) {
            return Result.error(result.error());
        }

        feeds.remove(user);
        return Result.ok(null);
    }

    private Feed updateFeedMessage(Message message, Feed oldFeed, String updateOp){
        Feed feed = oldFeed;
        if(feed == null) { feed = new Feed(); }

        if(updateOp.equals(UPDATE_ADD)) {
            message.setId(Globals.NUM_SEQ.get() * 256 + base);
            feed.addMessage(message);
        } else if (updateOp.equals(UPDATE_DELETE)){
            feed.removeMessage(message);
        }

        return feed;
    }

    private Feed updateFeedSubscriber(String user, String userSub, String updateOp) {
        Feed feed = feeds.get(user);
        if(feed == null) { feed = new Feed(); }

        if(updateOp.equals(UPDATE_ADD)) {
            feed.addSubscriber(userSub);
        } else if (updateOp.equals(UPDATE_DELETE)){
            feed.removeSubscriber(userSub);
        }

        // Merge
        return feed;
    }
}
