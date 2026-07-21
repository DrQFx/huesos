package qf.huesos.kernel.provider;

import qf.huesos.kernel.Kernel;
import qf.huesos.kernel.Screen;
import qf.huesos.kernel.provider.ra.Instruction;
import qf.huesos.kernel.provider.ra.RAParser;
import qf.huesos.kernel.provider.ra.Value;
import qf.huesos.kernel.scheduler.Process;
import qf.huesos.kernel.scheduler.RunnableApplication;
import qf.huesos.kernel.SysCall;

import java.util.*;

public class RAProvider implements RunnableApplication {
    private final int MEMORY_LIMIT = 1048576;
    private long PROCESS_ID;
    private int PC = 0;

    private boolean isFinished = false;

    public String VERSION;
    public String NAME;

    Stack<Value> stack = new Stack<>();
    Stack<Integer> callStack = new Stack<>();

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
            throw new Exception("Invalid metadata");
        }

        if (getName() == null) {
            setName("RA Application");
        }

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

    private void execute(List<Instruction> instructions) throws Exception {
        while (PC < instructions.size() && !isFinished) {
            Instruction instruction = instructions.get(PC);
            int oldPC = PC;

            switch (instruction.getOpcode()) {
                case PUSH -> push(instruction.getValue());
                case POP -> pop(instruction.getValue().asInt());
                case ADD -> add();
                case SUB -> sub();
                case MUL -> mul();
                case DIV -> div();
                case CALL -> call(instruction.getValue().asString());
                case VAR -> {}
                case STORE -> {
                    if (instruction.getSecondValue() != null) {
                        store(instruction.getValue(), instruction.getSecondValue().asInt());
                    } else {
                        store(instruction.getValue().asInt());
                    }
                }
                case LOAD -> load(instruction.getValue().asInt());
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

    // STACK
    private void push(Value item) {
        if (item.getType() == Value.Type.STR && isVariable(item.asString())) {
            item = memory[variables.get(item.asString())];
        }
        stack.push(item);
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
    private void add() {
        int b = stack.pop().asInt();
        int a = stack.pop().asInt();
        push(Value.of(Value.Type.INT, a + b));
    }

    private void sub() {
        int b = stack.pop().asInt();
        int a = stack.pop().asInt();
        push(Value.of(Value.Type.INT, a - b));
    }

    private void div() {
        int b = stack.pop().asInt();
        int a = stack.pop().asInt();
        push(Value.of(Value.Type.INT, a / b));
    }

    private void mul() {
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
        if (callStack.isEmpty()) {
            throw new Exception("Call stack underflow: RET called without a matching CALL");
        }
        PC = callStack.pop();
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
            item = memory[variables.get(item.asString())];
        }
        memory[addr] = item;
    }

    private void store(int addr) {
        Value item = stack.pop();
        if (item.getType() == Value.Type.STR && isVariable(item.asString())) {
            item = memory[variables.get(item.asString())];
        }
        memory[addr] = item;
    }

    private Value load(int addr) {
        push(memory[addr]);
        return memory[addr];
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

    private List<Integer> declareVariable(String name, Value value) throws Exception {
        List<Integer> addresses = new ArrayList<>();
        addresses.add(pushMem(value));
        variables.put(name, addresses.get(0));
        return addresses;
    }

    private boolean isVariable(String name) {
        return variables.get(name) != null;
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

        if (breakPoints.containsKey(label)) {
            callStack.push(PC + 1);
            PC = breakPoints.get(label);
            return;
        }

        throw new Exception("Invalid syscall or unknown function label: " + label);
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