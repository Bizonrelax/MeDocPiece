import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.github.javaparser.Position;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;

public class MethodMessenger extends JFrame {

    private static final String APP_NAME = "MeDocPiece";
    private static final String CONFIG_FILE_NAME = "SelectRecall.dat";
    private static final File CONFIG_DIR = new File(System.getProperty("user.home") + File.separator + "Documents" + File.separator + APP_NAME);
    private static final File CONFIG_FILE = new File(CONFIG_DIR, CONFIG_FILE_NAME);

    private static final String MARKER_COMMENT = "MeDocPiece marker A1TVGS34FB";
    private static final SecureRandom random = new SecureRandom();

    private JTextField filePathField;
    private JButton browseButton;
    private JButton showPathButton;
    private JList<MethodCheckBox> methodList;
    private DefaultListModel<MethodCheckBox> listModel;
    private JButton selectAllButton;
    private JButton deselectAllButton;
    private JButton invertSelectionButton;
    private JButton processButton;
    private JLabel statusLabel;
    private JSpinner fontSizeSpinner;
    private JSpinner messageFontSizeSpinner;
    private JComboBox<String> sortCombo;

    private File selectedFile;
    private List<MethodCheckBox> originalMethodCheckBoxes = new ArrayList<>();

    private int currentFontSize = 12;
    private int currentMessageFontSize = 24;

    public MethodMessenger() {
        initUI();
        loadConfig();
        updateProcessButtonText();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveConfig();
            }
        });
    }

    private void initUI() {
        setTitle(APP_NAME);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Верхняя панель
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(new JLabel("Путь к файлу *.java:"), BorderLayout.WEST);
        filePathField = new JTextField();
        filePathField.setEditable(false);
        topPanel.add(filePathField, BorderLayout.CENTER);

        JPanel fileButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        browseButton = new JButton("Обзор...");
        browseButton.addActionListener(new BrowseAction());
        fileButtonsPanel.add(browseButton);
        showPathButton = new JButton("📁");
        showPathButton.setToolTipText("Открыть папку с конфигами");
        showPathButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(CONFIG_DIR);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Не удалось открыть папку: " + ex.getMessage());
            }
        });
        fileButtonsPanel.add(showPathButton);
        topPanel.add(fileButtonsPanel, BorderLayout.EAST);

        // Панель настроек
        JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        settingsPanel.add(new JLabel("Шрифт списка:"));
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(currentFontSize, 8, 32, 1));
        fontSizeSpinner.addChangeListener(e -> {
            currentFontSize = (int) fontSizeSpinner.getValue();
            updateFontSize(currentFontSize);
            saveConfig();
        });
        settingsPanel.add(fontSizeSpinner);
        settingsPanel.add(new JLabel("Шрифт сообщений:"));
        messageFontSizeSpinner = new JSpinner(new SpinnerNumberModel(currentMessageFontSize, 8, 72, 1));
        messageFontSizeSpinner.addChangeListener(e -> {
            currentMessageFontSize = (int) messageFontSizeSpinner.getValue();
            saveConfig();
        });
        settingsPanel.add(messageFontSizeSpinner);
        settingsPanel.add(new JLabel("Сортировка:"));
        sortCombo = new JComboBox<>(new String[]{"По порядку", "По имени", "С маркером", "По весу"});
        sortCombo.addActionListener(e -> sortMethodList());
        settingsPanel.add(sortCombo);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(topPanel, BorderLayout.NORTH);
        northPanel.add(settingsPanel, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        // Список методов
        listModel = new DefaultListModel<>();
        methodList = new JList<>(listModel);
        methodList.setCellRenderer(new MethodListRenderer());
        methodList.addMouseListener(new MethodCheckBoxMouseListener());
        JScrollPane scrollPane = new JScrollPane(methodList);
        scrollPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        add(scrollPane, BorderLayout.CENTER);

        // Нижняя панель
        JPanel bottomPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        JPanel controlPanel = new JPanel();
        selectAllButton = new JButton("Выбрать всё");
        selectAllButton.addActionListener(e -> { setAllCheckBoxes(true); updateProcessButtonText(); });
        deselectAllButton = new JButton("Снять всё");
        deselectAllButton.addActionListener(e -> { setAllCheckBoxes(false); updateProcessButtonText(); });
        invertSelectionButton = new JButton("Инверсия выбора");
        invertSelectionButton.addActionListener(e -> { invertSelection(); updateProcessButtonText(); });
        controlPanel.add(selectAllButton);
        controlPanel.add(deselectAllButton);
        controlPanel.add(invertSelectionButton);
        processButton = new JButton("Добавить сообщения");
        processButton.addActionListener(new ProcessAction());
        processButton.setFont(processButton.getFont().deriveFont(Font.BOLD, 14));
        statusLabel = new JLabel("Готов");
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        bottomPanel.add(controlPanel);
        bottomPanel.add(processButton);
        bottomPanel.add(statusLabel);
        add(bottomPanel, BorderLayout.SOUTH);

        setSize(700, 600);
        setLocationRelativeTo(null);
    }

    private void updateProcessButtonText() {
        boolean anySelected = originalMethodCheckBoxes.stream().anyMatch(MethodCheckBox::isSelected);
        processButton.setText(anySelected ? "Добавить сообщения" : "Убрать все сообщения");
    }

    private void updateFontSize(int newSize) {
        for (int i = 0; i < listModel.size(); i++) {
            listModel.get(i).setFontSize(newSize);
        }
        methodList.repaint();
    }

    private void loadConfig() {
        Properties props = new Properties();
        if (CONFIG_FILE.exists()) {
            try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
                props.load(fis);
            } catch (IOException ignored) {}
        }
        try {
            currentFontSize = Integer.parseInt(props.getProperty("font.size", "12"));
            fontSizeSpinner.setValue(currentFontSize);
        } catch (NumberFormatException ignored) {}
        try {
            currentMessageFontSize = Integer.parseInt(props.getProperty("message.font.size", "24"));
            messageFontSizeSpinner.setValue(currentMessageFontSize);
        } catch (NumberFormatException ignored) {}
        String lastPath = props.getProperty("last.file.path");
        if (lastPath != null) {
            File f = new File(lastPath);
            if (f.exists() && f.isFile() && f.getName().endsWith(".java")) {
                selectedFile = f;
                filePathField.setText(f.getAbsolutePath());
                loadMethodsFromFile(f);
            }
        }
    }

    private void saveConfig() {
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
        Properties props = new Properties();
        if (selectedFile != null) {
            props.setProperty("last.file.path", selectedFile.getAbsolutePath());
        }
        props.setProperty("font.size", String.valueOf(currentFontSize));
        props.setProperty("message.font.size", String.valueOf(currentMessageFontSize));
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Конфигурация для " + APP_NAME);
        } catch (IOException ignored) {}
    }

    private void loadMethodsFromFile(File file) {
    listModel.clear();
    originalMethodCheckBoxes.clear();
    try {
        CompilationUnit cu;
        try {
            cu = StaticJavaParser.parse(file);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Файл не является синтаксически корректным Java-кодом или содержит конструкции, не поддерживаемые парсером.\n\n" +
                "Подробности:\n" + e.getMessage(),
                "Ошибка чтения файла",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
        if (methods.isEmpty()) {
            JOptionPane.showMessageDialog(this, "В файле не найдено методов.", "Информация", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Properties savedStates = FileStateManager.loadCheckBoxStates(file);
        int order = 0;
        for (MethodDeclaration method : methods) {
            String methodName = method.getNameAsString();
            boolean hasMarker = methodHasMarker(method);
            MethodInfo info = new MethodInfo(methodName, method, hasMarker, order++);
            MethodCheckBox mcb = new MethodCheckBox(info);
            mcb.setSelected(savedStates.getProperty(methodName, "true").equals("true"));
            originalMethodCheckBoxes.add(mcb);
        }
        sortMethodList();
        updateFontSize(currentFontSize);
        updateProcessButtonText();
        if (originalMethodCheckBoxes.size() == 1) {
            processSingleMethod(file);
        }
    } catch (Exception e) {
        // На случай других ошибок, например, при чтении файла
        JOptionPane.showMessageDialog(this, "Ошибка при загрузке методов: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
    }
}

    private void sortMethodList() {
        List<MethodCheckBox> sorted = new ArrayList<>(originalMethodCheckBoxes);
        String criterion = (String) sortCombo.getSelectedItem();
        if ("По имени".equals(criterion)) {
            sorted.sort(Comparator.comparing(mcb -> mcb.methodInfo.name));
        } else if ("С маркером".equals(criterion)) {
            sorted.sort((a, b) -> Boolean.compare(b.methodInfo.hasMarker, a.methodInfo.hasMarker));
        } else if ("По весу".equals(criterion)) {
            sorted.sort(Comparator.comparingInt(mcb -> mcb.methodInfo.weight));
        } else {
            sorted.sort(Comparator.comparingInt(mcb -> mcb.methodInfo.order));
        }
        listModel.clear();
        sorted.forEach(listModel::addElement);
    }

    private boolean methodHasMarker(MethodDeclaration method) {
        return method.getAllContainedComments().stream()
                .anyMatch(c -> c.getContent().contains(MARKER_COMMENT));
    }

    private void processSingleMethod(File file) {
        MethodCheckBox mcb = originalMethodCheckBoxes.get(0);
        try {
            insertOrRemoveMessageCode(file, mcb.methodInfo, mcb.isSelected());
            refreshMethodMarker(file, mcb.methodInfo);
            JOptionPane.showMessageDialog(this, "Готово", "Успех", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void insertOrRemoveMessageCode(File file, MethodInfo methodInfo, boolean insert) throws IOException {
    String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    String lineSeparator = detectLineSeparator(content);

    CompilationUnit cu;
    try {
        cu = StaticJavaParser.parse(file);
    } catch (Exception e) {
        throw new IOException("Файл содержит синтаксические ошибки, не удалось обработать метод " + methodInfo.name + ": " + e.getMessage());
    }

    MethodDeclaration method = cu.findAll(MethodDeclaration.class).stream()
            .filter(m -> m.getNameAsString().equals(methodInfo.name))
            .findFirst()
            .orElseThrow(() -> new IOException("Метод не найден"));

    BlockStmt body = method.getBody().orElseThrow(() -> new IOException("Нет тела метода"));
    Optional<Position> bodyStart = body.getBegin();
    Optional<Position> bodyEnd = body.getEnd();
    if (bodyStart.isEmpty() || bodyEnd.isEmpty()) {
        throw new IOException("Не удалось получить границы тела метода");
    }

    int startOffset = getOffsetFromPosition(content, lineSeparator, bodyStart.get());
    int endOffset = getOffsetFromPosition(content, lineSeparator, bodyEnd.get());

    String beforeBody = content.substring(0, startOffset);
    String afterBody = content.substring(endOffset);
    String bodyText = content.substring(startOffset, endOffset);

    // Удаляем старый маркерный блок
    bodyText = removeMarkedBlock(bodyText);

    if (!insert) {
        // Удаление: вычищаем все пробелы/пустые строки между '{' и следующим кодом
        bodyText = removeLeadingWhitespaceAfterBrace(bodyText, lineSeparator);
        String newContent = beforeBody + bodyText + afterBody;
        Files.write(file.toPath(), newContent.getBytes(StandardCharsets.UTF_8));
        ensureImportsInFile(file);
        return;
    }

    // Вставка
    String uniqueVar = generateUniqueVariableName(methodInfo.name);
    String block = 
        "// " + MARKER_COMMENT + " start" + lineSeparator +
        "JLabel " + uniqueVar + " = new JLabel(\"Вызов " + methodInfo.name + "\");" + lineSeparator +
        uniqueVar + ".setFont(new Font(\"Dialog\", Font.PLAIN, " + currentMessageFontSize + "));" + lineSeparator +
        "JOptionPane.showMessageDialog(null, " + uniqueVar + ", \"Заголовок\", JOptionPane.PLAIN_MESSAGE);" + lineSeparator +
        "// " + MARKER_COMMENT + " end";

    // Определяем отступ первой строки тела (после '{')
    String indentation = "";
    String afterOpenBrace = bodyText.substring(1); // после '{'
    int firstNewLine = afterOpenBrace.indexOf(lineSeparator);
    if (firstNewLine != -1) {
        String firstStmtLine = afterOpenBrace.substring(firstNewLine + lineSeparator.length());
        indentation = getLeadingWhitespace(firstStmtLine);
    }
    String indentedBlock = addIndentation(block, indentation, lineSeparator);

    // Собираем новое тело: блок сразу после '{', затем обязательный перевод строки, затем остальной код
    String newBodyText = "{" + lineSeparator + indentedBlock + lineSeparator + afterOpenBrace;
    newBodyText = removeLeadingWhitespaceAfterBrace(newBodyText, lineSeparator);

    String newContent = beforeBody + newBodyText + afterBody;
    Files.write(file.toPath(), newContent.getBytes(StandardCharsets.UTF_8));
    ensureImportsInFile(file);
}

/**
 * Удаляет все пробельные символы между '{' и первой значимой строкой,
 * оставляя ровно один lineSeparator.
 */
private String removeLeadingWhitespaceAfterBrace(String bodyText, String separator) {
    int bracePos = bodyText.indexOf('{');
    if (bracePos == -1) return bodyText;
    String afterBrace = bodyText.substring(bracePos + 1);
    if (afterBrace.startsWith(separator)) {
        afterBrace = afterBrace.substring(separator.length());
        // Убираем все пробелы/пустые строки в начале оставшейся части
        afterBrace = afterBrace.replaceFirst("^[ \\t\\r\\n]*", "");
        return bodyText.substring(0, bracePos + 1) + separator + afterBrace;
    } else {
        afterBrace = afterBrace.replaceFirst("^[ \\t]*", "");
        return bodyText.substring(0, bracePos + 1) + afterBrace;
    }
}

    // Удаляет блок между маркерами start и end
    private String removeMarkedBlock(String bodyText) {
        String startMarker = "// " + MARKER_COMMENT + " start";
        String endMarker = "// " + MARKER_COMMENT + " end";
        int startIdx = bodyText.indexOf(startMarker);
        while (startIdx != -1) {
            int endIdx = bodyText.indexOf(endMarker, startIdx);
            if (endIdx != -1) {
                // Конец строки с endMarker
                int endLineEnd = bodyText.indexOf('\n', endIdx);
                if (endLineEnd == -1) endLineEnd = bodyText.length() - 1;
                bodyText = bodyText.substring(0, startIdx) + bodyText.substring(endLineEnd + 1);
            } else {
                break;
            }
            startIdx = bodyText.indexOf(startMarker);
        }
        return bodyText;
    }


    // Вспомогательные методы
    private int getOffsetFromPosition(String content, String separator, Position pos) {
        int line = pos.line;
        int column = pos.column;
        String[] lines = content.split(separator, -1);
        int offset = 0;
        for (int i = 0; i < line - 1 && i < lines.length; i++) {
            offset += lines[i].length() + separator.length();
        }
        offset += column - 1;
        return offset;
    }

    private String detectLineSeparator(String content) {
        if (content.contains("\r\n")) return "\r\n";
        else if (content.contains("\n")) return "\n";
        else return System.lineSeparator();
    }

    private String getLeadingWhitespace(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (c == ' ' || c == '\t') sb.append(c);
            else break;
        }
        return sb.toString();
    }

    private String addIndentation(String block, String indent, String separator) {
        StringBuilder sb = new StringBuilder();
        String[] lines = block.split(separator, -1);
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].trim().isEmpty()) {
                sb.append(indent);
            }
            sb.append(lines[i]);
            if (i < lines.length - 1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    private void ensureImportsInFile(File file) throws IOException {
        String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        String[] requiredImports = {
            "import javax.swing.JLabel;",
            "import java.awt.Font;",
            "import javax.swing.JOptionPane;"
        };
        boolean changed = false;
        for (String imp : requiredImports) {
            if (!content.contains(imp)) {
                int insertPos = findLastImportPosition(content);
                if (insertPos == -1) {
                    int pkgEnd = content.indexOf(';');
                    if (pkgEnd != -1) {
                        insertPos = pkgEnd + 1;
                    } else {
                        insertPos = 0;
                    }
                }
                String separator = detectLineSeparator(content);
                content = content.substring(0, insertPos) + separator + imp + content.substring(insertPos);
                changed = true;
            }
        }
        if (changed) {
            Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
        }
    }

    private int findLastImportPosition(String content) {
        int lastImportEnd = -1;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("^import\\s+[^;]+;", java.util.regex.Pattern.MULTILINE);
        java.util.regex.Matcher m = p.matcher(content);
        while (m.find()) {
            lastImportEnd = m.end();
        }
        return lastImportEnd;
    }

    private String generateUniqueVariableName(String baseName) {
        StringBuilder suffix = new StringBuilder(4);
        for (int i = 0; i < 2; i++) suffix.append((char) ('0' + random.nextInt(10)));
        for (int i = 0; i < 2; i++) suffix.append((char) ('a' + random.nextInt(26)));
        return "msg_" + baseName.replaceAll("[^a-zA-Z0-9]", "_") + "_" + suffix;
    }

    private void refreshMethodMarker(File file, MethodInfo methodInfo) throws IOException {
        CompilationUnit cu = StaticJavaParser.parse(file);
        boolean hasMarker = cu.findAll(MethodDeclaration.class).stream()
                .filter(m -> m.getNameAsString().equals(methodInfo.name))
                .anyMatch(this::methodHasMarker);
        methodInfo.hasMarker = hasMarker;
        originalMethodCheckBoxes.stream()
                .filter(mcb -> mcb.methodInfo.name.equals(methodInfo.name))
                .findFirst().ifPresent(mcb -> mcb.updateMarkerStatus(hasMarker));
        methodList.repaint();
    }

    private void setAllCheckBoxes(boolean selected) {
        originalMethodCheckBoxes.forEach(mcb -> mcb.setSelected(selected));
        methodList.repaint();
    }

    private void invertSelection() {
        originalMethodCheckBoxes.forEach(mcb -> mcb.setSelected(!mcb.isSelected()));
        methodList.repaint();
    }

    private class MethodCheckBoxMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int index = methodList.locationToIndex(e.getPoint());
            if (index >= 0 && methodList.getCellBounds(index, index).contains(e.getPoint())) {
                MethodCheckBox mcb = listModel.get(index);
                mcb.setSelected(!mcb.isSelected());
                methodList.repaint();
                updateProcessButtonText();
            }
        }
    }

    private class BrowseAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser(selectedFile != null ? selectedFile.getParentFile() :
                    new File(System.getProperty("user.home") + File.separator + "Documents"));
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Java файлы", "java"));
            if (chooser.showOpenDialog(MethodMessenger.this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (file != null && file.getName().endsWith(".java")) {
                    selectedFile = file;
                    filePathField.setText(file.getAbsolutePath());
                    loadMethodsFromFile(file);
                    saveConfig();
                }
            }
        }
    }

    private class ProcessAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedFile == null) {
                JOptionPane.showMessageDialog(MethodMessenger.this, "Сначала выберите файл.");
                return;
            }
            processButton.setEnabled(false);
            statusLabel.setText("Обработка...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    for (MethodCheckBox mcb : originalMethodCheckBoxes) {
                        insertOrRemoveMessageCode(selectedFile, mcb.methodInfo, mcb.isSelected());
                    }
                    return null;
                }
                @Override
                protected void done() {
                    try {
                        get();
                        originalMethodCheckBoxes.forEach(mcb -> {
                            try { refreshMethodMarker(selectedFile, mcb.methodInfo); } catch (IOException ex) {}
                        });
                        Properties states = new Properties();
                        originalMethodCheckBoxes.forEach(mcb -> states.setProperty(mcb.methodInfo.name, String.valueOf(mcb.isSelected())));
                        FileStateManager.saveCheckBoxStates(selectedFile, states);
                        JOptionPane.showMessageDialog(MethodMessenger.this, "Готово");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MethodMessenger.this, "Ошибка: " + ex.getMessage());
                    } finally {
                        processButton.setEnabled(true);
                        statusLabel.setText("Готов");
                        setCursor(Cursor.getDefaultCursor());
                        updateProcessButtonText();
                    }
                }
            };
            worker.execute();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
            new MethodMessenger().setVisible(true);
        });
    }
}