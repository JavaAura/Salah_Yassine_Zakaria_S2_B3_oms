package com.oms.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders") // Renamed to orders for better clarity
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull(message = "Order date cannot be null")
    private LocalDate orderDate;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @NotNull(message = "Client cannot be null")
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderProduct> orderProducts = new HashSet<>();

    // Constructors
    public Order() {
        this.orderDate = LocalDate.now();
    }

    public Order(LocalDate orderDate, OrderStatus status, Client client) {
        this.orderDate = orderDate;
        this.status = status;
        this.client = client;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Set<OrderProduct> getOrderProducts() {
        return orderProducts;
    }

    public void setOrderProducts(Set<OrderProduct> orderProducts) {
        this.orderProducts = orderProducts;
    }

    public void addOrderProduct(OrderProduct orderProduct) {
        orderProducts.add(orderProduct);
        orderProduct.setOrder(this);
    }

    public void removeOrderProduct(OrderProduct orderProduct) {
        orderProducts.remove(orderProduct);
        orderProduct.setOrder(null);
    }

    public double getTotalAmount() {
        return orderProducts.stream()
                .mapToDouble(orderProduct -> orderProduct.getQuantity() * orderProduct.getProduct().getPrice())
                .sum();
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderDate=" + orderDate +
                ", status=" + status +
                ", clientId=" + (client != null ? client.getId() : null) +
                '}';
    }
}
