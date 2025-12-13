import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CalculatorApp extends Application {

    private final TextField display = new TextField("0");

    private Double storedValue = null;     // 已确认的左操作数
    private String pendingOp = null;       // 当前运算符: + - * /
    private boolean startNewNumber = true; // 是否要开始输入新数字

    @Override
    public void start(Stage stage) {
        display.setEditable(false);
        display.setAlignment(Pos.CENTER_RIGHT);
        display.setPrefHeight(50);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        grid.setAlignment(Pos.CENTER);

        // Row 0
        addBtn(grid, "C", 0, 0, e -> clear());
        addBtn(grid, "/", 1, 0, e -> operator("/"));
        addBtn(grid, "*", 2, 0, e -> operator("*"));
        addBtn(grid, "-", 3, 0, e -> operator("-"));

        // Row 1
        addBtn(grid, "7", 0, 1, e -> digit("7"));
        addBtn(grid, "8", 1, 1, e -> digit("8"));
        addBtn(grid, "9", 2, 1, e -> digit("9"));
        addBtn(grid, "+", 3, 1, e -> operator("+"));

        // Row 2
        addBtn(grid, "4", 0, 2, e -> digit("4"));
        addBtn(grid, "5", 1, 2, e -> digit("5"));
        addBtn(grid, "6", 2, 2, e -> digit("6"));
        addBtn(grid, "=", 3, 2, e -> equals());

        // Row 3
        addBtn(grid, "1", 0, 3, e -> digit("1"));
        addBtn(grid, "2", 1, 3, e -> digit("2"));
        addBtn(grid, "3", 2, 3, e -> digit("3"));

        // Row 4
        addBtn(grid, "0", 0, 4, e -> digit("0"));
        addBtn(grid, ".", 1, 4, e -> dot());

        VBox root = new VBox(10, display, grid);
        root.setPadding(new Insets(12));

        Scene scene = new Scene(root, 320, 360);
        stage.setTitle("Exercise 9 - JavaFX Calculator");
        stage.setScene(scene);
        stage.show();
    }

    private void addBtn(GridPane grid, String text, int col, int row, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button b = new Button(text);
        b.setPrefSize(70, 55);
        b.setOnAction(handler);
        grid.add(b, col, row);
    }

    private void digit(String d) {
        if (startNewNumber || display.getText().equals("0") || display.getText().equals("Error")) {
            display.setText(d);
            startNewNumber = false;
        } else {
            display.setText(display.getText() + d);
        }
    }

    private void dot() {
        if (display.getText().equals("Error")) {
            display.setText("0.");
            startNewNumber = false;
            return;
        }
        if (startNewNumber) {
            display.setText("0.");
            startNewNumber = false;
            return;
        }
        if (!display.getText().contains(".")) {
            display.setText(display.getText() + ".");
        }
    }

    private void operator(String op) {
        if (display.getText().equals("Error")) return;

        double current = Double.parseDouble(display.getText());

        if (storedValue == null) {
            storedValue = current;
        } else if (pendingOp != null && !startNewNumber) {
            storedValue = apply(storedValue, current, pendingOp);
            display.setText(format(storedValue));
        }

        pendingOp = op;
        startNewNumber = true;
    }

    private void equals() {
        if (display.getText().equals("Error")) return;
        if (storedValue == null || pendingOp == null) return;

        double current = Double.parseDouble(display.getText());
        double result = apply(storedValue, current, pendingOp);

        storedValue = result;
        pendingOp = null;
        display.setText(format(result));
        startNewNumber = true;
    }

    private double apply(double a, double b, String op) {
        try {
            return switch (op) {
                case "+" -> a + b;
                case "-" -> a - b;
                case "*" -> a * b;
                case "/" -> {
                    if (b == 0.0) throw new ArithmeticException("divide by zero");
                    yield a / b;
                }
                default -> b;
            };
        } catch (Exception ex) {
            display.setText("Error");
            storedValue = null;
            pendingOp = null;
            startNewNumber = true;
            return 0.0;
        }
    }

    private void clear() {
        display.setText("0");
        storedValue = null;
        pendingOp = null;
        startNewNumber = true;
    }

    private String format(double x) {
        // 整数就别显示 .0
        if (Math.abs(x - Math.rint(x)) < 1e-10) {
            return String.valueOf((long) Math.rint(x));
        }
        return String.valueOf(x);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
