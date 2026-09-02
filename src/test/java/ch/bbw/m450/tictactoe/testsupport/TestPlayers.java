package ch.bbw.m450.tictactoe.testsupport;

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
}
