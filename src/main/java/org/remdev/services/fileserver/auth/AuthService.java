package org.remdev.services.fileserver.auth;

import org.remdev.services.fileserver.models.ClientData;

/**
 * Very simple just for example and stub storage of tokens
 */
public interface AuthService {

    /**
     * Authenticates user by username and password
     * @param login user's login
     * @param password user's password
     * @return null if authentication failed and token if passed successfully;
     */
    ClientData authenticate(String login, String password);

    /**
     * Removes token from active tokens list user by username and password
     * @return null if token was not found and old client data if passed successfully;
     */
    ClientData removeToken(String token);

    /**
     * Provides the client ID by the token;
     * @param token the token to check
     * @return null if token is not valid or expired an non-null if client ID was found
     */
    ClientData getClient(String token);

    /**
     * Adds the client with the given name and password;
     * @return generated token for client if success and null otherwise
     */
    ClientData addClient(String login, String password);

    /**
     * Checks whether the client with the given login already exists
     * @return true if client with such login already exists and false otherwise
     */
    boolean hasClient(String login);
}
