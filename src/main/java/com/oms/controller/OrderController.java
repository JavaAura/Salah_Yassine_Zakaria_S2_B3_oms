package com.oms.controller;

import com.oms.dao.implementation.ProductDAOImpl;
import com.oms.model.Client;
import com.oms.model.Order;
import com.oms.model.OrderStatus;
import com.oms.model.Product;
import com.oms.service.OrderProductService;
import com.oms.service.OrderService;
import com.oms.service.ProductService;
import com.oms.util.ThymeleafUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class OrderController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private OrderService orderService;
    private ThymeleafUtil thymeleafUtil;
    private OrderProductService orderProductService ;
    private ProductService productService ;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService(); // Instantiate OrderService directly
        thymeleafUtil = new ThymeleafUtil(getServletContext());
        productService = new ProductService(new ProductDAOImpl());
        orderProductService = new OrderProductService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "list":
                listOrders(request, response);
                break;
            case "search":
                searchOrders(request, response);
                break;
            case "delete":
                deleteOrder(request, response);
                break;
            default:
                response.sendRedirect("orders?action=list");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("insert".equals(action)) {
            insertOrder(request, response);
        } else if ("update".equals(action)) {
            updateOrder(request, response);
        }
    }

    private void listOrders(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Order> orders = orderService.getAllOrdersOrderedByLatest();
        request.setAttribute("orders", orders);
        thymeleafUtil.returnView(request, response, "orders/order-list", null);
    }

    private void searchOrders(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String status = request.getParameter("search");
        List<Order> orders = orderService.findOrderBySearch(null, status);
        if (orders.isEmpty()) {
            request.setAttribute("error", "Order not found");
        }

        request.setAttribute("orders", orders);
        thymeleafUtil.returnView(request, response, "orders/order-list", null);
    }

  private void insertOrder(HttpServletRequest request, HttpServletResponse response) 
        throws IOException {

    HttpSession session = request.getSession();
    Client client = (Client) session.getAttribute("user");
    System.out.println(client.toString());

    int productId = Integer.parseInt(request.getParameter("productId"));
    Product product = productService.findProduct(productId);

    // Check if there is an existing order for the client
    Order order = orderService.findLastOrderByClientId(client.getId());

    if (order != null) {
        // If an order exists, add product to the existing order
        int quantity = Integer.parseInt(request.getParameter("quantity"));
        orderProductService.addProductToOrder(product, quantity, order);

    } else {
        // If no order exists, create a new one
        Order newOrder = new Order();
        newOrder.setClient(client);
        newOrder.setOrderDate(LocalDate.now());
        newOrder.setStatus(OrderStatus.PENDING);

        // Save the new order and retrieve the saved order with the generated ID
        newOrder = orderService.saveOrder(newOrder);

        // After the order is saved, proceed to add the product to the new order
        int quantity = Integer.parseInt(request.getParameter("quantity"));
        orderProductService.addProductToOrder(product, quantity, newOrder);

        // Set a success message in the session
        request.getSession().setAttribute("message", "Order added successfully!");
    }

    // Redirect to the product list page
    response.sendRedirect("products?action=list");
}

    private void updateOrder(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        String status = request.getParameter("status");

        orderService.updateOrderStatus(id, status);
        
        request.getSession().setAttribute("message", "Order updated successfully!");
        response.sendRedirect("orders?action=list");
    }

    private void deleteOrder(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        orderService.cancelOrder(id);
        request.getSession().setAttribute("message", "Order deleted successfully!");
        response.sendRedirect("orders?action=list");
    }
}
