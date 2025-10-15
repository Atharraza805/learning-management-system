package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import database.DatabaseConnection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class StudentDashboard extends JFrame {
    private int studentId;
    private String studentName;
    private JTable coursesTable;
    private JTable assignmentsTable;
    private DefaultTableModel courseTableModel;
    private DefaultTableModel assignmentTableModel;
    private JLabel coursesCountLabel;  // Add this
    private JLabel assignmentsCountLabel;  // Add this

    // New tables for materials and messages
    private JTable materialsTable;
    private DefaultTableModel materialsModel;
    private JTable messagesTable;
    private DefaultTableModel messagesModel;

    // Modern color palette - ADJUSTED FOR BETTER VISIBILITY
    private static final Color PRIMARY_COLOR = new Color(30, 144, 255);      // Dodger Blue
    private static final Color SECONDARY_COLOR = new Color(70, 130, 180);    // Steel Blue
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);       // Green
    private static final Color WARNING_COLOR = new Color(255, 193, 7);       // Bright Yellow
    private static final Color DANGER_COLOR = new Color(220, 53, 69);        // Red
    private static final Color DARK_BG = new Color(52, 58, 64);              // Dark Gray
    private static final Color LIGHT_BG = new Color(248, 249, 250);          // Very Light Gray
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);

    public StudentDashboard(int userId, String name) {
        this.studentId = userId;
        this.studentName = name;

        setTitle("Student Dashboard - " + name);
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initComponents();
        loadEnrolledCourses();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(LIGHT_BG);

        // Modern Header with gradient effect
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(getWidth(), 100));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Left side - Welcome message with icon
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        welcomePanel.setOpaque(false);

        JLabel iconLabel = new JLabel("📚");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome back,");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);

        JLabel nameLabel = new JLabel(studentName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 28));
        nameLabel.setForeground(Color.WHITE);

        textPanel.add(welcomeLabel);
        textPanel.add(nameLabel);

        welcomePanel.add(iconLabel);
        welcomePanel.add(textPanel);

        // Right side - Logout button
        JButton logoutButton = createStyledButton("Logout", DANGER_COLOR);
        logoutButton.addActionListener(e -> logout());

        headerPanel.add(welcomePanel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);

        // Tabbed Pane with custom styling
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(LIGHT_BG);
        tabbedPane.setForeground(TEXT_PRIMARY);

        // My Courses Tab
        JPanel coursesPanel = createCoursesPanel();

        // Assignments Tab
        JPanel assignmentsPanel = createAssignmentsPanel();

        // New tabs (created but not yet added)
        JPanel materialsPanel = createMaterialsPanel();
        JPanel messagesPanel = createMessagesPanel();

        tabbedPane.addTab("  📚 My Courses  ", coursesPanel);
        tabbedPane.addTab("  📝 Assignments  ", assignmentsPanel);

        // Append new tabs at the end
        tabbedPane.addTab("  📘 Study Materials  ", materialsPanel);
        tabbedPane.addTab("  📩 Messages  ", messagesPanel);

        // Add listener to load appropriate data when a tab is selected
        tabbedPane.addChangeListener(e -> {
            Component sel = tabbedPane.getSelectedComponent();
            if (sel == assignmentsPanel) {
                viewAssignments();
            } else if (sel == materialsPanel) {
                loadMaterials();
            } else if (sel == messagesPanel) {
                loadMessages();
            }
        });

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(LIGHT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create table model FIRST
        String[] courseColumns = {"Course Code", "Course Name", "Teacher", "Credits", "Grade"};
        courseTableModel = new DefaultTableModel(courseColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Stats cards at top - ONLY Total Courses
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statsPanel.setBackground(LIGHT_BG);
        statsPanel.setPreferredSize(new Dimension(0, 120));

        // Create stat card with dynamic count
        JPanel totalCoursesCard = new JPanel(new BorderLayout(15, 10));
        totalCoursesCard.setBackground(CARD_BG);
        totalCoursesCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SUCCESS_COLOR, 2, true),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        totalCoursesCard.setPreferredSize(new Dimension(300, 100));

        JLabel iconLabel = new JLabel("📚");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Total Enrolled Courses");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.BLACK);

        coursesCountLabel = new JLabel("0");
        coursesCountLabel.setFont(new Font("Arial", Font.BOLD, 40));
        coursesCountLabel.setForeground(SUCCESS_COLOR);

        textPanel.add(titleLabel);
        textPanel.add(coursesCountLabel);

        totalCoursesCard.add(iconLabel, BorderLayout.WEST);
        totalCoursesCard.add(textPanel, BorderLayout.CENTER);

        statsPanel.add(totalCoursesCard);

        // Table panel with card style
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(CARD_BG);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel tableTitle = new JLabel("Enrolled Courses");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 22));
        tableTitle.setForeground(Color.BLACK);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        coursesTable = new JTable(courseTableModel);
        styleTable(coursesTable);
        JScrollPane courseScrollPane = new JScrollPane(coursesTable);
        courseScrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        tableCard.add(tableTitle, BorderLayout.NORTH);
        tableCard.add(courseScrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        buttonPanel.setBackground(CARD_BG);

        JButton enrollButton = createStyledButton("➕ Enroll in Course", SUCCESS_COLOR);
        enrollButton.addActionListener(e -> enrollInCourse());

        buttonPanel.add(enrollButton);
        tableCard.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(statsPanel, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAssignmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(LIGHT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create table model FIRST
        String[] assignmentColumns = {"Assignment", "Course", "Due Date", "Max Marks", "Obtained", "Status"};
        assignmentTableModel = new DefaultTableModel(assignmentColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Stats cards - ONLY Total Assignments
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statsPanel.setBackground(LIGHT_BG);
        statsPanel.setPreferredSize(new Dimension(0, 120));

        // Create stat card with dynamic count
        JPanel totalAssignmentsCard = new JPanel(new BorderLayout(15, 10));
        totalAssignmentsCard.setBackground(CARD_BG);
        totalAssignmentsCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2, true),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        totalAssignmentsCard.setPreferredSize(new Dimension(300, 100));

        JLabel iconLabel = new JLabel("📝");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Total Assignments");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.BLACK);

        assignmentsCountLabel = new JLabel("0");
        assignmentsCountLabel.setFont(new Font("Arial", Font.BOLD, 40));
        assignmentsCountLabel.setForeground(PRIMARY_COLOR);

        textPanel.add(titleLabel);
        textPanel.add(assignmentsCountLabel);

        totalAssignmentsCard.add(iconLabel, BorderLayout.WEST);
        totalAssignmentsCard.add(textPanel, BorderLayout.CENTER);

        statsPanel.add(totalAssignmentsCard);

        // Table card
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(CARD_BG);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel tableTitle = new JLabel("My Assignments");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 22));
        tableTitle.setForeground(Color.BLACK);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        assignmentsTable = new JTable(assignmentTableModel);
        styleTable(assignmentsTable);
        JScrollPane assignmentScrollPane = new JScrollPane(assignmentsTable);
        assignmentScrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        tableCard.add(tableTitle, BorderLayout.NORTH);
        tableCard.add(assignmentScrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        buttonPanel.setBackground(CARD_BG);

        JButton submitButton = createStyledButton("📤 Submit Assignment", PRIMARY_COLOR);
        submitButton.addActionListener(e -> submitAssignment());

        JButton refreshButton = createStyledButton("🔄 Refresh", SECONDARY_COLOR);
        refreshButton.addActionListener(e -> viewAssignments());

        buttonPanel.add(submitButton);
        buttonPanel.add(refreshButton);
        tableCard.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(statsPanel, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);

        return panel;
    }

    // New: Study Materials tab for students
    private JPanel createMaterialsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(LIGHT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] cols = {"Title", "Course", "Description", "Uploaded Date", "File Path"};
        materialsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        materialsTable = new JTable(materialsModel);
        styleTable(materialsTable);
        JScrollPane sp = new JScrollPane(materialsTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220,220,220),1));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(CARD_BG);
        JButton btnRefresh = createStyledButton("Refresh", SECONDARY_COLOR);
        btnRefresh.addActionListener(e -> loadMaterials());
        JButton btnDownload = createStyledButton("⬇️ Download Selected", PRIMARY_COLOR);
        btnDownload.addActionListener(e -> downloadSelectedMaterial());
        bottom.add(btnRefresh);
        bottom.add(btnDownload);

        panel.add(sp, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    // New: Messages tab for students
    private JPanel createMessagesPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(LIGHT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] cols = {"Subject", "Course", "Message", "Sent Date"};
        messagesModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        messagesTable = new JTable(messagesModel);
        styleTable(messagesTable);
        JScrollPane sp = new JScrollPane(messagesTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220,220,220),1));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(CARD_BG);
        JButton btnRefresh = createStyledButton("Refresh", SECONDARY_COLOR);
        btnRefresh.addActionListener(e -> loadMessages());
        bottom.add(btnRefresh);

        panel.add(sp, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private void downloadSelectedMaterial() {
        int row = materialsTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a material row first."); return; }
        int modelRow = materialsTable.convertRowIndexToModel(row);
        Object filePathObj = materialsModel.getValueAt(modelRow, 4);
        if (filePathObj == null) { JOptionPane.showMessageDialog(this, "No file available for this material."); return; }
        String filePath = filePathObj.toString();
        File src = new File(filePath);
        if (!src.exists()) { JOptionPane.showMessageDialog(this, "File not found on server: " + filePath); return; }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(src.getName()));
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File dest = chooser.getSelectedFile();
        try (FileInputStream in = new FileInputStream(src); FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            JOptionPane.showMessageDialog(this, "Downloaded to: " + dest.getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage());
        }
    }

    private void loadMaterials() {
        materialsModel.setRowCount(0);
        String sql = "SELECT sm.material_id, sm.title, sm.description, sm.file_path, c.course_name, sm.upload_date " +
                     "FROM study_materials sm " +
                     "JOIN courses c ON sm.course_id = c.course_id " +
                     "JOIN enrollments e ON e.course_id = c.course_id " +
                     "WHERE e.student_id = ? ORDER BY sm.upload_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                materialsModel.addRow(new Object[]{
                        rs.getString("title"),
                        rs.getString("course_name"),
                        rs.getString("description"),
                        rs.getTimestamp("upload_date"),
                        rs.getString("file_path")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); JOptionPane.showMessageDialog(this, "Error loading materials: " + e.getMessage()); }
    }

    private void loadMessages() {
        messagesModel.setRowCount(0);
        String sql = "SELECT m.subject, m.message_text, c.course_name, m.sent_date " +
                     "FROM messages m " +
                     "JOIN courses c ON m.course_id = c.course_id " +
                     "JOIN enrollments e ON e.course_id = c.course_id " +
                     "WHERE e.student_id = ? ORDER BY m.sent_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                messagesModel.addRow(new Object[]{
                        rs.getString("subject"),
                        rs.getString("course_name"),
                        rs.getString("message_text"),
                        rs.getTimestamp("sent_date")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); JOptionPane.showMessageDialog(this, "Error loading messages: " + e.getMessage()); }
    }

    private JPanel createStatCard(String title, String value, Color color, String icon) {
        JPanel card = new JPanel(new BorderLayout(15, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2, true),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.BLACK);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 40));
        valueLabel.setForeground(color);

        textPanel.add(titleLabel);
        textPanel.add(valueLabel);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Arial", Font.PLAIN, 15));
        table.setRowHeight(50);
        table.setShowVerticalLines(true);
        table.setGridColor(new Color(200, 200, 200));
        table.setSelectionBackground(new Color(30, 144, 255, 80));
        table.setSelectionForeground(Color.BLACK);
        table.setIntercellSpacing(new Dimension(10, 5));
        table.setForeground(Color.BLACK);
        table.setBackground(Color.WHITE);

        // Header styling - FIXED
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 16));
        header.setBackground(new Color(40, 40, 40));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 55));
        header.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 40), 2));
        header.setReorderingAllowed(false);

        // Custom header renderer for better visibility
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        headerRenderer.setFont(new Font("Arial", Font.BOLD, 16));
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setBackground(new Color(40, 40, 40));
        headerRenderer.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // Center align cells with better renderer
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setFont(new Font("Arial", Font.PLAIN, 15));
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private void loadEnrolledCourses() {
        courseTableModel.setRowCount(0);

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT c.course_code, c.course_name, u.full_name, c.credits, e.grade " +
                          "FROM enrollments e " +
                          "JOIN courses c ON e.course_id = c.course_id " +
                          "JOIN users u ON c.teacher_id = u.user_id " +
                          "WHERE e.student_id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, studentId);

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                courseTableModel.addRow(new Object[]{
                    rs.getString("course_code"),
                    rs.getString("course_name"),
                    rs.getString("full_name"),
                    rs.getInt("credits"),
                    rs.getString("grade") == null ? "N/A" : rs.getString("grade")
                });
            }

            // Update the count label
            if (coursesCountLabel != null) {
                coursesCountLabel.setText(String.valueOf(courseTableModel.getRowCount()));
            }

            rs.close();
            pst.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Error loading courses: " + ex.getMessage());
        }
    }

    private void enrollInCourse() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT course_id, course_code, course_name FROM courses " +
                          "WHERE course_id NOT IN (SELECT course_id FROM enrollments WHERE student_id = ?)";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, studentId);

            ResultSet rs = pst.executeQuery();
            java.util.List<String> courseList = new java.util.ArrayList<>();
            java.util.Map<String, Integer> courseMap = new java.util.HashMap<>();

            while (rs.next()) {
                String display = rs.getString("course_code") + " - " + rs.getString("course_name");
                courseList.add(display);
                courseMap.put(display, rs.getInt("course_id"));
            }

            if (courseList.isEmpty()) {
                showInfo("No courses available for enrollment.");
                return;
            }

            String selected = (String) JOptionPane.showInputDialog(this,
                "Select a course to enroll:",
                "Enroll in Course",
                JOptionPane.QUESTION_MESSAGE,
                null,
                courseList.toArray(),
                courseList.get(0));

            if (selected != null) {
                int courseId = courseMap.get(selected);
                String insertQuery = "INSERT INTO enrollments (student_id, course_id) VALUES (?, ?)";
                PreparedStatement insertPst = conn.prepareStatement(insertQuery);
                insertPst.setInt(1, studentId);
                insertPst.setInt(2, courseId);
                insertPst.executeUpdate();
                insertPst.close();

                showSuccess("Successfully enrolled in course!");
                loadEnrolledCourses();
            }

            rs.close();
            pst.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Error: " + ex.getMessage());
        }
    }

    private void viewAssignments() {
        assignmentTableModel.setRowCount(0);

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT a.assignment_id, a.title, c.course_name, a.due_date, " +
                          "a.max_marks, s.marks_obtained " +
                          "FROM assignments a " +
                          "JOIN courses c ON a.course_id = c.course_id " +
                          "JOIN enrollments e ON c.course_id = e.course_id " +
                          "LEFT JOIN submissions s ON a.assignment_id = s.assignment_id AND s.student_id = ? " +
                          "WHERE e.student_id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, studentId);
            pst.setInt(2, studentId);

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String status = rs.getObject("marks_obtained") == null ? "Not Submitted" : "Submitted";
                assignmentTableModel.addRow(new Object[]{
                    rs.getString("title"),
                    rs.getString("course_name"),
                    rs.getDate("due_date"),
                    rs.getInt("max_marks"),
                    rs.getObject("marks_obtained") == null ? "N/A" : rs.getInt("marks_obtained"),
                    status
                });
            }

            // Update the count label
            if (assignmentsCountLabel != null) {
                assignmentsCountLabel.setText(String.valueOf(assignmentTableModel.getRowCount()));
            }

            rs.close();
            pst.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Error loading assignments: " + ex.getMessage());
        }
    }

    private void submitAssignment() {
        int selectedRow = assignmentsTable.getSelectedRow();
        if (selectedRow < 0) {
            showWarning("Please select an assignment to submit.");
            return;
        }

        String assignmentTitle = (String) assignmentTableModel.getValueAt(selectedRow, 0);
        String courseName = (String) assignmentTableModel.getValueAt(selectedRow, 1);
        String status = (String) assignmentTableModel.getValueAt(selectedRow, 5);

        if ("Submitted".equals(status)) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "You have already submitted this assignment.\nDo you want to resubmit?",
                "Already Submitted",
                JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            Connection conn = DatabaseConnection.getConnection();

            String getAssignmentQuery = "SELECT a.assignment_id, a.max_marks " +
                                       "FROM assignments a " +
                                       "JOIN courses c ON a.course_id = c.course_id " +
                                       "WHERE a.title = ? AND c.course_name = ?";
            PreparedStatement pst = conn.prepareStatement(getAssignmentQuery);
            pst.setString(1, assignmentTitle);
            pst.setString(2, courseName);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int assignmentId = rs.getInt("assignment_id");
                int maxMarks = rs.getInt("max_marks");

                JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
                panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                JLabel fileLabel = new JLabel("File Upload:");
                JButton chooseFileButton = createStyledButton("Choose File", SECONDARY_COLOR);
                JLabel selectedFileLabel = new JLabel("No file selected");
                selectedFileLabel.setForeground(TEXT_SECONDARY);

                JLabel commentsLabel = new JLabel("Comments:");
                JTextArea commentsArea = new JTextArea(3, 20);
                commentsArea.setLineWrap(true);
                commentsArea.setWrapStyleWord(true);
                JScrollPane commentsScroll = new JScrollPane(commentsArea);

                final String[] selectedFilePath = {null};
                chooseFileButton.addActionListener(e -> {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    int result = fileChooser.showOpenDialog(this);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        selectedFilePath[0] = fileChooser.getSelectedFile().getAbsolutePath();
                        selectedFileLabel.setText(fileChooser.getSelectedFile().getName());
                        selectedFileLabel.setForeground(SUCCESS_COLOR);
                    }
                });

                panel.add(fileLabel);
                JPanel filePanel = new JPanel(new BorderLayout(5, 0));
                filePanel.add(chooseFileButton, BorderLayout.WEST);
                filePanel.add(selectedFileLabel, BorderLayout.CENTER);
                panel.add(filePanel);

                panel.add(commentsLabel);
                panel.add(commentsScroll);

                JLabel infoLabel = new JLabel("<html><b>Assignment:</b> " + assignmentTitle +
                                             "<br><b>Max Marks:</b> " + maxMarks + "</html>");
                panel.add(new JLabel());
                panel.add(infoLabel);

                int result = JOptionPane.showConfirmDialog(this,
                    panel,
                    "Submit Assignment",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    String checkSubmission = "SELECT submission_id FROM submissions " +
                                            "WHERE assignment_id = ? AND student_id = ?";
                    PreparedStatement checkPst = conn.prepareStatement(checkSubmission);
                    checkPst.setInt(1, assignmentId);
                    checkPst.setInt(2, studentId);
                    ResultSet checkRs = checkPst.executeQuery();

                    if (checkRs.next()) {
                        int submissionId = checkRs.getInt("submission_id");
                        String updateQuery = "UPDATE submissions SET submission_date = NOW(), " +
                                            "feedback = ? WHERE submission_id = ?";
                        PreparedStatement updatePst = conn.prepareStatement(updateQuery);
                        updatePst.setString(1, commentsArea.getText());
                        updatePst.setInt(2, submissionId);
                        updatePst.executeUpdate();
                        updatePst.close();

                        showSuccess("Assignment resubmitted successfully!" +
                                   (selectedFilePath[0] != null ? "\nFile: " + selectedFilePath[0] : ""));
                    } else {
                        String insertQuery = "INSERT INTO submissions (assignment_id, student_id, feedback) " +
                                            "VALUES (?, ?, ?)";
                        PreparedStatement insertPst = conn.prepareStatement(insertQuery);
                        insertPst.setInt(1, assignmentId);
                        insertPst.setInt(2, studentId);
                        insertPst.setString(3, commentsArea.getText());
                        insertPst.executeUpdate();
                        insertPst.close();

                        showSuccess("Assignment submitted successfully!" +
                                   (selectedFilePath[0] != null ? "\nFile: " + selectedFilePath[0] : ""));
                    }

                    checkRs.close();
                    checkPst.close();
                    viewAssignments();
                }
            } else {
                showError("Assignment not found!");
            }

            rs.close();
            pst.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Error submitting assignment: " + ex.getMessage());
        }
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void logout() {
        this.dispose();
        new LoginFrame().setVisible(true);
    }
}
