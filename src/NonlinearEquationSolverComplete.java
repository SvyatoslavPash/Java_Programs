import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class NonlinearEquationSolverComplete extends JFrame {

    // Компоненты интерфейса
    private JComboBox<String> functionComboBox;
    private JTextField x0Field, x1Field;
    private JTextField epsilonField;
    private JTextField studyEpsilonField; // Новое поле для точности исследования
    private JComboBox<String> methodComboBox;
    private JTextArea resultArea;
    private GraphPanel graphPanel;
    private JButton solveButton, clearButton, studyButton, precisionStudyButton;

    // Форматирование чисел
    private DecimalFormat scientificFormat = new DecimalFormat("0.##E0");
    private DecimalFormat decimalFormat = new DecimalFormat("0.##########");

    public NonlinearEquationSolverComplete() {
        setTitle("Решение нелинейных уравнений - Методы секущих и дихотомии");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        initializeComponents();
        setupLayout();
        addEventListeners();
    }

    private void initializeComponents() {
        String[] functions = {
                "2.2 - 2^x",
                "x² - 2",
                "sin(x) - 0.5",
                "x³ - 2x - 5",
                "ln(x) + (x+1)³",
                "cos(x) - x - 2"
        };
        functionComboBox = new JComboBox<>(functions);
        functionComboBox.setSelectedIndex(0);

        x0Field = new JTextField("1.0", 8);
        x1Field = new JTextField("1.5", 8);
        epsilonField = new JTextField("1e-6", 8);
        studyEpsilonField = new JTextField("1e-6", 8); // Поле для точности исследования

        String[] methods = {"Метод секущих", "Метод дихотомии"};
        methodComboBox = new JComboBox<>(methods);

        resultArea = new JTextArea(20, 50);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        graphPanel = new GraphPanel();

        solveButton = new JButton("Решить уравнение");
        clearButton = new JButton("Очистить");
        studyButton = new JButton("Исследовать сходимость");
        precisionStudyButton = new JButton("Исследование точности");
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));

        JPanel controlPanel = createControlPanel();
        leftPanel.add(controlPanel, BorderLayout.NORTH);

        JPanel resultsPanel = createResultsPanel();
        leftPanel.add(resultsPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("График функции"));
        rightPanel.add(graphPanel, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(600);

        mainPanel.add(splitPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Параметры решения"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Строка 0: Выбор функции
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Функция f(x):"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1;
        panel.add(functionComboBox, gbc);

        // Строка 1: Начальные приближения
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0;
        panel.add(new JLabel("Начальные точки:"), gbc);
        gbc.gridx = 1;
        panel.add(createLabeledField("x₀:", x0Field), gbc);
        gbc.gridx = 2;
        panel.add(createLabeledField("x₁:", x1Field), gbc);

        // Строка 2: Точность для решения
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Точность решения ε:"), gbc);
        gbc.gridx = 1;
        panel.add(epsilonField, gbc);
        gbc.gridx = 2;
        panel.add(createLabeledField("Метод:", methodComboBox), gbc);

        // Строка 3: Точность для исследования
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Точность исследования:"), gbc);
        gbc.gridx = 1;
        panel.add(studyEpsilonField, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("")); // Пустая метка для выравнивания

        // Строка 4: Кнопки
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(solveButton);
        buttonPanel.add(studyButton);
        buttonPanel.add(precisionStudyButton);
        buttonPanel.add(clearButton);
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JPanel createLabeledField(String label, JComponent field) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.add(new JLabel(label));
        panel.add(field);
        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Результаты и итерации"));

        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void addEventListeners() {
        solveButton.addActionListener(e -> solveEquation());
        clearButton.addActionListener(e -> clearAll());
        studyButton.addActionListener(e -> studyConvergence());
        precisionStudyButton.addActionListener(e -> performPrecisionStudy());

        functionComboBox.addActionListener(e -> {
            updateInitialValues();
            graphPanel.setFunctionType(getCurrentFunctionType());
            graphPanel.repaint();
        });
    }

    private void updateInitialValues() {
        String functionType = getCurrentFunctionType();
        switch (functionType) {
            case "main":
                x0Field.setText("1.0");
                x1Field.setText("1.5");
                break;
            case "quadratic":
                x0Field.setText("1.0");
                x1Field.setText("2.0");
                break;
            case "trigonometric":
                x0Field.setText("0.0");
                x1Field.setText("1.0");
                break;
            case "cubic":
                x0Field.setText("2.0");
                x1Field.setText("3.0");
                break;
            case "logarithmic":
                x0Field.setText("0.1");
                x1Field.setText("0.5");
                break;
            case "trig":
                x0Field.setText("0.1");
                x1Field.setText("1.0");
                break;
        }
    }

    private String getCurrentFunctionType() {
        String selected = (String) functionComboBox.getSelectedItem();
        switch (selected) {
            case "2.2 - 2^x": return "main";
            case "x² - 2": return "quadratic";
            case "sin(x) - 0.5": return "trigonometric";
            case "x³ - 2x - 5": return "cubic";
            case "ln(x) + (x+1)³": return "logarithmic";
            case "cos(x) - x - 2": return "trig";
            default: return "main";
        }
    }

    // ==================== МАТЕМАТИЧЕСКИЕ МЕТОДЫ ====================

    private List<double[]> findRootIntervals(double a, double b, int segments, String functionType) {
        List<double[]> intervals = new ArrayList<>();
        double step = (b - a) / segments;

        double x1 = a;
        double f1 = evaluateFunction(x1, functionType);

        for (int i = 1; i <= segments; i++) {
            double x2 = a + i * step;
            double f2 = evaluateFunction(x2, functionType);

            if (f1 * f2 <= 0 || Math.abs(f1) < 1e-10 || Math.abs(f2) < 1e-10) {
                intervals.add(new double[]{Math.min(x1, x2), Math.max(x1, x2)});
            }

            x1 = x2;
            f1 = f2;
        }

        return intervals;
    }

    private boolean checkConvergence(double x0, double x1, String functionType) {
        if (Math.abs(x1 - x0) < 1e-15) {
            return false;
        }

        double f0 = evaluateFunction(x0, functionType);
        double f1 = evaluateFunction(x1, functionType);

        if (Math.abs(f1 - f0) < 1e-15) {
            return false;
        }

        return true;
    }

    private SolutionResult secantMethod(double x0, double x1, double epsilon, int maxIterations, String functionType) {
        List<IterationInfo> iterations = new ArrayList<>();
        long startTime = System.nanoTime();

        double x_prev = x0;
        double x_curr = x1;
        double f_prev = evaluateFunction(x_prev, functionType);
        double f_curr = evaluateFunction(x_curr, functionType);
        int iteration = 0;

        iterations.add(new IterationInfo(iteration, x_prev, f_prev, 0));
        iteration++;
        iterations.add(new IterationInfo(iteration, x_curr, f_curr, Math.abs(x_curr - x_prev)));

        while (Math.abs(f_curr) > epsilon && iteration < maxIterations) {
            if (Math.abs(f_curr - f_prev) < 1e-15) {
                break;
            }

            double x_next = x_curr - f_curr * (x_curr - x_prev) / (f_curr - f_prev);
            double f_next = evaluateFunction(x_next, functionType);
            double error = Math.abs(x_next - x_curr);

            iteration++;
            iterations.add(new IterationInfo(iteration, x_next, f_next, error));

            if (Math.abs(f_next) < epsilon || error < epsilon) {
                break;
            }

            x_prev = x_curr;
            x_curr = x_next;
            f_prev = f_curr;
            f_curr = f_next;
        }

        long endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1e9;

        return new SolutionResult(iterations.get(iterations.size() - 1).x, iterations,
                "Метод секущих", executionTime, functionType);
    }

    private SolutionResult dixotomiiMethod(double a, double b, double epsilon, int maxIterations, String functionType) {
        List<IterationInfo> iterations = new ArrayList<>();
        long startTime = System.nanoTime();

        double fa = evaluateFunction(a, functionType);
        double fb = evaluateFunction(b, functionType);

        if (fa * fb > 0) {
            throw new IllegalArgumentException("Функция не меняет знак на интервале [a,b]");
        }

        int iteration = 0;

        while (iteration < maxIterations && (b - a) / 2 > epsilon) {
            double c = (a + b) / 2;
            double fc = evaluateFunction(c, functionType);
            double error = (b - a) / 2;

            iterations.add(new IterationInfo(iteration, c, fc, error));

            if (Math.abs(fc) < epsilon) {
                break;
            }

            if (fa * fc < 0) {
                b = c;
                fb = fc;
            } else {
                a = c;
                fa = fc;
            }

            iteration++;
        }

        long endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1e9;

        return new SolutionResult(iterations.get(iterations.size() - 1).x, iterations,
                "Метод дихотомии", executionTime, functionType);
    }

    private double evaluateAccuracy(double root, String functionType) {
        return Math.abs(evaluateFunction(root, functionType));
    }

    private double evaluateFunction(double x, String functionType) {
        switch (functionType) {
            case "main":
                return 2.2 - Math.pow(2, x);
            case "quadratic":
                return x * x - 2;
            case "trigonometric":
                return Math.sin(x) - 0.5;
            case "cubic":
                return x * x * x - 2 * x - 5;
            case "logarithmic":
                if (x <= 0) return Double.NaN;
                return Math.log(x) + Math.pow(x + 1, 3);
            case "trig":
                return Math.cos(x) - x - 2;
            default:
                return 2.2 - Math.pow(2, x);
        }
    }

    private double evaluateDerivative(double x, String functionType) {
        double h = 1e-8;
        return (evaluateFunction(x + h, functionType) - evaluateFunction(x - h, functionType)) / (2 * h);
    }

    // ==================== ИССЛЕДОВАНИЕ СХОДИМОСТИ С ВОЗМОЖНОСТЬЮ ИЗМЕНЕНИЯ ТОЧНОСТИ ====================

    private void performConvergenceStudy(String functionType) {
        StringBuilder study = new StringBuilder();
        study.append("ИССЛЕДОВАНИЕ СКОРОСТИ СХОДИМОСТИ\n");
        study.append("================================\n\n");

        double x0, x1;
        String functionName = getFunctionName(functionType);

        // Получаем точность исследования из поля ввода
        double studyEpsilon;
        try {
            studyEpsilon = Double.parseDouble(studyEpsilonField.getText());
        } catch (NumberFormatException e) {
            studyEpsilon = 1e-6; // Значение по умолчанию
        }

        switch (functionType) {
            case "main":
                x0 = 1.0; x1 = 1.5;
                break;
            case "quadratic":
                x0 = 1.0; x1 = 2.0;
                break;
            case "trigonometric":
                x0 = 0.0; x1 = 1.0;
                break;
            case "cubic":
                x0 = 2.0; x1 = 3.0;
                break;
            case "logarithmic":
                x0 = 0.1; x1 = 0.5;
                break;
            case "trig":
                x0 = 0.1; x1 = 1.0;
            default:
                x0 = 1.0; x1 = 1.5;
        }

        study.append("Функция: ").append(functionName).append("\n");
        study.append("Начальные точки: x0=").append(x0).append(", x1=").append(x1).append("\n");
        study.append("Точность исследования: ε=").append(studyEpsilon).append("\n\n");

        study.append("Точность ε\t\tСекущие\tДихотомия\n");
        study.append("----------------------------------------\n");

        // Генерируем точности на основе заданной точности исследования
        double[] epsilons = generateEpsilons(studyEpsilon);

        for (double eps : epsilons) {
            try {
                SolutionResult secant = secantMethod(x0, x1, eps, 1000, functionType);
                SolutionResult bisection = dixotomiiMethod(x0, x1, eps, 1000, functionType);

                study.append(String.format("%-15s\t%d\t%d\n",
                        formatEpsilon(eps), secant.iterations.size(), bisection.iterations.size()));
            } catch (Exception e) {
                study.append(String.format("%-15s\t- \t- \n", formatEpsilon(eps)));
            }
        }

        resultArea.append(study.toString() + "\n");
    }

    // Генерация массива точностей для исследования
    private double[] generateEpsilons(double baseEpsilon) {
        List<Double> epsilons = new ArrayList<>();

        // Добавляем точности: baseEpsilon * 10, baseEpsilon, baseEpsilon / 10, baseEpsilon / 100
        epsilons.add(baseEpsilon * 10);
        epsilons.add(baseEpsilon);
        epsilons.add(baseEpsilon / 10);
        epsilons.add(baseEpsilon / 100);
        epsilons.add(baseEpsilon / 1000);

        return epsilons.stream().mapToDouble(Double::doubleValue).toArray();
    }

    // Форматирование точности для красивого вывода
    private String formatEpsilon(double epsilon) {
        if (epsilon >= 0.001) {
            return String.format("%.4f", epsilon);
        } else {
            return String.format("%.2e", epsilon);
        }
    }

    private void performAlgorithmComparison(String functionType) {
        StringBuilder comparison = new StringBuilder();
        comparison.append("СРАВНЕНИЕ АЛГОРИТМОВ\n");
        comparison.append("====================\n\n");

        double x0, x1;
        String functionName = getFunctionName(functionType);

        // Получаем точность исследования из поля ввода
        double studyEpsilon;
        try {
            studyEpsilon = Double.parseDouble(studyEpsilonField.getText());
        } catch (NumberFormatException e) {
            studyEpsilon = 1e-6; // Значение по умолчанию
        }

        switch (functionType) {
            case "main":
                x0 = 1.0; x1 = 1.5;
                break;
            case "quadratic":
                x0 = 1.0; x1 = 2.0;
                break;
            case "logarithmic":
                x0 = 0.1; x1 = 0.5;
            case "trig":
                x0 = 0.1; x1 = 1.0;
                break;
            default:
                x0 = 1.0; x1 = 1.5;
        }

        comparison.append("Функция: ").append(functionName).append("\n");
        comparison.append("Точность: ").append(studyEpsilon).append("\n\n");

        comparison.append("Метод\t\tКорень\t\tИтерации\tВремя(с)\tТочность\n");
        comparison.append("--------------------------------------------------------\n");

        try {
            SolutionResult secant = secantMethod(x0, x1, studyEpsilon, 1000, functionType);
            double secantAccuracy = evaluateAccuracy(secant.root, functionType);
            comparison.append(String.format("Секущие\t\t%.6f\t%d\t\t%.6f\t%.2e\n",
                    secant.root, secant.iterations.size(), secant.executionTime,
                    secantAccuracy));
        } catch (Exception e) {
            comparison.append("Секущие\t\t- \t\t- \t\t- \t\t-\n");
        }

        try {
            SolutionResult bisection = dixotomiiMethod(x0, x1, studyEpsilon, 1000, functionType);
            double bisectionAccuracy = evaluateAccuracy(bisection.root, functionType);

            comparison.append(String.format("Дихотомия\t%.6f\t%d\t\t%.6f\t%.2e\n",
                    bisection.root, bisection.iterations.size(), bisection.executionTime,
                    bisectionAccuracy));
        } catch (Exception e) {
            comparison.append("Дихотомия\t- \t\t- \t\t- \t\t-\n");
        }

        resultArea.append(comparison.toString() + "\n");
    }

    private String getFunctionName(String functionType) {
        switch (functionType) {
            case "main": return "2.2 - 2^x";
            case "quadratic": return "x² - 2";
            case "trigonometric": return "sin(x) - 0.5";
            case "trig": return "cos(x) - x -2";
            case "cubic": return "x³ - 2x - 5";
            case "logarithmic": return "ln(x) + (x+1)³";
            default: return "2.2 - 2^x";
        }
    }

    // ==================== МЕТОДЫ ДЛЯ ИССЛЕДОВАНИЯ ТОЧНОСТИ ====================

    private void performPrecisionStudy() {
        String functionType = getCurrentFunctionType();
        String functionName = getFunctionName(functionType);

        resultArea.setText("");
        StringBuilder output = new StringBuilder();

        output.append("ИССЛЕДОВАНИЕ СКОРОСТИ СХОДИМОСТИ В ЗАВИСИМОСТИ ОТ ТОЧНОСТИ\n");
        output.append("===========================================================\n\n");
        output.append("Функция: ").append(functionName).append("\n\n");

        // Заголовок таблицы
        output.append(String.format("%-12s %-15s %-15s %-12s %-15s\n",
                "Точность", "Метод", "Корень", "Итераций", "Время"));
        output.append("----------------------------------------------------------------\n");

        // Используем фиксированные точности для этого исследования
        double[] epsilons = {1e-2, 1e-4, 1e-6, 1e-8};
        String[] methods = {"Метод секущих", "Метод дихотомии"};

        // Получаем начальные значения для текущей функции
        double x0 = Double.parseDouble(x0Field.getText());
        double x1 = Double.parseDouble(x1Field.getText());

        for (double eps : epsilons) {
            for (String method : methods) {
                try {
                    SolutionResult result = null;
                    switch (method) {
                        case "Метод секущих":
                            result = secantMethod(x0, x1, eps, 1000, functionType);
                            break;
                        case "Метод дихотомии":
                            result = dixotomiiMethod(x0, x1, eps, 1000, functionType);
                            break;
                    }

                    if (result != null) {
                        String timeStr = String.format("%.6f с", result.executionTime);
                        if (result.executionTime < 0.001) {
                            timeStr = String.format("%.1f микросекунд", result.executionTime * 1e6);
                        }

                        output.append(String.format("%-12.0e %-15s %-15.6f %-12d %-15s\n",
                                eps, method, result.root, result.iterations.size(), timeStr));
                    }
                } catch (Exception e) {
                    output.append(String.format("%-12.0e %-15s %-15s %-12s %-15s\n",
                            eps, method, "Ошибка", "-", "-"));
                }
            }
            output.append("\n");
        }

        // Добавляем анализ сходимости
        output.append("\nАНАЛИЗ СХОДИМОСТИ:\n");
        output.append("• Метод секущих: суперлинейная сходимость, малое количество итераций\n");
        output.append("• Метод дихотомии: линейная сходимость, гарантированная сходимость\n");

        resultArea.setText(output.toString());
    }

    private ConvergenceResult checkConvergenceWithPrecision(double x0, double x1, double epsilon, String functionType, String method) {
        ConvergenceResult result = new ConvergenceResult();

        switch (method) {
            case "Метод секущих":
                double f0 = evaluateFunction(x0, functionType);
                double f1 = evaluateFunction(x1, functionType);
                result.converges = Math.abs(f1 - f0) > 1e-15 && Math.abs(x1 - x0) > 1e-15;
                result.details = String.format("f(x₁)-f(x₀) = %.2e, x₁-x₀ = %.2e",
                        Math.abs(f1 - f0), Math.abs(x1 - x0));
                result.condition = "|f(x₁)-f(x₀)| > ε и |x₁-x₀| > ε";
                break;

            case "Метод дихотомии":
                double fa = evaluateFunction(x0, functionType);
                double fb = evaluateFunction(x1, functionType);
                result.converges = fa * fb < 0;
                result.details = String.format("f(%.2f)×f(%.2f) = %.2e %s 0",
                        x0, x1, fa * fb, fa * fb < 0 ? "<" : ">");
                result.condition = "f(a)×f(b) < 0";
                break;
        }

        return result;
    }

    // ==================== ОСНОВНЫЕ ФУНКЦИИ ИНТЕРФЕЙСА ====================

    private void solveEquation() {
        try {
            double x0 = Double.parseDouble(x0Field.getText());
            double x1 = Double.parseDouble(x1Field.getText());
            double epsilon = Double.parseDouble(epsilonField.getText());
            String method = (String) methodComboBox.getSelectedItem();
            String functionType = getCurrentFunctionType();

            resultArea.setText("");
            StringBuilder output = new StringBuilder();

            output.append("РЕШЕНИЕ УРАВНЕНИЯ: ").append(getFunctionName(functionType)).append(" = 0\n");
            output.append("=".repeat(60)).append("\n\n");

            // 1. Поиск интервалов с корнями
            output.append("1. ПОИСК ИНТЕРВАЛОВ С КОРНЯМИ:\n");
            double searchStart = functionType.equals("logarithmic") ? 0.01 : -2;
            double searchEnd = functionType.equals("logarithmic") ? 2 : 3;

            List<double[]> intervals = findRootIntervals(searchStart, searchEnd, 100, functionType);
            if (intervals.isEmpty()) {
                output.append("   Корни не найдены в заданном диапазоне\n");
            } else {
                for (int i = 0; i < intervals.size(); i++) {
                    double[] interval = intervals.get(i);
                    output.append(String.format("   Интервал %d: [%.3f, %.3f]\n", i + 1, interval[0], interval[1]));
                }
            }
            output.append("\n");

            // 2. Улучшенная проверка сходимости с учетом точности
            output.append("2. ПРОВЕРКА СХОДИМОСТИ (ε = ").append(epsilon).append("):\n");
            ConvergenceResult convergence = checkConvergenceWithPrecision(x0, x1, epsilon, functionType, method);
            output.append("   Условие сходимости: ").append(convergence.condition).append("\n");
            output.append("   Проверка: ").append(convergence.details).append("\n");
            output.append("   Результат: ").append(convergence.converges ? "СХОДИТСЯ ✓" : "МОЖЕТ РАСХОДИТЬСЯ ✗").append("\n");

            if (!convergence.converges) {
                output.append("   ВНИМАНИЕ: Условие сходимости не выполняется!\n");
            }
            output.append("\n");

            // 3. Решение уравнения
            output.append("3. РЕШЕНИЕ УРАВНЕНИЯ (" + method + "):\n");

            SolutionResult result;
            switch (method) {
                case "Метод секущих":
                    result = secantMethod(x0, x1, epsilon, 1000, functionType);
                    break;
                case "Метод дихотомии":
                    result = dixotomiiMethod(x0, x1, epsilon, 1000, functionType);
                    break;
                default:
                    throw new IllegalArgumentException("Неизвестный метод");
            }

            // Вывод итераций
            output.append("Итерация\tx_n\t\tf(x_n)\t\tПогрешность\n");
            output.append("------------------------------------------------\n");
            for (IterationInfo iter : result.iterations) {
                output.append(String.format("%d\t\t%.6f\t%.2e\t%.2e\n",
                        iter.iteration, iter.x, iter.fx, iter.error));
            }
            output.append("\n");

            // 4. Оценка точности
            output.append("4. ОЦЕНКА ТОЧНОСТИ:\n");
            double accuracy = evaluateAccuracy(result.root, functionType);
            output.append(String.format("   Найденный корень: x = %s\n", decimalFormat.format(result.root)));
            output.append(String.format("   Значение функции: f(x) = %s\n", scientificFormat.format(accuracy)));
            output.append(String.format("   Количество итераций: %d\n", result.iterations.size()));
            output.append(String.format("   Время выполнения: %.6f сек\n", result.executionTime));
            output.append(String.format("   Достигнутая точность: %s\n", scientificFormat.format(accuracy)));

            // Проверка решения
            output.append("\n5. ПРОВЕРКА РЕШЕНИЯ:\n");
            double verification = evaluateFunction(result.root, functionType);
            output.append(String.format("   f(%s) = %s\n", decimalFormat.format(result.root), scientificFormat.format(verification)));

            resultArea.setText(output.toString());

            // 6. Обновление графика
            graphPanel.setRoot(result.root);
            graphPanel.setFunctionType(functionType);
            graphPanel.repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void studyConvergence() {
        String functionType = getCurrentFunctionType();

        resultArea.append("\n" + "=".repeat(60) + "\n");
        resultArea.append("ИССЛЕДОВАНИЕ СХОДИМОСТИ ДЛЯ: " + getFunctionName(functionType) + "\n");
        resultArea.append("=".repeat(60) + "\n\n");

        performConvergenceStudy(functionType);
        resultArea.append("\n");

        performAlgorithmComparison(functionType);
        resultArea.append("\n");

        resultArea.append("ИНФОРМАЦИЯ О ФУНКЦИИ:\n");
        resultArea.append("-------------------\n");
        switch (functionType) {
            case "logarithmic":
                resultArea.append("Функция: ln(x) + (x+1)³\n");
                resultArea.append("Область определения: x > 0\n");
                resultArea.append("Поведение:\n");
                resultArea.append("  - При x -> 0+: ln(x) -> -∞, (x+1)³ -> 1, f(x) -> -∞\n");
                resultArea.append("  - При x -> +∞: ln(x) -> +∞, (x+1)³ -> +∞, f(x) -> +∞\n");
                resultArea.append("  - Имеет один действительный корень на интервале (0, 1)\n");
                break;
            default:
                resultArea.append("Функция: " + getFunctionName(functionType) + "\n");
        }
        resultArea.append("\n");
    }

    private void clearAll() {
        resultArea.setText("");
        graphPanel.clear();
        graphPanel.repaint();
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ КЛАССЫ ====================

    private static class IterationInfo {
        int iteration;
        double x;
        double fx;
        double error;

        IterationInfo(int iteration, double x, double fx, double error) {
            this.iteration = iteration;
            this.x = x;
            this.fx = fx;
            this.error = error;
        }
    }

    private static class SolutionResult {
        double root;
        List<IterationInfo> iterations;
        String methodName;
        double executionTime;
        String functionType;

        SolutionResult(double root, List<IterationInfo> iterations, String methodName,
                       double executionTime, String functionType) {
            this.root = root;
            this.iterations = iterations;
            this.methodName = methodName;
            this.executionTime = executionTime;
            this.functionType = functionType;
        }
    }

    private static class ConvergenceResult {
        boolean converges;
        String details;
        String condition;
    }

    // ==================== КЛАСС ДЛЯ ПОСТРОЕНИЯ ГРАФИКА ====================

    private class GraphPanel extends JPanel {
        private double root = Double.NaN;
        private String functionType = "main";
        private List<Point2D.Double> functionPoints;

        public GraphPanel() {
            setPreferredSize(new Dimension(500, 400));
            setBackground(Color.WHITE);
            functionPoints = new ArrayList<>();
            generateFunctionPoints();
        }

        public void setRoot(double root) {
            this.root = root;
        }

        public void setFunctionType(String functionType) {
            this.functionType = functionType;
            generateFunctionPoints();
        }

        public void clear() {
            this.root = Double.NaN;
            this.functionPoints.clear();
        }

        private void generateFunctionPoints() {
            functionPoints.clear();

            double xMin, xMax;
            switch (functionType) {
                case "logarithmic":
                    xMin = 0.01;
                    xMax = 2.0;
                    break;
                case "main":
                case "quadratic":
                case "trigonometric":
                case "cubic":
                default:
                    xMin = -2.0;
                    xMax = 3.0;
            }

            int points = 200;

            for (int i = 0; i <= points; i++) {
                double x = xMin + (xMax - xMin) * i / points;
                double y = evaluateFunction(x, functionType);
                if (!Double.isNaN(y) && !Double.isInfinite(y)) {
                    functionPoints.add(new Point2D.Double(x, y));
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int padding = 50;

            double xMin = functionPoints.get(0).x;
            double xMax = functionPoints.get(functionPoints.size() - 1).x;
            double yMin = Double.MAX_VALUE;
            double yMax = -Double.MAX_VALUE;

            for (Point2D.Double point : functionPoints) {
                if (!Double.isNaN(point.y) && !Double.isInfinite(point.y)) {
                    yMin = Math.min(yMin, point.y);
                    yMax = Math.max(yMax, point.y);
                }
            }

            yMin -= 0.1 * (yMax - yMin);
            yMax += 0.1 * (yMax - yMin);

            double xScale = (width - 2 * padding) / (xMax - xMin);
            double yScale = (height - 2 * padding) / (yMax - yMin);

            g2d.setColor(Color.LIGHT_GRAY);
            int zeroY = (int) (height - padding - (-yMin) * yScale);
            g2d.drawLine(padding, zeroY, width - padding, zeroY);

            int zeroX = (int) (padding + (-xMin) * xScale);
            g2d.drawLine(zeroX, padding, zeroX, height - padding);

            g2d.setStroke(new BasicStroke(1));
            g2d.setColor(new Color(240, 240, 240));
            for (double x = Math.ceil(xMin); x <= xMax; x += 0.5) {
                int xPixel = (int) (padding + (x - xMin) * xScale);
                g2d.drawLine(xPixel, padding, xPixel, height - padding);
            }

            for (double y = Math.ceil(yMin); y <= yMax; y += 0.5) {
                int yPixel = (int) (height - padding - (y - yMin) * yScale);
                g2d.drawLine(padding, yPixel, width - padding, yPixel);
            }

            g2d.setColor(Color.BLUE);
            g2d.setStroke(new BasicStroke(2));

            Point2D.Double prevPoint = null;
            for (Point2D.Double point : functionPoints) {
                if (!Double.isNaN(point.y) && !Double.isInfinite(point.y)) {
                    int x = (int) (padding + (point.x - xMin) * xScale);
                    int y = (int) (height - padding - (point.y - yMin) * yScale);

                    if (prevPoint != null) {
                        int prevX = (int) (padding + (prevPoint.x - xMin) * xScale);
                        int prevY = (int) (height - padding - (prevPoint.y - yMin) * yScale);
                        g2d.drawLine(prevX, prevY, x, y);
                    }
                    prevPoint = point;
                } else {
                    prevPoint = null;
                }
            }

            if (!Double.isNaN(root)) {
                g2d.setColor(Color.RED);
                int xRoot = (int) (padding + (root - xMin) * xScale);
                int yRoot = (int) (height - padding - (0 - yMin) * yScale);

                g2d.fillOval(xRoot - 5, yRoot - 5, 10, 10);

                g2d.setColor(Color.RED);
                g2d.drawString(String.format("x = %.4f", root), xRoot + 10, yRoot - 10);
            }

            g2d.setColor(Color.BLACK);
            g2d.drawString("x", width - padding + 5, zeroY - 5);
            g2d.drawString("f(x)", zeroX + 5, padding - 5);

            String functionName = getFunctionName(functionType);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString("График функции: " + functionName, padding, 20);
        }
    }

    // ==================== ГЛАВНЫЙ МЕТОД ====================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            NonlinearEquationSolverComplete app = new NonlinearEquationSolverComplete();
            app.setVisible(true);
        });
    }
}