package ch.bbw.m450.tictactoe.players;

import static ch.bbw.m450.tictactoe.testsupport.Boards.EMPTY;
import static ch.bbw.m450.tictactoe.testsupport.Boards.board;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

/**
 * Tests for the player that reads its move from the console. JUnit Pioneer's {@link StdIo}
 * feeds the given lines into {@code System.in} and hands the captured {@code System.out}
 * to the test as {@link StdOut}, so the player can be driven without a terminal.
 */
@DisplayName("HumanPlayer")
class HumanPlayerTest {

	private final HumanPlayer player = new HumanPlayer();

	@Test
	@StdIo("4")
	@DisplayName("plays to the position typed on the console")
	void playsToTheTypedPosition() {
		assertThat(player.play(board(EMPTY), Stone.CROSS)).isEqualTo(4);
	}

	@Test
	@StdIo("8")
	@DisplayName("shows the board and asks for the colour to play before reading")
	void showsTheBoardAndAsksForTheColour(StdOut out) {
		player.play(board("X...O...."), Stone.CIRCLE);

		assertThat(out.capturedString()).startsWith(TicTacToeMain.toString(board("X...O....")))
				.contains("where to to put the next CIRCLE? (0-8):");
	}

	@Test
	@StdIo("middle")
	@DisplayName("fails on input that is not a number")
	void rejectsNonNumericInput() {
		assertThatThrownBy(() -> player.play(board(EMPTY), Stone.CROSS)).isInstanceOf(NumberFormatException.class)
				.hasMessageContaining("middle");
	}
}
