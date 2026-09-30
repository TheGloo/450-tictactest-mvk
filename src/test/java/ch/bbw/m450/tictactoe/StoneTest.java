package ch.bbw.m450.tictactoe;

import static org.assertj.core.api.Assertions.assertThat;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Stone")
class StoneTest {

	@Test
	@DisplayName("the opponent of CROSS is CIRCLE and vice versa")
	void opponentIsTheOtherColour() {
		assertThat(Stone.CROSS.opponent()).isEqualTo(Stone.CIRCLE);
		assertThat(Stone.CIRCLE.opponent()).isEqualTo(Stone.CROSS);
	}
}
