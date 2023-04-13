package sd2223.trab1.server.java;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.java.Result.ErrorCode;
import sd2223.trab1.api.java.Users;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class JavaUsers implements Users {
	private final Map<String,User> users = new HashMap<>();

	private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

	@Override
	public Result<String> createUser(User user) {
		Log.info("createUser : " + user);
		
		// Check if user data is valid
		if(user.getName() == null || user.getPwd() == null || user.getDisplayName() == null || user.getDomain() == null) {
			Log.info("User object invalid.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}
		
		// Insert user, checking if name already exists
		if( users.putIfAbsent(user.getName(), user) != null ) {
			Log.info("User already exists.");
			return Result.error( ErrorCode.CONFLICT);
		}
		return Result.ok( user.getName() );
	}

	@Override
	public Result<User> getUser(String name, String pwd) {
		Log.info("getUser : user = " + name + "; pwd = " + pwd);
		
		// Check if user is valid
		if(name == null || pwd == null) {
			Log.info("Name or Password null.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}
		
		User user = users.get(name);			
		// Check if user exists 
		if( user == null ) {
			Log.info("User does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}
		
		//Check if the password is correct
		if( !user.getPwd().equals( pwd)) {
			Log.info("Password is incorrect.");
			return Result.error( ErrorCode.FORBIDDEN);
		}
		
		return Result.ok(user);
	}

	@Override
	public Result<User> updateUser(String name, String pwd, User user) {
		// Check if user is valid
		if(name == null || pwd == null || !name.equals(user.getName())) {
			throw new WebApplicationException( Response.Status.BAD_REQUEST );
		}

		// Check if there is a user
		User _user = users.get(name);
		if (_user == null){
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		// Check if the password is correct
		if (!_user.getPwd().equals(pwd)){
			throw new WebApplicationException(Response.Status.FORBIDDEN);
		}

		// update user info
		String newName = user.getDisplayName();
		String newDomain = user.getDomain();
		String newPassword = user.getPwd();

		_user.setDisplayName(newName == null ? _user.getDisplayName() : newName);
		_user.setDomain(newDomain == null ? _user.getDomain() : newDomain);
		_user.setPwd(newPassword == null ? _user.getPwd() : newPassword);
		users.put(name , _user);

		return Result.ok( _user );
	}

	@Override
	public Result<User> deleteUser(String name, String pwd) {
		// Check if user is valid
		if(name == null || pwd == null) {
			throw new WebApplicationException( Response.Status.BAD_REQUEST );
		}

		var user = users.get(name);

		// Check if user exists
		if( user == null ) {
			throw new WebApplicationException( Response.Status.NOT_FOUND );
		}

		//Check if the password is correct
		if( !user.getPwd().equals( pwd)) {
			throw new WebApplicationException( Response.Status.FORBIDDEN );
		}

		users.remove(user.getName());

		return Result.ok(user);
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		if(pattern == null) {
			throw new WebApplicationException( Response.Status.BAD_REQUEST );
		}

		return Result.ok(getPublicUsers(pattern));
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
			String patt = pattern.toLowerCase();
			if(pattern.equals("") || userName.contains(patt)){
				User newUser = new User(user.getName(), "", user.getDomain(), user.getDisplayName());
				result.add(newUser);
			}
		}

		return result;
	}
}
