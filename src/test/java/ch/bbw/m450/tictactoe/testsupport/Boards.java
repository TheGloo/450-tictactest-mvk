package ch.bbw.m450.tictactoe.testsupport;

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
	 * Fixture for a parameterized test: the eight boards on which CROSS holds a winning
	 * line — one per row, column and diagonal — each paired with the name of that line.
	 */
	public static Stream<Arguments> winningLinesForCross() {
		return Stream.of(Arguments.of("top row", "XXX......"),
				Arguments.of("middle row", "...XXX..."),
				Arguments.of("bottom row", "......XXX"),
				Arguments.of("left column", "X..X..X.."),
				Arguments.of("middle column", ".X..X..X."),
				Arguments.of("right column", "..X..X..X"),
				Arguments.of("main diagonal", "X...X...X"),
				Arguments.of("anti diagonal", "..X.X.X.."));
	}
}
