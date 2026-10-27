package com.order.ui;

import com.order.config.DatabaseConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {

    private JTabbedPane tabbedPane;

    private DashboardPanel dashboardPanel;
    private CustomerPanel customerPanel;
    private CategoryPanel categoryPanel;
    private ProductPanel productPanel;
    private OrderPanel orderPanel;
    private OrderItemPanel orderItemPanel;
    private PaymentPanel paymentPanel;

    public MainFrame() {
        super("Online Order System - Java Swing & MySQL Microproject");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(UIStyle.COLOR_BG);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(UIStyle.COLOR_HEADER_BG);
        headerPanel.setBorder(new EmptyBorder(14, 20, 14, 20));

        // Left Branding
        JPanel brandPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        brandPanel.setOpaque(false);

        JLabel appTitle = new JLabel("🛍️ Online Order System");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        appTitle.setForeground(Color.WHITE);

        JLabel collegeLbl = new JLabel("Shah & Anchor Kutchchi Engineering College · SYIT1 Microproject");
        collegeLbl.setFont(UIStyle.FONT_SUBTITLE);
        collegeLbl.setForeground(new Color(148, 163, 184));

        brandPanel.add(appTitle);
        brandPanel.add(collegeLbl);
        headerPanel.add(brandPanel, BorderLayout.WEST);

        // Right Info Badge
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        infoPanel.setOpaque(false);

        JLabel authorsLbl = new JLabel("Team: Sonali Makwana (66) & Shravani Nakhwa (69)", SwingConstants.RIGHT);
        authorsLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        authorsLbl.setForeground(new Color(226, 232, 240));

        boolean dbOk = DatabaseConfig.testConnection();
        JLabel statusLbl = new JLabel(dbOk ? "🟢 MySQL Connected (online_order_system)" : "🔴 Database Disconnected", SwingConstants.RIGHT);
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLbl.setForeground(dbOk ? new Color(52, 211, 153) : new Color(248, 113, 113));

        infoPanel.add(authorsLbl);
        infoPanel.add(statusLbl);
        headerPanel.add(infoPanel, BorderLayout.EAST);

        getContentPane().add(headerPanel, BorderLayout.NORTH);

        // Tabbed Pane
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setBorder(null);

        // Panels
        dashboardPanel = new DashboardPanel();
        customerPanel = new CustomerPanel();
        categoryPanel = new CategoryPanel();
        productPanel = new ProductPanel();
        orderPanel = new OrderPanel();
        orderItemPanel = new OrderItemPanel();
        paymentPanel = new PaymentPanel();

        tabbedPane.addTab("  📊 Dashboard  ", dashboardPanel);
        tabbedPane.addTab("  👥 Customers  ", customerPanel);
        tabbedPane.addTab("  🏷️ Categories  ", categoryPanel);
        tabbedPane.addTab("  📦 Products  ", productPanel);
        tabbedPane.addTab("  🛒 Orders  ", orderPanel);
        tabbedPane.addTab("  📋 Order Items  ", orderItemPanel);
        tabbedPane.addTab("  💳 Payments  ", paymentPanel);

        // Auto-refresh data on tab switch
        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            switch (idx) {
                case 0: dashboardPanel.refreshStats(); break;
                case 1: customerPanel.loadCustomers(); break;
                case 2: categoryPanel.loadCategories(); break;
                case 3:
                    productPanel.loadCategories();
                    productPanel.loadProducts();
                    break;
                case 4: orderPanel.loadOrders(); break;
                case 5: orderItemPanel.loadItems(); break;
                case 6: paymentPanel.loadPayments(); break;
            }
        });

        getContentPane().add(tabbedPane, BorderLayout.CENTER);
    }
}
