package qf.huesos.kernel.provider;

import qf.huesos.kernel.Kernel;
import qf.huesos.kernel.Screen;
import qf.huesos.kernel.provider.ra.*;
import qf.huesos.kernel.scheduler.Process;
import qf.huesos.kernel.scheduler.RunnableApplication;
import qf.huesos.kernel.SysCall;

import java.util.*;

public class RAProvider implements RunnableApplication, Provider {
    private final int MEMORY_LIMIT = 1048576;
    private long PROCESS_ID;
    private int PC = 0;

    private boolean isFinished = false;

    public String VERSION;
    public String NAME;

    Stack<Value> stack = new Stack<>();
    Stack<Frame> frameStack = new Stack<>();
    List<Value> frameArguments = new Stack<>();

    Value[] memory = new Value[MEMORY_LIMIT];
    Map<String, Integer> variables = new HashMap<>(MEMORY_LIMIT);
    Map<String, Integer> breakPoints;

    SysCall call = new SysCall();
    RAParser parser;
    List<Instruction> instructions;

    public RAProvider(String path) throws Exception {
        parser = new RAParser(call.parseFile(path));
        this.instructions = parser.parse();
        breakPoints = parser.getBreakPoints();
        PC = parser.getEntryPoint();

        try {
            NAME = parser.getName();
            VERSION = parser.getVersion();
        } catch (NullPointerException e) {

        }

        if (getName() == null) {
            setName("RA Application");
        }

        linkDependencies();

        for (Instruction instruction : instructions) {
            switch (instruction.getOpcode()) {
                case VAR -> declareVariable(instruction.getSecondValue().asString(), instruction.getValue());
                default -> {}
            }
        }

        run();
    }

    public void run() throws Exception {
        execute(this.instructions);
    }

    private void linkDependencies() throws Exception {
        Queue<String> libsToLoad = new LinkedList<>();
        Set<String> loadedLibs = new HashSet<>();

        for (Instruction inst : instructions) {
            if (inst.getOpcode() == Opcode.USE) {
                String lib = inst.getValue().asString();
                if (!loadedLibs.contains(lib)) {
                    libsToLoad.add(lib);
                }
            }
        }

        while (!libsToLoad.isEmpty()) {
            String libPath = libsToLoad.poll();
            if (loadedLibs.contains(libPath)) continue;
            loadedLibs.add(libPath);

            RAParser libParser = new RAParser(call.parseFile(libPath));
            List<Instruction> libInstructions = libParser.parse();

            int offset = this.instructions.size();

            for (Map.Entry<String, Integer> entry : libParser.getBreakPoints().entrySet()) {
                if (!entry.getKey().equals("ENTRY")) {
                    this.breakPoints.put(entry.getKey(), entry.getValue() + offset);
                }
            }

            for (Instruction inst : libInstructions) {
                if (inst.getOpcode() == Opcode.USE) {
                    String newLib = inst.getValue().asString();
                    if (!loadedLibs.contains(newLib)) {
                        libsToLoad.add(newLib);
                    }
                }
            }

            this.instructions.addAll(libInstructions);
        }
    }

    private void execute(List<Instruction> instructions) throws Exception {
        while (PC < instructions.size() && !isFinished) {
            Instruction instruction = instructions.get(PC);
            int oldPC = PC;

            switch (instruction.getOpcode()) {
                case USE -> {}
                case VAR -> {}
                case PUSH -> push(instruction.getValue());
                case POP -> pop(instruction.getValue().asInt());
                case ADD -> add();
                case SUB -> sub();
                case MUL -> mul();
                case DIV -> div();
                case CALL -> call(instruction.getValue().asString());
                case STORE -> {
                    store(instruction.getValue());
                }
                case LOAD -> {
                    if (instruction.getValue() != null) {
                        load(instruction.getValue());
                    } else {
                        load();
                    }
                }
                case ARG -> {
                    if (instruction.getValue() !=null && instruction.getSecondValue() != null) {
                        frameArguments.add(instruction.getValue().asInt(), instruction.getSecondValue());
                    } else {
                        throw new Exception("Invalid ARG usage");
                    }
                }
                case LOADARG -> {
                    if (!frameStack.isEmpty()) {
                        Frame frame = frameStack.peek();
                        Value arg = frame.getArguments().get(instruction.getValue().asInt());

                        push(arg);
                    } else {
                        throw new Exception("LOADARG instruction outside a function || The frame stack is empty");
                    }
                }
                case DB -> declareLocalVariable(instruction.getSecondValue().asString(), instruction.getValue());
                case SWAP -> swap();
                case DUP -> dup();
                case DROP -> drop();
                case OVER -> over();
                case EQ -> EQ();
                case NE -> NE();
                case LT -> LT();
                case LE -> LE();
                case GT -> GT();
                case GE -> GE();
                case JMP -> JMP(instruction.getValue().asString());
                case JZ -> JZ(instruction.getValue().asString());
                case JNZ -> JNZ(instruction.getValue().asString());
                case RET -> RET();
                case HALT -> {
                    HALT();
                    return;
                }
                default -> throw new Exception("Invalid Opcode");
            }

            if (PC == oldPC) {
                PC++;
            }
        }
    }

    private void use(Value item) throws Exception {
        String path = item.asString();
        RAParser parser = new RAParser(call.parseFile(path));
        List<Instruction> libInstructions = parser.parse();

        instructions.addAll(PC + 1, libInstructions);
        breakPoints.putAll(parser.getBreakPoints());
    }

    // STACK
    private void push(Value item) throws Exception {
        stack.push(resolveValue(item));
    }

    private Value pop(int addr) {
        Value value = stack.pop();
        memory[addr] = value;
        return value;
    }

    private void swap() {
        Value firstValue = stack.pop();
        Value secondValue = stack.pop();
        stack.push(firstValue);
        stack.push(secondValue);
    }

    private void dup() {
        Value value = stack.pop();
        stack.push(value);
        stack.push(value);
    }

    private void over() {
        Value firstValue = stack.pop();
        Value secondValue = stack.pop();
        stack.push(firstValue);
        stack.push(secondValue);
        stack.push(firstValue);
    }

    private void drop() {
        stack.pop();
    }

    // METH
    private void add() throws Exception {
        int b = stack.pop().asInt();
        int a = stack.pop().asInt();
        push(Value.of(Value.Type.INT, a + b));
    }

    private void sub() throws Exception {
        int b = stack.pop().asInt();
        int a = stack.pop().asInt();
        push(Value.of(Value.Type.INT, a - b));
    }

    private void div() throws Exception {
        int b = stack.pop().asInt();
        int a = stack.pop().asInt();
        push(Value.of(Value.Type.INT, a / b));
    }

    private void mul() throws Exception {
        int b = stack.pop().asInt();
        int a = stack.pop().asInt();
        push(Value.of(Value.Type.INT, a * b));
    }

    // LOGIC
    private void JMP(String breakpoint) {
        PC = breakPoints.get(breakpoint);
    }

    private void JZ(String breakpoint) {
        if (stack.pop().asInt() == 0) {
            PC = breakPoints.get(breakpoint);
        }
    }

    private void JNZ(String breakpoint) {
        if (stack.pop().asInt() != 0) {
            PC = breakPoints.get(breakpoint);
        }
    }

    private void EQ() {
        int b = stack.pop().asInt();
        int a = stack.pop().asInt();
        stack.push(Value.of(Value.Type.INT, a == b ? 1 : 0));
    }

    private void NE() {
        int b = stack.pop().asInt();
        int a = stack.pop().asInt();
        stack.push(Value.of(Value.Type.INT, a != b ? 1 : 0));
    }

    private void LT() {
        int b = stack.pop().asInt();
        int a = stack.pop().asInt();
        stack.push(Value.of(Value.Type.INT, a < b ? 1 : 0));
    }

    private void LE() {
        int b = stack.pop().asInt();
        int a = stack.pop().asInt();
        stack.push(Value.of(Value.Type.INT, a <= b ? 1 : 0));
    }

    private void GT() {
        int b = stack.pop().asInt();
        int a = stack.pop().asInt();
        stack.push(Value.of(Value.Type.INT, a > b ? 1 : 0));
    }

    private void GE() {
        int b = stack.pop().asInt();
        int a = stack.pop().asInt();
        stack.push(Value.of(Value.Type.INT, a >= b ? 1 : 0));
    }

    private void RET() throws Exception {
        if (frameStack.isEmpty()) {
            throw new Exception("Frame stack underflow: RET called without a matching CALL");
        }
        PC = frameStack.pop().getReturnAddress();
    }

    private void HALT() {
        isFinished = true;
        if (PROCESS_ID != 0) {
            call.killProcess(PROCESS_ID);
        }
    }

    // MEMORY
    private void store(Value item, int addr) {
        if (item.getType() == Value.Type.STR && isVariable(item.asString())) {
            if (item.asString().startsWith("&")) {
                String name = item.asString().substring(1);
                item = Value.of(Value.Type.INT, variables.get(name));
            } else {
                item = memory[variables.get(item.asString())];
            }
        }
        memory[addr] = item;
    }

    private void store(int addr) throws Exception {
        Value item = stack.pop();

        memory[addr] = resolveValue(item);
    }
    private void store(Value item) throws Exception {
        memory[resolveValue(item).asInt()] = stack.pop();
    }

    private void load(Value item) throws Exception {
        push(memory[resolveValue(item).asInt()]);
    }
    private void load() throws Exception {
        push(memory[stack.pop().asInt()]);
    }

    private int pushMem(Value item) throws Exception {
        for (int curr = 0; curr < MEMORY_LIMIT; curr++) {
            if (memory[curr] == null) {
                memory[curr] = item;
                return curr;
            }
        }
        throw new Exception("Out of memory");
    }

    private void pushMemIns(Value item) throws Exception {
        for (int curr = 0; curr < MEMORY_LIMIT; curr++) {
            if (memory[curr] == null) {
                memory[curr] = item;
                stack.push(Value.of(Value.Type.INT, curr));
            }
        }
        throw new Exception("Out of memory");
    }

    private List<Integer> declareVariable(String name, Value value) throws Exception {
        List<Integer> addresses = new ArrayList<>();
        addresses.add(pushMem(value));
        variables.put(name, addresses.get(0));
        return addresses;
    }
    private void declareLocalVariable(String name, Value value) throws Exception {
        if (frameStack.isEmpty()) {
            throw new Exception("Local variable declaration outside a function || Frame Stack is empty");
        }
        Frame frame = frameStack.peek();

        int addr = pushMem(value);

        frame.addLocal(name, addr);
    }

    private boolean isVariable(String name) {
        if (!frameStack.isEmpty()) {
            Frame frame = frameStack.peek();
            if (frame.isLocalVariable(name))
                return true;
        }

        return variables.containsKey(name);
    }

    private void call(String label) throws Exception {
        label = label.trim();

        if (label.equals("PRINT")) {
            Value stackValue = stack.pop();
            try {
                call.print(stackValue.asString(), Kernel.screen.COLOR_WHITE);
            } catch (Exception e) {
                call.print(String.valueOf(stackValue.asInt()), Kernel.screen.COLOR_WHITE);
            }
            return;
        }

        if (label.equals("INPUT")) {
            stack.push(new Value(Value.Type.STR, call.input()));
            return;
        }

        if (label.equals("FILLRECT")) {
            int color = stack.pop().asInt();
            int w = stack.pop().asInt();
            int h = stack.pop().asInt();
            int y = stack.pop().asInt();
            int x = stack.pop().asInt();

            call.fillRectangle(x, y, w, h, color);

            return;
        }

        if (label.equals("KILL")) {
            long processId = stack.pop().asLong();

            call.killProcess(processId);
            return;
        }

        if (label.equals("NFILE")) {
            String fileName = stack.pop().asString();

            call.newFile(fileName);
            return;
        }

        if (label.equals("MDIR")) {
            String dirName = stack.pop().asString();

            call.makeDirectory(dirName);
            return;
        }

        if (label.equals("FWRITE")) {
            String path = stack.pop().asString();
            String data = stack.pop().asString();

            call.writeToFile(path, data);
            return;
        }

        if (label.equals("SETDIR")) {
            String dir = stack.pop().asString();

            call.setDirectory(dir);
            return;
        }

        if (label.equals("FMOV")) {
            String file = stack.pop().asString();
            String dest = stack.pop().asString();

            call.moveFile(file, dest);
            return;
        }

        if (label.equals("FCP")) {
            String file = stack.pop().asString();
            String dest = stack.pop().asString();

            call.copyFile(file, dest);
            return;
        }

        if (label.equals("GETDIR")) {
            stack.push(Value.of(Value.Type.STR, call.getDirectory()));
            return;
        }

        if (label.equals("CLEAR")) {
            int color = stack.pop().asInt();

            call.clearScreen(color);
            return;
        }

        // --------------------------------------------------------

        Frame frame = new Frame();

        if (!frameArguments.isEmpty()) {
            List<Value> args = new ArrayList<>(frameArguments);
            frame.setArguments(args);

            frameArguments.clear();
        }

        if (breakPoints.containsKey(label)) {
            frame.setReturnAddress(PC + 1);
            frameStack.push(frame);
            PC = breakPoints.get(label);
            return;
        }

        throw new Exception("Invalid syscall or unknown function label: " + label);
    }

    private Value resolveValue(Value item) throws Exception {
        if (item.getType() == Value.Type.STR) {
            String strVal = item.asString();
            if (strVal.startsWith("&")) {
                String name = strVal.substring(1);
                if (isVariable(name)) {
                    if (!frameStack.isEmpty()) {
                        Frame frame = frameStack.peek();
                        if (frame.isLocalVariable(name)) {
                            return Value.of(Value.Type.INT, frame.getLocal(name));
                        }
                    } else {
                        return Value.of(Value.Type.INT, variables.get(name));
                    }
                } else {
                    throw new Exception("Unknown pointer: " + strVal);
                }
            }

            else if (!frameStack.isEmpty() && isVariable(strVal)) {
                Frame frame = frameStack.peek();
                if (frame.isLocalVariable(strVal)) {
                    return memory[frame.getLocal(strVal)];
                }
            }

            else if (isVariable(strVal)) {
                return memory[variables.get(strVal)];
            }
        }
        return item;
    }

    @Override
    public void step(Screen screen, Process process) throws Exception {
        if (PROCESS_ID == 0) {
            PROCESS_ID = process.getId();
        }
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }

    @Override
    public void setName(String name) {
        this.NAME = name;
    }

    @Override
    public String getName() {
        return NAME;
    }
}