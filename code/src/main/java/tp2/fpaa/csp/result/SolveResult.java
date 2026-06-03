package tp2.fpaa.csp.result;

public final class SolveResult<S> {

    private final S    solution;
    private final long nodesVisited;
    private final long backtracks;
    private final long attempts;

    public SolveResult(S solution, long nodesVisited, long backtracks, long attempts) {
        this.solution     = solution;
        this.nodesVisited = nodesVisited;
        this.backtracks   = backtracks;
        this.attempts     = attempts;
    }

    public S       getSolution()     { return solution; }
    public long    getNodesVisited() { return nodesVisited; }
    public long    getBacktracks()   { return backtracks; }
    public long    getAttempts()     { return attempts; }
    public boolean hasSolution()     { return solution != null; }
}
