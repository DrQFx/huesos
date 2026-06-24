package qf.huesos.provider;

import qf.huesos.kernel.Kernel;
import qf.huesos.kernel.Screen;
import qf.huesos.kernel.scheduler.Process;
import qf.huesos.kernel.scheduler.RunnableApplication;
import qf.huesos.kernel.SysCall;

import java.util.*;

public class RAHandler implements RunnableApplication {

    private final SysCall sys = new SysCall();

    // Память: 64K ячеек (int)
    private static final int MEM_SIZE = 65536;
    private int[] memory = new int[MEM_SIZE];

    // Стек данных и стек вызовов (используем Deque вместо Stack)
    private Deque<Integer> dataStack = new ArrayDeque<>();
    private Deque<Integer> callStack = new ArrayDeque<>();

    // Список инструкций (после парсинга) и счётчик команд
    private List<Instruction> instructions = new ArrayList<>();
    private int ip = 0;             // индекс текущей инструкции

    // Метки: имя -> индекс в instructions
    private Map<String, Integer> labels = new HashMap<>();

    // Флаг работы
    private boolean running = false;

    // Метаданные из секции .info
    private Map<String, String> appInfo = new HashMap<>();

    private String name;

    @Override
    public void step(Screen screen, Process process) throws Exception {

    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    // Вспомогательный класс для инструкции
    private static class Instruction {
        String opcode;
        String operand;   // может быть числом или меткой
        int lineNumber;    // для сообщений об ошибках

        Instruction(String opcode, String operand, int lineNumber) {
            this.opcode = opcode;
            this.operand = operand;
            this.lineNumber = lineNumber;
        }
    }

    // Основной метод запуска
    public void run(String fileName) throws Exception {
        // Сброс состояния
        memory = new int[MEM_SIZE];
        dataStack.clear();
        callStack.clear();
        instructions.clear();
        labels.clear();
        appInfo.clear();
        ip = 0;
        running = true;

        // Чтение и парсинг файла
        List<String> rawLines = sys.parseFile(fileName);
        parseProgram(rawLines);

        // Выполнение
        while (running && ip < instructions.size()) {
            Instruction ins = instructions.get(ip);
            ip++;
            try {
                execute(ins);
            } catch (Exception e) {
                throw new Exception(
                        "Ошибка выполнения (строка " + ins.lineNumber + "): " + e.getMessage(), e);
            }
        }
    }

    /**
     * Разбор исходного кода на секции.
     */
    private void parseProgram(List<String> lines) throws Exception {
        String currentSection = null;
        int lineNumber = 0;
        int dataPtr = 0;        // текущий адрес для размещения данных

        for (String rawLine : lines) {
            lineNumber++;
            String line = rawLine.trim();

            // Удаляем комментарии (всё после ';')
            int commentIdx = line.indexOf(';');
            if (commentIdx != -1) {
                line = line.substring(0, commentIdx).trim();
            }
            if (line.isEmpty()) continue;

            // Обработка директив секций
            if (line.startsWith(".info")) {
                currentSection = "info";
                continue;
            } else if (line.startsWith(".data")) {
                currentSection = "data";
                continue;
            } else if (line.startsWith(".code")) {
                currentSection = "code";
                continue;
            }

            if (currentSection == null) {
                throw new Exception("Строка " + lineNumber + ": код вне секции");
            }

            switch (currentSection) {
                case "info":
                    parseInfoLine(line, lineNumber);
                    break;
                case "data":
                    dataPtr = parseDataLine(line, lineNumber, dataPtr);
                    break;
                case "code":
                    parseCodeLine(line, lineNumber);
                    break;
            }
        }

        // После сбора инструкций заменяем метки в операндах на адреса
        resolveLabels();
    }

    /**
     * Разбор строки секции .info (формат: ключ=значение)
     */
    private void parseInfoLine(String line, int lineNumber) throws Exception {
        String[] parts = line.split("=", 2);
        if (parts.length != 2) {
            throw new Exception("Строка " + lineNumber + ": неверный формат .info (ожидается ключ=значение)");
        }
        appInfo.put(parts[0].trim(), parts[1].trim());
    }

    /**
     * Разбор строки секции .data.
     * Возвращает новый указатель данных.
     */
    private int parseDataLine(String line, int lineNumber, int dataPtr) throws Exception {
        String[] tokens = line.trim().split("\\s+");
        String directive = tokens[0].trim();

        switch (directive) {
            case ".word":
                for (int i = 1; i < tokens.length; i++) {
                    if (dataPtr >= MEM_SIZE)
                        throw new Exception("Строка " + lineNumber + ": превышение размера памяти");
                    try {
                        int val = Integer.parseInt(tokens[i]);
                        memory[dataPtr++] = val;
                    } catch (NumberFormatException e) {
                        throw new Exception("Строка " + lineNumber + ": неверное число в .word");
                    }
                }
                break;

            case ".string":
                if (tokens.length < 2) {
                    throw new Exception("Строка " + lineNumber + ": .string требует аргумент");
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < tokens.length; i++) {
                    if (i > 1) sb.append(' ');
                    sb.append(tokens[i]);
                }
                String str = sb.toString();
                if (str.startsWith("\"") && str.endsWith("\"")) {
                    str = str.substring(1, str.length() - 1);
                }
                for (char c : str.toCharArray()) {
                    if (dataPtr >= MEM_SIZE)
                        throw new Exception("Строка " + lineNumber + ": превышение размера памяти");
                    memory[dataPtr++] = (int) c;
                }
                if (dataPtr >= MEM_SIZE)
                    throw new Exception("Строка " + lineNumber + ": превышение размера памяти");
                memory[dataPtr++] = 0; // null-терминатор
                break;

            case ".space":
                if (tokens.length < 2) {
                    throw new Exception("Строка " + lineNumber + ": .space требует размер");
                }
                try {
                    int size = Integer.parseInt(tokens[1]);
                    if (dataPtr + size >= MEM_SIZE) {
                        throw new Exception("Строка " + lineNumber + ": превышение размера памяти");
                    }
                    dataPtr += size;
                } catch (NumberFormatException e) {
                    throw new Exception("Строка " + lineNumber + ": неверный размер в .space");
                }
                break;

            default:
                throw new Exception("Строка " + lineNumber + ": неизвестная директива данных " + directive);
        }

        return dataPtr;
    }

    /**
     * Разбор строки секции .code.
     */
    private void parseCodeLine(String line, int lineNumber) throws Exception {
        // Проверка на метку (строка заканчивается двоеточием)
        if (line.endsWith(":")) {
            String label = line.substring(0, line.length() - 1).trim(); // trim() исправлено
            if (labels.containsKey(label)) {
                throw new Exception("Строка " + lineNumber + ": метка " + label + " уже определена");
            }
            labels.put(label, instructions.size());
            return;
        }

        // Разбор инструкции
        String[] parts = line.split("\\s+", 2);
        String opcode = parts[0].toUpperCase();
        String operand = (parts.length > 1) ? parts[1].trim() : null;

        if (!isValidOpcode(opcode)) {
            throw new Exception("Строка " + lineNumber + ": неизвестный опкод " + opcode);
        }

        if (requiresOperand(opcode) && operand == null) {
            throw new Exception("Строка " + lineNumber + ": инструкция " + opcode + " требует операнд");
        }

        instructions.add(new Instruction(opcode, operand, lineNumber));
    }

    private boolean isValidOpcode(String op) {
        return switch (op) {
            case "PUSH", "POP", "DUP", "SWAP",
                 "ADD", "SUB", "MUL", "DIV", "MOD",
                 "EQ", "NE", "LT", "GT", "LE", "GE",
                 "JMP", "JZ", "JNZ", "CALL", "RET",
                 "LOAD", "STORE", "SYSCALL", "HALT" -> true;
            default -> false;
        };
    }

    private boolean requiresOperand(String op) {
        return switch (op) {
            case "PUSH", "JMP", "JZ", "JNZ", "CALL", "SYSCALL" -> true;
            default -> false;
        };
    }

    /**
     * Замена имён меток в инструкциях на числовые адреса.
     */
    private void resolveLabels() throws Exception {
        for (Instruction ins : instructions) {
            if (ins.operand == null) continue;

            boolean isJump = switch (ins.opcode) {
                case "JMP", "JZ", "JNZ", "CALL" -> true;
                default -> false;
            };

            if (isJump) {
                Integer target = labels.get(ins.operand);
                if (target == null) {
                    throw new Exception("Строка " + ins.lineNumber + ": метка " + ins.operand + " не найдена");
                }
                ins.operand = String.valueOf(target);
            }
        }
    }

    /**
     * Выполнение одной инструкции.
     */
    private void execute(Instruction ins) throws Exception {
        String op = ins.opcode;

        switch (op) {
            case "PUSH" -> {
                int val = parseIntOperand(ins);
                dataStack.push(val);
            }
            case "POP" -> {
                checkStack(1);
                dataStack.pop();
            }
            case "DUP" -> {
                checkStack(1);
                dataStack.push(dataStack.peek());
            }
            case "SWAP" -> {
                checkStack(2);
                int a = dataStack.pop();
                int b = dataStack.pop();
                dataStack.push(a);
                dataStack.push(b);
            }

            // Арифметика
            case "ADD" -> binaryOp((a, b) -> a + b);
            case "SUB" -> binaryOp((a, b) -> a - b);
            case "MUL" -> binaryOp((a, b) -> a * b);
            case "DIV" -> binaryOp((a, b) -> {
                if (b == 0) throw new ArithmeticException("Деление на ноль");
                return a / b;
            });
            case "MOD" -> binaryOp((a, b) -> {
                if (b == 0) throw new ArithmeticException("Деление на ноль");
                return a % b;
            });

            // Сравнения
            case "EQ" -> binaryCmp((a, b) -> a == b);
            case "NE" -> binaryCmp((a, b) -> a != b);
            case "LT" -> binaryCmp((a, b) -> a < b);
            case "GT" -> binaryCmp((a, b) -> a > b);
            case "LE" -> binaryCmp((a, b) -> a <= b);
            case "GE" -> binaryCmp((a, b) -> a >= b);

            // Переходы
            case "JMP" -> {
                int target = parseIntOperand(ins);
                checkInstructionAddress(target, ins.lineNumber);
                ip = target;
            }
            case "JZ" -> {
                checkStack(1);
                int cond = dataStack.pop();
                if (cond == 0) {
                    int target = parseIntOperand(ins);
                    checkInstructionAddress(target, ins.lineNumber);
                    ip = target;
                }
            }
            case "JNZ" -> {
                checkStack(1);
                int cond = dataStack.pop();
                if (cond != 0) {
                    int target = parseIntOperand(ins);
                    checkInstructionAddress(target, ins.lineNumber);
                    ip = target;
                }
            }
            case "CALL" -> {
                int target = parseIntOperand(ins);
                checkInstructionAddress(target, ins.lineNumber);
                callStack.push(ip);
                ip = target;
            }
            case "RET" -> {
                if (callStack.isEmpty()) {
                    throw new Exception("Строка " + ins.lineNumber + ": RET при пустом стеке вызовов");
                }
                ip = callStack.pop();
            }

            // Память
            case "LOAD" -> {
                checkStack(1);
                int addr = dataStack.pop();
                checkAddress(addr, ins.lineNumber);
                dataStack.push(memory[addr]);
            }
            case "STORE" -> {
                checkStack(2);
                int addr = dataStack.pop();      // исправлен порядок
                int val = dataStack.pop();
                checkAddress(addr, ins.lineNumber);
                memory[addr] = val;
            }

            // Системные вызовы
            case "SYSCALL" -> {
                int num = parseIntOperand(ins);
                handleSyscall(num, ins.lineNumber);
            }

            case "HALT" -> running = false;

            default -> throw new Exception("Строка " + ins.lineNumber + ": неизвестный опкод " + op);
        }
    }

    // Вспомогательные методы

    private void checkStack(int needed) throws Exception {
        if (dataStack.size() < needed) {
            throw new Exception("Недостаточно элементов на стеке (требуется " + needed + ")");
        }
    }

    private void checkAddress(int addr, int line) throws Exception {
        if (addr < 0 || addr >= MEM_SIZE) {
            throw new Exception("Строка " + line + ": адрес " + addr + " вне допустимого диапазона");
        }
    }

    private void checkInstructionAddress(int addr, int line) throws Exception {
        if (addr < 0 || addr >= instructions.size()) {
            throw new Exception("Строка " + line + ": переход на неверный адрес " + addr);
        }
    }

    private int parseIntOperand(Instruction ins) throws Exception {
        try {
            return Integer.parseInt(ins.operand);
        } catch (NumberFormatException e) {
            throw new Exception("Строка " + ins.lineNumber + ": неверное число '" + ins.operand + "'");
        }
    }

    @FunctionalInterface
    private interface IntBinaryOperator {
        int apply(int a, int b) throws Exception;
    }

    @FunctionalInterface
    private interface IntComparator {
        boolean compare(int a, int b) throws Exception;
    }

    private void binaryOp(IntBinaryOperator op) throws Exception {
        checkStack(2);
        int b = dataStack.pop();
        int a = dataStack.pop();
        dataStack.push(op.apply(a, b));
    }

    private void binaryCmp(IntComparator cmp) throws Exception {
        checkStack(2);
        int b = dataStack.pop();
        int a = dataStack.pop();
        dataStack.push(cmp.compare(a, b) ? 1 : 0);
    }

    private void handleSyscall(int num, int line) throws Exception {
        switch (num) {
            case 1: // print int
                checkStack(1);
                int val = dataStack.pop();
                sys.print(String.valueOf(val), Kernel.screen.COLOR_WHITE);
                break;
            case 2: // print string (нуль-терминированная)
                checkStack(1);
                int addr = dataStack.pop();
                checkAddress(addr, line);
                StringBuilder sb = new StringBuilder();
                while (addr < MEM_SIZE && memory[addr] != 0) {
                    sb.append((char) memory[addr]);
                    addr++;
                }
                sys.print(sb.toString(), Kernel.screen.COLOR_WHITE);
                break;
            default:
                throw new Exception("Строка " + line + ": неизвестный системный вызов " + num);
        }
    }
}