package jarwitch.data;

import java.util.Objects;

public class Pair<T, S> {
    private final T left;
    private final S right;

    public static <T, S> Pair<T, S> of(T left, S right) {
        return new Pair<>(left, right);
    }

    public Pair(T left, S right) {
        this.left = left;
        this.right = right;
    }

    public T getLeft() {
        return left;
    }

    public S getRight() {
        return right;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair<?, ?>) {
            Pair<?, ?> compair = (Pair<?, ?>) obj;
            return left.equals(compair.left) && right.equals(compair.right);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
}
