package qf.huesos.kernel.provider.ra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RAParser {
    private List<String> program = new ArrayList<>();

    private int IP = 0;
    private int ENTRY_POINT = 0;
    private String NAME;
    private String VERSION;
    private Map<String, Integer> breakPoints = new HashMap<>();

    public RAParser(List<String> program) {
        this.program = program;
    }

    private String peek() {
        if (IP >= program.size())
            return null;
        return program.get(IP);
    }

    private String consume() {
        if (IP >= program.size())
            return null;
        return program.get(IP++);
    }

    public List<Instruction> parse() throws Exception {
        List<Instruction> instructions = new ArrayList<>();

        while (peek() != null) {
            String token = peek().trim();
            if (token.isEmpty()) {
                consume();
                continue;
            }
            String[] parts = token.split("\\s+");

            switch (parts[0]) {
                case "use":
                    parseUse(instructions);
                    break;
                case ".info":
                    parseInfoBlock();
                    break;
                case ".data":
                    parseDataBlock(instructions);
                    break;
                case ".code":
                    parseCodeBlock(instructions);
                    break;
                default:
                    throw new Exception("Unexpected token: " + token);
            }
        }
        return instructions;
    }

    private void parseUse(List<Instruction> instructions) throws Exception {
        String line = consume().trim();
        String[] parts = line.split("\\s+");

        if (parts.length < 2) {
            throw new Exception("Invalid use syntax: " + line);
        }

        String lib = parts[1];
        if (lib.startsWith("\"") && lib.endsWith("\"")) {
            lib = lib.substring(1, lib.length() - 1);
        }

        instructions.add(new Instruction(Opcode.USE, Value.of(Value.Type.STR, lib)));
    }

    private void parseInfoBlock() {
        consume();
        while (peek() != null && !peek().trim().startsWith(".")) {
            String line = consume().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(":", 2);
            if (parts.length != 2) continue;

            switch (parts[0].trim()) {
                case "NAME":
                    NAME = parts[1].trim();
                    break;
                case "VERSION":
                    VERSION = parts[1].trim();
                    break;
            }
        }
    }

    private void parseDataBlock(List<Instruction> instructions) throws Exception {
        consume();
        while (peek() != null && !peek().trim().startsWith(".")) {
            String line = consume().trim();
            if (line.isEmpty()) continue;

            String[] leftRight = line.split("\\|", 2);
            if (leftRight.length != 2)
                throw new Exception("Missing | : " + line);

            String[] nameType = leftRight[0].split(":", 2);
            if (nameType.length != 2)
                throw new Exception("Missing : " + line);

            String name = nameType[0].trim();
            Value.Type type = resolveType(nameType[1].trim());
            String value = leftRight[1].trim();

            if (type == Value.Type.STR) {
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
            }
            instructions.add(new Instruction(Opcode.VAR, new Value(type, value), new Value(Value.Type.STR, name)));
        }
    }

    private void parseCodeBlock(List<Instruction> instructions) throws Exception {
        consume();
        while (peek() != null && !peek().trim().startsWith(".")) {
            String line = consume().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            String opcode = parts[0].trim();

            if (opcode.endsWith(":")) {
                String label = opcode.substring(0, opcode.length() - 1);
                breakPoints.put(label, instructions.size());
                if (label.equals("ENTRY")) {
                    ENTRY_POINT = instructions.size();
                }

                if (parts.length == 1) {
                    continue;
                } else {
                    String[] newParts = new String[parts.length - 1];
                    System.arraycopy(parts, 1, newParts, 0, parts.length - 1);
                    parts = newParts;
                    opcode = parts[0].trim();
                }
            }

            switch (opcode) {
                case "PUSH" -> {
                    if (parts.length != 2) throw new Exception("The PUSH instruction requires an argument: " + line);
                    instructions.add(new Instruction(Opcode.PUSH, parseValue(parts[1])));
                }
                case "ADD" -> instructions.add(new Instruction(Opcode.ADD));
                case "SUB" -> instructions.add(new Instruction(Opcode.SUB));
                case "MUL" -> instructions.add(new Instruction(Opcode.MUL));
                case "DIV" -> instructions.add(new Instruction(Opcode.DIV));
                case "CALL" -> {
                    if (parts.length == 2) {
                        instructions.add(new Instruction(Opcode.CALL, Value.of(Value.Type.STR, parts[1])));
                    } else {
                        throw new Exception("Invalid CALL syntax: " + line);
                    }
                }
                case "POP" -> {
                    if (parts.length != 2) throw new Exception("The POP instruction requires an argument: " + line);
                    instructions.add(new Instruction(Opcode.POP, Value.of(Value.Type.INT, Integer.parseInt(parts[1]))));
                }
                case "STORE" -> {
                    if (parts.length < 2) throw new Exception("The STORE instruction requires at least one argument: " + line);
                    if (parts.length == 3) {
                        instructions.add(new Instruction(Opcode.STORE, parseValue(parts[1]), parseValue(parts[2])));
                    } else {
                        instructions.add(new Instruction(Opcode.STORE, parseValue(parts[1])));
                    }
                }
                case "LOAD" -> {
                    if (parts.length == 2) {
                        instructions.add(new Instruction(Opcode.LOAD, parseValue(parts[1])));
                    } else {
                        instructions.add(new Instruction(Opcode.LOAD));
                    }
                }
                case "ARG" -> {
                    if (parts.length == 3) {
                        instructions.add(new Instruction(Opcode.ARG, parseValue(parts[1]), parseValue(parts[2])));
                    } else {
                        throw new Exception("ARG requires at least one argument");
                    }
                }
                case "LOADARG" -> {
                    if (parts.length == 2) {
                        instructions.add(new Instruction(Opcode.LOADARG, parseValue(parts[1])));
                    } else {
                        throw new Exception("LOADARG requires an argument");
                    }
                }
                case "DB" -> {
                    if (parts.length == 6) {
                        String[] leftRight = line.split("\\|", 2);
                        if (leftRight.length != 2)
                            throw new Exception("Missing | : " + line);

                        String[] nameType = leftRight[0].split(":", 2);
                        if (nameType.length != 2)
                            throw new Exception("Missing : " + line);

                        String[] nameNoDB = nameType[0].trim().split("\\s+", 2);
                        if (nameNoDB.length != 2)
                            throw new Exception("Invalid DB usage");

                        String name = nameNoDB[1].trim();
                        Value.Type type = resolveType(nameType[1].trim());
                        String value = leftRight[1].trim();

                        if (type == Value.Type.STR) {
                            if (value.startsWith("\"") && value.endsWith("\"")) {
                                value = value.substring(1, value.length() - 1);
                            }
                        }

                        instructions.add(new Instruction(Opcode.DB, Value.of(type, value), Value.of(Value.Type.STR, name)));
                    } else if (parts.length == 5) {
                        String[] nameType = line.split(":", 2);
                        if (nameType.length !=2)
                            throw new Exception("Invalid DB usage" + line);

                        String name = nameType[0].trim();
                        Value.Type type = resolveType(nameType[1].trim());

                        instructions.add(new Instruction(Opcode.DB, Value.of(type), Value.of(Value.Type.STR, name)));
                    } else {
                        throw new Exception("Invalid DB usage " + line);
                    }
                }
                case "SWAP" -> instructions.add(new Instruction(Opcode.SWAP));
                case "DUP" -> instructions.add(new Instruction(Opcode.DUP));
                case "OVER" -> instructions.add(new Instruction(Opcode.OVER));
                case "DROP" -> instructions.add(new Instruction(Opcode.DROP));
                case "EQ" -> instructions.add(new Instruction(Opcode.EQ));
                case "NE" -> instructions.add(new Instruction(Opcode.NE));
                case "LT" -> instructions.add(new Instruction(Opcode.LT));
                case "LE" -> instructions.add(new Instruction(Opcode.LE));
                case "GT" -> instructions.add(new Instruction(Opcode.GT));
                case "GE" -> instructions.add(new Instruction(Opcode.GE));
                case "JMP" -> {
                    if (parts.length != 2) throw new Exception("The JMP instruction requires an argument: " + line);
                    instructions.add(new Instruction(Opcode.JMP, parseValue(parts[1])));
                }
                case "JZ" -> {
                    if (parts.length != 2) throw new Exception("The JZ instruction requires an argument: " + line);
                    instructions.add(new Instruction(Opcode.JZ, parseValue(parts[1])));
                }
                case "JNZ" -> {
                    if (parts.length != 2) throw new Exception("The JNZ instruction requires an argument: " + line);
                    instructions.add(new Instruction(Opcode.JNZ, parseValue(parts[1])));
                }
                case "RET" -> instructions.add(new Instruction(Opcode.RET));
                case "HALT" -> instructions.add(new Instruction(Opcode.HALT));
                default -> throw new Exception("Unknown instruction: " + opcode);
            }
        }
    }

    private Value.Type resolveType(String type) throws Exception {
        switch (type) {
            case "string" -> {
                return Value.Type.STR;
            }
            case "int" -> {
                return Value.Type.INT;
            }

            case "double" -> {
                return Value.Type.DOUBLE;
            }
            case "long" -> {
                return Value.Type.LONG;
            }
            case "float" -> {
                return Value.Type.FLOAT;
            }

            case "object" -> {
                return Value.Type.OBJ;
            }
            case "bool" -> {
                return Value.Type.BOOL;
            }
            default -> throw new Exception("Unknown type: " + type);
        }
    }

    private Value parseValue(String s) {
        try {
            return Value.of(Value.Type.INT, Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Value.of(Value.Type.STR, s);
        }
    }

    public int getEntryPoint() { return this.ENTRY_POINT; }
    public String getName() { return this.NAME; }
    public String getVersion() { return this.VERSION; }
    public Map<String, Integer> getBreakPoints() { return breakPoints; }
}