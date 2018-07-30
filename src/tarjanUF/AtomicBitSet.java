package tarjanUF;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class AtomicBitSet {
    private final AtomicIntegerArray A;

    // Create a new AtomicIntegerArray to accomodate `length` bits.
    public AtomicBitSet(int length) {
        // Round it properly to have atleast length bits.
        // Note: unsigned division.
        A = new AtomicIntegerArray((length + 31) >>> 5);
    }

    // Set the `x^{th}` bit to be true.
    public void set(int x) {
        int index = (x >>> 5);
        while (true) {
            int prevI = A.get(index);
            int newI = prevI | (1 << x);
            if (newI == prevI || A.compareAndSet(index, prevI, newI)) {
                return;
            }
        }
    }

    // Return the value of the `x^{th}` bit.
    public boolean get(int x) {
        return (A.get(x >>> 5) & (1 << x)) != 0;
    }
}
