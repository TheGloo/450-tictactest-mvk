package ch.bbw.m450.tictactoe.players;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.TicTacToePlayer;
import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.lifecycle.AfterProperty;
import net.jqwik.api.lifecycle.BeforeProperty;

/**
 * Property-based tests with <a href="https://jqwik.net">jqwik</a>. Instead of a few
 * hand-picked games, jqwik generates 1000 random inputs per property and, if one fails,
 * shrinks it to the smallest counterexample it can find.
 */
class PerfectPlayerProperties {

	private final PerfectPlayer player = new PerfectPlayer();

	private PrintStream console;

	/** The game prints every board; a thousand games would flood the test output. */
	@BeforeProperty
	void muteConsole() {
		console = System.out;
		System.setOut(new PrintStream(OutputStream.nullOutputStream()));
	}

	@AfterProperty
	void restoreConsole() {
		System.setOut(console);
	}

	@Property
	void neverLosesAgainstAnyOpponent(@ForAll("fieldOrders") List<Integer> preferences, @ForAll Stone ownColor) {
		// the opponent takes the first free field in its (random) order of preference
		TicTacToePlayer opponent = (board, color) -> preferences.stream()
				.filter(field -> board[field] == null)
				.findFirst()
				.orElseThrow();

		var winner = ownColor == Stone.CROSS ? TicTacToeMain.play(player, opponent) : TicTacToeMain.play(opponent, player);

		assertThat(winner).isNotEqualTo(ownColor.opponent());
	}

	@Property
	void playsToAFreeFieldWithoutChangingTheBoard(@ForAll("openBoards") Stone[] board, @ForAll Stone color) {
		var before = board.clone();

		var move = player.play(board, color);

		assertThat(board[move]).isNull();
		assertThat(board).containsExactly(before);
	}

	/** Every permutation of the nine fields. */
	@Provide
	Arbitrary<List<Integer>> fieldOrders() {
		return Arbitraries.shuffle(0, 1, 2, 3, 4, 5, 6, 7, 8);
	}

	/** Boards on which the game is still going: at least one free field and no line yet. */
	@Provide
	Arbitrary<Stone[]> openBoards() {
		return Arbitraries.of(Stone.CROSS, Stone.CIRCLE)
				.injectNull(0.4)
				.array(Stone[].class)
				.ofSize(TicTacToeMain.BOARD_SIZE)
				.filter(PerfectPlayerProperties::hasFreeField)
				.filter(board -> !TicTacToeMain.isWin(board, Stone.CROSS) && !TicTacToeMain.isWin(board, Stone.CIRCLE));
	}

	private static boolean hasFreeField(Stone[] board) {
		for (var stone : board) {
			if (stone == null) {
				return true;
			}
		}
		return false;
	}
}
