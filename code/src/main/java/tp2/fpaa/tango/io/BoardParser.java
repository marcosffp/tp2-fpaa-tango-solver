package tp2.fpaa.tango.io;

import tp2.fpaa.tango.board.Board;
import tp2.fpaa.tango.board.Cell;
import tp2.fpaa.tango.domain.Constraint;
import tp2.fpaa.tango.domain.ConstraintType;
import tp2.fpaa.tango.domain.Symbol;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class BoardParser {

    private BoardParser() {}

    public static Board parse(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        int lineIndex = 0;

        int size = Integer.parseInt(lines.get(lineIndex++).trim());
        Cell[] cells = parseCells(lines, lineIndex, size);
        lineIndex += size;

        List<Constraint> constraints = parseConstraints(lines, lineIndex, size);
        return new Board(size, cells, constraints);
    }

    private static Cell[] parseCells(List<String> lines, int startLine, int size) {
        Cell[] cells = new Cell[size * size];
        for (int row = 0; row < size; row++) {
            String[] tokens = lines.get(startLine + row).trim().split("\\s+");
            for (int col = 0; col < size; col++) {
                cells[row * size + col] = parseCell(tokens[col]);
            }
        }
        return cells;
    }

    private static Cell parseCell(String token) {
        switch (token.toUpperCase()) {
            case "S": return new Cell(Symbol.SUN, true);
            case "M": return new Cell(Symbol.MOON, true);
            default:  return new Cell(null, false);
        }
    }

    private static List<Constraint> parseConstraints(List<String> lines, int startLine, int size) {
        List<Constraint> constraints = new ArrayList<>();
        for (int i = startLine; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (!line.isEmpty()) {
                constraints.add(parseConstraint(line, size));
            }
        }
        return constraints;
    }

    private static Constraint parseConstraint(String line, int size) {
        String[] tokens = line.split("\\s+");
        ConstraintType type = tokens[0].equals("=") ? ConstraintType.EQUAL : ConstraintType.OPPOSITE;
        int firstIndex = Integer.parseInt(tokens[1]) * size + Integer.parseInt(tokens[2]);
        int secondIndex = Integer.parseInt(tokens[3]) * size + Integer.parseInt(tokens[4]);
        return new Constraint(firstIndex, secondIndex, type);
    }
}
