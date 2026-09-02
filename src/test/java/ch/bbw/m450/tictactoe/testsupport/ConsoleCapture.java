package ch.bbw.m450.tictactoe.testsupport;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Fixture that swaps {@code System.out} for a buffer around a single test and puts the
 * original stream back afterwards, whether the test passed or failed.
 *
 * <p>{@link ch.bbw.m450.tictactoe.TicTacToeMain#play} prints the board on every game, which
 * would otherwise clutter the build output. Capturing it keeps the output clean and lets a
 * test assert on what the game reported.
 *
 * <p>Register it as an instance field so each test method gets its own empty buffer:
 *
 * <pre>
 * &#64;RegisterExtension
 * final ConsoleCapture console = new ConsoleCapture();
 * </pre>
 */
public class ConsoleCapture implements BeforeEachCallback, AfterEachCallback {

	private final ByteArrayOutputStream captured = new ByteArrayOutputStream();

	private PrintStream original;

	@Override
	public void beforeEach(ExtensionContext context) {
		original = System.out;
		System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
	}

	@Override
	public void afterEach(ExtensionContext context) {
		System.setOut(original);
	}

	/** Everything the code under test printed to stdout during the current test. */
	public String output() {
		return captured.toString(StandardCharsets.UTF_8);
	}
}
