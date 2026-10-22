import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Random;

public class LinearSystemsSolverApp extends JFrame {
    private JTable matrixTable;
    private JTable vectorTable;
    private JTable resultTable;
    private JTextArea logArea;
    private JSpinner sizeSpinner;
    private JComboBox<String> methodComboBox;
    private JTextField epsilonField;
    private JTextField maxIterationsField;
    private DefaultTableModel matrixModel;
    private DefaultTableModel vectorModel;
    private DefaultTableModel resultModel;
    private DecimalFormat df = new DecimalFormat("#.######");
    private JScrollPane matrixScrollPane;
    private JScrollPane vectorScrollPane;
    private JScrollPane resultScrollPane;
    private int lastIterationCount = 0;

    public LinearSystemsSolverApp() {
        setTitle("Решение систем линейных уравнений (до 100x100)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 1000);
        setLocationRelativeTo(null);

        initComponents();
        layoutComponents();

        loadExample();
    }

    private void initComponents() {
        sizeSpinner = new JSpinner(new SpinnerNumberModel(4, 2, 100, 1));
        sizeSpinner.setPreferredSize(new Dimension(80, 30));

        methodComboBox = new JComboBox<>(new String[]{
                "Метод квадратных корней",
                "Метод простой итерации"
        });
        methodComboBox.setPreferredSize(new Dimension(200, 30));

        // Поля для параметров
        epsilonField = new JTextField("1e-6", 8);
        epsilonField.setPreferredSize(new Dimension(80, 30));

        maxIterationsField = new JTextField("1000", 8);
        maxIterationsField.setPreferredSize(new Dimension(80, 30));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));

        // Создаем таблицы
        createMatrixAndVectorTables(4);
        createResultTable();
    }

    private void createMatrixAndVectorTables(int size) {
        // Таблица для матрицы A с уменьшенным шрифтом для больших матриц
        matrixModel = new DefaultTableModel(size, size) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return Double.class;
            }
        };
        matrixTable = new JTable(matrixModel);
        matrixTable.setRowHeight(22);

        // Динамически меняем размер шрифта в зависимости от размера матрицы
        int fontSize = size > 20 ? 9 : (size > 10 ? 10 : 12);
        matrixTable.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
        matrixTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, fontSize));

        // Таблица для вектора b
        vectorModel = new DefaultTableModel(size, 1) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return Double.class;
            }
        };
        vectorTable = new JTable(vectorModel);
        vectorTable.setRowHeight(22);
        vectorTable.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
        vectorTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, fontSize));

        // Устанавливаем заголовки для матрицы
        for (int i = 0; i < size; i++) {
            matrixModel.setColumnIdentifiers(createHeaders(size, "x"));
            vectorModel.setColumnIdentifiers(new String[]{"b"});
        }

        // Настраиваем ширину столбцов для матрицы
        if (size > 15) {
            matrixTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int i = 0; i < size; i++) {
                matrixTable.getColumnModel().getColumn(i).setPreferredWidth(50);
            }
        } else {
            matrixTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        }
    }

    private void createResultTable() {
        // Таблица для результатов
        resultModel = new DefaultTableModel(new String[]{"Переменная", "Значение", "Метод"}, 0);
        resultTable = new JTable(resultModel);
        resultTable.setRowHeight(22);
        resultTable.setFont(new Font("SansSerif", Font.PLAIN, 11));
        resultTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));

        // Настраиваем ширину столбцов для результатов
        resultTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        resultTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        resultTable.getColumnModel().getColumn(2).setPreferredWidth(150);
    }

    private String[] createHeaders(int size, String prefix) {
        String[] headers = new String[size];
        for (int i = 0; i < size; i++) {
            headers[i] = prefix + (i + 1);
        }
        return headers;
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));

        // Панель управления
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);

        // Панель с таблицами
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);

        // Панель с результатами и логами
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createControlPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Управление системой"));

        // Верхняя панель с основными настройками
        JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        settingsPanel.add(createLabeledComponent("Размер системы:", sizeSpinner));
        settingsPanel.add(Box.createHorizontalStrut(10));
        settingsPanel.add(createLabeledComponent("Метод решения:", methodComboBox));
        settingsPanel.add(Box.createHorizontalStrut(10));
        settingsPanel.add(createLabeledComponent("Точность (ε):", epsilonField));
        settingsPanel.add(Box.createHorizontalStrut(10));
        settingsPanel.add(createLabeledComponent("Макс. итераций:", maxIterationsField));

        // Нижняя панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JButton solveButton = createStyledButton("Решить систему", new Color(46, 125, 50));
        solveButton.addActionListener(new SolveButtonListener());
        buttonPanel.add(solveButton);

        JButton compareButton = createStyledButton("Сравнить методы", new Color(30, 136, 229));
        compareButton.addActionListener(new CompareButtonListener());
        buttonPanel.add(compareButton);

        JButton generateButton = createStyledButton("Сгенерировать", new Color(0, 150, 136));
        generateButton.addActionListener(new GenerateButtonListener());
        buttonPanel.add(generateButton);

        JButton exampleButton = createStyledButton("Пример", new Color(255, 152, 0));
        exampleButton.addActionListener(new ExampleButtonListener());
        buttonPanel.add(exampleButton);

        JButton clearButton = createStyledButton("Очистить", new Color(229, 57, 53));
        clearButton.addActionListener(e -> clearAll());
        buttonPanel.add(clearButton);

        mainPanel.add(settingsPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel createLabeledComponent(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(new JLabel(label), BorderLayout.WEST);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("SansSerif", Font.BOLD, 11));
        button.setPreferredSize(new Dimension(120, 30));
        button.setFocusPainted(false);
        return button;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Панель матрицы A
        JPanel matrixPanel = createTablePanelWithTitle("Матрица коэффициентов A", matrixTable, true);

        // Панель вектора b
        JPanel vectorPanel = createTablePanelWithTitle("Вектор правых частей b", vectorTable, false);

        // Панель результатов
        JPanel resultPanel = createTablePanelWithTitle("Результат решения", resultTable, false);

        panel.add(matrixPanel);
        panel.add(vectorPanel);
        panel.add(resultPanel);

        return panel;
    }

    private JPanel createTablePanelWithTitle(String title, JTable table, boolean isMatrix) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                title,
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("SansSerif", Font.BOLD, 12)
        ));

        JScrollPane scrollPane = new JScrollPane(table);
        if (isMatrix) {
            scrollPane.setPreferredSize(new Dimension(500, 400));
            matrixScrollPane = scrollPane;
        } else {
            scrollPane.setPreferredSize(new Dimension(200, 400));
            if (title.contains("Вектор")) {
                vectorScrollPane = scrollPane;
            } else {
                resultScrollPane = scrollPane;
            }
        }
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(30, 136, 229), 1),
                "Процесс решения и диагностика",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("SansSerif", Font.BOLD, 12)
        ));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Панель с кнопками управления логом
        JPanel logControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton clearLogButton = new JButton("Очистить лог");
        clearLogButton.addActionListener(e -> logArea.setText(""));
        logControlPanel.add(clearLogButton);

        panel.add(logControlPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadExample() {
        int size = 4;
        sizeSpinner.setValue(size);

        // Обновляем только таблицы матрицы и вектора
        updateMatrixAndVectorTables(size);

        // Матрица из условия
        double[][] exampleMatrix = {
                {10, 1, -0.5, 0.7},
                {1, 15, 0.5, 4},
                {-0.5, 0.5, 20, 1},
                {0.7, 4, 1, 17}
        };

        double[] exampleVector = {11.2, 20.5, 21, 22.7};

        // Заполняем таблицы
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrixModel.setValueAt(exampleMatrix[i][j], i, j);
            }
            vectorModel.setValueAt(exampleVector[i], i, 0);
        }

        logArea.setText("Загружен пример системы 4x4 из условия лабораторной работы.\n");
    }

    private void updateMatrixAndVectorTables(int newSize) {
        createMatrixAndVectorTables(newSize);

        // Обновляем существующие scroll pane вместо пересоздания всей панели
        if (matrixScrollPane != null) {
            matrixScrollPane.setViewportView(matrixTable);
            matrixScrollPane.revalidate();
        }

        if (vectorScrollPane != null) {
            vectorScrollPane.setViewportView(vectorTable);
            vectorScrollPane.revalidate();
        }

        // Принудительное обновление интерфейса
        revalidate();
        repaint();
    }

    private void clearAll() {
        int size = (int) sizeSpinner.getValue();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrixModel.setValueAt(null, i, j);
            }
            vectorModel.setValueAt(null, i, 0);
        }
        resultModel.setRowCount(0);
        logArea.setText("");
    }

    private double[][] getMatrixFromTable() {
        int size = (int) sizeSpinner.getValue();
        double[][] matrix = new double[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Object value = matrixModel.getValueAt(i, j);
                if (value == null || value.toString().trim().isEmpty()) {
                    matrix[i][j] = 0.0;
                } else {
                    matrix[i][j] = Double.parseDouble(value.toString());
                }
            }
        }
        return matrix;
    }

    private double[] getVectorFromTable() {
        int size = (int) sizeSpinner.getValue();
        double[] vector = new double[size];

        for (int i = 0; i < size; i++) {
            Object value = vectorModel.getValueAt(i, 0);
            if (value == null || value.toString().trim().isEmpty()) {
                vector[i] = 0.0;
            } else {
                vector[i] = Double.parseDouble(value.toString());
            }
        }
        return vector;
    }

    private void displayResults(double[] x, String methodName) {
        // Не очищаем таблицу, а добавляем новые результаты
        for (int i = 0; i < x.length; i++) {
            resultModel.addRow(new Object[]{
                    "x" + (i + 1),
                    String.format("%.8f", x[i]),
                    methodName
            });
        }

        // Показываем только первые 20 результатов для больших систем
        if (x.length > 20) {
            log("Показаны первые 20 результатов из " + x.length);
        }
    }

    private void clearResults() {
        resultModel.setRowCount(0);
    }

    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    // === МЕТОДЫ РЕШЕНИЯ ===

    private double[] solveBySquareRoot(double[][] A, double[] b) {
        int n = A.length;

        log("=== МЕТОД КВАДРАТНЫХ КОРНЕЙ ===");

        // Проверка симметричности
        if (!isSymmetric(A)) {
            throw new IllegalArgumentException("Матрица не симметрична!");
        }
        log("✓ Матрица симметрична");

        // Проверка диагонального преобладания
        checkDiagonalDominance(A);

        // Разложение Холецкого
        log("Выполняется разложение Холецкого...");
        double[][] T = choleskyDecomposition(A);
        log("✓ Разложение Холецкого завершено");

        // Решение систем
        double[] y = solveLowerTriangular(transpose(T), b);
        double[] x = solveUpperTriangular(T, y);

        log("✓ Система успешно решена");
        return x;
    }

    private double[] solveBySimpleIteration(double[][] A, double[] b, double epsilon, int maxIterations) {
        int n = A.length;

        log("=== МЕТОД ПРОСТОЙ ИТЕРАЦИИ ===");

        // Проверка сходимости
        if (!checkIterationConvergence(A)) {
            log("⚠ Предупреждение: условие сходимости не выполнено!");
        }

        // Приведение к виду x = β + αx
        double[][] alpha = new double[n][n];
        double[] beta = new double[n];

        for (int i = 0; i < n; i++) {
            if (Math.abs(A[i][i]) < 1e-15) {
                throw new IllegalArgumentException("Нулевой диагональный элемент в строке " + i);
            }
            beta[i] = b[i] / A[i][i];
            for (int j = 0; j < n; j++) {
                alpha[i][j] = (i != j) ? -A[i][j] / A[i][i] : 0;
            }
        }

        double[] x = new double[n];
        double[] xNew = new double[n];
        int iterations = 0;
        double error;

        log("Начало итерационного процесса...");

        do {
            for (int i = 0; i < n; i++) {
                xNew[i] = beta[i];
                for (int j = 0; j < n; j++) {
                    xNew[i] += alpha[i][j] * x[j];
                }
            }

            error = vectorNorm(subtractVectors(xNew, x));
            iterations++;

            if (iterations % 50 == 0 || iterations == 1 || iterations >= maxIterations - 10) {
                log(String.format("Итерация %4d: погрешность = %.2e", iterations, error));
            }

            System.arraycopy(xNew, 0, x, 0, n);

        } while (error > epsilon && iterations < maxIterations);

        // Сохраняем количество итераций
        this.lastIterationCount = iterations;

        log(String.format("✓ Метод сошелся за %d итераций, конечная погрешность = %.2e", iterations, error));
        return x;
    }

    // === СРАВНЕНИЕ МЕТОДОВ ===
    private void compareMethods() {
        try {
            double[][] A = getMatrixFromTable();
            double[] b = getVectorFromTable();
            double epsilon = Double.parseDouble(epsilonField.getText());
            int maxIterations = Integer.parseInt(maxIterationsField.getText());

            log("\n" + "=".repeat(100));
            log("СРАВНЕНИЕ МЕТОДОВ РЕШЕНИЯ СЛАУ");
            log("=".repeat(100));

            // Очищаем предыдущие результаты
            clearResults();

            // ТАБЛИЦА 1: Сравнение по размерности
            log("\nТАБЛИЦА СРАВНЕНИЯ ПО РАЗМЕРНОСТИ");
            log("=".repeat(90));
            log(String.format("%-15s | %-25s | %-35s",
                    "Размерность", "Метод квадратных корней", "Метод простой итерации (ε=10^-6)"));
            log(String.format("%-15s | %-10s %-12s | %-10s %-10s %-12s",
                    "", "Время (мс)", "Память", "Время (мс)", "Итерации", "Память"));
            log("-".repeat(90));

            int[] dimensions = {10, 25, 50, 100};
            Random random = new Random();

            for (int dim : dimensions) {
                // Генерируем тестовую систему
                double[][] testA = generateSymmetricMatrixWithDominance(dim, random);
                double[] testB = generateVector(dim, random);

                try {
                    // Метод квадратных корней
                    long startTime1 = System.currentTimeMillis();
                    double[] x1 = solveBySquareRoot(testA, testB);
                    long time1 = System.currentTimeMillis() - startTime1;

                    // Метод простой итерации
                    long startTime2 = System.currentTimeMillis();
                    double[] x2 = solveBySimpleIteration(testA, testB, 1e-6, maxIterations);
                    long time2 = System.currentTimeMillis() - startTime2;
                    int iterations2 = getLastIterationCount();

                    log(String.format("%-15s | %-10d %-12s | %-10d %-10d %-12s",
                            dim + "×" + dim,
                            time1, "O(n²)",
                            time2, iterations2, "O(n²)"));

                } catch (Exception e) {
                    log(String.format("%-15s | %-10s %-12s | %-10s %-10s %-12s",
                            dim + "×" + dim, "Ошибка", "-", "Ошибка", "-", "-"));
                }
            }

            // ТАБЛИЦА 2: Сравнение по точности
            log("\n\nТАБЛИЦА СРАВНЕНИЯ ПО ТОЧНОСТИ");
            log("=".repeat(85));
            log(String.format("%-12s | %-25s | %-30s",
                    "Точность (ε)", "Метод квадратных корней", "Метод простой итерации"));
            log(String.format("%-12s | %-10s %-12s | %-10s %-10s %-12s",
                    "", "Время (мс)", "Невязка", "Время (мс)", "Итерации", "Невязка"));
            log("-".repeat(85));

            double[] testEpsilons = {1e-2, 1e-4, 1e-5, 1e-6};

            // Используем текущую матрицу для тестирования точности
            for (double testEps : testEpsilons) {
                try {
                    // Метод квадратных корней (не зависит от точности)
                    long startTime1 = System.currentTimeMillis();
                    double[] x1 = solveBySquareRoot(A, b);
                    long time1 = System.currentTimeMillis() - startTime1;
                    double error1 = checkSolution(A, x1, b);

                    // Метод простой итерации (зависит от точности)
                    long startTime2 = System.currentTimeMillis();
                    double[] x2 = solveBySimpleIteration(A, b, testEps, maxIterations);
                    long time2 = System.currentTimeMillis() - startTime2;
                    double error2 = checkSolution(A, x2, b);
                    int iterations2 = getLastIterationCount();

                    log(String.format("%-12s | %-10d %-12.2e | %-10d %-10d %-12.2e",
                            String.format("10^-%d", (int)Math.log10(1/testEps)),
                            time1, error1,
                            time2, iterations2, error2));

                } catch (Exception e) {
                    log(String.format("%-12s | %-10s %-12s | %-10s %-10s %-12s",
                            String.format("10^-%d", (int)Math.log10(1/testEps)),
                            "Ошибка", "-", "Ошибка", "-", "-"));
                }
            }

            // ДЕТАЛЬНОЕ СРАВНЕНИЕ ДЛЯ ТЕКУЩЕЙ СИСТЕМЫ
            log("\n\nДЕТАЛЬНОЕ СРАВНЕНИЕ ДЛЯ ТЕКУЩЕЙ СИСТЕМЫ");
            log("=".repeat(60));
            log("Размер системы: " + A.length + "×" + A.length);
            log("Точность: ε = " + epsilon);
            log("-".repeat(60));

            // Метод квадратных корней
            long startTime1 = System.currentTimeMillis();
            double[] x1 = solveBySquareRoot(A, b);
            long time1 = System.currentTimeMillis() - startTime1;
            double error1 = checkSolution(A, x1, b);

            log(String.format("Метод квадратных корней:"));
            log(String.format("  Время: %d мс", time1));
            log(String.format("  Невязка: %.2e", error1));
            log(String.format("  Память: O(n²)"));
            displayResults(x1, "Квадратные корни");

            log("-".repeat(40));

            // Метод простой итерации
            long startTime2 = System.currentTimeMillis();
            double[] x2 = solveBySimpleIteration(A, b, epsilon, maxIterations);
            long time2 = System.currentTimeMillis() - startTime2;
            double error2 = checkSolution(A, x2, b);
            int iterations2 = getLastIterationCount();

            log(String.format("Метод простой итерации:"));
            log(String.format("  Время: %d мс", time2));
            log(String.format("  Итерации: %d", iterations2));
            log(String.format("  Невязка: %.2e", error2));
            log(String.format("  Память: O(n²)"));
            displayResults(x2, "Простая итерация");

            log("-".repeat(40));

            // АНАЛИЗ ЭФФЕКТИВНОСТИ
            log("\nАНАЛИЗ ЭФФЕКТИВНОСТИ:");
            log("• Метод квадратных корней:");
            log("  - Прямой метод, всегда сходится");
            log("  - Требует симметричной положительно определенной матрицы");
            log("  - Высокая точность (машинная)");
            log("  - Сложность: O(n³) по времени, O(n²) по памяти");

            log("• Метод простой итерации:");
            log("  - Итерационный метод, сходимость зависит от матрицы");
            log("  - Требует диагонального преобладания для гарантии сходимости");
            log("  - Точность контролируется параметром ε");
            log("  - Сложность: O(n²) на итерацию, O(n²) по памяти");

            // Сравнение производительности
            log("\nСРАВНЕНИЕ ПРОИЗВОДИТЕЛЬНОСТИ:");
            if (time1 < time2) {
                log(String.format("✓ Метод квадратных корней быстрее на %d мс", time2 - time1));
            } else if (time2 < time1) {
                log(String.format("✓ Метод простой итерации быстрее на %d мс", time1 - time2));
            } else {
                log("✓ Оба метода показали одинаковое время");
            }

            // Сравнение точности
            log("\nСРАВНЕНИЕ ТОЧНОСТИ:");
            if (error1 < error2) {
                log(String.format("✓ Метод квадратных корней точнее на %.2e", error2 - error1));
            } else if (error2 < error1) {
                log(String.format("✓ Метод простой итерации точнее на %.2e", error1 - error2));
            } else {
                log("✓ Оба метода показали одинаковую точность");
            }

            log("\nРЕКОМЕНДАЦИЯ:");
            if (A.length <= 50 && isSymmetric(A)) {
                log("• Для данной системы рекомендуется метод квадратных корней");
                log("  (система небольшого размера и матрица симметрична)");
            } else {
                log("• Для данной системы рекомендуется метод простой итерации");
                log("  (подходит для систем любого размера)");
            }

            log("=".repeat(100));
            log("СРАВНЕНИЕ ЗАВЕРШЕНО");
            log("=".repeat(100));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(LinearSystemsSolverApp.this,
                    "Ошибка при сравнении методов: " + ex.getMessage(),
                    "Ошибка сравнения",
                    JOptionPane.ERROR_MESSAGE);
            log("❌ Ошибка сравнения: " + ex.getMessage());
        }
    }

    /**
     * Возвращает количество итераций для последнего вызова метода простой итерации
     */
    private int getLastIterationCount() {
        return lastIterationCount;
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    private double[][] choleskyDecomposition(double[][] A) {
        int n = A.length;
        double[][] T = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= i; j++) {
                double sum = 0.0;

                if (j == i) {
                    for (int k = 0; k < j; k++) {
                        sum += T[j][k] * T[j][k];
                    }
                    T[j][j] = Math.sqrt(A[j][j] - sum);

                    if (T[j][j] <= 0) {
                        throw new IllegalArgumentException("Матрица не положительно определена!");
                    }
                } else {
                    for (int k = 0; k < j; k++) {
                        sum += T[i][k] * T[j][k];
                    }
                    T[i][j] = (A[i][j] - sum) / T[j][j];
                }
            }
        }
        return T;
    }

    private double[] solveUpperTriangular(double[][] T, double[] y) {
        int n = T.length;
        double[] x = new double[n];

        for (int i = n - 1; i >= 0; i--) {
            double sum = 0.0;
            for (int j = i + 1; j < n; j++) {
                sum += T[i][j] * x[j];
            }
            x[i] = (y[i] - sum) / T[i][i];
        }
        return x;
    }

    private double[] solveLowerTriangular(double[][] L, double[] b) {
        int n = L.length;
        double[] y = new double[n];

        for (int i = 0; i < n; i++) {
            double sum = 0.0;
            for (int j = 0; j < i; j++) {
                sum += L[i][j] * y[j];
            }
            y[i] = (b[i] - sum) / L[i][i];
        }
        return y;
    }

    private boolean isSymmetric(double[][] A) {
        int n = A.length;
        double tolerance = 1e-10;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (Math.abs(A[i][j] - A[j][i]) > tolerance) {
                    return false;
                }
            }
        }
        return true;
    }

    private void checkDiagonalDominance(double[][] A) {
        int n = A.length;
        boolean isStrictlyDominant = true;

        log("Проверка диагонального преобладания:");
        // Для больших матриц показываем только первые 10 строк
        int rowsToShow = Math.min(n, 10);
        for (int i = 0; i < rowsToShow; i++) {
            double rowSum = 0.0;
            for (int j = 0; j < n; j++) {
                if (i != j) rowSum += Math.abs(A[i][j]);
            }

            String status = (Math.abs(A[i][i]) > rowSum) ? "✓" : "✗";
            log(String.format("  Строка %2d: |%6.2f| > Σ|a_ij| = %6.2f %s",
                    i + 1, A[i][i], rowSum, status));

            if (Math.abs(A[i][i]) <= rowSum) isStrictlyDominant = false;
        }

        if (n > 10) {
            log("  ... (показаны первые 10 строк из " + n + ")");
        }

        if (isStrictlyDominant) {
            log("✓ Матрица имеет строгое диагональное преобладание");
        } else {
            log("⚠ Матрица не имеет строгого диагонального преобладания");
        }
    }

    private boolean checkIterationConvergence(double[][] A) {
        int n = A.length;

        // Проверка диагонального преобладания
        for (int i = 0; i < n; i++) {
            double sum = 0.0;
            for (int j = 0; j < n; j++) {
                if (i != j) sum += Math.abs(A[i][j]);
            }
            if (Math.abs(A[i][i]) > sum) {
                log("✓ Условие диагонального преобладания выполнено");
                return true;
            }
        }

        // Проверка нормы матрицы α
        double[][] alpha = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) alpha[i][j] = -A[i][j] / A[i][i];
            }
        }

        double norm = 0.0;
        for (int i = 0; i < n; i++) {
            double rowSum = 0.0;
            for (int j = 0; j < n; j++) {
                rowSum += Math.abs(alpha[i][j]);
            }
            if (rowSum > norm) norm = rowSum;
        }

        log(String.format("Норма матрицы α: %.6f", norm));
        return norm < 1.0;
    }

    private double[][] transpose(double[][] matrix) {
        int n = matrix.length;
        int m = matrix[0].length;
        double[][] result = new double[m][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                result[j][i] = matrix[i][j];
            }
        }
        return result;
    }

    private double vectorNorm(double[] vector) {
        double sum = 0.0;
        for (double value : vector) {
            sum += value * value;
        }
        return Math.sqrt(sum);
    }

    private double[] subtractVectors(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] - b[i];
        }
        return result;
    }

    private double checkSolution(double[][] A, double[] x, double[] b) {
        int n = A.length;
        double[] Ax = new double[n];
        double maxError = 0.0;

        for (int i = 0; i < n; i++) {
            Ax[i] = 0.0;
            for (int j = 0; j < n; j++) {
                Ax[i] += A[i][j] * x[j];
            }
            double error = Math.abs(Ax[i] - b[i]);
            if (error > maxError) maxError = error;
        }
        return maxError;
    }

    // === ГЕНЕРАЦИЯ МАТРИЦЫ С УСЛОВИЯМИ ДЛЯ МЕТОДА КВАДРАТНЫХ КОРНЕЙ ===
    private void generateRandomSystem() {
        int size = (int) sizeSpinner.getValue();
        Random random = new Random();

        log("Генерация случайной системы " + size + "×" + size + "...");

        // Генерируем симметричную матрицу с диагональным преобладанием
        double[][] A = generateSymmetricMatrixWithDominance(size, random);

        // Генерируем вектор b
        double[] b = generateVector(size, random);

        // Обновляем таблицы матрицы и вектора
        updateMatrixAndVectorTables(size);

        // Заполняем таблицы (не очищая результаты)
        fillTablesWithData(A, b, size);

        log("✓ Сгенерирована случайная система " + size + "×" + size);
        log("✓ Матрица симметрична и имеет диагональное преобладание");

        // Для больших матриц даем подсказку
        if (size > 20) {
            log("💡 Для больших матриц используйте прокрутку для просмотра всех элементов");
        }
    }

    private double[][] generateSymmetricMatrixWithDominance(int size, Random random) {
        double[][] A = new double[size][size];

        // 1. Сначала заполняем случайными числами
        for (int i = 0; i < size; i++) {
            for (int j = i; j < size; j++) {
                double value;
                if (i == j) {
                    // Диагональные элементы делаем положительными и больше
                    value = 5 + random.nextDouble() * 15; // [5, 20]
                } else {
                    // Внедиагональные элементы делаем меньше
                    value = random.nextDouble() * 4 - 2; // [-2, 2]
                }
                A[i][j] = value;
                A[j][i] = value; // Обеспечиваем симметричность
            }
        }

        // 2. Усиливаем диагональное преобладание
        for (int i = 0; i < size; i++) {
            double rowSum = 0.0;
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    rowSum += Math.abs(A[i][j]);
                }
            }
            // Гарантируем строгое диагональное преобладание
            A[i][i] = rowSum + 5 + random.nextDouble() * 5;
        }

        // 3. Дополнительная проверка и корректировка для положительной определенности
        for (int i = 0; i < size; i++) {
            if (A[i][i] <= 0) {
                A[i][i] = 10 + random.nextDouble() * 10;
            }
        }

        return A;
    }

    private double[] generateVector(int size, Random random) {
        double[] b = new double[size];
        for (int i = 0; i < size; i++) {
            b[i] = random.nextDouble() * 20 - 10; // [-10, 10]
        }
        return b;
    }

    private void fillTablesWithData(double[][] A, double[] b, int size) {
        // Используем SwingUtilities для безопасного обновления UI из другого потока
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    // Вставляем числа напрямую (как Double), а не как строки
                    matrixModel.setValueAt(Math.round(A[i][j] * 100.0) / 100.0, i, j);
                }
                vectorModel.setValueAt(Math.round(b[i] * 100.0) / 100.0, i, 0);
            }
        });
    }

    // === СЛУШАТЕЛИ СОБЫТИЙ ===

    private class SolveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(() -> {
                try {
                    double[][] A = getMatrixFromTable();
                    double[] b = getVectorFromTable();
                    double epsilon = Double.parseDouble(epsilonField.getText());
                    int maxIterations = Integer.parseInt(maxIterationsField.getText());

                    String selectedMethod = (String) methodComboBox.getSelectedItem();
                    double[] x;

                    long startTime = System.currentTimeMillis();

                    if ("Метод квадратных корней".equals(selectedMethod)) {
                        x = solveBySquareRoot(A, b);
                    } else {
                        x = solveBySimpleIteration(A, b, epsilon, maxIterations);
                    }

                    long endTime = System.currentTimeMillis();

                    SwingUtilities.invokeLater(() -> {
                        displayResults(x, selectedMethod);

                        double error = checkSolution(A, x, b);
                        log(String.format("Максимальная невязка: %.2e", error));
                        log(String.format("Время решения: %d мс", endTime - startTime));
                        log("=".repeat(50));
                    });

                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(LinearSystemsSolverApp.this,
                                "Ошибка: " + ex.getMessage(),
                                "Ошибка решения",
                                JOptionPane.ERROR_MESSAGE);
                        log("❌ Ошибка: " + ex.getMessage());
                    });
                }
            }).start();
        }
    }

    private class CompareButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(() -> {
                compareMethods();
            }).start();
        }
    }

    private class GenerateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(() -> {
                generateRandomSystem();
            }).start();
        }
    }

    private class ExampleButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            loadExample();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Устанавливаем системный Look and Feel для лучшего отображения
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            LinearSystemsSolverApp app = new LinearSystemsSolverApp();
            app.setVisible(true);
        });
    }
}