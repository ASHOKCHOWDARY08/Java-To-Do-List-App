
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;

enum Priority {
    LOW, MEDIUM, HIGH
}

class Task implements Serializable {
    String title;
    boolean completed;
    LocalDate dueDate;
    Priority priority;

    public Task(String title, LocalDate dueDate, Priority priority) {
        this.title = title;
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = false;
    }

    @Override
    public String toString() {
        return String.format("<html>%s <b>%s</b><br><small>Due: %s | Priority: %s</small></html>",
                completed ? "✅" : "🕒", title, dueDate, priority);
    }
}

public class AdvancedToDoList extends JFrame {
    private final DefaultListModel<Task> taskModel = new DefaultListModel<>();
    private final JList<Task> taskList = new JList<>(taskModel);
    private final JTextField titleInput = new JTextField(15);
    private final JTextField dateInput = new JTextField(10); // Format: YYYY-MM-DD
    private final JComboBox<Priority> priorityInput = new JComboBox<>(Priority.values());
    private final JTextField searchInput = new JTextField(15);
    private final JButton addButton = new JButton("➕ Add Task");
    private final JButton deleteButton = new JButton("🗑️ Delete");
    private final JButton markDoneButton = new JButton("✅ Mark Done");
    private final JToggleButton themeToggle = new JToggleButton("🌙 Dark Mode");

    private final String FILE_NAME = "advanced_tasks.txt";
    private boolean darkMode = false;

    public AdvancedToDoList() {
        super("📝 Advanced To-Do List");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 550);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        loadTasks();
        buildUI();
        applyTheme();
        setVisible(true);
    }

    private void buildUI() {
        taskList.setCellRenderer(new DefaultListCellRenderer() {{
            setVerticalAlignment(SwingConstants.TOP);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }});
        taskList.setFixedCellHeight(60);
        taskList.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(titleInput, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Due Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(dateInput, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(priorityInput, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        inputPanel.add(addButton, gbc);

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(inputPanel, BorderLayout.CENTER);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("🔍 Search:"));
        searchPanel.add(searchInput);
        searchPanel.add(themeToggle);

        topPanel.add(searchPanel, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.add(markDoneButton);
        buttonPanel.add(deleteButton);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(taskList), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Event Handling
        addButton.addActionListener(e -> addTask());
        deleteButton.addActionListener(e -> deleteSelected());
        markDoneButton.addActionListener(e -> markSelectedDone());
        themeToggle.addActionListener(e -> toggleTheme());

        searchInput.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTasks(); }
            public void removeUpdate(DocumentEvent e) { filterTasks(); }
            public void changedUpdate(DocumentEvent e) { filterTasks(); }
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { saveTasks(); }
        });
    }

    private void addTask() {
        String title = titleInput.getText().trim();
        String dateText = dateInput.getText().trim();
        if (title.isEmpty() || dateText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in both title and date.");
            return;
        }
        try {
            LocalDate date = LocalDate.parse(dateText);
            Priority priority = (Priority) priorityInput.getSelectedItem();
            taskModel.addElement(new Task(title, date, priority));
            titleInput.setText("");
            dateInput.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.");
        }
    }

    private void deleteSelected() {
        int index = taskList.getSelectedIndex();
        if (index != -1) {
            taskModel.remove(index);
        }
    }

    private void markSelectedDone() {
        int index = taskList.getSelectedIndex();
        if (index != -1) {
            taskModel.get(index).completed = true;
            taskList.repaint();
        }
    }

    private void filterTasks() {
        String query = searchInput.getText().toLowerCase();
        DefaultListModel<Task> filtered = new DefaultListModel<>();
        for (int i = 0; i < taskModel.size(); i++) {
            Task t = taskModel.get(i);
            if (t.title.toLowerCase().contains(query)) {
                filtered.addElement(t);
            }
        }
        taskList.setModel(filtered.isEmpty() && query.isEmpty() ? taskModel : filtered);
    }

    private void toggleTheme() {
        darkMode = !darkMode;
        themeToggle.setText(darkMode ? "☀ Light Mode" : "🌙 Dark Mode");
        applyTheme();
    }

    private void applyTheme() {
        Color bg = darkMode ? new Color(30, 30, 30) : Color.WHITE;
        Color fg = darkMode ? Color.WHITE : Color.BLACK;

        getContentPane().setBackground(bg);
        taskList.setBackground(bg);
        taskList.setForeground(fg);
        titleInput.setBackground(bg);
        titleInput.setForeground(fg);
        dateInput.setBackground(bg);
        dateInput.setForeground(fg);
        searchInput.setBackground(bg);
        searchInput.setForeground(fg);
    }

    private void saveTasks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            ArrayList<Task> list = new ArrayList<>();
            for (int i = 0; i < taskModel.size(); i++) {
                list.add(taskModel.get(i));
            }
            oos.writeObject(list);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTasks() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            ArrayList<Task> loaded = (ArrayList<Task>) ois.readObject();
            for (Task t : loaded) {
                taskModel.addElement(t);
            }
        } catch (Exception e) {
            System.out.println("No saved tasks found.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdvancedToDoList::new);
    }
}
