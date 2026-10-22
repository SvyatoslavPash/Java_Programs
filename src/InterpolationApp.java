import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class InterpolationApp extends JFrame {

    // Исходные данные из варианта 2
    private double[] defaultX = {0.2, 0.8, 1.4, 2.0, 2.6, 3.2, 3.8};
    private double[] defaultY = {-6.4, -4.4, 2.2, 4.7, 5.1, 3.3, 1.7};

    private List<Double> pointsX = new ArrayList<>();
    private List<Double> pointsY = new ArrayList<>();

    // Компоненты интерфейса
    private GraphPanel graphPanel;
    private JTable dataTable;
    private JTextField newXField, newYField;
    private JCheckBox lagrangeCheck, splineCheck, pointsCheck;
    private JLabel statusLabel;
    private JSpinner resolutionSpinner;
    private JTextArea solutionArea;
    private JTabbedPane solutionTabs;

    // Форматтер для чисел
    private DecimalFormat df = new DecimalFormat("#0.0000");
    private DecimalFormat dfSimple = new DecimalFormat("#0.00");

    // Коэффициенты сплайна для отображения в решении
    private double[] splineA, splineB, splineC, splineH;
    private String[] lagrangePolynomials;
    private double[] splineG; // Для отображения правых частей уравнений

    public InterpolationApp() {
        setTitle("Интерполяция: Лагранж и Параболический сплайн");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        setLayout(new BorderLayout());

        initData();
        initUI();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initData() {
        pointsX.clear();
        pointsY.clear();
        for (int i = 0; i < defaultX.length; i++) {
            pointsX.add(defaultX[i]);
            pointsY.add(defaultY[i]);
        }
    }

    private void initUI() {
        // Панель управления слева
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Управление"));
        controlPanel.setPreferredSize(new Dimension(300, 800));

        // Панель ввода данных
        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Добавить точку"));

        inputPanel.add(new JLabel("X:"));
        newXField = new JTextField("1.0");
        inputPanel.add(newXField);

        inputPanel.add(new JLabel("Y:"));
        newYField = new JTextField("0.0");
        inputPanel.add(newYField);

        JButton addButton = new JButton("Добавить точку");
        addButton.addActionListener(e -> addPoint());
        inputPanel.add(addButton);

        JButton resetButton = new JButton("Сброс к варианту 2");
        resetButton.addActionListener(e -> resetData());
        inputPanel.add(resetButton);

        // Панель отображения данных
        String[] columns = {"X", "Y"};
        Object[][] data = new Object[pointsX.size()][2];
        for (int i = 0; i < pointsX.size(); i++) {
            data[i][0] = df.format(pointsX.get(i));
            data[i][1] = df.format(pointsY.get(i));
        }

        dataTable = new JTable(data, columns);
        JScrollPane tableScroll = new JScrollPane(dataTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Таблица точек"));

        // Панель настроек отображения
        JPanel displayPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        displayPanel.setBorder(BorderFactory.createTitledBorder("Отображение"));

        lagrangeCheck = new JCheckBox("Полином Лагранжа", true);
        splineCheck = new JCheckBox("Параболический сплайн", true);
        pointsCheck = new JCheckBox("Исходные точки", true);

        displayPanel.add(lagrangeCheck);
        displayPanel.add(splineCheck);
        displayPanel.add(pointsCheck);

        // Настройка разрешения
        JPanel resolutionPanel = new JPanel(new FlowLayout());
        resolutionPanel.add(new JLabel("Разрешение:"));
        resolutionSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 1000, 10));
        resolutionPanel.add(resolutionSpinner);

        // Кнопки управления масштабом
        JPanel zoomPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        zoomPanel.setBorder(BorderFactory.createTitledBorder("Масштабирование"));

        JButton zoomInButton = new JButton("Увеличить (+)");
        zoomInButton.addActionListener(e -> graphPanel.zoomIn());

        JButton zoomOutButton = new JButton("Уменьшить (-)");
        zoomOutButton.addActionListener(e -> graphPanel.zoomOut());

        JButton panLeftButton = new JButton("Влево ←");
        panLeftButton.addActionListener(e -> graphPanel.panLeft());

        JButton panRightButton = new JButton("Вправо →");
        panRightButton.addActionListener(e -> graphPanel.panRight());

        zoomPanel.add(zoomInButton);
        zoomPanel.add(zoomOutButton);
        zoomPanel.add(panLeftButton);
        zoomPanel.add(panRightButton);

        JPanel zoomPanel2 = new JPanel(new GridLayout(2, 2, 5, 5));
        JButton panUpButton = new JButton("Вверх ↑");
        panUpButton.addActionListener(e -> graphPanel.panUp());

        JButton panDownButton = new JButton("Вниз ↓");
        panDownButton.addActionListener(e -> graphPanel.panDown());

        JButton resetZoomButton = new JButton("Сбросить масштаб");
        resetZoomButton.addActionListener(e -> graphPanel.resetZoom());

        JButton autoZoomButton = new JButton("Автомасштаб");
        autoZoomButton.addActionListener(e -> graphPanel.autoZoom());

        zoomPanel2.add(panUpButton);
        zoomPanel2.add(panDownButton);
        zoomPanel2.add(resetZoomButton);
        zoomPanel2.add(autoZoomButton);

        // Кнопки управления
        JButton calculateButton = new JButton("Рассчитать");
        calculateButton.addActionListener(e -> calculate());

        JButton clearButton = new JButton("Очистить все точки");
        clearButton.addActionListener(e -> clearAllPoints());

        // Статус
        statusLabel = new JLabel("Готово к работе");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());

        // Сборка панели управления
        controlPanel.add(inputPanel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(tableScroll);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(displayPanel);
        controlPanel.add(resolutionPanel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(zoomPanel);
        controlPanel.add(zoomPanel2);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(calculateButton);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(clearButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(statusLabel);

        // Графическая панель
        graphPanel = new GraphPanel();

        // Панель с решением
        JPanel solutionPanel = new JPanel(new BorderLayout());
        solutionPanel.setBorder(BorderFactory.createTitledBorder("Решение"));
        solutionPanel.setPreferredSize(new Dimension(400, 800));

        solutionTabs = new JTabbedPane();

        solutionArea = new JTextArea();
        solutionArea.setEditable(false);
        solutionArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        solutionArea.setLineWrap(true);
        solutionArea.setWrapStyleWord(true);

        JScrollPane solutionScroll = new JScrollPane(solutionArea);
        solutionScroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        solutionTabs.addTab("Лагранж", solutionScroll);

        // Создаем отдельную панель для решения сплайна
        JTextArea splineSolutionArea = new JTextArea();
        splineSolutionArea.setEditable(false);
        splineSolutionArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        splineSolutionArea.setLineWrap(true);
        splineSolutionArea.setWrapStyleWord(true);
        JScrollPane splineScroll = new JScrollPane(splineSolutionArea);
        solutionTabs.addTab("Сплайн", splineScroll);

        solutionPanel.add(solutionTabs, BorderLayout.CENTER);

        // Добавляем слушатель изменения вкладок
        solutionTabs.addChangeListener(e -> {
            int selectedTab = solutionTabs.getSelectedIndex();
            if (selectedTab == 0) {
                updateLagrangeSolution();
            } else if (selectedTab == 1) {
                updateSplineSolution(splineSolutionArea);
            }
        });

        // Добавляем компоненты в окно
        add(controlPanel, BorderLayout.WEST);
        add(graphPanel, BorderLayout.CENTER);
        add(solutionPanel, BorderLayout.EAST);

        // Рассчитать начальные данные
        calculate();
    }

    private void addPoint() {
        try {
            double x = Double.parseDouble(newXField.getText());
            double y = Double.parseDouble(newYField.getText());

            pointsX.add(x);
            pointsY.add(y);

            updateTable();
            statusLabel.setText("Точка добавлена: (" + df.format(x) + ", " + df.format(y) + ")");

            // Обновляем график
            SwingUtilities.invokeLater(() -> {
                calculate();
                graphPanel.autoZoom(); // Автоматически масштабируем при добавлении точки
            });

        } catch (NumberFormatException ex) {
            statusLabel.setText("Ошибка: некорректный ввод чисел");
        }
    }

    private void resetData() {
        initData();
        updateTable();
        calculate();
        graphPanel.autoZoom();
        statusLabel.setText("Данные сброшены к варианту 2");
    }

    private void clearAllPoints() {
        pointsX.clear();
        pointsY.clear();
        updateTable();
        graphPanel.clearGraphs();
        solutionArea.setText("");
        statusLabel.setText("Все точки очищены");
    }

    private void updateTable() {
        Object[][] data = new Object[pointsX.size()][2];
        for (int i = 0; i < pointsX.size(); i++) {
            data[i][0] = df.format(pointsX.get(i));
            data[i][1] = df.format(pointsY.get(i));
        }
        dataTable.setModel(new DefaultTableModel(data, new String[]{"X", "Y"}));
    }

    private void calculate() {
        if (pointsX.size() < 2) {
            statusLabel.setText("Ошибка: нужно минимум 2 точки");
            graphPanel.setData(pointsX, pointsY, new ArrayList<>(), new ArrayList<>(),
                    lagrangeCheck.isSelected(), splineCheck.isSelected(),
                    pointsCheck.isSelected());
            return;
        }

        try {
            // Создаем массивы для вычислений
            double[] x = new double[pointsX.size()];
            double[] y = new double[pointsY.size()];
            for (int i = 0; i < pointsX.size(); i++) {
                x[i] = pointsX.get(i);
                y[i] = pointsY.get(i);
            }

            // Рассчитываем Лагранж
            List<Point2D.Double> lagrangePoints = new ArrayList<>();
            if (lagrangeCheck.isSelected()) {
                lagrangePoints = calculateLagrange(x, y);
            }

            // Рассчитываем сплайн
            List<Point2D.Double> splinePoints = new ArrayList<>();
            if (splineCheck.isSelected()) {
                splinePoints = calculateParabolicSpline(x, y);
            }

            // Передаем точки на график
            graphPanel.setData(pointsX, pointsY, lagrangePoints, splinePoints,
                    lagrangeCheck.isSelected(), splineCheck.isSelected(),
                    pointsCheck.isSelected());

            // Обновляем текст решения
            int selectedTab = solutionTabs.getSelectedIndex();
            if (selectedTab == 0) {
                updateLagrangeSolution();
            } else if (selectedTab == 1) {
                // Получаем текстовую область для сплайна из вкладки
                Component comp = solutionTabs.getComponentAt(1);
                if (comp instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane) comp;
                    Component view = scrollPane.getViewport().getView();
                    if (view instanceof JTextArea) {
                        updateSplineSolution((JTextArea) view);
                    }
                }
            }

            statusLabel.setText("Расчет выполнен. Точек: " + pointsX.size() +
                    ", Лагранж: " + lagrangePoints.size() +
                    ", Сплайн: " + splinePoints.size());

        } catch (Exception ex) {
            statusLabel.setText("Ошибка расчета: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void updateLagrangeSolution() {
        if (lagrangePolynomials == null) {
            solutionArea.setText("Сначала выполните расчет полинома Лагранжа");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== РЕШЕНИЕ ПОСТРОЕНИЯ ПОЛИНОМА ЛАГРАНЖА ===\n\n");

        sb.append("Даны узлы интерполяции:\n");
        for (int i = 0; i < pointsX.size(); i++) {
            sb.append(String.format("x%d = %s, y%d = %s\n",
                    i, df.format(pointsX.get(i)), i, df.format(pointsY.get(i))));
        }
        sb.append("\n");

        sb.append("ШАГ 1: Строим базисные полиномы p_i(x):\n");
        sb.append("Условия: p_i(x_j) = δ_ij = {1, если i=j; 0, если i≠j}\n\n");

        for (int i = 0; i < lagrangePolynomials.length; i++) {
            sb.append(String.format("p_%d(x) = %s\n", i, lagrangePolynomials[i]));
        }
        sb.append("\n");

        sb.append("ШАГ 2: Формируем интерполяционный полином Лагранжа:\n");
        sb.append("L_n(x) = Σ [y_i * p_i(x)], i=0..n\n");
        sb.append("L_n(x) = ");

        for (int i = 0; i < pointsX.size(); i++) {
            sb.append(String.format("(%s)*p_%d(x)", df.format(pointsY.get(i)), i));
            if (i < pointsX.size() - 1) {
                sb.append(" + ");
            }
        }
        sb.append("\n\n");

        sb.append("ШАГ 3: Общий вид полинома:\n");
        sb.append("L_n(x) = Σ y_i * Π [(x - x_j)/(x_i - x_j)], j≠i\n");
        sb.append("где i = 0.." + (pointsX.size() - 1) + "\n\n");

        sb.append("Полином имеет степень не выше " + (pointsX.size() - 1) + "\n");

        solutionArea.setText(sb.toString());
    }

    private void updateSplineSolution(JTextArea splineArea) {
        if (splineA == null || splineB == null || splineC == null || splineH == null) {
            splineArea.setText("Сначала выполните расчет параболического сплайна");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== РЕШЕНИЕ ПОСТРОЕНИЯ ПАРАБОЛИЧЕСКОГО СПЛАЙНА ===\n\n");

        sb.append("Даны узлы интерполяции:\n");
        for (int i = 0; i < pointsX.size(); i++) {
            sb.append(String.format("x%d = %s, y%d = %s\n",
                    i, df.format(pointsX.get(i)), i, df.format(pointsY.get(i))));
        }
        sb.append("\n");

        sb.append("ШАГ 1: Определяем отрезки h_i = x_i - x_{i-1}:\n");
        for (int i = 1; i < pointsX.size(); i++) {
            sb.append(String.format("h_%d = x_%d - x_%d = %s - %s = %s\n",
                    i, i, i-1, df.format(pointsX.get(i)), df.format(pointsX.get(i-1)), df.format(splineH[i])));
        }
        sb.append("\n");

        sb.append("ШАГ 2: Форма сплайна на каждом отрезке [x_{i-1}, x_i]:\n");
        sb.append("P_i(x) = a_i + b_i*(x - x_{i-1}) + c_i*(x - x_{i-1})²\n\n");

        sb.append("ШАГ 3: Условия интерполяции и непрерывности:\n");
        sb.append("1) P_i(x_{i-1}) = y_{i-1}  => a_i = y_{i-1}\n");
        sb.append("2) P_i(x_i) = y_i  => a_i + b_i*h_i + c_i*h_i² = y_i\n");
        sb.append("3) Непрерывность производной: P_i'(x_i) = P_{i+1}'(x_i)\n");
        sb.append("   где P_i'(x) = b_i + 2*c_i*(x - x_{i-1})\n");
        sb.append("   => b_i + 2*c_i*h_i = b_{i+1}\n\n");

        sb.append("ШАГ 4: Вычисляем коэффициенты a_i:\n");
        for (int i = 1; i < splineA.length; i++) {
            sb.append(String.format("a_%d = y_%d = %s\n", i, i-1, df.format(splineA[i])));
        }
        sb.append("\n");

        sb.append("ШАГ 5: Из условия (2) выражаем b_i:\n");
        sb.append("b_i = (y_i - y_{i-1})/h_i - c_i*h_i\n");
        for (int i = 1; i < splineB.length; i++) {
            sb.append(String.format("b_%d = (%s - %s)/%s - c_%d*%s\n",
                    i, df.format(pointsY.get(i)), df.format(pointsY.get(i-1)),
                    df.format(splineH[i]), i, df.format(splineH[i])));
        }
        sb.append("\n");

        sb.append("ШАГ 6: Система уравнений для c_i из условия непрерывности:\n");
        sb.append("c_i*h_i + c_{i+1}*h_{i+1} = g_i, где\n");
        sb.append("g_i = (y_{i+1} - y_i)/h_{i+1} - (y_i - y_{i-1})/h_i\n\n");

        sb.append("Вычисляем правые части g_i:\n");
        for (int i = 1; i < pointsX.size() - 1; i++) {
            double g = (pointsY.get(i+1) - pointsY.get(i)) / splineH[i+1]
                    - (pointsY.get(i) - pointsY.get(i-1)) / splineH[i];
            sb.append(String.format("g_%d = (%s - %s)/%s - (%s - %s)/%s = %s\n",
                    i, df.format(pointsY.get(i+1)), df.format(pointsY.get(i)), df.format(splineH[i+1]),
                    df.format(pointsY.get(i)), df.format(pointsY.get(i-1)), df.format(splineH[i]),
                    df.format(g)));
        }
        sb.append("\n");

        sb.append("ШАГ 7: Дополнительное условие (производная на правом конце = 0):\n");
        sb.append("P_n'(x_n) = b_n + 2*c_n*h_n = 0\n");
        sb.append("=> c_n*h_n = -b_n/2\n");
        sb.append("Из выражения для b_n: c_n*h_n = g_n, где\n");
        double g_n = -(pointsY.get(pointsX.size()-1) - pointsY.get(pointsX.size()-2)) / splineH[splineH.length-1];
        sb.append(String.format("g_n = -(y_n - y_{n-1})/h_n = -(%s - %s)/%s = %s\n",
                df.format(pointsY.get(pointsX.size()-1)), df.format(pointsY.get(pointsX.size()-2)),
                df.format(splineH[splineH.length-1]), df.format(g_n)));
        sb.append("\n");

        sb.append("ШАГ 8: Решаем систему уравнений (обратный ход):\n");
        sb.append(String.format("1) c_%d*h_%d = g_%d\n",
                splineC.length-1, splineC.length-1, splineC.length-1));
        sb.append(String.format("   c_%d = g_%d/h_%d = %s/%s = %s\n",
                splineC.length-1, splineC.length-1, splineC.length-1,
                df.format(g_n), df.format(splineH[splineH.length-1]), df.format(splineC[splineC.length-1])));

        for (int i = splineC.length-2; i >= 1; i--) {
            sb.append(String.format("%d) c_%d*h_%d + c_%d*h_%d = g_%d\n",
                    splineC.length-i, i, i, i+1, i+1, i));
            sb.append(String.format("   c_%d = (g_%d - c_%d*h_%d)/h_%d\n",
                    i, i, i+1, i+1, i));
            sb.append(String.format("   c_%d = (%s - %s*%s)/%s = %s\n",
                    i, df.format(splineG[i]), df.format(splineC[i+1]),
                    df.format(splineH[i+1]), df.format(splineH[i]), df.format(splineC[i])));
        }
        sb.append("\n");

        sb.append("ШАГ 9: Вычисляем коэффициенты b_i:\n");
        for (int i = 1; i < splineB.length; i++) {
            sb.append(String.format("b_%d = (%s - %s)/%s - %s*%s = %s\n",
                    i, df.format(pointsY.get(i)), df.format(pointsY.get(i-1)),
                    df.format(splineH[i]), df.format(splineC[i]), df.format(splineH[i]),
                    df.format(splineB[i])));
        }
        sb.append("\n");

        sb.append("ШАГ 10: Итоговые уравнения сплайна:\n");
        for (int i = 1; i < splineA.length; i++) {
            sb.append(String.format("P_%d(x) = %s + %s*(x - %s) + %s*(x - %s)²\n",
                    i, df.format(splineA[i]), df.format(splineB[i]),
                    df.format(pointsX.get(i-1)), df.format(splineC[i]), df.format(pointsX.get(i-1))));
            sb.append(String.format("   для x ∈ [%s, %s]\n",
                    df.format(pointsX.get(i-1)), df.format(pointsX.get(i))));
            sb.append("\n");
        }

        splineArea.setText(sb.toString());
    }

    // Метод для расчета полинома Лагранжа
    private List<Point2D.Double> calculateLagrange(double[] x, double[] y) {
        List<Point2D.Double> result = new ArrayList<>();
        int resolution = (Integer) resolutionSpinner.getValue();

        // Находим min и max X
        double minX = x[0], maxX = x[0];
        for (double xi : x) {
            if (xi < minX) minX = xi;
            if (xi > maxX) maxX = xi;
        }

        // Расширяем диапазон для лучшего отображения
        double range = maxX - minX;
        double startX = minX - range * 0.2;
        double endX = maxX + range * 0.2;
        double step = (endX - startX) / resolution;

        // Сохраняем базисные полиномы для отображения в решении
        lagrangePolynomials = new String[x.length];
        for (int i = 0; i < x.length; i++) {
            lagrangePolynomials[i] = generateLagrangePolynomialString(i, x);
        }

        for (double xi = startX; xi <= endX; xi += step) {
            double yi = lagrangeInterpolation(xi, x, y);
            result.add(new Point2D.Double(xi, yi));
        }

        return result;
    }

    private String generateLagrangePolynomialString(int index, double[] x) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int j = 0; j < x.length; j++) {
            if (j != index) {
                sb.append(String.format("(x - %s)", df.format(x[j])));
                if (j < x.length - 1) {
                    boolean skip = (j == x.length - 2 && index == x.length - 1);
                    if (!skip) sb.append("*");
                }
            }
        }
        sb.append(")/(");
        for (int j = 0; j < x.length; j++) {
            if (j != index) {
                sb.append(String.format("(%s - %s)", df.format(x[index]), df.format(x[j])));
                if (j < x.length - 1) {
                    boolean skip = (j == x.length - 2 && index == x.length - 1);
                    if (!skip) sb.append("*");
                }
            }
        }
        sb.append(")");
        return sb.toString();
    }

    // Интерполяция Лагранжа в точке
    private double lagrangeInterpolation(double pointX, double[] x, double[] y) {
        double result = 0.0;
        int n = x.length;

        for (int i = 0; i < n; i++) {
            double term = y[i];
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    term *= (pointX - x[j]) / (x[i] - x[j]);
                }
            }
            result += term;
        }

        return result;
    }

    // Метод для расчета параболического сплайна
    private List<Point2D.Double> calculateParabolicSpline(double[] x, double[] y) {
        int n = x.length - 1; // Количество интервалов

        // Массивы для коэффициентов
        splineH = new double[n + 1]; // h[i] = x[i] - x[i-1]
        splineA = new double[n + 1];
        splineB = new double[n + 1];
        splineC = new double[n + 1];
        splineG = new double[n + 1];

        // Вычисляем h
        for (int i = 1; i <= n; i++) {
            splineH[i] = x[i] - x[i - 1];
        }

        // Устанавливаем a[i] = y[i-1]
        for (int i = 1; i <= n; i++) {
            splineA[i] = y[i - 1];
        }

        // Вычисляем g[i]
        // Для внутренних узлов
        for (int i = 1; i < n; i++) {
            splineG[i] = (y[i + 1] - y[i]) / splineH[i + 1] - (y[i] - y[i - 1]) / splineH[i];
        }

        // Дополнительное условие: c[n] * h[n] = g[n]
        splineG[n] = -(y[n] - y[n - 1]) / splineH[n];

        // Решаем систему для c[i] (обратный ход)
        splineC[n] = splineG[n] / splineH[n];
        for (int i = n - 1; i >= 1; i--) {
            splineC[i] = (splineG[i] - splineH[i + 1] * splineC[i + 1]) / splineH[i];
        }

        // Вычисляем b[i]
        for (int i = 1; i <= n; i++) {
            splineB[i] = (y[i] - y[i - 1]) / splineH[i] - splineC[i] * splineH[i];
        }

        // Генерируем точки сплайна
        List<Point2D.Double> result = new ArrayList<>();
        int pointsPerSegment = (Integer) resolutionSpinner.getValue() / n;
        if (pointsPerSegment < 5) pointsPerSegment = 5;

        for (int i = 1; i <= n; i++) {
            double xStart = x[i - 1];
            double xEnd = x[i];
            double step = (xEnd - xStart) / pointsPerSegment;

            for (double xi = xStart; xi <= xEnd; xi += step) {
                double dx = xi - xStart;
                double yi = splineA[i] + splineB[i] * dx + splineC[i] * dx * dx;
                result.add(new Point2D.Double(xi, yi));
            }
        }

        // Добавляем последнюю точку
        double dx = x[n] - x[n - 1];
        double yn = splineA[n] + splineB[n] * dx + splineC[n] * dx * dx;
        result.add(new Point2D.Double(x[n], yn));

        return result;
    }

    // Внутренний класс для отрисовки графиков
    class GraphPanel extends JPanel {
        private List<Double> originalX;
        private List<Double> originalY;
        private List<Point2D.Double> lagrangePoints;
        private List<Point2D.Double> splinePoints;
        private boolean showLagrange, showSpline, showPoints;

        // Параметры масштабирования и смещения
        private double viewMinX, viewMaxX, viewMinY, viewMaxY;
        private double scaleX, scaleY;
        private int padding = 60;

        // Для перетаскивания мышью
        private Point dragStartPoint = null;
        private double dragStartMinX, dragStartMaxX, dragStartMinY, dragStartMaxY;

        public GraphPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createTitledBorder("График"));

            // Добавляем слушатели мыши для масштабирования и панорамирования
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e) || e.isControlDown()) {
                        dragStartPoint = e.getPoint();
                        dragStartMinX = viewMinX;
                        dragStartMaxX = viewMaxX;
                        dragStartMinY = viewMinY;
                        dragStartMaxY = viewMaxY;
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    dragStartPoint = null;
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragStartPoint != null) {
                        int dx = e.getX() - dragStartPoint.x;
                        int dy = e.getY() - dragStartPoint.y;

                        // Преобразуем смещение в пикселях в смещение в координатах графика
                        double xRange = viewMaxX - viewMinX;
                        double yRange = viewMaxY - viewMinY;
                        int width = getWidth() - 2 * padding;
                        int height = getHeight() - 2 * padding;

                        double dxWorld = -dx * xRange / width;
                        double dyWorld = dy * yRange / height; // Ось Y инвертирована

                        viewMinX = dragStartMinX + dxWorld;
                        viewMaxX = dragStartMaxX + dxWorld;
                        viewMinY = dragStartMinY + dyWorld;
                        viewMaxY = dragStartMaxY + dyWorld;

                        calculateScales();
                        repaint();
                    }
                }
            });

            addMouseWheelListener(e -> {
                double zoomFactor = 1.1;
                if (e.getWheelRotation() > 0) {
                    zoomFactor = 1 / zoomFactor;
                }

                int mouseX = e.getX();
                int mouseY = e.getY();

                // Преобразуем координаты мыши в мировые координаты
                double mouseWorldX = viewMinX + (mouseX - padding) * (viewMaxX - viewMinX) / (getWidth() - 2 * padding);
                double mouseWorldY = viewMaxY - (mouseY - padding) * (viewMaxY - viewMinY) / (getHeight() - 2 * padding);

                // Масштабируем относительно положения мыши
                viewMinX = mouseWorldX - (mouseWorldX - viewMinX) * zoomFactor;
                viewMaxX = mouseWorldX + (viewMaxX - mouseWorldX) * zoomFactor;
                viewMinY = mouseWorldY - (mouseWorldY - viewMinY) * zoomFactor;
                viewMaxY = mouseWorldY + (viewMaxY - mouseWorldY) * zoomFactor;

                calculateScales();
                repaint();
            });

            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    calculateScales();
                    repaint();
                }
            });
        }

        public void setData(List<Double> origX, List<Double> origY,
                            List<Point2D.Double> lagrange, List<Point2D.Double> spline,
                            boolean showLag, boolean showSpl, boolean showPt) {
            this.originalX = origX;
            this.originalY = origY;
            this.lagrangePoints = lagrange;
            this.splinePoints = spline;
            this.showLagrange = showLag;
            this.showSpline = showSpl;
            this.showPoints = showPt;

            calculateScales();
            repaint();
        }

        public void clearGraphs() {
            originalX = null;
            originalY = null;
            lagrangePoints = null;
            splinePoints = null;
            repaint();
        }

        public void autoZoom() {
            if (originalX == null || originalX.isEmpty()) {
                viewMinX = -5; viewMaxX = 5; viewMinY = -5; viewMaxY = 5;
                calculateScales();
                repaint();
                return;
            }

            // Находим границы всех данных
            double minX = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double maxY = -Double.MAX_VALUE;

            // Исходные точки
            for (int i = 0; i < originalX.size(); i++) {
                double x = originalX.get(i);
                double y = originalY.get(i);
                if (x < minX) minX = x;
                if (x > maxX) maxX = x;
                if (y < minY) minY = y;
                if (y > maxY) maxY = y;
            }

            // Точки Лагранжа
            if (lagrangePoints != null && showLagrange) {
                for (Point2D.Double p : lagrangePoints) {
                    if (p.x < minX) minX = p.x;
                    if (p.x > maxX) maxX = p.x;
                    if (p.y < minY) minY = p.y;
                    if (p.y > maxY) maxY = p.y;
                }
            }

            // Точки сплайна
            if (splinePoints != null && showSpline) {
                for (Point2D.Double p : splinePoints) {
                    if (p.x < minX) minX = p.x;
                    if (p.x > maxX) maxX = p.x;
                    if (p.y < minY) minY = p.y;
                    if (p.y > maxY) maxY = p.y;
                }
            }

            // Добавляем отступы 10%
            double xRange = maxX - minX;
            double yRange = maxY - minY;

            if (xRange == 0) xRange = 2;
            if (yRange == 0) yRange = 2;

            viewMinX = minX - xRange * 0.1;
            viewMaxX = maxX + xRange * 0.1;
            viewMinY = minY - yRange * 0.1;
            viewMaxY = maxY + yRange * 0.1;

            calculateScales();
            repaint();
        }

        public void zoomIn() {
            double zoomFactor = 1.2;
            double centerX = (viewMinX + viewMaxX) / 2;
            double centerY = (viewMinY + viewMaxY) / 2;

            double newWidth = (viewMaxX - viewMinX) / zoomFactor;
            double newHeight = (viewMaxY - viewMinY) / zoomFactor;

            viewMinX = centerX - newWidth / 2;
            viewMaxX = centerX + newWidth / 2;
            viewMinY = centerY - newHeight / 2;
            viewMaxY = centerY + newHeight / 2;

            calculateScales();
            repaint();
        }

        public void zoomOut() {
            double zoomFactor = 1.2;
            double centerX = (viewMinX + viewMaxX) / 2;
            double centerY = (viewMinY + viewMaxY) / 2;

            double newWidth = (viewMaxX - viewMinX) * zoomFactor;
            double newHeight = (viewMaxY - viewMinY) * zoomFactor;

            viewMinX = centerX - newWidth / 2;
            viewMaxX = centerX + newWidth / 2;
            viewMinY = centerY - newHeight / 2;
            viewMaxY = centerY + newHeight / 2;

            calculateScales();
            repaint();
        }

        public void panLeft() {
            double xRange = viewMaxX - viewMinX;
            viewMinX -= xRange * 0.1;
            viewMaxX -= xRange * 0.1;
            calculateScales();
            repaint();
        }

        public void panRight() {
            double xRange = viewMaxX - viewMinX;
            viewMinX += xRange * 0.1;
            viewMaxX += xRange * 0.1;
            calculateScales();
            repaint();
        }

        public void panUp() {
            double yRange = viewMaxY - viewMinY;
            viewMinY += yRange * 0.1;
            viewMaxY += yRange * 0.1;
            calculateScales();
            repaint();
        }

        public void panDown() {
            double yRange = viewMaxY - viewMinY;
            viewMinY -= yRange * 0.1;
            viewMaxY -= yRange * 0.1;
            calculateScales();
            repaint();
        }

        public void resetZoom() {
            viewMinX = -10; viewMaxX = 10; viewMinY = -10; viewMaxY = 10;
            calculateScales();
            repaint();
        }

        private void calculateScales() {
            int width = getWidth() - 2 * padding;
            int height = getHeight() - 2 * padding;

            if (width > 0 && height > 0) {
                scaleX = width / (viewMaxX - viewMinX);
                scaleY = height / (viewMaxY - viewMinY);
            } else {
                scaleX = 1;
                scaleY = 1;
            }
        }

        private int transformX(double x) {
            return padding + (int)((x - viewMinX) * scaleX);
        }

        private int transformY(double y) {
            return getHeight() - padding - (int)((y - viewMinY) * scaleY);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Рисуем информацию о текущем масштабе
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            String scaleInfo = String.format("X: [%.2f, %.2f]  Y: [%.2f, %.2f]",
                    viewMinX, viewMaxX, viewMinY, viewMaxY);
            g2d.drawString(scaleInfo, 10, getHeight() - 10);
            g2d.drawString("Управление: ПКМ+перетаскивание - панорамирование, Колесо мыши - масштаб",
                    10, getHeight() - 25);

            drawGrid(g2d);
            drawAxes(g2d);

            if (showLagrange && lagrangePoints != null && !lagrangePoints.isEmpty()) {
                drawLagrange(g2d);
            }

            if (showSpline && splinePoints != null && !splinePoints.isEmpty()) {
                drawSpline(g2d);
            }

            if (showPoints && originalX != null && !originalX.isEmpty()) {
                drawPoints(g2d);
            }

            drawLegend(g2d);
        }

        private void drawGrid(Graphics2D g2d) {
            g2d.setColor(new Color(240, 240, 240));

            // Вычисляем шаг сетки
            double xStep = getGridStep(viewMaxX - viewMinX);
            double yStep = getGridStep(viewMaxY - viewMinY);

            // Вертикальные линии
            double startX = Math.ceil(viewMinX / xStep) * xStep;
            for (double x = startX; x <= viewMaxX; x += xStep) {
                int xCoord = transformX(x);
                if (xCoord >= padding && xCoord <= getWidth() - padding) {
                    g2d.drawLine(xCoord, padding, xCoord, getHeight() - padding);
                }
            }

            // Горизонтальные линии
            double startY = Math.ceil(viewMinY / yStep) * yStep;
            for (double y = startY; y <= viewMaxY; y += yStep) {
                int yCoord = transformY(y);
                if (yCoord >= padding && yCoord <= getHeight() - padding) {
                    g2d.drawLine(padding, yCoord, getWidth() - padding, yCoord);
                }
            }
        }

        private double getGridStep(double range) {
            double[] steps = {0.1, 0.2, 0.5, 1, 2, 5, 10, 20, 50, 100};
            for (double step : steps) {
                if (range / step <= 20) {
                    return step;
                }
            }
            return 100;
        }

        private void drawAxes(Graphics2D g2d) {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));

            // Ось X
            int yAxis = transformY(0);
            if (yAxis >= padding && yAxis <= getHeight() - padding) {
                g2d.drawLine(padding, yAxis, getWidth() - padding, yAxis);
            }

            // Ось Y
            int xAxis = transformX(0);
            if (xAxis >= padding && xAxis <= getWidth() - padding) {
                g2d.drawLine(xAxis, padding, xAxis, getHeight() - padding);
            }

            g2d.setFont(new Font("Arial", Font.PLAIN, 10));

            // Подписи по оси X
            double xStep = getGridStep(viewMaxX - viewMinX);
            double startX = Math.ceil(viewMinX / xStep) * xStep;
            for (double x = startX; x <= viewMaxX; x += xStep) {
                int xCoord = transformX(x);
                if (xCoord >= padding && xCoord <= getWidth() - padding) {
                    g2d.drawString(dfSimple.format(x), xCoord - 10, transformY(0) + 15);
                    g2d.drawLine(xCoord, transformY(0) - 3, xCoord, transformY(0) + 3);
                }
            }

            // Подписи по оси Y
            double yStep = getGridStep(viewMaxY - viewMinY);
            double startY = Math.ceil(viewMinY / yStep) * yStep;
            for (double y = startY; y <= viewMaxY; y += yStep) {
                int yCoord = transformY(y);
                if (yCoord >= padding && yCoord <= getHeight() - padding) {
                    g2d.drawString(dfSimple.format(y), transformX(0) + 5, yCoord + 5);
                    g2d.drawLine(transformX(0) - 3, yCoord, transformX(0) + 3, yCoord);
                }
            }

            // Названия осей
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString("X", getWidth() - padding + 10, transformY(0) - 10);
            g2d.drawString("Y", transformX(0) + 10, padding - 10);
        }

        private void drawLagrange(Graphics2D g2d) {
            g2d.setColor(new Color(0, 100, 200));
            g2d.setStroke(new BasicStroke(2));

            Point2D.Double prevPoint = null;
            for (Point2D.Double p : lagrangePoints) {
                if (p.x >= viewMinX && p.x <= viewMaxX && p.y >= viewMinY && p.y <= viewMaxY) {
                    if (prevPoint != null) {
                        int x1 = transformX(prevPoint.x);
                        int y1 = transformY(prevPoint.y);
                        int x2 = transformX(p.x);
                        int y2 = transformY(p.y);

                        // Проверяем, чтобы линия была в пределах графика
                        if (Math.abs(x1 - x2) < getWidth() * 2 && Math.abs(y1 - y2) < getHeight() * 2) {
                            g2d.drawLine(x1, y1, x2, y2);
                        }
                    }
                    prevPoint = p;
                } else {
                    prevPoint = null;
                }
            }
        }

        private void drawSpline(Graphics2D g2d) {
            g2d.setColor(new Color(200, 0, 100));
            g2d.setStroke(new BasicStroke(2));

            Point2D.Double prevPoint = null;
            for (Point2D.Double p : splinePoints) {
                if (p.x >= viewMinX && p.x <= viewMaxX && p.y >= viewMinY && p.y <= viewMaxY) {
                    if (prevPoint != null) {
                        int x1 = transformX(prevPoint.x);
                        int y1 = transformY(prevPoint.y);
                        int x2 = transformX(p.x);
                        int y2 = transformY(p.y);

                        if (Math.abs(x1 - x2) < getWidth() * 2 && Math.abs(y1 - y2) < getHeight() * 2) {
                            g2d.drawLine(x1, y1, x2, y2);
                        }
                    }
                    prevPoint = p;
                } else {
                    prevPoint = null;
                }
            }
        }

        private void drawPoints(Graphics2D g2d) {
            for (int i = 0; i < originalX.size(); i++) {
                double x = originalX.get(i);
                double y = originalY.get(i);

                if (x >= viewMinX && x <= viewMaxX && y >= viewMinY && y <= viewMaxY) {
                    int screenX = transformX(x);
                    int screenY = transformY(y);

                    // Рисуем точку большего размера
                    g2d.setColor(new Color(0, 150, 0));
                    g2d.fillOval(screenX - 7, screenY - 7, 14, 14);
                    g2d.setColor(Color.WHITE);
                    g2d.fillOval(screenX - 4, screenY - 4, 8, 8);
                    g2d.setColor(new Color(0, 150, 0));
                    g2d.fillOval(screenX - 2, screenY - 2, 4, 4);

                    // Подпись точки
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                    g2d.drawString("P" + i, screenX + 10, screenY - 10);
                }
            }
        }

        private void drawLegend(Graphics2D g2d) {
            int legendX = getWidth() - 200;
            int legendY = padding + 20;

            g2d.setColor(new Color(255, 255, 255, 230));
            g2d.fillRect(legendX - 10, legendY - 20, 190, 100);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(legendX - 10, legendY - 20, 190, 100);

            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString("Легенда:", legendX, legendY);

            g2d.setFont(new Font("Arial", Font.PLAIN, 11));

            if (showPoints) {
                g2d.setColor(new Color(0, 150, 0));
                g2d.fillOval(legendX, legendY + 20, 10, 10);
                g2d.setColor(Color.BLACK);
                g2d.drawString("Исходные точки", legendX + 20, legendY + 28);
            }

            if (showLagrange) {
                g2d.setColor(new Color(0, 100, 200));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(legendX, legendY + 45, legendX + 15, legendY + 45);
                g2d.setColor(Color.BLACK);
                g2d.drawString("Полином Лагранжа", legendX + 20, legendY + 50);
            }

            if (showSpline) {
                g2d.setColor(new Color(200, 0, 100));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(legendX, legendY + 70, legendX + 15, legendY + 70);
                g2d.setColor(Color.BLACK);
                g2d.drawString("Параболический сплайн", legendX + 20, legendY + 75);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new InterpolationApp();
        });
    }
}