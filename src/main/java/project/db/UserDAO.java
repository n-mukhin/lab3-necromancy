package project.db;

import project.entity.WebUser;

public interface UserDAO {
    /**
     * Adds a new user to the database.
     *
     * @param user The WebUser object to be added.
     */
    void addUser(WebUser user);

    /**
     * Retrieves a user by their username.
     *
     * @param username The username of the user.
     * @return The WebUser object if found, otherwise null.
     */
    WebUser getUserByUsername(String username);

    /**
     * Retrieves a user by their ID.
     *
     * @param userId The ID of the user.
     * @return The WebUser object if found, otherwise null.
     */
    WebUser getUserById(Long userId);

    /**
     * Updates an existing user in the database.
     *
     * @param user The WebUser object with updated information.
     */
    void updateUser(WebUser user);

    /**
     * Deletes a user from the database.
     *
     * @param user The WebUser object to be deleted.
     */
    void deleteUser(WebUser user);
}
