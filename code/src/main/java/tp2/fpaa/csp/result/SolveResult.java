package tp2.fpaa.csp.result;

public final class SolveResult<S> {

    private final S solution;
    private final long nodesVisited;
    private final int backtracks;

    public SolveResult(S solution, long nodesVisited, int backtracks) {
        this.solution = solution;
        this.nodesVisited = nodesVisited;
        this.backtracks = backtracks;
    }

    public S getSolution() {
        return solution;
    }

    public long getNodesVisited() {
        return nodesVisited;
    }

    public int getBacktracks() {
        return backtracks;
    }

    public boolean hasSolution() {
        return solution != null;
    }
}
