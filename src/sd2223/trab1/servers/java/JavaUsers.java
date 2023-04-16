package sd2223.trab1.servers.java;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.java.Result.ErrorCode;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.clients.FeedsClientFactory;

/**
 * Class JavaUsers - Handles users resource
 *
 * @author Francisco Parrinha   58360
 * @author Martin Magdalinchev  58172
 */
public class JavaUsers implements Users {

	/** Constants */
	private static final Logger LOG = Logger.getLogger(JavaUsers.class.getName());

	/** Variables */
	private final Map<String,User> users = new HashMap<>();

	@Override
	public Result<String> createUser(User user) {
		LOG.info("createUser : " + user);

		// Check if user data is valid
		if(user.getName() == null || user.getPwd() == null || user.getDisplayName() == null || user.getDomain() == null) {
			LOG.info("User object invalid.");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		// Insert user, checking if name already exists
		if( users.putIfAbsent(user.getName(), user) != null ) {

			LOG.info("User already exists.");
			return Result.error(ErrorCode.CONFLICT);
		}

		return Result.ok( user.getName() + "@" + user.getDomain() );
	}

	@Override
	public Result<User> getUser(String name, String pwd) {
		LOG.info("getUser : user = " + name + "; pwd = " + pwd);

		// Check if user is valid
		if(name == null || pwd == null) {
			LOG.info("Name or Password null.");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		User user = users.get(name);
		// Check if user exists
		if( user == null ) {
			LOG.info("User does not exist.");
			return Result.error(ErrorCode.NOT_FOUND);
		}

		//Check if the password is correct
		if( !user.getPwd().equals( pwd)) {
			LOG.info("Password is incorrect.");
			return Result.error(ErrorCode.FORBIDDEN);
		}

		return Result.ok(user);
	}

	@Override
	public Result<User> updateUser(String name, String pwd, User user) {
		LOG.info("updateUser : user = " + name + "; pwd = " + pwd + "; new password = " + "; new name = " +
				user.getDisplayName() + user.getPwd() + "; new domain = " + user.getDomain());

		// Check if user is valid
		if(name == null || pwd == null || !name.equals(user.getName())) {
			LOG.info("Name or Password null or tried to change id.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}

		// Check if there is a user
		User oldUser = users.get(name);
		if (oldUser == null){
			LOG.info("User does not exist.");
			return Result.error(ErrorCode.NOT_FOUND);
		}

		// Check if the password is correct
		if (!oldUser.getPwd().equals(pwd)){
			LOG.info("Password is incorrect.");
			return Result.error(ErrorCode.FORBIDDEN);
		}

		return Result.ok(aux_updateUser(oldUser, user, name));
	}

	@Override
	public Result<User> deleteUser(String name, String pwd) {
		LOG.info("deleteUser : user = " + name + "; pwd = " + pwd);

		// Check if user is valid
		if(name == null || pwd == null) {
			LOG.info("Name or Password null.");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		var user = users.get(name);

		// Check if user exists
		if( user == null ) {
			LOG.info("User does not exist.");
			return Result.error(ErrorCode.NOT_FOUND);
		}

		//Check if the password is correct
		if( !user.getPwd().equals( pwd)) {
			LOG.info("Password is incorrect.");
			return Result.error(ErrorCode.FORBIDDEN);
		}

		users.remove(user.getName());

		return Result.ok(user);
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		LOG.info("searchUsers : pattern = " + pattern + ";");

		if(pattern == null) {
			LOG.info("Pattern null.");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		return Result.ok(getPublicUsers(pattern));
	}

	/** Auxiliary methods */

	private User aux_updateUser(User oldUser, User newUser, String userID){
		// Temp variables
		String name = newUser.getDisplayName();
		String domain = newUser.getDomain();
		String pwd = newUser.getPwd();

		// Reset values
		oldUser.setDisplayName(name == null ? oldUser.getDisplayName() : name);
		oldUser.setDomain(domain == null ? oldUser.getDomain() : domain);
		oldUser.setPwd(pwd == null ? oldUser.getPwd() : pwd);

		// Merge
		users.put(userID , oldUser);

		return oldUser;
	}

	/**
	 * Gets all public users
	 *
	 * @return list of public users
	 */
	private List<User> getPublicUsers(String pattern){
		List<User> result = new LinkedList<>();

		for (User user : users.values()){
			String userName = user.getName().toLowerCase();
			String patternLowerCase = pattern.toLowerCase();
			if(pattern.equals("") || userName.contains(patternLowerCase)){
				result.add(user);
			}
		}

		return result;
	}
}
