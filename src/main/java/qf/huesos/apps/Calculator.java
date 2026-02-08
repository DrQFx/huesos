package qf.huesos.apps;

import qf.huesos.kernel.RunnableApplication;
import qf.huesos.kernel.Screen;
import qf.huesos.kernel.graphics.TUI.Button;
import qf.huesos.kernel.graphics.TUI.Label;
import qf.huesos.kernel.graphics.TerminalApplication;


public class Calculator extends TerminalApplication implements RunnableApplication {
    private final int bg = screen.COLOR_BLACK;
    private final String path = "fs/system/apps/calculator/main.htui";

    private int result;
    private CalculatorModel calc = new CalculatorModel();

    public Calculator(Screen screen) throws Exception {
        super(screen);
        setBgColor(bg);
        setPath(path);
        start();
    }

    @Override
    protected void onUiLoaded() {
        bindButtons();
    }

    @Override
    public void run(Screen screen) throws Exception {
        Calculator calculator = new Calculator(screen);
    }

    private void bindButtons() {
        Label result = (Label) TUIElementsByID.get("result");

        for (int i = 0; i <= 9; i++) {
            Button btn = (Button) TUIElementsByID.get("num_" + i);
            if (btn == null)
                throw new IllegalStateException("Button num_" + i + " not found");
            int digit = i;

            btn.onClick = () -> {
                calc.digit(digit);
                result.text = String.valueOf(calc.getCurrent());
            };
        }

        ((Button) TUIElementsByID.get("act_plus")).onClick = () -> {
            calc.plus();
            result.text = "0";
        };

        ((Button) TUIElementsByID.get("act_minus")).onClick = () -> {
            calc.minus();
            result.text = "0";
        };

        ((Button) TUIElementsByID.get("equal")).onClick = () -> {
            int value = calc.equal();
            result.text = String.valueOf(value);
        };
    }
}

class CalculatorModel {
    private int current = 0;
    private int stored = 0;
    private char operation = 0;

    public void digit(int d) {
        current = current * 10 + d;
    }

    public void plus() {
        stored = current;
        current = 0;
        operation = '+';
    }

    public void minus() {
        stored = current;
        current = 0;
        operation = '-';
    }

    public int equal() {
        int result = switch (operation) {
            case '+' -> stored + current;
            case '-' -> stored - current;
            default -> current;
        };
        current = result;
        operation = 0;
        return result;
    }

    public int getCurrent() {
        return current;
    }

    public void clear() {
        current = 0;
        stored = 0;
        operation = 0;
    }
}
