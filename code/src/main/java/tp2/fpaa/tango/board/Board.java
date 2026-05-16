package tp2.fpaa.tango.board;

import tp2.fpaa.tango.domain.Constraint;

import java.util.List;

public final class Board {

    private final int size;
    private final Cell[] cells;
    private final List<Constraint> constraints;

    public Board(int size, Cell[] cells, List<Constraint> constraints) {
        this.size = size;
        this.cells = cells;
        this.constraints = List.copyOf(constraints);
    }

    public int getSize() {
        return size;
    }

    public Cell getCell(int row, int col) {
        return cells[row * size + col];
    }

    public Cell getCellAt(int index) {
        return cells[index];
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public int cellCount() {
        return size * size;
    }
}
