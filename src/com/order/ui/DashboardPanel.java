package com.order.ui;

import com.order.config.DatabaseConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardPanel extends JPanel {

    private JLabel custCountLabel;
    private JLabel prodCountLabel;
    private JLabel orderCountLabel;
    private JLabel revenueLabel;

    private JTable recentOrdersTable;
    private DefaultTableModel recentOrdersModel;

    private JTable lowStockTable;
    private DefaultTableModel lowStockModel;

    public DashboardPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIStyle.COLOR_BG);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initUI();
        refreshStats();
    }

    private void initUI() {
        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel titleLbl = new JLabel("System Overview & Analytics");
        titleLbl.setFont(UIStyle.FONT_TITLE);
        titleLbl.setForeground(UIStyle.COLOR_TEXT_MAIN);

        JButton refreshBtn = new JButton("Refresh Dashboard");
        UIStyle.styleButton(refreshBtn, UIStyle.COLOR_SECONDARY);
        refreshBtn.addActionListener(e -> refreshStats());

        topPanel.add(titleLbl, BorderLayout.WEST);
        topPanel.add(refreshBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Content
        JPanel center = new JPanel(new BorderLayout(15, 15));
        center.setOpaque(false);

        // KPI Cards Grid (4 Cards)
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.setPreferredSize(new Dimension(0, 100));

        custCountLabel = new JLabel("0");
        prodCountLabel = new JLabel("0");
        orderCountLabel = new JLabel("0");
        revenueLabel = new JLabel("₹0.00");

        kpiPanel.add(createKpiCard("👥 Customers", custCountLabel, UIStyle.COLOR_PRIMARY));
        kpiPanel.add(createKpiCard("📦 Products", prodCountLabel, UIStyle.COLOR_SUCCESS));
        kpiPanel.add(createKpiCard("🛒 Orders", orderCountLabel, UIStyle.COLOR_PURPLE));
        kpiPanel.add(createKpiCard("💰 Total Revenue", revenueLabel, UIStyle.COLOR_WARNING));

        center.add(kpiPanel, BorderLayout.NORTH);

        // Tables Split: Recent Orders (Left) and Low Stock Alerts (Right)
        JPanel tablesGrid = new JPanel(new GridLayout(1, 2, 15, 0));
        tablesGrid.setOpaque(false);

        // Left Table: Recent Orders
        JPanel recentCard = UIStyle.createCardPanel();
        recentCard.setLayout(new BorderLayout(8, 8));
        JLabel recentTitle = UIStyle.createHeaderLabel("Recent Orders");
        recentCard.add(recentTitle, BorderLayout.NORTH);

        String[] recentCols = {"ID", "Date", "Customer", "Status", "Amount (₹)"};
        recentOrdersModel = new DefaultTableModel(recentCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        recentOrdersTable = new JTable(recentOrdersModel);
        UIStyle.styleTable(recentOrdersTable);
        recentCard.add(new JScrollPane(recentOrdersTable), BorderLayout.CENTER);

        // Right Table: Stock Alert
        JPanel stockCard = UIStyle.createCardPanel();
        stockCard.setLayout(new BorderLayout(8, 8));
        JLabel stockTitle = UIStyle.createHeaderLabel("⚠️ Low Stock Monitor (<= 30 units)");
        stockCard.add(stockTitle, BorderLayout.NORTH);

        String[] stockCols = {"ID", "Product Name", "Stock", "Unit Price (₹)"};
        lowStockModel = new DefaultTableModel(stockCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        lowStockTable = new JTable(lowStockModel);
        UIStyle.styleTable(lowStockTable);
        stockCard.add(new JScrollPane(lowStockTable), BorderLayout.CENTER);

        tablesGrid.add(recentCard);
        tablesGrid.add(stockCard);

        center.add(tablesGrid, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = UIStyle.createCardPanel();
        card.setLayout(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, accentColor),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UIStyle.FONT_LABEL);
        titleLbl.setForeground(UIStyle.COLOR_TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(UIStyle.COLOR_TEXT_MAIN);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void refreshStats() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Customers
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM CUSTOMER");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) custCountLabel.setText(String.valueOf(rs.getInt(1)));
            }

            // Products
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM PRODUCT");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) prodCountLabel.setText(String.valueOf(rs.getInt(1)));
            }

            // Orders
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM ORDERS");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) orderCountLabel.setText(String.valueOf(rs.getInt(1)));
            }

            // Revenue (excluding cancelled)
            try (PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(SUM(total_amount), 0) FROM ORDERS WHERE status != 'Cancelled'");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal rev = rs.getBigDecimal(1);
                    revenueLabel.setText("₹" + (rev != null ? rev.toPlainString() : "0.00"));
                }
            }

            // Recent 6 Orders
            recentOrdersModel.setRowCount(0);
            String recentSql = "SELECT o.order_id, o.order_date, c.name, o.status, o.total_amount " +
                               "FROM ORDERS o JOIN CUSTOMER c ON o.customer_id = c.customer_id " +
                               "ORDER BY o.order_id DESC LIMIT 6";
            try (PreparedStatement ps = conn.prepareStatement(recentSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    recentOrdersModel.addRow(new Object[]{
                            rs.getInt(1),
                            rs.getDate(2),
                            rs.getString(3),
                            rs.getString(4),
                            rs.getBigDecimal(5).toPlainString()
                    });
                }
            }

            // Low Stock Alert (<= 30 units)
            lowStockModel.setRowCount(0);
            String stockSql = "SELECT product_id, name, stock_qty, price FROM PRODUCT WHERE stock_qty <= 30 ORDER BY stock_qty ASC";
            try (PreparedStatement ps = conn.prepareStatement(stockSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lowStockModel.addRow(new Object[]{
                            rs.getInt(1),
                            rs.getString(2),
                            rs.getInt(3),
                            rs.getBigDecimal(4).toPlainString()
                    });
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not refresh dashboard: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
