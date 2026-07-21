package qf.huesos.kernel.provider.ra;

public class Instruction {
    Opcode opcode;
    Value value;
    Value second_value;

    public Instruction(Opcode opcode) {
        this.opcode = opcode;
    }

    public Instruction(Opcode opcode, Value value) {
        this.opcode = opcode;
        this.value = value;
    }

    public Instruction(Opcode opcode, Value value, Value second_value) {
        this.opcode = opcode;
        this.value = value;
        this.second_value = second_value;
    }

    public Opcode getOpcode() {
        return this.opcode;
    }
    public Value getValue() {
        return this.value;
    }
    public Value getSecondValue() {
        return this.second_value;
    }

    @Override
    public String toString() {
        if (second_value != null)
            return opcode + "\n" + value.toString() + "\n" + second_value.toString() + "\n";
        if (value != null)
            return opcode + "\n" + value.toString() + "\n";

        return opcode + "\n";
    }
}
