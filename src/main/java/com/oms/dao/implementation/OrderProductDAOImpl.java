package com.oms.dao.implementation;




import com.oms.model.OrderProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

public class OrderProductDAOImpl {

    private static final Logger logger = LoggerFactory.getLogger(OrderProductDAOImpl.class);
    private EntityManager entityManager;

    public OrderProductDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    
    public void addOrderProduct(OrderProduct orderProduct) {
        EntityTransaction transaction = null;
        try {
            transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.persist(orderProduct);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error adding order product", e);
        }
    }

    
    public List<OrderProduct> findCurrentCartForClient(int clientId, int orderId) {
        entityManager.clear();
        String jpql = "SELECT op FROM OrderProduct op WHERE op.order.client.id = :clientId AND op.order.id = :orderId";
        TypedQuery<OrderProduct> query = entityManager.createQuery(jpql, OrderProduct.class);
        query.setParameter("clientId", clientId);
        query.setParameter("orderId", orderId);
        return query.getResultList();
    }

    
    public void deleteByOrderId(int orderId) {
        entityManager.getTransaction().begin();
        Query query = entityManager.createQuery("DELETE FROM OrderProduct op WHERE op.order.id = :orderId");
        query.setParameter("orderId", orderId);
        query.executeUpdate();
        entityManager.getTransaction().commit();
    }

    
    public void updateOrderStatusToValid(int orderId) {
        entityManager.getTransaction().begin();
        entityManager.createQuery("UPDATE OrderProduct op SET op.isValid = true WHERE op.order.id = :orderId")
                .setParameter("orderId", orderId)
                .executeUpdate();
        entityManager.getTransaction().commit();
    }
}
