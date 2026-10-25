package ch.bbw.m450.tictactoe.players;

import static ch.bbw.m450.tictactoe.testsupport.BoardAssert.assertThatBoard;
import static ch.bbw.m450.tictactoe.testsupport.Boards.DRAW;
import static ch.bbw.m450.tictactoe.testsupport.Boards.EMPTY;
import static ch.bbw.m450.tictactoe.testsupport.Boards.board;
import static ch.bbw.m450.tictactoe.testsupport.TestPlayers.greedy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

@DisplayName("PerfectPlayer")
class PerfectPlayerTest {

	private final PerfectPlayer player = new PerfectPlayer();

	@Nested
	@DisplayName("single moves")
	class SingleMoves {

		@ParameterizedTest(name = "{0} as {1} -> {2}")
		@DisplayName("completes its own line as soon as it can")
		@CsvSource({
				"XX.OO...., CROSS, 2", // top row
				"OO.XX.X.., CIRCLE, 2", // top row
				"OXXO.X..., CIRCLE, 6", // left column
				"..X.XO.O., CROSS, 6", // anti diagonal
		})
		void takesTheImmediateWin(String pattern, Stone color, int expected) {
			assertThat(player.play(board(pattern), color)).isEqualTo(expected);
		}

		@ParameterizedTest(name = "{0} as {1} -> {2}")
		@DisplayName("blocks the opponent's two in a line when it cannot win itself")
		@CsvSource({
				"XX.O....., CIRCLE, 2", // top row
				"OO..X..X., CROSS, 2", // top row
				"..OXOX..X, CROSS, 6", // anti diagonal
		})
		void blocksTheOpponentsThreat(String pattern, Stone color, int expected) {
			assertThat(player.play(board(pattern), color)).isEqualTo(expected);
		}

		@Test
		@DisplayName("wins at once instead of blocking first, even though blocking would also win later")
		void prefersTheFasterWin() {
			//  . . .      7 completes the bottom row now; 3 blocks CIRCLE and still
			//  . O O      forces a win, but only two moves later
			//  X . X
			assertThat(player.play(board("....OOX.X"), Stone.CROSS)).isEqualTo(7);
		}

		@Test
		@DisplayName("takes the first of several equally good moves, so its play is deterministic")
		void choosesTheFirstOfEquallyGoodMoves() {
			// on an empty board every opening leads to a draw against perfect play
			assertThat(player.play(board(EMPTY), Stone.CROSS)).isZero();
		}

		@Test
		@DisplayName("refuses to play on a full board")
		void failsOnAFullBoard() {
			assertThatThrownBy(() -> player.play(board(DRAW), Stone.CROSS)).isInstanceOf(IllegalStateException.class)
					.hasMessage("cannot play at all");
		}
	}

	@Nested
	@DisplayName("whole games")
	class WholeGames {

		@ParameterizedTest(name = "as {0}")
		@DisplayName("never loses, whatever the opponent does")
		@EnumSource(Stone.class)
		void neverLosesAgainstAnyOpponent(Stone ownColor) {
			// the opponent tries every free field at every turn, so this plays out
			// every game the perfect player can get into
			playOutEveryGame(board(EMPTY), Stone.CROSS, ownColor);
		}

		@Test
		@StdIo
		@DisplayName("ends in a draw against itself")
		void drawsAgainstItself(StdOut out) {
			assertThat(TicTacToeMain.play(new PerfectPlayer(), new PerfectPlayer())).isNull();
			assertThat(out.capturedString()).contains("it's a draw!");
		}

		@Test
		@StdIo
		@DisplayName("beats the greedy player as CROSS")
		void beatsTheGreedyPlayerAsCross(StdOut out) {
			assertThat(TicTacToeMain.play(player, greedy())).isEqualTo(Stone.CROSS);
			assertThat(out.capturedString()).contains("...and the winner is: " + Stone.CROSS);
		}

		@Test
		@StdIo
		@DisplayName("beats the greedy player as CIRCLE")
		void beatsTheGreedyPlayerAsCircle(StdOut out) {
			// X:0 O:4 X:1 O:2 X:3 O:6 -> CIRCLE holds the anti-diagonal 2-4-6
			assertThat(TicTacToeMain.play(greedy(), player)).isEqualTo(Stone.CIRCLE);
			assertThat(out.capturedString()).contains("...and the winner is: " + Stone.CIRCLE);
		}

		/**
		 * Plays {@code board} to the end along every branch: the perfect player makes its
		 * move, the opponent branches over all free fields. Fails as soon as the opponent
		 * has three in a line on any board reached.
		 */
		private void playOutEveryGame(Stone[] board, Stone toMove, Stone ownColor) {
			assertThatBoard(board).isNotWinFor(ownColor.opponent());
			if (TicTacToeMain.isWin(board, ownColor) || isFull(board)) {
				return;
			}
			if (toMove == ownColor) {
				playOutWith(board, player.play(board, ownColor), toMove, ownColor);
				return;
			}
			for (var field = 0; field < board.length; field++) {
				if (board[field] == null) {
					playOutWith(board, field, toMove, ownColor);
				}
			}
		}

		private void playOutWith(Stone[] board, int move, Stone toMove, Stone ownColor) {
			board[move] = toMove;
			playOutEveryGame(board, toMove.opponent(), ownColor);
			board[move] = null;
		}

		private boolean isFull(Stone[] board) {
			for (var stone : board) {
				if (stone == null) {
					return false;
				}
			}
			return true;
		}
	}
}
