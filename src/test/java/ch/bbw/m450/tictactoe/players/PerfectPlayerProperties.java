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
 * Property-based test with <a href="https://jqwik.net">jqwik</a>. Instead of a few
 * hand-picked games, jqwik generates 1000 random opponents and, if one of them wins,
 * shrinks it to the simplest counterexample it can find.
 */
class PerfectPlayerProperties {

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
		var player = new PerfectPlayer();

		var winner = ownColor == Stone.CROSS ? TicTacToeMain.play(player, opponent) : TicTacToeMain.play(opponent, player);

		assertThat(winner).isNotEqualTo(ownColor.opponent());
	}

	/** Every permutation of the nine fields. */
	@Provide
	Arbitrary<List<Integer>> fieldOrders() {
		return Arbitraries.shuffle(0, 1, 2, 3, 4, 5, 6, 7, 8);
	}
}
