package ron.thewizard.roleplayextras.util;

import java.util.Objects;
import java.util.function.Supplier;

public final class Lazy<E> implements Supplier<E>, org.apache.logging.log4j.util.Lazy<E> {

    private final Supplier<E> supplier;
    private E value;

    private Lazy(Supplier<E> supplier) {
        this.supplier = supplier;
    }

    public static <E> Lazy<E> of(Supplier<E> supplier) {
        Objects.requireNonNull(supplier, "Can't create lazy if supplier is null!");
        return new Lazy<>(supplier);
    }

    @Override
    public void set(E newValue) {
        this.value = newValue;
    }

    @Override
    public boolean isInitialized() {
        return this.value != null;
    }

    @Override
    public E value() {
        if (this.value == null) {
            this.value = this.supplier.get();
        }
        return this.value;
    }

    @Override
    public E get() {
        return this.value();
    }
}
