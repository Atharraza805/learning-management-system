package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import database.DatabaseConnection;

public class AdminDashboard extends JFrame {
    private int adminId;
    private String adminName;
    private JTable usersTable;
    private JTable coursesTable;
    private DefaultTableModel userTableModel;
    private DefaultTableModel courseTableModel;
    private JLabel usersCountLabel;
    private JLabel coursesCountLabel;
    private JLabel studentsCountLabel;
    private JLabel teachersCountLabel;

    // Color palette
    private static final Color PRIMARY_COLOR = new Color(220, 53, 69);
    private static final Color SECONDARY_COLOR = new Color(108, 117, 125);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color INFO_COLOR = new Color(23, 162, 184);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Color LIGHT_BG = new Color(248, 249, 250);
    private static final Color CARD_BG = Color.WHITE;

    public AdminDashboard(int userId, String name) {
        this.adminId = userId;
        this.adminName = name;

        setTitle("Admin Dashboard - " + name);
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initComponents();
        loadUsers();
        loadCourses();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(LIGHT_BG);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(getWidth(), 100));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        welcomePanel.setOpaque(false);

        JLabel iconLabel = new JLabel("👨‍💼");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Administrator Dashboard");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);

        JLabel nameLabel = new JLabel(adminName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 28));
        nameLabel.setForeground(Color.WHITE);

        textPanel.add(welcomeLabel);
        textPanel.add(nameLabel);

        welcomePanel.add(iconLabel);
        welcomePanel.add(textPanel);

        JButton logoutButton = createStyledButton("Logout", new Color(139, 0, 0));
        logoutButton.addActionListener(e -> logout());

        headerPanel.add(welcomePanel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.setBackground(LIGHT_BG);

        JPanel usersPanel = createUsersPanel();
        JPanel coursesPanel = createCoursesPanel();
        JPanel statsPanel = createStatisticsPanel();

        tabbedPane.addTab("  👥 Users Management  ", usersPanel);
        tabbedPane.addTab("  📚 Courses Management  ", coursesPanel);
        tabbedPane.addTab("  📊 Statistics  ", statsPanel);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(LIGHT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] userColumns = {"User ID", "Username", "Full Name", "Email", "Role"};
        userTableModel = new DefaultTableModel(userColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBackground(LIGHT_BG);
        statsPanel.setPreferredSize(new Dimension(0, 120));

        JPanel totalUsersCard = createDynamicStatCard("Total Users", "0", INFO_COLOR, "👥", 1);
        JPanel studentsCard = createDynamicStatCard("Students", "0", SUCCESS_COLOR, "🎓", 2);
        JPanel teachersCard = createDynamicStatCard("Teachers", "0", WARNING_COLOR, "👨‍🏫", 3);

        statsPanel.add(totalUsersCard);
        statsPanel.add(studentsCard);
        statsPanel.add(teachersCard);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(CARD_BG);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel tableTitle = new JLabel("All Users");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 22));
        tableTitle.setForeground(Color.BLACK);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        usersTable = new JTable(userTableModel);
        styleTable(usersTable);
        JScrollPane userScrollPane = new JScrollPane(usersTable);
        userScrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        tableCard.add(tableTitle, BorderLayout.NORTH);
        tableCard.add(userScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        buttonPanel.setBackground(CARD_BG);

        JButton addUserButton = createStyledButton("➕ Add User", SUCCESS_COLOR);
        JButton editUserButton = createStyledButton("✏️ Edit User", INFO_COLOR);
        JButton deleteUserButton = createStyledButton("🗑️ Delete User", DANGER_COLOR);
        JButton refreshButton = createStyledButton("🔄 Refresh", SECONDARY_COLOR);

        addUserButton.addActionListener(e -> addUser());
        editUserButton.addActionListener(e -> editUser());
        deleteUserButton.addActionListener(e -> deleteUser());
        refreshButton.addActionListener(e -> loadUsers());

        buttonPanel.add(addUserButton);
        buttonPanel.add(editUserButton);
        buttonPanel.add(deleteUserButton);
        buttonPanel.add(refreshButton);
        tableCard.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(statsPanel, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(LIGHT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] courseColumns = {"Course ID", "Course Code", "Course Name", "Teacher", "Credits"};
        courseTableModel = new DefaultTableModel(courseColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statsPanel.setBackground(LIGHT_BG);
        statsPanel.setPreferredSize(new Dimension(0, 120));

        JPanel totalCoursesCard = createDynamicStatCard("Total Courses", "0", PRIMARY_COLOR, "📚", 4);
        statsPanel.add(totalCoursesCard);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(CARD_BG);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel tableTitle = new JLabel("All Courses");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 22));
        tableTitle.setForeground(Color.BLACK);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        coursesTable = new JTable(courseTableModel);
        styleTable(coursesTable);
        JScrollPane courseScrollPane = new JScrollPane(coursesTable);
        courseScrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        tableCard.add(tableTitle, BorderLayout.NORTH);
        tableCard.add(courseScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        buttonPanel.setBackground(CARD_BG);

        JButton addCourseButton = createStyledButton("➕ Add Course", SUCCESS_COLOR);
        JButton editCourseButton = createStyledButton("✏️ Edit Course", INFO_COLOR);
        JButton deleteCourseButton = createStyledButton("🗑️ Delete Course", DANGER_COLOR);
        JButton refreshButton = createStyledButton("🔄 Refresh", SECONDARY_COLOR);

        addCourseButton.addActionListener(e -> addCourse());
        editCourseButton.addActionListener(e -> editCourse());
        deleteCourseButton.addActionListener(e -> deleteCourse());
        refreshButton.addActionListener(e -> loadCourses());

        buttonPanel.add(addCourseButton);
        buttonPanel.add(editCourseButton);
        buttonPanel.add(deleteCourseButton);
        buttonPanel.add(refreshButton);
        tableCard.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(statsPanel, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);

        return panel;
    }

    // ------------------------------------------------------------------
    // (All database methods and UI helper methods continue unchanged)
    // ------------------------------------------------------------------

    private void loadUsers() {
        userTableModel.setRowCount(0);
        int studentCount = 0;
        int teacherCount = 0;

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT user_id, username, full_name, email, role FROM users ORDER BY user_id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                userTableModel.addRow(new Object[]{
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("role")
                });

                String role = rs.getString("role");
                if ("student".equals(role)) studentCount++;
                else if ("teacher".equals(role)) teacherCount++;
            }

            if (usersCountLabel != null) usersCountLabel.setText(String.valueOf(userTableModel.getRowCount()));
            if (studentsCountLabel != null) studentsCountLabel.setText(String.valueOf(studentCount));
            if (teachersCountLabel != null) teachersCountLabel.setText(String.valueOf(teacherCount));

            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Error loading users: " + ex.getMessage());
        }
    }

    private void loadCourses() {
        courseTableModel.setRowCount(0);

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT c.course_id, c.course_code, c.course_name, u.full_name, c.credits " +
                    "FROM courses c " +
                    "LEFT JOIN users u ON c.teacher_id = u.user_id " +
                    "ORDER BY c.course_id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                courseTableModel.addRow(new Object[]{
                        rs.getInt("course_id"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getString("full_name") == null ? "No Teacher" : rs.getString("full_name"),
                        rs.getInt("credits")
                });
            }

            if (coursesCountLabel != null) {
                coursesCountLabel.setText(String.valueOf(courseTableModel.getRowCount()));
            }

            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Error loading courses: " + ex.getMessage());
        }
    }

    private void addUser() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField fullNameField = new JTextField();
        JTextField emailField = new JTextField();
        String[] roles = {"student", "teacher", "admin"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Full Name:"));
        panel.add(fullNameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Role:"));
        panel.add(roleCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New User", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                String query = "INSERT INTO users (username, password, full_name, email, role) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(query);
                pst.setString(1, usernameField.getText());
                pst.setString(2, new String(passwordField.getPassword()));
                pst.setString(3, fullNameField.getText());
                pst.setString(4, emailField.getText());
                pst.setString(5, (String) roleCombo.getSelectedItem());

                pst.executeUpdate();
                pst.close();

                showSuccess("User added successfully!");
                loadUsers();
            } catch (SQLException ex) {
                ex.printStackTrace();
                showError("Error: " + ex.getMessage());
            }
        }
    }

    private void editUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow < 0) {
            showWarning("Please select a user to edit.");
            return;
        }

        int userId = (int) userTableModel.getValueAt(selectedRow, 0);
        String currentUsername = (String) userTableModel.getValueAt(selectedRow, 1);
        String currentFullName = (String) userTableModel.getValueAt(selectedRow, 2);
        String currentEmail = (String) userTableModel.getValueAt(selectedRow, 3);
        String currentRole = (String) userTableModel.getValueAt(selectedRow, 4);

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField usernameField = new JTextField(currentUsername);
        JTextField fullNameField = new JTextField(currentFullName);
        JTextField emailField = new JTextField(currentEmail);
        String[] roles = {"student", "teacher", "admin"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);
        roleCombo.setSelectedItem(currentRole);

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Full Name:"));
        panel.add(fullNameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Role:"));
        panel.add(roleCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit User", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                String query = "UPDATE users SET username=?, full_name=?, email=?, role=? WHERE user_id=?";
                PreparedStatement pst = conn.prepareStatement(query);
                pst.setString(1, usernameField.getText());
                pst.setString(2, fullNameField.getText());
                pst.setString(3, emailField.getText());
                pst.setString(4, (String) roleCombo.getSelectedItem());
                pst.setInt(5, userId);

                pst.executeUpdate();
                pst.close();

                showSuccess("User updated successfully!");
                loadUsers();
            } catch (SQLException ex) {
                ex.printStackTrace();
                showError("Error: " + ex.getMessage());
            }
        }
    }

    private void deleteUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow < 0) {
            showWarning("Please select a user to delete.");
            return;
        }

        int userId = (int) userTableModel.getValueAt(selectedRow, 0);
        String username = (String) userTableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete user: " + username + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                String query = "DELETE FROM users WHERE user_id=?";
                PreparedStatement pst = conn.prepareStatement(query);
                pst.setInt(1, userId);
                pst.executeUpdate();
                pst.close();

                showSuccess("User deleted successfully!");
                loadUsers();
            } catch (SQLException ex) {
                ex.printStackTrace();
                showError("Error: " + ex.getMessage());
            }
        }
    }

    private void addCourse() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String teacherQuery = "SELECT user_id, full_name FROM users WHERE role='teacher'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(teacherQuery);

            java.util.List<String> teacherList = new java.util.ArrayList<>();
            java.util.Map<String, Integer> teacherMap = new java.util.HashMap<>();

            while (rs.next()) {
                String name = rs.getString("full_name");
                teacherList.add(name);
                teacherMap.put(name, rs.getInt("user_id"));
            }

            JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
            JTextField codeField = new JTextField();
            JTextField nameField = new JTextField();
            JTextArea descArea = new JTextArea(3, 20);
            JComboBox<String> teacherCombo = new JComboBox<>(teacherList.toArray(new String[0]));
            JTextField creditsField = new JTextField();

            panel.add(new JLabel("Course Code:"));
            panel.add(codeField);
            panel.add(new JLabel("Course Name:"));
            panel.add(nameField);
            panel.add(new JLabel("Description:"));
            panel.add(new JScrollPane(descArea));
            panel.add(new JLabel("Teacher:"));
            panel.add(teacherCombo);
            panel.add(new JLabel("Credits:"));
            panel.add(creditsField);

            int result = JOptionPane.showConfirmDialog(this, panel, "Add New Course", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                String selectedTeacher = (String) teacherCombo.getSelectedItem();
                int teacherId = teacherMap.get(selectedTeacher);

                String insertQuery = "INSERT INTO courses (course_code, course_name, description, teacher_id, credits) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(insertQuery);
                pst.setString(1, codeField.getText());
                pst.setString(2, nameField.getText());
                pst.setString(3, descArea.getText());
                pst.setInt(4, teacherId);
                pst.setInt(5, Integer.parseInt(creditsField.getText()));

                pst.executeUpdate();
                pst.close();

                showSuccess("Course added successfully!");
                loadCourses();
            }

            rs.close();
            stmt.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Error: " + ex.getMessage());
        }
    }

    private void editCourse() {
        JOptionPane.showMessageDialog(this, "Edit course functionality - similar to edit user");
    }

    private void deleteCourse() {
        int selectedRow = coursesTable.getSelectedRow();
        if (selectedRow < 0) {
            showWarning("Please select a course to delete.");
            return;
        }

        int courseId = (int) courseTableModel.getValueAt(selectedRow, 0);
        String courseName = (String) courseTableModel.getValueAt(selectedRow, 2);

        try {
            Connection conn = DatabaseConnection.getConnection();

            String checkQuery = "SELECT COUNT(*) as count FROM enrollments WHERE course_id=?";
            PreparedStatement checkPst = conn.prepareStatement(checkQuery);
            checkPst.setInt(1, courseId);
            ResultSet rs = checkPst.executeQuery();
            rs.next();
            int enrollmentCount = rs.getInt("count");
            rs.close();
            checkPst.close();

            String warningMsg = enrollmentCount > 0
                    ? "This course has " + enrollmentCount + " student(s) enrolled.\n"
                    + "Deleting will remove all enrollments, assignments, and submissions.\n\n"
                    + "Are you sure you want to delete: " + courseName + "?"
                    : "Are you sure you want to delete course: " + courseName + "?";

            int confirm = JOptionPane.showConfirmDialog(this,
                    warningMsg,
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                // Delete related data
                String deleteSubmissions = "DELETE FROM submissions WHERE assignment_id IN "
                        + "(SELECT assignment_id FROM assignments WHERE course_id=?)";
                PreparedStatement pst1 = conn.prepareStatement(deleteSubmissions);
                pst1.setInt(1, courseId);
                pst1.executeUpdate();
                pst1.close();

                String deleteAssignments = "DELETE FROM assignments WHERE course_id=?";
                PreparedStatement pst2 = conn.prepareStatement(deleteAssignments);
                pst2.setInt(1, courseId);
                pst2.executeUpdate();
                pst2.close();

                String deleteEnrollments = "DELETE FROM enrollments WHERE course_id=?";
                PreparedStatement pst3 = conn.prepareStatement(deleteEnrollments);
                pst3.setInt(1, courseId);
                pst3.executeUpdate();
                pst3.close();

                String deleteCourse = "DELETE FROM courses WHERE course_id=?";
                PreparedStatement pst4 = conn.prepareStatement(deleteCourse);
                pst4.setInt(1, courseId);
                pst4.executeUpdate();
                pst4.close();

                showSuccess("Course deleted successfully!");
                loadCourses();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Error deleting course: " + ex.getMessage());
        }
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(LIGHT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("System Statistics Overview");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        JPanel statsGrid = new JPanel(new GridLayout(3, 2, 25, 25));
        statsGrid.setBackground(LIGHT_BG);

        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users");
            rs.next();
            int totalUsers = rs.getInt("count");

            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users WHERE role='student'");
            rs.next();
            int totalStudents = rs.getInt("count");

            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users WHERE role='teacher'");
            rs.next();
            int totalTeachers = rs.getInt("count");

            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM courses");
            rs.next();
            int totalCourses = rs.getInt("count");

            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM enrollments");
            rs.next();
            int totalEnrollments = rs.getInt("count");

            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM assignments");
            rs.next();
            int totalAssignments = rs.getInt("count");

            statsGrid.add(createLargeStatCard("Total Users", String.valueOf(totalUsers), INFO_COLOR, "👥"));
            statsGrid.add(createLargeStatCard("Total Students", String.valueOf(totalStudents), SUCCESS_COLOR, "🎓"));
            statsGrid.add(createLargeStatCard("Total Teachers", String.valueOf(totalTeachers), WARNING_COLOR, "👨‍🏫"));
            statsGrid.add(createLargeStatCard("Total Courses", String.valueOf(totalCourses), PRIMARY_COLOR, "📚"));
            statsGrid.add(createLargeStatCard("Total Enrollments", String.valueOf(totalEnrollments), new Color(255, 140, 0), "📝"));
            statsGrid.add(createLargeStatCard("Total Assignments", String.valueOf(totalAssignments), new Color(138, 43, 226), "📋"));

            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Error loading statistics: " + ex.getMessage());
        }

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(statsGrid, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDynamicStatCard(String title, String value, Color color, String icon, int type) {
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

        switch (type) {
            case 1 -> usersCountLabel = valueLabel;
            case 2 -> studentsCountLabel = valueLabel;
            case 3 -> teachersCountLabel = valueLabel;
            case 4 -> coursesCountLabel = valueLabel;
        }

        textPanel.add(titleLabel);
        textPanel.add(valueLabel);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createLargeStatCard(String title, String value, Color color, String icon) {
        JPanel card = new JPanel(new BorderLayout(20, 15));
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 3, true),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 50));
        valueLabel.setForeground(Color.WHITE);

        textPanel.add(titleLabel);
        textPanel.add(valueLabel);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

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
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(45);
        table.setShowVerticalLines(true);
        table.setGridColor(new Color(200, 200, 200));
        table.setSelectionBackground(new Color(220, 53, 69, 60));
        table.setSelectionForeground(Color.BLACK);
        table.setIntercellSpacing(new Dimension(10, 5));
        table.setForeground(Color.BLACK);
        table.setBackground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 15));
        header.setBackground(new Color(40, 40, 40));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 50));
        header.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 40), 2));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        headerRenderer.setFont(new Font("Arial", Font.BOLD, 15));
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setBackground(new Color(40, 40, 40));
        headerRenderer.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setFont(new Font("Arial", Font.PLAIN, 14));
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private void logout() {
        this.dispose();
        new LoginFrame().setVisible(true);
    }

    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }
}


