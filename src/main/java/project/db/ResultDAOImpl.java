package project.db;

import project.entity.ResultEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.Root;

import java.util.Collection;


public class ResultDAOImpl implements ResultDAO {
    private final EntityManager entityManager = JPAUtils.getFactory().createEntityManager();

    @Override
    public void addNewResult(ResultEntity result) {
        entityManager.getTransaction().begin();
        try {
            entityManager.persist(result);
            entityManager.flush();
            entityManager.refresh(result);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    @Override
    public void updateResult(Long result_id, ResultEntity result) {
        entityManager.getTransaction().begin();
        entityManager.merge(result);
        entityManager.getTransaction().commit();
    }

    @Override
    public ResultEntity getResultById(Long result_id) {
        return entityManager.getReference(ResultEntity.class, result_id);
    }

    @Override
    public Collection<ResultEntity> getAllResults() {
        var cm = entityManager.getCriteriaBuilder().createQuery(ResultEntity.class);
        Root<ResultEntity> root = cm.from(ResultEntity.class);
        return entityManager.createQuery(cm.select(root)).getResultList();
    }

    @Override
    public void deleteResult(ResultEntity result) {
        entityManager.getTransaction().begin();
        entityManager.remove(result);
        entityManager.getTransaction().commit();
    }

    /**
     * This method also handles transaction rollback in case of an error.
     */
    @Override
    public void clearResults() {
        entityManager.getTransaction().begin();
        try {
            Query query = entityManager.createQuery("DELETE FROM ResultEntity r");
            query.executeUpdate();
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e; // Or handle the exception as needed
        } finally {
            entityManager.clear();
        }
    }

    @Override
    public Collection<ResultEntity> getResultsByUser(Long userId) {
        // JPQL query to fetch results associated with the specified user
        Query query = entityManager.createQuery(
                "SELECT r FROM ResultEntity r JOIN r.users u WHERE u.id = :userId", ResultEntity.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }
}
