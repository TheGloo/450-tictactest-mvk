package ch.bbw.m450.tictactoe.players;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.TicTacToePlayer;

/**
 * A player that never loses: it searches the complete game tree (minimax in its negamax
 * form) and plays the move with the best guaranteed outcome. Among equally good moves it
 * prefers the one that wins soonest, or loses latest.
 */
public class PerfectPlayer implements TicTacToePlayer {

	/** Score of a position that is already won; one point less for every move it is away. */
	private static final int WIN = 10;

	@Override
	public int play(Stone[] board, Stone colorToPlay) {
		var position = board.clone(); // the search puts stones down and takes them back
		var bestMove = -1;
		var bestScore = Integer.MIN_VALUE;
		for (var i = 0; i < position.length; i++) {
			if (position[i] == null) {
				var score = scoreOfMove(position, i, colorToPlay);
				if (score > bestScore) {
					bestScore = score;
					bestMove = i;
				}
			}
		}
		if (bestMove < 0) {
			throw new IllegalStateException("cannot play at all");
		}
		return bestMove;
	}

	/** Plays {@code color} to {@code field}, scores the result for {@code color} and undoes the move. */
	private static int scoreOfMove(Stone[] position, int field, Stone color) {
		position[field] = color;
		var score = -score(position, color.opponent());
		position[field] = null;
		return score;
	}

	/**
	 * Score of {@code position} for {@code color}, who is about to move: positive if
	 * {@code color} can force a win, negative if the opponent can, {@code 0} for a draw.
	 */
	private static int score(Stone[] position, Stone color) {
		if (TicTacToeMain.isWin(position, color.opponent())) {
			return -WIN; // the opponent has just completed a line
		}
		var best = Integer.MIN_VALUE;
		for (var i = 0; i < position.length; i++) {
			if (position[i] == null) {
				best = Math.max(best, scoreOfMove(position, i, color));
			}
		}
		if (best == Integer.MIN_VALUE) {
			return 0; // the board is full and nobody has a line: a draw
		}
		// one move further away: a win is worth a little less, a loss a little less bad
		return best - Integer.signum(best);
	}
}
