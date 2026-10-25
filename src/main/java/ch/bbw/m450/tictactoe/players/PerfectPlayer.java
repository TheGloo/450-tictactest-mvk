package ch.bbw.m450.tictactoe.players;

import java.util.HashMap;
import java.util.Map;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.TicTacToePlayer;

/**
 * A player that never loses: it searches the complete game tree (minimax in its negamax
 * form) and plays the move with the best guaranteed outcome. Among equally good moves it
 * prefers the one that wins soonest, or loses latest.
 *
 * <p>A 3x3 board has only 5478 reachable positions, so every position is evaluated once
 * and remembered. Afterwards a move costs a handful of map lookups.
 */
public class PerfectPlayer implements TicTacToePlayer {

	/** Score of a position that is already won; one point is deducted per move it is away. */
	private static final int WIN = 10;

	private static final int DRAW = 0;

	/** position (see {@link #key}) -> score for the colour that is about to move there */
	private final Map<Integer, Integer> scores = new HashMap<>();

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
	private int scoreOfMove(Stone[] position, int field, Stone color) {
		position[field] = color;
		var score = -score(position, color.opponent());
		position[field] = null;
		return score;
	}

	/**
	 * Score of {@code position} for {@code color}, who is about to move: positive if
	 * {@code color} can force a win, negative if the opponent can, {@code 0} for a draw.
	 */
	private int score(Stone[] position, Stone color) {
		var key = key(position, color);
		var known = scores.get(key);
		if (known != null) {
			return known;
		}
		var score = search(position, color);
		scores.put(key, score);
		return score;
	}

	private int search(Stone[] position, Stone color) {
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
			return DRAW; // the board is full and nobody has a line
		}
		// one move further away: a win is worth a little less, a loss a little less bad
		return best - Integer.signum(best);
	}

	/** Encodes the position as a base-3 number and the colour to move in the lowest bit. */
	private static int key(Stone[] position, Stone color) {
		var code = 0;
		for (var stone : position) {
			code = code * 3 + (stone == null ? 0 : stone == Stone.CROSS ? 1 : 2);
		}
		return code * 2 + (color == Stone.CROSS ? 0 : 1);
	}
}
