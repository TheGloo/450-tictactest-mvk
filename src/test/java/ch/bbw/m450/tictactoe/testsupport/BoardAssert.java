package ch.bbw.m450.tictactoe.testsupport;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import org.assertj.core.api.AbstractAssert;

/**
 * Custom AssertJ assertions for a TicTacToe board.
 *
 * <p>Lets a test say what it means ({@code assertThatBoard(DRAW).hasNoWinner()}) instead of
 * spelling out {@code assertThat(TicTacToeMain.isWin(board, CROSS)).isFalse()} once per
 * colour, and prints the offending board when an assertion fails.
 */
public class BoardAssert extends AbstractAssert<BoardAssert, Stone[]> {

	private BoardAssert(Stone[] actual) {
		super(actual, BoardAssert.class);
	}

	public static BoardAssert assertThatBoard(Stone[] actual) {
		return new BoardAssert(actual);
	}

	/** Convenience entry point that builds the board from a {@link Boards} pattern first. */
	public static BoardAssert assertThatBoard(String pattern) {
		return new BoardAssert(Boards.board(pattern));
	}

	/** The given colour has three in a line. */
	public BoardAssert isWinFor(Stone color) {
		isNotNull();
		if (!TicTacToeMain.isWin(actual, color)) {
			failWithMessage("expected %s to have three in a line on board:%n%n%s", color, Boards.render(actual));
		}
		return this;
	}

	/** The given colour does not have three in a line. */
	public BoardAssert isNotWinFor(Stone color) {
		isNotNull();
		if (TicTacToeMain.isWin(actual, color)) {
			failWithMessage("expected %s not to have three in a line on board:%n%n%s", color, Boards.render(actual));
		}
		return this;
	}

	/** The given colour wins and — just as important — the opponent does not. */
	public BoardAssert isWonBy(Stone color) {
		return isWinFor(color).isNotWinFor(color.opponent());
	}

	/** Neither colour has three in a line. */
	public BoardAssert hasNoWinner() {
		return isNotWinFor(Stone.CROSS).isNotWinFor(Stone.CIRCLE);
	}
}
