package qf.huesos.kernel.provider.ra;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Frame {
    private int returnAddress;
    private Map<String, Integer> locals = new HashMap<>();
    private List<Value> arguments;

    public Map<String, Integer> getLocals() {
        return this.locals;
    }

    public boolean isLocalVariable (String name) {
        return locals.containsKey(name);
    }

    public int getReturnAddress() {
        return this.returnAddress;
    }
    public List<Value> getArguments () {
        return this.arguments;
    }

    public void setLocals(Map<String, Integer> locals) {
        this.locals = locals;
    }
    public void addLocal(String name, int addr) {
        this.locals.put(name, addr);
    }

    public int getLocal(String name) throws Exception {
        if (!isLocalVariable(name))
            throw new Exception(name + " is not a local variable");
        return locals.get(name);
    }

    public void setArguments(List<Value> arguments) {
        this.arguments = arguments;
    }
    public void addArgument(Value arg) {
        this.arguments.add(arg);
    }

    public void setReturnAddress(int returnAddress) {
        this.returnAddress = returnAddress;
    }
}
