package dev.gigaherz.enderthing.util;

public class LongMutable implements ILongAccessor
{
    private long value;

    public LongMutable(long value)
    {
        this.value = value;
    }

    @Override
    public long get()
    {
        return value;
    }

    @Override
    public void set(long value)
    {
        this.value = value;
    }
}
