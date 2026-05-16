package tp2.fpaa.tango.board;

public final class BoardFactory {

    private BoardFactory() {}

    public static Board deepCopy(Board original) {
        int size = original.getSize();
        Cell[] copiedCells = new Cell[size * size];
        for (int i = 0; i < copiedCells.length; i++) {
            Cell source = original.getCellAt(i);
            copiedCells[i] = new Cell(source.getSymbol(), source.isFixed());
        }
        return new Board(size, copiedCells, original.getConstraints());
    }
}
