package tp2.fpaa.csp.contract;

public interface ConstraintChecker<S> {
    boolean isValid(S state);
}
