package ch.bbw.m450.tictactoe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import ch.bbw.m450.tictactoe.players.GreedyPlayer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TicTacToeMainTest {

	/**
	 * Builds a board from a 9 character pattern, where 'X' is a cross, 'O' a circle
	 * and any other character an empty field.
	 */
	private static Stone[] board(String pattern) {
		var b = new Stone[TicTacToeMain.BOARD_SIZE];
		for (var i = 0; i < TicTacToeMain.BOARD_SIZE; i++) {
			b[i] = switch (pattern.charAt(i)) {
				case 'X' -> Stone.CROSS;
				case 'O' -> Stone.CIRCLE;
				default -> null;
			};
		}
		return b;
	}

	@ParameterizedTest(name = "board \"{0}\" is a win for CROSS")
	@DisplayName("isWin detects all three rows, columns and diagonals")
	@ValueSource(strings = {"XXX......", "...XXX...", "......XXX", // rows
			"X..X..X..", ".X..X..X.", "..X..X..X", // columns
			"X...X...X", "..X.X.X.."}) // diagonals
	void isWinDetectsEveryWinningLine(String pattern) {
		var b = board(pattern);
		assertThat(TicTacToeMain.isWin(b, Stone.CROSS)).isTrue();
		assertThat(TicTacToeMain.isWin(b, Stone.CIRCLE)).isFalse();
	}

	@Test
	@DisplayName("isWin is false for an empty board and for a full board without a line")
	void isWinIsFalseWithoutThreeInALine() {
		assertThat(TicTacToeMain.isWin(board("........."), Stone.CROSS)).isFalse();
		// a full board ending in a draw:
		//   X O X
		//   X O O
		//   O X X
		var draw = board("XOXXOOOXX");
		assertThat(TicTacToeMain.isWin(draw, Stone.CROSS)).isFalse();
		assertThat(TicTacToeMain.isWin(draw, Stone.CIRCLE)).isFalse();
	}

	@Test
	@DisplayName("two greedy players fill the board left to right, so CROSS wins on 2-4-6")
	void twoGreedyPlayersLetCrossWin() {
		// both players always take the first free field, so the moves are
		// X:0 O:1 X:2 O:3 X:4 O:5 X:6 -> CROSS holds the anti-diagonal 2-4-6
		assertThat(TicTacToeMain.play(new GreedyPlayer(), new GreedyPlayer())).isEqualTo(Stone.CROSS);
	}

	@Test
	@DisplayName("play refuses to run a player against itself")
	void playRejectsTheSamePlayerTwice() {
		var player = new GreedyPlayer();
		assertThatThrownBy(() -> TicTacToeMain.play(player, player)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("players must differ");
	}

	@ParameterizedTest(name = "playing to position {0} is rejected")
	@DisplayName("play rejects moves outside the board and onto occupied fields")
	@ValueSource(ints = {-1, 9, 0}) // out of range low, out of range high, already taken by CROSS
	void playRejectsInvalidMoves(int position) {
		TicTacToePlayer cheater = (b, colorToPlay) -> position;
		// the greedy CROSS player opens on field 0, then the cheating CIRCLE player moves
		assertThatThrownBy(() -> TicTacToeMain.play(new GreedyPlayer(), cheater)).isInstanceOf(IllegalStateException.class)
				.hasMessage("cannot play to position " + position);
	}
}
