package ch.bbw.m450.tictactoe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Dummy tests that prove the test setup itself works: JUnit 5 discovers and runs tests,
 * and AssertJ is on the test classpath.
 */
class DummySetupTest {

	@Test
	@DisplayName("JUnit 5 assertions are available and run")
	void junitDummy() {
		assertFalse(false, "if this test runs at all, JUnit Jupiter is wired up correctly");
	}

	@Test
	@DisplayName("AssertJ assertions are available and run")
	void assertJDummy() {
		assertThat(true).as("if this test runs at all, AssertJ is on the classpath")
				.isTrue();
	}
}
