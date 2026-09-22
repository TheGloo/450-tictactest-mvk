package ch.bbw.m450.tictactoe.players;

import static ch.bbw.m450.tictactoe.testsupport.Boards.DRAW;
import static ch.bbw.m450.tictactoe.testsupport.Boards.EMPTY;
import static ch.bbw.m450.tictactoe.testsupport.Boards.board;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for the computer player. It is not only shipped with the game, it is also the
 * predictable opponent the {@code play} tests rely on, so its choice has to be exact.
 */
@DisplayName("GreedyPlayer")
class GreedyPlayerTest {

	private final GreedyPlayer player = new GreedyPlayer();

	@ParameterizedTest(name = "on \"{0}\" it plays to {1}")
	@DisplayName("always takes the first free field, top-left first")
	@CsvSource({"'.........', 0", // empty board -> the very first field
			"'X........', 1", // first field taken -> next one
			"'XOXOX....', 5", // gap starts in the middle row
			"'XOXOXOXO.', 8", // only the last field is left
			"'XXXXXXXX.', 8", // free field behind a full winning row
			"'.XXXXXXXX', 0"}) // free field in front of everything
	void takesTheFirstFreeField(String pattern, int expected) {
		assertThat(player.play(board(pattern), Stone.CROSS)).isEqualTo(expected);
	}

	@Test
	@DisplayName("plays the same field regardless of the colour it is asked to play")
	void ignoresTheColour() {
		assertThat(player.play(board(EMPTY), Stone.CROSS)).isEqualTo(player.play(board(EMPTY), Stone.CIRCLE));
	}

	@Test
	@DisplayName("throws when the board is full")
	void throwsOnAFullBoard() {
		assertThatThrownBy(() -> player.play(board(DRAW), Stone.CROSS)).isInstanceOf(IllegalStateException.class)
				.hasMessage("cannot play at all");
	}
}
