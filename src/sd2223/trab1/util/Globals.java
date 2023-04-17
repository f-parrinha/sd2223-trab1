package sd2223.trab1.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Record Globals - Defines global constants
 *
 * @author Francisco Parrinha   58360
 * @author Martin Magdlinchev   58172
 */
public record Globals() {
    public static final String USERS_SERVICE_NAME = "users";
    public static final String FEEDS_SERVICE_NAME = "feeds";
    public static final AtomicLong NUM_SEQ = new AtomicLong(0);
}