package ch.bbw.m450.tictactoe.players;

import static ch.bbw.m450.tictactoe.testsupport.Boards.DRAW;
import static ch.bbw.m450.tictactoe.testsupport.Boards.board;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("GreedyPlayer")
class GreedyPlayerTest {

	private final GreedyPlayer player = new GreedyPlayer();

	@ParameterizedTest(name = "{0} -> {1}")
	@DisplayName("takes the first free field")
	@CsvSource({ "........., 0", "XO......., 2", "XOXOXOXO., 8" })
	void takesTheFirstFreeField(String pattern, int expected) {
		assertThat(player.play(board(pattern), Stone.CROSS)).isEqualTo(expected);
	}

	@Test
	@DisplayName("refuses to play on a full board")
	void failsOnAFullBoard() {
		assertThatThrownBy(() -> player.play(board(DRAW), Stone.CROSS)).isInstanceOf(IllegalStateException.class)
				.hasMessage("cannot play at all");
	}
}
