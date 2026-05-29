package zzzank.probejs.lang.parchment.data;

import java.util.function.IntSupplier;

/**
 * @author ZZZank
 */
public class LazyInt extends Number implements IntSupplier {
    private IntSupplier supplier;
    private int cached;

    public LazyInt(IntSupplier supplier) {
        this.supplier = supplier;
    }

    @Override
    public int getAsInt() {
        if (supplier != null) {
            cached = supplier.getAsInt();
            supplier = null;
        }
        return cached;
    }

    @Override
    public int intValue() {
        return getAsInt();
    }

    @Override
    public long longValue() {
        return getAsInt();
    }

    @Override
    public float floatValue() {
        return getAsInt();
    }

    @Override
    public double doubleValue() {
        return getAsInt();
    }

    @Override
    public String toString() {
        return Integer.toString(getAsInt());
    }
}
