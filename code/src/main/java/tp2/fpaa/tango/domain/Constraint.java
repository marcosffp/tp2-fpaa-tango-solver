package tp2.fpaa.tango.domain;

public final class Constraint {

    private final int firstIndex;
    private final int secondIndex;
    private final ConstraintType type;

    public Constraint(int firstIndex, int secondIndex, ConstraintType type) {
        this.firstIndex = firstIndex;
        this.secondIndex = secondIndex;
        this.type = type;
    }

    public int getFirstIndex() {
        return firstIndex;
    }

    public int getSecondIndex() {
        return secondIndex;
    }

    public ConstraintType getType() {
        return type;
    }
}
