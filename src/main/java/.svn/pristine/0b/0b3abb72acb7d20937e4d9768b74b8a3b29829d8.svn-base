package project.db;

import project.entity.WebUser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class UserDAOImpl implements UserDAO {
    private final EntityManager entityManager = JPAUtils.getFactory().createEntityManager();

    @Override
    public void addUser(WebUser user) {
        entityManager.getTransaction().begin();
        entityManager.persist(user);
        entityManager.getTransaction().commit();
    }

    @Override
    public WebUser getUserByUsername(String username) {
        try {
            TypedQuery<WebUser> query = entityManager.createQuery(
                    "SELECT u FROM WebUser u WHERE u.username = :username", WebUser.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public WebUser getUserById(Long userId) {
        return entityManager.find(WebUser.class, userId);
    }

    @Override
    public void updateUser(WebUser user) {
        entityManager.getTransaction().begin();
        entityManager.merge(user);
        entityManager.getTransaction().commit();
    }

    @Override
    public void deleteUser(WebUser user) {
        entityManager.getTransaction().begin();
        WebUser managedUser = entityManager.contains(user) ? user : entityManager.merge(user);
        entityManager.remove(managedUser);
        entityManager.getTransaction().commit();
    }
}
