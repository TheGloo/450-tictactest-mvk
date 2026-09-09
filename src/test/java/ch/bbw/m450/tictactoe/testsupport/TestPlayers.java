package ch.bbw.m450.tictactoe.testsupport;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import ch.bbw.m450.tictactoe.TicTacToePlayer;
import ch.bbw.m450.tictactoe.players.GreedyPlayer;

/**
 * Player test doubles for driving {@link ch.bbw.m450.tictactoe.TicTacToeMain#play} with a
 * predictable opponent. The real {@link ch.bbw.m450.tictactoe.players.HumanPlayer} reads
 * from stdin and is therefore unusable in a test.
 */
public final class TestPlayers {

	private TestPlayers() {
	}

	/**
	 * A fresh {@link GreedyPlayer}, which always takes the first free field. Deliberately a
	 * new instance per call: {@code play} rejects the same instance as both opponents.
	 */
	public static TicTacToePlayer greedy() {
		return new GreedyPlayer();
	}

	/**
	 * A cheating player that ignores the board and always plays to the same position —
	 * including positions off the board or already taken.
	 */
	public static TicTacToePlayer alwaysPlayingTo(int position) {
		return (board, colorToPlay) -> position;
	}

	/**
	 * A player that plays a scripted list of positions in order, ignoring the board. Lets a
	 * test steer a game into an exact final position, such as a draw.
	 *
	 * @throws NoSuchElementException if the game asks for more moves than were scripted,
	 * 		which means the test's expectation about the game length was wrong
	 */
	public static TicTacToePlayer playingSequence(int... positions) {
		var next = new AtomicInteger();
		return (board, colorToPlay) -> {
			var move = next.getAndIncrement();
			if (move >= positions.length) {
				throw new NoSuchElementException(
						"the scripted player ran out of moves after " + positions.length + " turns");
			}
			return positions[move];
		};
	}
}
