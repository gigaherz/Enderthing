package gigaherz.enderthing.util;

import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

public class IndirectLong implements ILongAccessor
{
    private final LongSupplier getter;
    private final LongConsumer setter;

    public IndirectLong(LongSupplier getter, LongConsumer setter)
    {
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public long get()
    {
        return getter.getAsLong();
    }

    @Override
    public void set(long value)
    {
        setter.accept(value);
    }
}
