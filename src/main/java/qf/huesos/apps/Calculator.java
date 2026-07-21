package qf.huesos.apps;

import qf.huesos.kernel.scheduler.Process;
import qf.huesos.kernel.scheduler.RunnableApplication;
import qf.huesos.kernel.Screen;
import qf.huesos.kernel.graphics.TUI.Button;
import qf.huesos.kernel.graphics.TUI.Label;
import qf.huesos.kernel.graphics.TerminalApplication;


public class Calculator extends TerminalApplication implements RunnableApplication {
    private final int bg = screen.COLOR_BLACK;
    private final String path = "fs/system/apps/calculator/general.htui";

    private int result;
    private CalculatorModel calc = new CalculatorModel();

    private String name;
    private long PROCESS_ID;

    public Calculator(Screen screen) throws Exception {
        super(screen);
        setBgColor(bg);
        setPath(path);
        setName("Calculator");
        start();
    }

    @Override
    protected void onUiLoaded() {
        bindButtons();
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
                result.text = calc.getDisplay();
            };
        }

        ((Button) TUIElementsByID.get("act_plus")).onClick = () -> {
            result.text = calc.operator('+');
        };

        ((Button) TUIElementsByID.get("act_minus")).onClick = () -> {
            result.text = calc.operator('-');
        };

        ((Button) TUIElementsByID.get("act_multiply")).onClick = () -> {
            result.text = calc.operator('*');
        };

        ((Button) TUIElementsByID.get("act_divide")).onClick = () -> {
            result.text = calc.operator('/');
        };

        ((Button) TUIElementsByID.get("clear")).onClick = () -> {
            calc.clear();
            result.text = "0";
        };

        ((Button) TUIElementsByID.get("equal")).onClick = () -> {
            result.text = calc.equal();
        };

        ((Button) TUIElementsByID.get("exit")).onClick = () -> {
            stop();
            if (PROCESS_ID != 0) {
                qf.huesos.kernel.Kernel.getInstance().getScheduler().killProcess(PROCESS_ID);
            }
        };
    }

    @Override
    public void step(Screen screen, Process process) throws Exception {
        if (PROCESS_ID == 0) {
            PROCESS_ID = process.getId();
        }
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void onStop() {
        stop();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}

class CalculatorModel {
    private int current = 0;
    private int stored = 0;
    private char operation = 0;
    private boolean enteringNewNumber = false;

    public void digit(int d) {
        if (enteringNewNumber) {
            current = d;
            enteringNewNumber = false;
            return;
        }

        current = current * 10 + d;
    }

    public String operator(char nextOperation) {
        if (operation != 0 && !enteringNewNumber) {
            String result = equal();
            if ("Error".equals(result)) {
                return result;
            }
        }

        stored = current;
        operation = nextOperation;
        enteringNewNumber = true;
        return getDisplay();
    }

    public String equal() {
        if (operation == 0) {
            enteringNewNumber = true;
            return getDisplay();
        }

        if (operation == '/' && current == 0) {
            clear();
            return "Error";
        }

        int result = switch (operation) {
            case '+' -> stored + current;
            case '-' -> stored - current;
            case '*' -> stored * current;
            case '/' -> stored / current;
            default -> current;
        };
        current = result;
        operation = 0;
        enteringNewNumber = true;
        return getDisplay();
    }

    public String getDisplay() {
        return String.valueOf(current);
    }

    public void clear() {
        current = 0;
        stored = 0;
        operation = 0;
        enteringNewNumber = false;
    }
}
