package ch.bbw.m450.tictactoe;

import static ch.bbw.m450.tictactoe.testsupport.BoardAssert.assertThatBoard;
import static ch.bbw.m450.tictactoe.testsupport.Boards.DRAW;
import static ch.bbw.m450.tictactoe.testsupport.Boards.EMPTY;
import static ch.bbw.m450.tictactoe.testsupport.TestPlayers.alwaysPlayingTo;
import static ch.bbw.m450.tictactoe.testsupport.TestPlayers.greedy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import ch.bbw.m450.tictactoe.testsupport.ConsoleCapture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for the game rules. Boards, player doubles and the board assertions live in
 * {@link ch.bbw.m450.tictactoe.testsupport}, so every test here reads as one scenario.
 */
@DisplayName("TicTacToeMain")
class TicTacToeMainTest {

	@Nested
	@DisplayName("isWin")
	class IsWin {

		@ParameterizedTest(name = "{0}")
		@DisplayName("detects all three rows, columns and diagonals")
		@MethodSource("ch.bbw.m450.tictactoe.testsupport.Boards#winningLinesForCross")
		void detectsEveryWinningLine(String line, String pattern) {
			assertThatBoard(pattern).isWonBy(Stone.CROSS);
		}

		@Test
		@DisplayName("is false for an empty board and for a full board without a line")
		void isFalseWithoutThreeInALine() {
			assertThatBoard(EMPTY).hasNoWinner();
			assertThatBoard(DRAW).hasNoWinner();
		}
	}

	@Nested
	@DisplayName("play")
	class Play {

		/** Keeps the board that the game loop prints out of the build output. */
		@RegisterExtension
		final ConsoleCapture console = new ConsoleCapture();

		@Test
		@DisplayName("lets CROSS win on 2-4-6 when two greedy players meet")
		void twoGreedyPlayersLetCrossWin() {
			// both players always take the first free field, so the moves are
			// X:0 O:1 X:2 O:3 X:4 O:5 X:6 -> CROSS holds the anti-diagonal 2-4-6
			assertThat(TicTacToeMain.play(greedy(), greedy())).isEqualTo(Stone.CROSS);
			assertThat(console.output()).contains("...and the winner is: " + Stone.CROSS);
		}

		@Test
		@DisplayName("refuses to run a player against itself")
		void rejectsTheSamePlayerTwice() {
			var player = greedy();
			assertThatThrownBy(() -> TicTacToeMain.play(player, player)).isInstanceOf(IllegalArgumentException.class)
					.hasMessage("players must differ");
		}

		@ParameterizedTest(name = "playing to position {0} is rejected")
		@DisplayName("rejects moves outside the board and onto occupied fields")
		@ValueSource(ints = {-1, 9, 0}) // out of range low, out of range high, already taken by CROSS
		void rejectsInvalidMoves(int position) {
			// the greedy CROSS player opens on field 0, then the cheating CIRCLE player moves
			assertThatThrownBy(() -> TicTacToeMain.play(greedy(), alwaysPlayingTo(position)))
					.isInstanceOf(IllegalStateException.class)
					.hasMessage("cannot play to position " + position);
		}
	}
}
