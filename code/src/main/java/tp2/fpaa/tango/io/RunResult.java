package tp2.fpaa.tango.io;

public final class RunResult {

    private final String  boardName;
    private final int     boardSize;
    private final long    emptyCells;
    private final String  algorithm;
    private final long    timeNs;
    private final long    nodesVisited;
    private final long    constraintChecks;
    private final long    backtracks;
    private final boolean solved;

    public RunResult(String boardName, int boardSize, long emptyCells, String algorithm,
                     long timeNs, long nodesVisited, long constraintChecks,
                     long backtracks, boolean solved) {
        this.boardName        = boardName;
        this.boardSize        = boardSize;
        this.emptyCells       = emptyCells;
        this.algorithm        = algorithm;
        this.timeNs           = timeNs;
        this.nodesVisited     = nodesVisited;
        this.constraintChecks = constraintChecks;
        this.backtracks       = backtracks;
        this.solved           = solved;
    }

    public String  getBoardName()        { return boardName; }
    public int     getBoardSize()        { return boardSize; }
    public long    getEmptyCells()       { return emptyCells; }
    public String  getAlgorithm()        { return algorithm; }
    public long    getTimeNs()           { return timeNs; }
    public long    getNodesVisited()     { return nodesVisited; }
    public long    getConstraintChecks() { return constraintChecks; }
    public long    getBacktracks()       { return backtracks; }
    public boolean isSolved()            { return solved; }

    public double percentExplored() {
        long evaluated = algorithm.equals("bf") ? constraintChecks : nodesVisited;
        if (emptyCells >= 62) return evaluated * 100.0 / (double)(1L << 62);
        return evaluated * 100.0 / (double)(1L << emptyCells);
    }

    public double nodesPerSecond() {
        return timeNs == 0 ? 0 : nodesVisited / (timeNs / 1_000_000_000.0);
    }

    public double pruningRate() {
        long denom = nodesVisited + backtracks;
        return denom == 0 ? 0 : backtracks * 100.0 / denom;
    }
}
