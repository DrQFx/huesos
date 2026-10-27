package qf.huesos.kernel.provider.ra;

public final class Value {

    public enum Type {
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        STR,
        BOOL,
        OBJ,
        NULL
    }

    private final Type type;
    private final Object value;

    public Value(Type type, Object value) {
        this.type = type;
        this.value = value;
    }
    public Value(Type type) {
        this.type = type;
        this.value = null;
    }

    public static Value of(Type type, Object object) {
        return new Value(type, object);
    }
    public static Value of(Type type) {
        return new Value(type);
    }

    public Type getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public int asInt() {
        check(Type.INT);
        return (Integer) value;
    }

    public long asLong() {
        check(Type.LONG);
        return (Long) value;
    }

    public float asFloat() {
        check(Type.FLOAT);
        return (Float) value;
    }

    public double asDouble() {
        check(Type.DOUBLE);
        return (Double) value;
    }

    public boolean asBool() {
        check(Type.BOOL);
        return (Boolean) value;
    }

    public String asString() {
        check(Type.STR);
        return (String) value;
    }

    public <T> T asObject(Class<T> tClass) {
        check(Type.OBJ);
        return tClass.cast(value);
    }

    private void check(Type expected) {
        if (type != expected) {
            throw new IllegalStateException(
                    "Expected " + expected + ", got " + type
            );
        }
    }
}
