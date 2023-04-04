package sd2223.trab1.clients;

import sd2223.trab1.api.User;
import sd2223.trab1.api.rest.UsersService;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 * Class RestUsersClient - defines a REST client in the user service
 *
 * @author Francisco Parrinha 	58360
 * @author Martin Magdalinchev 	58172
 */
public class RestUsersClient extends RestClient implements UsersService {

	/** Variables */
	private final WebTarget target;

	/** Constructor */
	RestUsersClient(URI serverURI) {
		super(serverURI);
		target = client.target(serverURI).path(UsersService.PATH);
	}

	@Override
	public String createUser(User user) {
		return super.reTry(() -> clt_createUser(user));
	}

	@Override
	public User getUser(String name, String pwd) {
		return super.reTry(() -> clt_getUser(name, pwd));
	}

	@Override
	public User updateUser(String name, String pwd, User user) {
		return super.reTry(() -> clt_updateUser(name, pwd, user));
	}

	@Override
	public User deleteUser(String name, String pwd) {
		return super.reTry(() -> clt_deleteUser(name, pwd));
	}

	@Override
	public List<User> searchUsers(String pattern) {
		return super.reTry( () -> clt_searchUser(pattern));
	}

	/** Generate http request to create a user */
	private String clt_createUser(User user) {

		Response r = target.request()
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(user, MediaType.APPLICATION_JSON));

		if (r.getStatus() == Status.OK.getStatusCode() && r.hasEntity()) {
			return r.readEntity(String.class);
		}

		System.out.println("Error, HTTP error status: " + r.getStatus());
		return null;
	}

	/** Generate http request to delete a user */
	private User clt_deleteUser(String name, String password) {

		Response r = target.path( name )
				.queryParam(UsersService.PWD, password).request()
				.accept(MediaType.APPLICATION_JSON)
				.delete();


		if (r.getStatus() == Status.OK.getStatusCode() && r.hasEntity()) {
			return r.readEntity(User.class);
		}

		System.out.println("Error, HTTP error status: " + r.getStatus());
		return null;
	}

	/** Generate http request to get a desired user info */
	private User clt_getUser(String name, String pwd) {

		Response r = target.path(name)
				.queryParam(UsersService.PWD, pwd).request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		if (r.getStatus() == Status.OK.getStatusCode() && r.hasEntity()) {
			return r.readEntity(User.class);
		}

		System.out.println("Error, HTTP error status: " + r.getStatus());
		return null;
	}

	/** Generate http request to update a desired user info */
	private User clt_updateUser(String name, String pwd, User user) {
		Response r = target.path(name)
				.queryParam(UsersService.PWD, pwd).request()
				.accept(MediaType.APPLICATION_JSON)
				.put(Entity.entity(user, MediaType.APPLICATION_JSON));

		if (r.getStatus() == Status.OK.getStatusCode() && r.hasEntity()) {
			return r.readEntity(User.class);
		}

		System.out.println("Error, HTTP error status: " + r.getStatus());
		return null;
	}

	/** Generate http request to search users by pattern */
	private List<User> clt_searchUser(String query) {
		Response r = target.queryParam(UsersService.QUERY, query).request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		if (r.getStatus() == Status.OK.getStatusCode() && r.hasEntity()) {
			return r.readEntity(new GenericType<>() {});
		}

		System.out.println("Error, HTTP error status: " + r.getStatus());
		return new LinkedList<>();	// Returns empty list to avoid "null checking" in other functions
	}
}
