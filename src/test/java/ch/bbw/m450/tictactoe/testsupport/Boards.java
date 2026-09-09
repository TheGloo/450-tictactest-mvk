package ch.bbw.m450.tictactoe.testsupport;

import java.util.List;
import java.util.stream.Stream;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import org.junit.jupiter.params.provider.Arguments;

/**
 * Board fixtures and the helper that builds them.
 *
 * <p>Boards are written as a nine character pattern, read left to right and top to bottom,
 * so a test can state the board it means in a single readable literal:
 *
 * <pre>
 *   "XOX" + "XOO" + "OXX"   ->    X O X
 *                                 X O O
 *                                 O X X
 * </pre>
 */
public final class Boards {

	/** An untouched board where every field is still {@code null}. */
	public static final String EMPTY = ".........";

	/**
	 * A full board that ends in a draw:
	 *
	 * <pre>
	 *   X O X
	 *   X O O
	 *   O X X
	 * </pre>
	 */
	public static final String DRAW = "XOXXOOOXX";

	/**
	 * The moves CROSS plays to fill {@link #DRAW}, in turn order. Neither colour ever holds
	 * three in a line, not even intermediately, so the game runs the full nine rounds.
	 */
	public static final int[] DRAW_MOVES_CROSS = {0, 2, 3, 7, 8};

	/** The moves CIRCLE plays to fill {@link #DRAW}, interleaved with {@link #DRAW_MOVES_CROSS}. */
	public static final int[] DRAW_MOVES_CIRCLE = {1, 4, 5, 6};

	/** The eight winning lines, written for CROSS and named after the line they occupy. */
	private static final List<Arguments> LINES = List.of(Arguments.of("top row", "XXX......"),
			Arguments.of("middle row", "...XXX..."),
			Arguments.of("bottom row", "......XXX"),
			Arguments.of("left column", "X..X..X.."),
			Arguments.of("middle column", ".X..X..X."),
			Arguments.of("right column", "..X..X..X"),
			Arguments.of("main diagonal", "X...X...X"),
			Arguments.of("anti diagonal", "..X.X.X.."));

	private Boards() {
	}

	/**
	 * Builds a board from a nine character pattern, where {@code 'X'} is a cross,
	 * {@code 'O'} a circle and every other character an empty field.
	 *
	 * @throws IllegalArgumentException if the pattern is not exactly nine characters long,
	 * 		which would otherwise surface as a confusing {@link IndexOutOfBoundsException}
	 */
	public static Stone[] board(String pattern) {
		if (pattern.length() != TicTacToeMain.BOARD_SIZE) {
			throw new IllegalArgumentException("a board pattern needs exactly " + TicTacToeMain.BOARD_SIZE
					+ " characters, but got " + pattern.length() + ": \"" + pattern + "\"");
		}
		var board = new Stone[TicTacToeMain.BOARD_SIZE];
		for (var i = 0; i < TicTacToeMain.BOARD_SIZE; i++) {
			board[i] = switch (pattern.charAt(i)) {
				case 'X' -> Stone.CROSS;
				case 'O' -> Stone.CIRCLE;
				default -> null;
			};
		}
		return board;
	}

	/**
	 * Renders a board back into three lines of three characters. Used for failure messages,
	 * unlike {@link TicTacToeMain#toString(Stone[])} it emits no ANSI escape sequences.
	 */
	public static String render(Stone[] board) {
		var sb = new StringBuilder();
		for (var row = 0; row < 3; row++) {
			for (var col = 0; col < 3; col++) {
				var stone = board[row * 3 + col];
				sb.append(stone == null ? '.' : stone == Stone.CROSS ? 'X' : 'O');
			}
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}

	/**
	 * Fixture for a parameterized test: all sixteen boards on which one colour holds a
	 * winning line — the eight rows, columns and diagonals, once for CROSS and once for
	 * CIRCLE — as {@code (colour, line name, pattern)}.
	 *
	 * <p>Deriving the CIRCLE boards from the CROSS ones keeps the eight line patterns in a
	 * single place, so a corrected line stays correct for both colours.
	 */
	public static Stream<Arguments> winningLines() {
		return Stream.of(Stone.CROSS, Stone.CIRCLE)
				.flatMap(color -> LINES.stream()
						.map(line -> Arguments.of(color, line.get()[0], recolor((String) line.get()[1], color))));
	}

	/**
	 * Fixture for a parameterized test: boards on which neither colour has three in a line.
	 * Covers the empty board, two full boards and the near misses that a naive win check
	 * would most likely get wrong — a line one stone short, and lines blocked by the opponent.
	 */
	public static Stream<Arguments> boardsWithoutWinner() {
		return Stream.of(Arguments.of("empty board", EMPTY),
				Arguments.of("full board ending in a draw", DRAW),
				Arguments.of("full board without a line", "XOXOXOOXO"),
				Arguments.of("two in a row, third field still free", "XX......."),
				Arguments.of("row blocked by the opponent", "XXO......"),
				Arguments.of("column blocked by the opponent", "X..X..O.."),
				Arguments.of("main diagonal blocked by the opponent", "X...X...O"),
				Arguments.of("anti diagonal blocked by the opponent", "..X.X.O.."),
				Arguments.of("three stones of one colour, but not in a line", "XX...X..."),
				Arguments.of("both colours two in a row", "XXOOO...."));
	}

	/** Rewrites a CROSS pattern for the given colour. */
	private static String recolor(String pattern, Stone color) {
		return color == Stone.CROSS ? pattern : pattern.replace('X', 'O');
	}
}
