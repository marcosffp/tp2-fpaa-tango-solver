package tp2.fpaa.csp.contract;

import java.util.List;

public interface ValueIterator<S> {
    List<Integer> getValues(S state, int variable);
}
