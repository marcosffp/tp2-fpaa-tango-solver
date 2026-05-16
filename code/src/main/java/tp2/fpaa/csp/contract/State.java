package tp2.fpaa.csp.contract;

public interface State<S> {
    S clone();
    void assign(int variable, int value);
    void unassign(int variable);
    boolean isComplete();
}
