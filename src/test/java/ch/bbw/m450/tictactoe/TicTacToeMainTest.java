package ch.bbw.m450.tictactoe;

import static ch.bbw.m450.tictactoe.testsupport.BoardAssert.assertThatBoard;
import static ch.bbw.m450.tictactoe.testsupport.Boards.DRAW;
import static ch.bbw.m450.tictactoe.testsupport.Boards.EMPTY;
import static ch.bbw.m450.tictactoe.testsupport.Boards.board;
import static ch.bbw.m450.tictactoe.testsupport.TestPlayers.alwaysPlayingTo;
import static ch.bbw.m450.tictactoe.testsupport.TestPlayers.greedy;
import static ch.bbw.m450.tictactoe.testsupport.TestPlayers.playingInOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

/**
 * Tests for the game rules. Boards, player doubles and the board assertions live in
 * {@link ch.bbw.m450.tictactoe.testsupport}, so every test here reads as one scenario.
 *
 * <p>Everything the game prints is captured with JUnit Pioneer's {@link StdIo}: that keeps
 * the boards out of the build output and lets a test assert on what the game reported.
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

		@ParameterizedTest(name = "{0}")
		@DisplayName("detects all three rows, columns and diagonals of CIRCLE")
		@MethodSource("ch.bbw.m450.tictactoe.testsupport.Boards#winningLinesForCircle")
		void detectsEveryWinningLineOfCircle(String line, String pattern) {
			assertThatBoard(pattern).isWonBy(Stone.CIRCLE);
		}

		@ParameterizedTest(name = "{0}")
		@DisplayName("is false when one field of the line holds the other colour")
		@MethodSource("ch.bbw.m450.tictactoe.testsupport.Boards#nearlyWinningLinesForCross")
		void isFalseForTwoInALine(String line, String pattern) {
			assertThatBoard(pattern).hasNoWinner();
		}

		@Test
		@DisplayName("is false for an empty board and for a full board without a line")
		void isFalseWithoutThreeInALine() {
			assertThatBoard(EMPTY).hasNoWinner();
			assertThatBoard(DRAW).hasNoWinner();
		}
	}

	@Nested
	@DisplayName("toString")
	class ToString {

		private static final String BOLD = "\033[1m";
		private static final String GREY = "\033[37m";
		private static final String RESET = "\033[0m";

		@Test
		@DisplayName("renders stones in bold and empty fields as their grey index, three per line")
		void rendersTheBoard() {
			var expected = BOLD + "X" + RESET + "  " + GREY + "1" + RESET + "  " + BOLD + "O" + RESET + "  \n"
					+ GREY + "3" + RESET + "  " + BOLD + "X" + RESET + "  " + GREY + "5" + RESET + "  \n"
					+ GREY + "6" + RESET + "  " + GREY + "7" + RESET + "  " + BOLD + "O" + RESET + "  \n";
			assertThat(TicTacToeMain.toString(board("X.O.X...O"))).isEqualTo(expected);
		}
	}

	@Nested
	@DisplayName("play")
	class Play {

		@Test
		@StdIo
		@DisplayName("lets CROSS win on 2-4-6 when two greedy players meet")
		void twoGreedyPlayersLetCrossWin(StdOut out) {
			// both players always take the first free field, so the moves are
			// X:0 O:1 X:2 O:3 X:4 O:5 X:6 -> CROSS holds the anti-diagonal 2-4-6
			assertThat(TicTacToeMain.play(greedy(), greedy())).isEqualTo(Stone.CROSS);
			assertThat(out.capturedString()).contains("...and the winner is: " + Stone.CROSS);
		}

		@Test
		@StdIo
		@DisplayName("lets CIRCLE win and announces it")
		void circleCanWin(StdOut out) {
			// X:0 O:3 X:1 O:4 X:8 O:5 -> CIRCLE holds the middle row
			assertThat(TicTacToeMain.play(playingInOrder(0, 1, 8), playingInOrder(3, 4, 5))).isEqualTo(Stone.CIRCLE);
			assertThat(out.capturedString()).contains("...and the winner is: " + Stone.CIRCLE);
		}

		@Test
		@StdIo
		@DisplayName("ends in a draw after nine moves without a line")
		void fullBoardWithoutLineIsADraw(StdOut out) {
			// X:0 O:1 X:2 O:4 X:3 O:5 X:7 O:6 X:8 -> the DRAW board
			assertThat(TicTacToeMain.play(playingInOrder(0, 2, 3, 7, 8), playingInOrder(1, 4, 5, 6))).isNull();
			assertThat(out.capturedString()).contains("it's a draw!");
		}

		@Test
		@DisplayName("refuses to run a player against itself")
		void rejectsTheSamePlayerTwice() {
			var player = greedy();
			assertThatThrownBy(() -> TicTacToeMain.play(player, player)).isInstanceOf(IllegalArgumentException.class)
					.hasMessage("players must differ");
		}

		@ParameterizedTest(name = "playing to position {0} is rejected")
		@StdIo
		@DisplayName("rejects moves outside the board and onto occupied fields")
		@ValueSource(ints = {-1, 9, 0}) // out of range low, out of range high, already taken by CROSS
		void rejectsInvalidMoves(int position, StdOut out) {
			// the greedy CROSS player opens on field 0, then the cheating CIRCLE player moves
			assertThatThrownBy(() -> TicTacToeMain.play(greedy(), alwaysPlayingTo(position)))
					.isInstanceOf(IllegalStateException.class)
					.hasMessage("cannot play to position " + position);
			// the board as it was before the invalid move is shown to the players
			assertThat(out.capturedString()).contains(TicTacToeMain.toString(board("X........")));
		}
	}

	@Nested
	@DisplayName("main")
	class Main {

		@Test
		@StdIo("9")
		@DisplayName("lets a human play CROSS against the greedy player")
		void humanPlaysCrossAgainstTheGreedyPlayer(StdOut out) {
			// the human's first move is invalid on purpose and ends the game at once;
			// that is enough to prove that main asks the console player to move as CROSS
			assertThatThrownBy(() -> TicTacToeMain.main(new String[0])).isInstanceOf(IllegalStateException.class)
					.hasMessage("cannot play to position 9");
			assertThat(out.capturedString()).contains("where to to put the next " + Stone.CROSS + "? (0-8):");
		}
	}
}
