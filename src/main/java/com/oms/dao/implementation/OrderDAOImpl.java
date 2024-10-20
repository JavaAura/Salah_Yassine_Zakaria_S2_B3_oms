package com.oms.dao.implementation;

import com.oms.model.Order;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class OrderDAOImpl {
    private EntityManager entityManager;
    private static final Logger logger = LoggerFactory.getLogger(OrderDAOImpl.class);
    public OrderDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    
    public List<Order> getOrdersByClientId(int clientId) {
        entityManager.clear();
        String jpql = "SELECT o FROM Order o WHERE o.client.id = :clientId";
        TypedQuery<Order> query = entityManager.createQuery(jpql, Order.class);
        query.setParameter("clientId", clientId);
        return query.getResultList();
    }

    
    public Order findById(int id) {
        entityManager.clear();
        return entityManager.find(Order.class, id);
    }

    
    @Transactional
    public void update(Order order) {
        if (order != null) {
            entityManager.getTransaction().begin();
            entityManager.merge(order);
            entityManager.getTransaction().commit();
        }
    }

    
    public List<Order> getAllOrdersOrderedByLatest() {
        entityManager.clear();
        String jpql = "SELECT o FROM Order o ORDER BY o.orderDate DESC";
        TypedQuery<Order> query = entityManager.createQuery(jpql, Order.class);
        return query.getResultList();
    }

    
    public Order findLastOrderByClientId(int clientId) {
        TypedQuery<Order> query = entityManager.createQuery(
                "SELECT o FROM Order o WHERE o.client.id = :clientId ORDER BY o.id DESC", Order.class);
        query.setParameter("clientId", clientId);
        query.setMaxResults(1);
        return query.getResultStream().findFirst().orElse(null);
    }

    
    public List<Order> findByIdOrStatus(Integer id, String status) {
        String jpql = "SELECT o FROM Order o WHERE o.id = :id OR o.status = :status";
        TypedQuery<Order> query = entityManager.createQuery(jpql, Order.class);
        query.setParameter("id", id);
        query.setParameter("status", status);
        return query.getResultList();
    }

    
    @Transactional
    public Order saveOrder(Order order) {
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(order);
            entityManager.getTransaction().commit();
            // The order object now contains the generated ID
            return order;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error saving order", e);
            return null; // or throw an appropriate exception
        }
    }

}
