package tp2.fpaa.tango.heuristic;

import tp2.fpaa.csp.contract.ValueIterator;
import tp2.fpaa.tango.adapter.TangoBoardAdapter;

import java.util.List;

public final class TangoValueIterator implements ValueIterator<TangoBoardAdapter> {

    private static final List<Integer> SYMBOL_VALUES = List.of(0, 1);

    @Override
    public List<Integer> getValues(TangoBoardAdapter adapter, int variable) {
        return SYMBOL_VALUES;
    }
}
