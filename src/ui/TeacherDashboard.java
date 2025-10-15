// File: TeacherDashboard.java
// Place this in the same package (ui) and replace your existing TeacherDashboard.java
package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseConnection;

public class TeacherDashboard extends JFrame {
    private int teacherId;
    private String teacherName;
    private JTable coursesTable;
    private JTable studentsTable;
    private DefaultTableModel courseTableModel;
    private DefaultTableModel studentTableModel;
    private JLabel coursesCountLabel;
    private JLabel studentsCountLabel;

    // New UI components for Materials & Messages
    private JComboBox<CourseItem> cbMaterialsCourses;
    private JTextField tfMaterialTitle;
    private JTextArea taMaterialDesc;
    private JTextField tfMaterialFilePath;
    private JButton btnChooseMaterialFile;
    private JButton btnUploadMaterial;
    private File selectedMaterialFile;
    private final File materialsDir = new File("materials");

    private JComboBox<CourseItem> cbMessageCourses;
    private JTextField tfMessageSubject;
    private JTextArea taMessageBody;
    private JButton btnSendMessage;

    // Modern color palette
    private static final Color PRIMARY_COLOR = new Color(30, 144, 255);
    private static final Color SECONDARY_COLOR = new Color(70, 130, 180);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Color INFO_COLOR = new Color(23, 162, 184);
    private static final Color LIGHT_BG = new Color(248, 249, 250);
    private static final Color CARD_BG = Color.WHITE;

    public TeacherDashboard(int userId, String name) {
        this.teacherId = userId;
        this.teacherName = name;

        setTitle("Teacher Dashboard - " + name);
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!materialsDir.exists()) {
            materialsDir.mkdirs();
        }

        initComponents();
        loadMyCourses();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(LIGHT_BG);

        // Modern Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(SUCCESS_COLOR);
        headerPanel.setPreferredSize(new Dimension(getWidth(), 100));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Left side - Welcome message with icon
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        welcomePanel.setOpaque(false);

        JLabel iconLabel = new JLabel("👨‍🏫");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome back,");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);

        JLabel nameLabel = new JLabel(teacherName);
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

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.setBackground(LIGHT_BG);

        // My Courses Tab
        JPanel coursesPanel = createCoursesPanel();

        // Enrolled Students Tab
        JPanel studentsPanel = createStudentsPanel();

        tabbedPane.addTab("  📚 My Courses  ", coursesPanel);
        tabbedPane.addTab("  👥 Enrolled Students  ", studentsPanel);

        // Add listener to load students when tab is selected
        tabbedPane.addChangeListener(e -> {
            // **Note**: Enrolled Students tab is still index 1 (we will append new tabs after this)
            if (tabbedPane.getSelectedIndex() == 1) {
                viewEnrolledStudents();
            }
        });

        // --- Append new tabs at the end so existing indices remain unchanged ---
        tabbedPane.addTab("  📂 Study Materials  ", createMaterialsPanel());
        tabbedPane.addTab("  💬 Messages  ", createMessagesPanel());

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(LIGHT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create table model FIRST
        String[] courseColumns = {"Course Code", "Course Name", "Credits", "Students Enrolled"};
        courseTableModel = new DefaultTableModel(courseColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Stats card
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statsPanel.setBackground(LIGHT_BG);
        statsPanel.setPreferredSize(new Dimension(0, 120));

        JPanel totalCoursesCard = createDynamicStatCard("Total Courses", "0", SUCCESS_COLOR, "📚", true);
        statsPanel.add(totalCoursesCard);

        // Table card
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(CARD_BG);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel tableTitle = new JLabel("My Courses");
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

        JButton createAssignmentButton = createStyledButton("➕ Create Assignment", PRIMARY_COLOR);
        JButton gradeButton = createStyledButton("📊 Grade Submissions", WARNING_COLOR);

        createAssignmentButton.addActionListener(e -> createAssignment());
        gradeButton.addActionListener(e -> gradeSubmissions());

        buttonPanel.add(createAssignmentButton);
        buttonPanel.add(gradeButton);
        tableCard.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(statsPanel, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(LIGHT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create table model FIRST
        String[] studentColumns = {"Student Name", "Email", "Course", "Grade"};
        studentTableModel = new DefaultTableModel(studentColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Stats card
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statsPanel.setBackground(LIGHT_BG);
        statsPanel.setPreferredSize(new Dimension(0, 120));

        JPanel totalStudentsCard = createDynamicStatCard("Total Students", "0", INFO_COLOR, "👥", false);
        statsPanel.add(totalStudentsCard);

        // Table card
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(CARD_BG);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel tableTitle = new JLabel("Enrolled Students");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 22));
        tableTitle.setForeground(Color.BLACK);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        studentsTable = new JTable(studentTableModel);
        styleTable(studentsTable);
        JScrollPane studentScrollPane = new JScrollPane(studentsTable);
        studentScrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        tableCard.add(tableTitle, BorderLayout.NORTH);
        tableCard.add(studentScrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        buttonPanel.setBackground(CARD_BG);

        JButton refreshButton = createStyledButton("🔄 Refresh", SECONDARY_COLOR);
        refreshButton.addActionListener(e -> viewEnrolledStudents());

        buttonPanel.add(refreshButton);
        tableCard.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(statsPanel, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);

        return panel;
    }

    // ---------- New: Materials Tab ----------
    private JPanel createMaterialsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(LIGHT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(LIGHT_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Course:"), gbc);
        cbMaterialsCourses = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = y++; form.add(cbMaterialsCourses, gbc);

        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Title:"), gbc);
        tfMaterialTitle = new JTextField();
        gbc.gridx = 1; gbc.gridy = y++; form.add(tfMaterialTitle, gbc);

        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Description:"), gbc);
        taMaterialDesc = new JTextArea(5, 30);
        JScrollPane spDesc = new JScrollPane(taMaterialDesc);
        gbc.gridx = 1; gbc.gridy = y++; form.add(spDesc, gbc);

        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("File:"), gbc);
        JPanel fileRow = new JPanel(new BorderLayout(6, 0));
        fileRow.setBackground(LIGHT_BG);
        tfMaterialFilePath = new JTextField();
        tfMaterialFilePath.setEditable(false);
        btnChooseMaterialFile = new JButton("Choose File");
        btnChooseMaterialFile.addActionListener(this::onChooseMaterialFile);
        fileRow.add(tfMaterialFilePath, BorderLayout.CENTER);
        fileRow.add(btnChooseMaterialFile, BorderLayout.EAST);
        gbc.gridx = 1; gbc.gridy = y++; form.add(fileRow, gbc);

        btnUploadMaterial = new JButton("Upload");
        btnUploadMaterial.addActionListener(e -> onUploadMaterial());
        gbc.gridx = 1; gbc.gridy = y++; form.add(btnUploadMaterial, gbc);

        panel.add(form, BorderLayout.NORTH);

        loadCoursesIntoCombo(cbMaterialsCourses); // populate course choices
        return panel;
    }

    // ---------- New: Messages Tab ----------
    private JPanel createMessagesPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(LIGHT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(LIGHT_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Course:"), gbc);
        cbMessageCourses = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = y++; form.add(cbMessageCourses, gbc);

        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Subject:"), gbc);
        tfMessageSubject = new JTextField();
        gbc.gridx = 1; gbc.gridy = y++; form.add(tfMessageSubject, gbc);

        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Message:"), gbc);
        taMessageBody = new JTextArea(6, 30);
        JScrollPane spMsg = new JScrollPane(taMessageBody);
        gbc.gridx = 1; gbc.gridy = y++; form.add(spMsg, gbc);

        btnSendMessage = new JButton("Send");
        btnSendMessage.addActionListener(e -> onSendMessage());
        gbc.gridx = 1; gbc.gridy = y++; form.add(btnSendMessage, gbc);

        panel.add(form, BorderLayout.NORTH);

        loadCoursesIntoCombo(cbMessageCourses); // populate course choices
        return panel;
    }

    private void onChooseMaterialFile(ActionEvent ev) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Documents and PDFs", "pdf", "doc", "docx", "ppt", "pptx", "txt"));
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            selectedMaterialFile = chooser.getSelectedFile();
            tfMaterialFilePath.setText(selectedMaterialFile.getAbsolutePath());
        }
    }

    private void onUploadMaterial() {
        CourseItem course = (CourseItem) cbMaterialsCourses.getSelectedItem();
        if (course == null) { JOptionPane.showMessageDialog(this, "Please select a course."); return; }
        String title = tfMaterialTitle.getText().trim();
        if (title.isEmpty()) { JOptionPane.showMessageDialog(this, "Please enter a title."); return; }
        if (selectedMaterialFile == null) { JOptionPane.showMessageDialog(this, "Please choose a file to upload."); return; }

        try {
            String safeName = System.currentTimeMillis() + "_" + selectedMaterialFile.getName();
            File dest = new File(materialsDir, safeName);
            Files.copy(selectedMaterialFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            String dbPath = dest.getAbsolutePath();

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO study_materials (course_id, title, description, file_path, uploaded_by) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, course.id);
                ps.setString(2, title);
                ps.setString(3, taMaterialDesc.getText().trim());
                ps.setString(4, dbPath);
                ps.setInt(5, teacherId);
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Material uploaded successfully.");
            // reset form
            tfMaterialTitle.setText("");
            taMaterialDesc.setText("");
            tfMaterialFilePath.setText("");
            selectedMaterialFile = null;
        } catch (IOException | SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error uploading material: " + ex.getMessage());
        }
    }

    private void onSendMessage() {
        CourseItem course = (CourseItem) cbMessageCourses.getSelectedItem();
        if (course == null) { JOptionPane.showMessageDialog(this, "Please select a course."); return; }
        String subject = tfMessageSubject.getText().trim();
        String msg = taMessageBody.getText().trim();
        if (subject.isEmpty() || msg.isEmpty()) { JOptionPane.showMessageDialog(this, "Subject and message cannot be empty."); return; }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO messages (course_id, sender_id, subject, message_text) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, course.id);
            ps.setInt(2, teacherId);
            ps.setString(3, subject);
            ps.setString(4, msg);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Message sent.");
            tfMessageSubject.setText("");
            taMessageBody.setText("");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error sending message: " + ex.getMessage());
        }
    }

    private void loadCoursesIntoCombo(JComboBox<CourseItem> combo) {
        combo.removeAllItems();
        String sql = "SELECT course_id, course_name FROM courses WHERE teacher_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ResultSet rs = ps.executeQuery();
            List<CourseItem> list = new ArrayList<>();
            while (rs.next()) {
                CourseItem c = new CourseItem(rs.getInt("course_id"), rs.getString("course_name"));
                list.add(c);
            }
            for (CourseItem c : list) combo.addItem(c);
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            // don't block UI on failure to load course list; show message
            JOptionPane.showMessageDialog(this, "Error loading courses for materials/messages: " + e.getMessage());
        }
    }

    private JPanel createDynamicStatCard(String title, String value, Color color, String icon, boolean isCourseCard) {
        JPanel card = new JPanel(new BorderLayout(15, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2, true),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        card.setPreferredSize(new Dimension(300, 100));

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

        if (isCourseCard) {
            coursesCountLabel = valueLabel;
        } else {
            studentsCountLabel = valueLabel;
        }

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

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 16));
        header.setBackground(new Color(40, 40, 40));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 55));
        header.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 40), 2));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        headerRenderer.setFont(new Font("Arial", Font.BOLD, 16));
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setBackground(new Color(40, 40, 40));
        headerRenderer.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setFont(new Font("Arial", Font.PLAIN, 15));
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private void loadMyCourses() {
        courseTableModel.setRowCount(0);

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT c.course_code, c.course_name, c.credits, " +
                    "COUNT(e.student_id) as student_count " +
                    "FROM courses c " +
                    "LEFT JOIN enrollments e ON c.course_id = e.course_id " +
                    "WHERE c.teacher_id = ? " +
                    "GROUP BY c.course_id";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, teacherId);

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                courseTableModel.addRow(new Object[]{
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getInt("credits"),
                        rs.getInt("student_count")
                });
            }

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

    private void viewEnrolledStudents() {
        studentTableModel.setRowCount(0);

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT u.full_name, u.email, c.course_name, e.grade " +
                    "FROM enrollments e " +
                    "JOIN users u ON e.student_id = u.user_id " +
                    "JOIN courses c ON e.course_id = c.course_id " +
                    "WHERE c.teacher_id = ? " +
                    "ORDER BY c.course_name, u.full_name";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, teacherId);

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                studentTableModel.addRow(new Object[]{
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("course_name"),
                        rs.getString("grade") == null ? "Not Graded" : rs.getString("grade")
                });
            }

            if (studentsCountLabel != null) {
                studentsCountLabel.setText(String.valueOf(studentTableModel.getRowCount()));
            }

            rs.close();
            pst.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Error loading students: " + ex.getMessage());
        }
    }

    private void createAssignment() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT course_id, course_name FROM courses WHERE teacher_id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, teacherId);

            ResultSet rs = pst.executeQuery();
            java.util.List<String> courseList = new java.util.ArrayList<>();
            java.util.Map<String, Integer> courseMap = new java.util.HashMap<>();

            while (rs.next()) {
                String courseName = rs.getString("course_name");
                courseList.add(courseName);
                courseMap.put(courseName, rs.getInt("course_id"));
            }

            if (courseList.isEmpty()) {
                showInfo("You don't have any courses assigned.");
                return;
            }

            JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
            JComboBox<String> courseCombo = new JComboBox<>(courseList.toArray(new String[0]));
            JTextField titleField = new JTextField();
            JTextArea descArea = new JTextArea(3, 20);
            JTextField dueDateField = new JTextField("YYYY-MM-DD");
            JTextField maxMarksField = new JTextField();

            panel.add(new JLabel("Course:"));
            panel.add(courseCombo);
            panel.add(new JLabel("Title:"));
            panel.add(titleField);
            panel.add(new JLabel("Description:"));
            panel.add(new JScrollPane(descArea));
            panel.add(new JLabel("Due Date:"));
            panel.add(dueDateField);
            panel.add(new JLabel("Max Marks:"));
            panel.add(maxMarksField);

            int result = JOptionPane.showConfirmDialog(this, panel,
                    "Create Assignment", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                String selectedCourse = (String) courseCombo.getSelectedItem();
                int courseId = courseMap.get(selectedCourse);

                String insertQuery = "INSERT INTO assignments (course_id, title, description, due_date, max_marks) " +
                        "VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertPst = conn.prepareStatement(insertQuery);
                insertPst.setInt(1, courseId);
                insertPst.setString(2, titleField.getText());
                insertPst.setString(3, descArea.getText());
                insertPst.setString(4, dueDateField.getText());
                insertPst.setInt(5, Integer.parseInt(maxMarksField.getText()));
                insertPst.executeUpdate();
                insertPst.close();

                showSuccess("Assignment created successfully!");
            }

            rs.close();
            pst.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Error creating assignment: " + ex.getMessage());
        }
    }

    private void gradeSubmissions() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT s.submission_id, a.title, c.course_name, u.full_name, " +
                    "a.max_marks, s.marks_obtained " +
                    "FROM submissions s " +
                    "JOIN assignments a ON s.assignment_id = a.assignment_id " +
                    "JOIN courses c ON a.course_id = c.course_id " +
                    "JOIN users u ON s.student_id = u.user_id " +
                    "WHERE c.teacher_id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, teacherId);

            ResultSet rs = pst.executeQuery();

            DefaultTableModel model = new DefaultTableModel(
                    new String[]{"Submission ID", "Assignment", "Course", "Student", "Max Marks", "Obtained"}, 0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("submission_id"),
                        rs.getString("title"),
                        rs.getString("course_name"),
                        rs.getString("full_name"),
                        rs.getInt("max_marks"),
                        rs.getObject("marks_obtained") == null ? "Not Graded" : rs.getInt("marks_obtained")
                });
            }

            JTable submissionsTable = new JTable(model);
            styleTable(submissionsTable);
            JScrollPane scrollPane = new JScrollPane(submissionsTable);

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(scrollPane, BorderLayout.CENTER);

            JButton gradeButton = createStyledButton("Grade Selected", SUCCESS_COLOR);
            gradeButton.addActionListener(e -> {
                int selectedRow = submissionsTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int submissionId = (int) model.getValueAt(selectedRow, 0);
                    int maxMarks = (int) model.getValueAt(selectedRow, 4);

                    String marks = JOptionPane.showInputDialog(this,
                            "Enter marks (0-" + maxMarks + "):");

                    if (marks != null && !marks.isEmpty()) {
                        try {
                            int marksObtained = Integer.parseInt(marks);
                            if (marksObtained >= 0 && marksObtained <= maxMarks) {
                                String updateQuery = "UPDATE submissions SET marks_obtained = ? WHERE submission_id = ?";
                                PreparedStatement updatePst = conn.prepareStatement(updateQuery);
                                updatePst.setInt(1, marksObtained);
                                updatePst.setInt(2, submissionId);
                                updatePst.executeUpdate();
                                updatePst.close();

                                showSuccess("Grade updated successfully!");
                                model.setValueAt(marksObtained, selectedRow, 5);
                            } else {
                                showError("Invalid marks!");
                            }
                        } catch (Exception ex2) {
                            showError("Error: " + ex2.getMessage());
                        }
                    }
                } else {
                    showWarning("Please select a submission to grade.");
                }
            });

            panel.add(gradeButton, BorderLayout.SOUTH);

            JDialog dialog = new JDialog(this, "Grade Submissions", true);
            dialog.add(panel);
            dialog.setSize(700, 400);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

            rs.close();
            pst.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Error loading submissions: " + ex.getMessage());
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

    // small helper class for course combo boxes
    private static class CourseItem {
        public final int id;
        public final String name;
        public CourseItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }
}
