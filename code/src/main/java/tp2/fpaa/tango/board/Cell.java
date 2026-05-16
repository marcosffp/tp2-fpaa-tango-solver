package tp2.fpaa.tango.board;

import tp2.fpaa.tango.domain.Symbol;

public final class Cell {

    private Symbol symbol;
    private final boolean fixed;

    public Cell(Symbol symbol, boolean fixed) {
        this.symbol = symbol;
        this.fixed = fixed;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public boolean isFixed() {
        return fixed;
    }

    public boolean isEmpty() {
        return symbol == null;
    }

    public void setSymbol(Symbol symbol) {
        if (fixed) {
            throw new IllegalStateException("Não é possível modificar uma célula fixa");
        }
        this.symbol = symbol;
    }
}
