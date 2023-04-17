package sd2223.trab1.api;

import java.util.*;

/**
 * Class Feed - Structure of a feed in the system
 *
 * @author Francisco Parrinha   58360
 * @author Martin Magdalinchev  58172
 */
public class Feed {

    /** Variables */
    private final List<Message> messages;
    private final List<String> subscribers;

    /** Constructor */
    public Feed() {
        this.messages =  new LinkedList<>();
        this.subscribers = new LinkedList<>();
    }

    /**
     * Subscribe to a new user
     * @param user user to subscribe id@domain
     * @return subscribers list
     */
    public List<String> addSubscriber(String user){
        if(subscribers.isEmpty() || !subscribers.contains(user)) {
            subscribers.add(user);
        }

        return  subscribers;
    }

    /**
     * Returns feed's messages
     * @return messages
     */
    public Message getMessage(long mid) {
        for (Message message : messages) {
            if(message.getId() == mid){
                return message;
            }
        }

        return null;
    }

    /**
     * Adds a new message to the feed
     * @param message message to add
     * @return messages list
     */
    public List<Message> addMessage(Message message) {
        if(messages.isEmpty() || !messages.contains(message)) {
            messages.add(message);
        }

        return messages;
    }

    /**
     * Removes message from the feed
     * @param message message to remove
     * @return messages list
     */
    public List<Message> removeMessage(Message message) {
        if(!messages.isEmpty()) {
            messages.remove(message);
        }

        return messages;
    }

    /**
     * Removes subscriber from the feed
     * @param user subscriber to remove
     * @return subscribers list
     */
    public List<String> removeSubscriber(String user) {
        if(!subscribers.isEmpty()) {
            subscribers.remove(user);
        }

        return subscribers;
    }

    /**
     * Returns feed's messages newer than the given time
     * @param time oldest time
     * @return messages
     */
    public List<Message> getMessages(long time) {
        return time == 0 ? messages : messages.stream().filter(m -> m.getCreationTime() > time).toList();
    }

    /**
     * Returns feed's subscribers
     * @return subscribers
     */
    public List<String> getSubscribers() {
        return subscribers;
    }
}
