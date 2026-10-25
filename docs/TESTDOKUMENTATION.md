# Testdokumentation – TicTacToe (M450)

Dokumentation aller Unit-Tests nach dem **GIVEN / WHEN / THEN**-Pattern.

| | |
|---|---|
| **Projekt** | `450-tictactest-mvk` (TicTacToe) |
| **Test-Frameworks** | JUnit 6 (Jupiter) `6.1.3`, JUnit Pioneer `2.3.0`, jqwik `1.10.2` |
| **Assertion-Library** | AssertJ `3.27.7` |
| **Coverage / Mutation** | JaCoCo `0.8.15` (Branch ≥ 90 % erzwungen), PIT `1.30.0` |
| **Build-Tool** | Gradle Wrapper `9.8.2` (`./gradlew build`) |
| **Repository** | <https://github.com/TheGloo/450-tictactest-mvk> |

---

## 1. Setup

Die Test-Abhängigkeiten sind in [`build.gradle`](https://github.com/TheGloo/450-tictactest-mvk/blob/main/build.gradle) deklariert:

```gradle
dependencies {
    testImplementation platform('org.junit:junit-bom:6.1.3')
    testImplementation 'org.junit.jupiter:junit-jupiter'
    testImplementation 'org.assertj:assertj-core:3.27.7'
    testImplementation 'org.junit-pioneer:junit-pioneer:2.3.0'   // @StdIo: System.in / System.out
    testImplementation 'net.jqwik:jqwik:1.10.2'                   // Property-based Testing
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

test {
    useJUnitPlatform()
    testLogging {
        events 'passed', 'skipped', 'failed'
    }
}
```

Ausführen der Tests:

```bash
./gradlew test       # Tests + JaCoCo-Report
./gradlew build      # zusätzlich Coverage Verification (Branch >= 90 %)
./gradlew pitest     # Mutation Testing
```

---

## 2. Link zum Test-Code auf GitHub

| Testklasse | Zweck | Link |
|---|---|---|
| `DummySetupTest` | Dummy-Tests für JUnit & AssertJ (D1, D2) | [DummySetupTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/DummySetupTest.java) |
| `TicTacToeMainTest` | Spielregeln: `isWin`, `toString`, `play`, `main` (T1–T10, T17) | [TicTacToeMainTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/TicTacToeMainTest.java) |
| `StoneTest` | `Stone.opponent()` (T13) | [StoneTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/StoneTest.java) |
| `GreedyPlayerTest` | mitgelieferter Spieler (T11, T12) | [GreedyPlayerTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/players/GreedyPlayerTest.java) |
| `HumanPlayerTest` | Konsolenspieler mit JUnit Pioneer (T14–T16) | [HumanPlayerTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/players/HumanPlayerTest.java) |
| `PerfectPlayerTest` | perfekter Spieler (T18–T25) | [PerfectPlayerTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/players/PerfectPlayerTest.java) |
| `PerfectPlayerProperties` | jqwik-Properties (P1, P2) | [PerfectPlayerProperties.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/players/PerfectPlayerProperties.java) |

Gesamter Test-Ordner: <https://github.com/TheGloo/450-tictactest-mvk/tree/main/src/test/java/ch/bbw/m450/tictactoe>

### Test-Helper und Fixtures

Damit die Tests selbst nur noch das Szenario beschreiben, liegen Testdaten, Test-Doubles
und eigene Assertions im Paket
[`testsupport`](https://github.com/TheGloo/450-tictactest-mvk/tree/main/src/test/java/ch/bbw/m450/tictactoe/testsupport):

| Klasse | Art | Aufgabe |
|---|---|---|
| [`Boards`](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/testsupport/Boards.java) | Fixture + Helper | Konstanten `EMPTY` und `DRAW`, der Builder `board(String)` (9-Zeichen-Muster → `Stone[]`), `render(…)` für lesbare Fehlermeldungen und `winningLinesForCross()`, `winningLinesForCircle()` und `nearlyWinningLinesForCross()` als Datenquellen für die parametrisierten Tests |
| [`TestPlayers`](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/testsupport/TestPlayers.java) | Test-Doubles | `greedy()`, `alwaysPlayingTo(int)` und `playingInOrder(int...)` — ein berechenbarer, ein „schummelnder" und ein gescripteter Gegner, als Lambdas von Hand geschrieben (kein Mocking-Framework) |
| [`BoardAssert`](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/testsupport/BoardAssert.java) | eigene AssertJ-Assertion | `assertThatBoard(…).isWonBy(CROSS)` / `.hasNoWinner()` / `.isNotWinFor(…)`; bei einem Fehlschlag wird das betroffene Brett mit ausgegeben |
| JUnit Pioneer `@StdIo` | Extension (Framework) | Ersetzt `System.in` durch die angegebenen Zeilen und reicht `System.out` als `StdOut` in den Test. Damit lassen sich `HumanPlayer` und `main` ohne Terminal treiben, und die Spieltests prüfen die Konsolenausgabe |

Beispiel für eine Fehlermeldung von `BoardAssert`:

```
expected CROSS to have three in a line on board:

XX.
...
...
```

---

## 3. Dummy-Tests (Setup-Nachweis)

Diese beiden Tests prüfen keine Fachlogik. Sie beweisen nur, dass das Test-Setup
funktioniert: dass JUnit 5 Tests findet und ausführt, und dass AssertJ auf dem
Test-Classpath liegt.

Quelle: [`DummySetupTest.java`](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/DummySetupTest.java)

### D1 – `junitDummy()`

*"JUnit 5 assertions are available and run"* — [Zeile 15–19](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/DummySetupTest.java#L15-L19)

| | |
|---|---|
| **GIVEN** | Das Projekt ist mit der JUnit-5-Jupiter-Abhängigkeit konfiguriert und `useJUnitPlatform()` ist im Gradle-`test`-Task aktiviert. |
| **WHEN** | Gradle führt den Test aus und dieser ruft die JUnit-Assertion `assertFalse(false)` auf. |
| **THEN** | Der Test wird überhaupt gefunden, ausgeführt und läuft grün durch — JUnit 5 ist korrekt eingebunden. |

```java
assertFalse(false, "if this test runs at all, JUnit Jupiter is wired up correctly");
```

> **Hinweis zur Aufgabenstellung:** Für den Fehlschlag-Screenshot (Kapitel 6) wird genau
> dieser Aufruf temporär auf `assertFalse(true)` geändert.

### D2 – `assertJDummy()`

*"AssertJ assertions are available and run"* — [Zeile 21–26](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/DummySetupTest.java#L21-L26)

| | |
|---|---|
| **GIVEN** | Das Projekt hat `org.assertj:assertj-core` als `testImplementation`-Abhängigkeit. |
| **WHEN** | Der Test ruft die AssertJ-Fluent-Assertion `assertThat(true).isTrue()` auf. |
| **THEN** | Der Test kompiliert und läuft grün durch — AssertJ ist auf dem Test-Classpath verfügbar. |

```java
assertThat(true).as("if this test runs at all, AssertJ is on the classpath")
        .isTrue();
```

---

## 4. Fachliche Tests des TicTacToe-Projekts

Quelle: [`TicTacToeMainTest.java`](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/TicTacToeMainTest.java)

Die Tests sind mit `@Nested` nach der getesteten Methode gruppiert (`isWin`, `toString`, `play`, `main`),
damit die Testausgabe die Struktur der Klasse widerspiegelt. Die Spielfelder kommen aus
`Boards` und werden über die Hilfsmethode `Boards.board(String pattern)` aus einem
9-Zeichen-Muster gebaut (`X` = Kreuz, `O` = Kreis, alles andere = leeres Feld).
Das Brett ist ein eindimensionales Array der Länge 9:

```
 0 | 1 | 2
---+---+---
 3 | 4 | 5
---+---+---
 6 | 7 | 8
```

### T1 – `IsWin.detectsEveryWinningLine(String line, String pattern)`

*"TicTacToeMain > isWin > detects all three rows, columns and diagonals"* — [Zeile 32–37](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/TicTacToeMainTest.java#L32-L37)

Parametrisierter Test mit 8 Durchläufen (3 Reihen, 3 Spalten, 2 Diagonalen).

| | |
|---|---|
| **GIVEN** | Ein Spielfeld aus der Fixture `Boards.winningLinesForCross()`, auf dem `CROSS` genau eine der 8 möglichen Gewinnlinien besetzt — je einmal pro Reihe, Spalte und Diagonale, jeweils mit dem Namen der Linie als Testtitel. |
| **WHEN** | Das Brett mit `assertThatBoard(pattern).isWonBy(Stone.CROSS)` geprüft wird. Die Assertion ruft intern `TicTacToeMain.isWin` für beide Farben auf. |
| **THEN** | Für `CROSS` liefert `isWin` `true` (die Gewinnlinie wird erkannt), für `CIRCLE` liefert sie `false` (es wird kein falscher Gewinner gemeldet). |

**Warum dieser Test?** `isWin` besteht aus 8 hart kodierten Bedingungen. Ein Tippfehler in
einem einzigen Index würde eine Gewinnlinie unerkannt lassen. Der Test deckt jede Linie
einzeln ab.

### T2 – `IsWin.isFalseWithoutThreeInALine()`

*"TicTacToeMain > isWin > is false for an empty board and for a full board without a line"* — [Zeile 39–43](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/TicTacToeMainTest.java#L39-L43)

| | |
|---|---|
| **GIVEN** | Zwei Spielfelder ohne Gewinnlinie aus den Fixtures `Boards.EMPTY` (`"........."`) und `Boards.DRAW` (`"XOXXOOOXX"`). |
| **WHEN** | Beide Bretter mit `assertThatBoard(…).hasNoWinner()` geprüft werden, was `isWin` für `CROSS` und `CIRCLE` aufruft. |
| **THEN** | Alle Aufrufe liefern `false` — es wird kein Gewinner gemeldet, wo keiner ist. |

**Warum dieser Test?** Das leere Brett ist der wichtigste Grenzfall: Alle Felder sind `null`.
Ohne die vorgelagerte `b[x] == color`-Prüfung würde `null == null == null` fälschlicherweise
als Gewinn gelten. Der Test sichert diesen Negativfall ab.

### T3 – `Play.twoGreedyPlayersLetCrossWin()`

*"TicTacToeMain > play > lets CROSS win on 2-4-6 when two greedy players meet"* — [Zeile 55–63](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/TicTacToeMainTest.java#L55-L63)

| | |
|---|---|
| **GIVEN** | Zwei unabhängige `GreedyPlayer`-Instanzen aus `TestPlayers.greedy()`; der `GreedyPlayer` setzt immer auf das erste freie Feld von oben links. JUnit Pioneer (`@StdIo`) fängt `System.out` für die Dauer des Tests ab und reicht es als `StdOut` herein. |
| **WHEN** | Eine komplette Partie mit `TicTacToeMain.play(greedy(), greedy())` gespielt wird. Die Züge sind deterministisch: X→0, O→1, X→2, O→3, X→4, O→5, X→6. |
| **THEN** | Die Methode gibt `Stone.CROSS` zurück, denn `CROSS` besetzt nach dem 7. Zug die Nebendiagonale 2-4-6 und gewinnt — und die abgefangene Konsolenausgabe enthält `"...and the winner is: CROSS"`. |

**Warum dieser Test?** Dies ist der Happy-Path-Integrationstest: Er prüft die komplette
Spielschleife — Zugreihenfolge, Farbwechsel, Gewinnerkennung und Rückgabewert — in einem
vollständig deterministischen Szenario.

### T4 – `Play.rejectsTheSamePlayerTwice()`

*"TicTacToeMain > play > refuses to run a player against itself"* — [Zeile 64–70](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/TicTacToeMainTest.java#L64-L70)

| | |
|---|---|
| **GIVEN** | Eine einzige `TestPlayers.greedy()`-Instanz, die als beide Spieler übergeben werden soll. |
| **WHEN** | `TicTacToeMain.play(player, player)` mit derselben Referenz für X und O aufgerufen wird. |
| **THEN** | Es wird eine `IllegalArgumentException` mit der Nachricht `"players must differ"` geworfen; das Spiel startet gar nicht erst. |

**Warum dieser Test?** Die Spielschleife unterscheidet die beiden Spieler über einen
Referenzvergleich (`currentPlayer == xPlayer`). Wäre es dieselbe Instanz, würde die
Farbzuordnung brechen. Der Test sichert diese Vorbedingung ab.

### T5 – `Play.rejectsInvalidMoves(int position)`

*"TicTacToeMain > play > rejects moves outside the board and onto occupied fields"* — [Zeile 72–80](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/TicTacToeMainTest.java#L72-L80)

Parametrisierter Test mit 3 Durchläufen: `-1`, `9` und `0`.

| | |
|---|---|
| **GIVEN** | Ein regulärer `TestPlayers.greedy()` als X und `TestPlayers.alwaysPlayingTo(position)` als „schummelnder" O, der immer auf die ungültige Position `position` setzt. X eröffnet auf Feld `0`. |
| **WHEN** | `TicTacToeMain.play(greedy, cheater)` gespielt wird und der Schummler seinen Zug macht — mit `-1` (unterhalb des Bretts), `9` (oberhalb des Bretts) oder `0` (bereits von X belegt). |
| **THEN** | Es wird eine `IllegalStateException` mit der Nachricht `"cannot play to position <position>"` geworfen, und die mit Pioneer abgefangene Ausgabe enthält das Brett `X........`, wie es vor dem ungültigen Zug war. |

**Warum dieser Test?** Er deckt alle drei Zweige der Zug-Validierung ab
(`playTo < 0`, `playTo >= 9`, `board[playTo] != null`) — beide Bereichsgrenzen und die
Belegt-Prüfung.

---

## 4a. Tests, die aus dem Mutation Testing entstanden sind

Der erste PIT-Lauf liess 23 Mutanten überleben (siehe [MUTATIONTESTING.md](MUTATIONTESTING.md)). Für jede Lücke wurde ein Test ergänzt:

### T6 – `IsWin.detectsEveryWinningLineOfCircle(String line, String pattern)`

*"TicTacToeMain > isWin > detects all three rows, columns and diagonals of CIRCLE"* — [TicTacToeMainTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/TicTacToeMainTest.java)

Parametrisierter Test mit 8 Durchläufen.

| | |
|---|---|
| **GIVEN** | Die 8 Gewinnbretter aus `Boards.winningLinesForCircle()`, die aus `winningLinesForCross()` durch Ersetzen von `X` durch `O` entstehen. |
| **WHEN** | Jedes Brett mit `assertThatBoard(pattern).isWonBy(Stone.CIRCLE)` geprüft wird. |
| **THEN** | `isWin` liefert `true` für `CIRCLE` und `false` für `CROSS`. |

**Warum dieser Test?** PIT konnte in `isWin` mehrere Vergleiche durch `true` ersetzen, ohne dass ein Test fehlschlug, weil nur Siege von CROSS geprüft wurden.

### T7 – `IsWin.isFalseForTwoInALine(String line, String pattern)`

*"TicTacToeMain > isWin > is false when one field of the line holds the other colour"* — [TicTacToeMainTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/TicTacToeMainTest.java)

Parametrisierter Test mit 24 Durchläufen (8 Linien × 3 Felder).

| | |
|---|---|
| **GIVEN** | Die 24 Bretter aus `Boards.nearlyWinningLinesForCross()`: jede der 8 Gewinnlinien von CROSS, bei der je eines der drei Felder stattdessen von CIRCLE besetzt ist. |
| **WHEN** | Jedes Brett mit `assertThatBoard(pattern).hasNoWinner()` geprüft wird. |
| **THEN** | Für keine Farbe wird ein Gewinn gemeldet. |

**Warum dieser Test?** Erst diese Bretter töten Mutanten wie `b[0] == b[1]` → `true`: Zwei eigene Steine plus ein gegnerischer dürfen nie als Linie gelten.

### T8 – `Play.circleCanWin()`

*"TicTacToeMain > play > lets CIRCLE win and announces it"* — [TicTacToeMainTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/TicTacToeMainTest.java)

| | |
|---|---|
| **GIVEN** | Zwei gescriptete Spieler aus `TestPlayers.playingInOrder(…)`: X spielt 0, 1, 8; O spielt 3, 4, 5. |
| **WHEN** | Die Partie mit `TicTacToeMain.play(x, o)` gespielt wird. |
| **THEN** | Rückgabe ist `Stone.CIRCLE` (mittlere Reihe 3-4-5), die Ausgabe enthält `"...and the winner is: CIRCLE"`. |

**Warum dieser Test?** Bis dahin gewann in jedem Spieltest CROSS; die Farbzuweisung `currentPlayer == xPlayer ? CROSS : CIRCLE` war faktisch ungeprüft.

### T9 – `Play.fullBoardWithoutLineIsADraw()`

*"TicTacToeMain > play > ends in a draw after nine moves without a line"* — [TicTacToeMainTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/TicTacToeMainTest.java)

| | |
|---|---|
| **GIVEN** | Gescriptete Spieler, deren Züge (X: 0, 2, 3, 7, 8; O: 1, 4, 5, 6) genau das Remis-Brett `Boards.DRAW` ergeben. |
| **WHEN** | Die Partie über alle neun Züge gespielt wird. |
| **THEN** | Rückgabe ist `null`, die Ausgabe enthält `"it's a draw!"`. |

**Warum dieser Test?** Der Remis-Pfad (`return null` nach der Schleife) hatte keine Coverage; PIT meldete `println("it's a draw!")` als `NO_COVERAGE` und konnte die Schleifengrenze `round < 9` unbemerkt verändern.

### T10 – `ToString.rendersTheBoard()`

*"TicTacToeMain > toString > renders stones in bold and empty fields as their grey index, three per line"* — [TicTacToeMainTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/TicTacToeMainTest.java)

| | |
|---|---|
| **GIVEN** | Das Brett `X.O.X...O` und die erwartete Ausgabe aus den ANSI-Sequenzen `\033[1m` (fett), `\033[37m` (grau) und `\033[0m` (zurücksetzen). |
| **WHEN** | `TicTacToeMain.toString(board)` aufgerufen wird. |
| **THEN** | Die Ausgabe ist zeichengenau gleich: drei Zeilen, Steine fett, leere Felder als graue Indizes, je zwei Leerzeichen dahinter. |

**Warum dieser Test?** `toString` war zu 100 % abgedeckt, aber ungeprüft: Selbst `return ""` überlebte. Nur ein exakter Vergleich tötet alle acht Mutanten in dieser Methode.

### T11 – `GreedyPlayerTest.takesTheFirstFreeField(String pattern, int expected)`

*"GreedyPlayer > takes the first free field"* — [GreedyPlayerTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/players/GreedyPlayerTest.java)

Parametrisierter Test mit 3 Durchläufen.

| | |
|---|---|
| **GIVEN** | Drei Bretter per `@CsvSource`: leer, die ersten beiden Felder belegt, nur Feld 8 frei. |
| **WHEN** | `player.play(board, CROSS)` aufgerufen wird. |
| **THEN** | Rückgabe ist 0, 2 bzw. 8. |

**Warum dieser Test?** Die Schleifengrenze `i < BOARD_SIZE` war nur indirekt über Partien geprüft; der letzte freie Platz ist der Grenzwert.

### T12 – `GreedyPlayerTest.failsOnAFullBoard()`

*"GreedyPlayer > refuses to play on a full board"* — [GreedyPlayerTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/players/GreedyPlayerTest.java)

| | |
|---|---|
| **GIVEN** | Das volle Brett `Boards.DRAW`. |
| **WHEN** | `player.play(board, CROSS)` aufgerufen wird. |
| **THEN** | `IllegalStateException("cannot play at all")`. |

**Warum dieser Test?** Mit `i <= BOARD_SIZE` käme stattdessen eine `ArrayIndexOutOfBoundsException`; PIT hatte genau diesen Mutanten überleben lassen.

### T13 – `StoneTest.opponentIsTheOtherColour()`

*"Stone > the opponent of CROSS is CIRCLE and vice versa"* — [StoneTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/StoneTest.java)

| | |
|---|---|
| **GIVEN** | Die beiden Enum-Werte `CROSS` und `CIRCLE`. |
| **WHEN** | `opponent()` auf beiden aufgerufen wird. |
| **THEN** | `CROSS.opponent()` ist `CIRCLE`, `CIRCLE.opponent()` ist `CROSS`. |

**Warum dieser Test?** `CIRCLE.opponent()` wurde nirgends aufgerufen, der Vergleich `this == CROSS` liess sich durch `true` ersetzen.


---

## 4b. StdIn/StdOut-Tests mit JUnit Pioneer

Quelle: [`HumanPlayerTest.java`](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/players/HumanPlayerTest.java). JUnit Pioneers `@StdIo("…")` ersetzt `System.in` für die Dauer des Tests durch die angegebenen Zeilen; ein Parameter vom Typ `StdOut` liefert alles, was auf `System.out` geschrieben wurde.

### T14 – `HumanPlayerTest.playsToTheTypedPosition()`

*"HumanPlayer > plays to the position typed on the console"* — [HumanPlayerTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/players/HumanPlayerTest.java)

| | |
|---|---|
| **GIVEN** | `@StdIo("4")`: Auf `System.in` liegt die Zeile `4`. Ein leeres Brett. |
| **WHEN** | `player.play(board, CROSS)` aufgerufen wird. |
| **THEN** | Rückgabe ist `4`. |

**Warum dieser Test?** Erster Nachweis überhaupt, dass der Konsolenspieler die Eingabe korrekt in eine Position übersetzt; bis dahin war die Klasse nicht testbar.

### T15 – `HumanPlayerTest.showsTheBoardAndAsksForTheColour(StdOut out)`

*"HumanPlayer > shows the board and asks for the colour to play before reading"* — [HumanPlayerTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/players/HumanPlayerTest.java)

| | |
|---|---|
| **GIVEN** | `@StdIo("8")`, das Brett `X...O....` und die Farbe `CIRCLE`. |
| **WHEN** | `player.play(board, CIRCLE)` aufgerufen wird. |
| **THEN** | Die abgefangene Ausgabe beginnt mit `TicTacToeMain.toString(board)` und enthält `"where to to put the next CIRCLE? (0-8):"`. |

**Warum dieser Test?** Der Spieler muss sehen, worauf er antwortet; PIT entfernt sonst das `println` unbemerkt.

### T16 – `HumanPlayerTest.rejectsNonNumericInput()`

*"HumanPlayer > fails on input that is not a number"* — [HumanPlayerTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/players/HumanPlayerTest.java)

| | |
|---|---|
| **GIVEN** | `@StdIo("middle")`. |
| **WHEN** | `player.play(board, CROSS)` aufgerufen wird. |
| **THEN** | `NumberFormatException`, deren Meldung die Eingabe `middle` enthält. |

**Warum dieser Test?** Hält das im Testkonzept als Risiko R4 beschriebene Verhalten fest, ohne den Originalcode zu ändern.

### T17 – `TicTacToeMainTest.Main.humanPlaysCrossAgainstTheGreedyPlayer(StdOut out)`

*"TicTacToeMain > main > lets a human play CROSS against the greedy player"* — [TicTacToeMainTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/TicTacToeMainTest.java)

| | |
|---|---|
| **GIVEN** | `@StdIo("9")`: Der Mensch tippt als ersten Zug die ungültige Position 9. |
| **WHEN** | `TicTacToeMain.main(new String[0])` aufgerufen wird. |
| **THEN** | Die Ausgabe enthält den Prompt `"where to to put the next CROSS? (0-8):"`, und es wird `IllegalStateException("cannot play to position 9")` geworfen. |

**Warum dieser Test?** Belegt die Verdrahtung in `main` (Mensch ist X, Spiel läuft). Ein vollständiges Spiel ist über `main` nicht möglich, weil `HumanPlayer` pro Zug einen neuen `Scanner` anlegt, der die restliche gepufferte Eingabe verschluckt. JaCoCo zählt die Zeilen von `main` deshalb nicht als abgedeckt (Sonde nach der Exception nicht erreicht), PIT tötet die Mutanten in `main` aber.


---

## 4c. Der perfekte Spieler

Quelle: [`PerfectPlayerTest.java`](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/players/PerfectPlayerTest.java). `PerfectPlayer` durchsucht den vollständigen Spielbaum (Minimax in Negamax-Form) und merkt sich jede bewertete Stellung.

### T18 – `SingleMoves.takesTheImmediateWin(String pattern, Stone color, int expected)`

*"PerfectPlayer > single moves > completes its own line as soon as it can"* — [PerfectPlayerTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/players/PerfectPlayerTest.java)

Parametrisierter Test mit 4 Durchläufen.

| | |
|---|---|
| **GIVEN** | Vier Bretter per `@CsvSource`, auf denen die Farbe am Zug zwei Steine in einer Linie hat (Reihe, Spalte, Diagonale; als CROSS und als CIRCLE). |
| **WHEN** | `player.play(board, color)` aufgerufen wird. |
| **THEN** | Rückgabe ist das dritte Feld der Linie. |

**Warum dieser Test?** Die einfachste Eigenschaft eines perfekten Spielers.

### T19 – `SingleMoves.blocksTheOpponentsThreat(String pattern, Stone color, int expected)`

*"PerfectPlayer > single moves > blocks the opponent's two in a line when it cannot win itself"* — [PerfectPlayerTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/players/PerfectPlayerTest.java)

Parametrisierter Test mit 3 Durchläufen.

| | |
|---|---|
| **GIVEN** | Drei Bretter, auf denen der Gegner zwei in einer Linie hat und die eigene Farbe nicht sofort gewinnen kann. |
| **WHEN** | `player.play(board, color)` aufgerufen wird. |
| **THEN** | Rückgabe ist das Feld, das die gegnerische Linie unterbricht. |

**Warum dieser Test?** Ein Spieler, der nicht blockt, verliert im nächsten Zug.

### T20 – `SingleMoves.prefersTheFasterWin()`

*"PerfectPlayer > single moves > wins at once instead of blocking first, even though blocking would also win later"* — [PerfectPlayerTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/players/PerfectPlayerTest.java)

| | |
|---|---|
| **GIVEN** | Das Brett `....OOX.X` (per Brute-Force gesucht): CROSS gewinnt sofort auf 7, könnte aber auch auf 3 blocken und zwei Züge später erzwungen gewinnen. |
| **WHEN** | `player.play(board, CROSS)` aufgerufen wird. |
| **THEN** | Rückgabe ist `7`. |

**Warum dieser Test?** Ohne die Abschwächung der Bewertung pro Zugtiefe wären beide Züge gleich gut und der Spieler nähme den niedrigeren Index 3. Der Test macht die Tiefenbewertung prüfbar und tötet die entsprechenden Mutanten.

### T21 – `SingleMoves.failsOnAFullBoard()`

*"PerfectPlayer > single moves > refuses to play on a full board"* — [PerfectPlayerTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/players/PerfectPlayerTest.java)

| | |
|---|---|
| **GIVEN** | Das volle Brett `Boards.DRAW`. |
| **WHEN** | `player.play(board, CROSS)` aufgerufen wird. |
| **THEN** | `IllegalStateException("cannot play at all")`. |

**Warum dieser Test?** Gleiches Verhalten wie `GreedyPlayer` im selben Fall.

### T22 – `WholeGames.neverLosesAgainstAnyOpponent(Stone ownColor)`

*"PerfectPlayer > whole games > never loses, whatever the opponent does"* — [PerfectPlayerTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/players/PerfectPlayerTest.java)

Parametrisierter Test mit 2 Durchläufen (CROSS, CIRCLE).

| | |
|---|---|
| **GIVEN** | Ein leeres Brett und die eigene Farbe aus `@EnumSource(Stone.class)`. |
| **WHEN** | Die Hilfsmethode `playOutEveryGame` jede Partie zu Ende spielt: Ist der perfekte Spieler am Zug, spielt er seinen Zug; ist der Gegner am Zug, wird für **jedes** freie Feld ein eigener Spielzweig verfolgt. |
| **THEN** | Auf keinem einzigen erreichten Brett hat der Gegner drei in einer Linie (`assertThatBoard(board).isNotWinFor(opponent)`); die Anzahl gespielter Partien ist positiv. |

**Warum dieser Test?** Das ist der eigentliche Beweis für „perfekt“: nicht einige Partien, sondern alle, die der Spieler überhaupt erreichen kann, als X und als O.

### T23 – `WholeGames.drawsAgainstItself(StdOut out)`

*"PerfectPlayer > whole games > ends in a draw against itself"* — [PerfectPlayerTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/players/PerfectPlayerTest.java)

| | |
|---|---|
| **GIVEN** | Zwei `PerfectPlayer`-Instanzen, Ausgabe über Pioneer abgefangen. |
| **WHEN** | `TicTacToeMain.play(perfect, perfect)` gespielt wird. |
| **THEN** | Rückgabe `null`, Ausgabe enthält `"it's a draw!"`. |

**Warum dieser Test?** TicTacToe ist bei beidseitig perfektem Spiel ein Remis; alles andere wäre ein Fehler in einem der beiden.

### T24 – `WholeGames.beatsTheGreedyPlayerAsCross(StdOut out)`

*"PerfectPlayer > whole games > beats the greedy player as CROSS"* — [PerfectPlayerTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/players/PerfectPlayerTest.java)

| | |
|---|---|
| **GIVEN** | `PerfectPlayer` als X, `TestPlayers.greedy()` als O. |
| **WHEN** | Die Partie gespielt wird. |
| **THEN** | Rückgabe `CROSS` und die Gewinnmeldung. |

**Warum dieser Test?** Ein Spieler, der nur nie verliert (z. B. immer Remis anstrebt), wäre nicht perfekt; gegen einen schwachen Gegner muss er gewinnen.

### T25 – `WholeGames.beatsTheGreedyPlayerAsCircle(StdOut out)`

*"PerfectPlayer > whole games > beats the greedy player as CIRCLE"* — [PerfectPlayerTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/players/PerfectPlayerTest.java)

| | |
|---|---|
| **GIVEN** | `TestPlayers.greedy()` als X, `PerfectPlayer` als O. |
| **WHEN** | Die Partie gespielt wird (X:0 O:4 X:1 O:2 X:3 O:6). |
| **THEN** | Rückgabe `CIRCLE` (Nebendiagonale 2-4-6) und die Gewinnmeldung. |

**Warum dieser Test?** Als Nachziehender muss der Spieler zuerst richtig verteidigen (Mitte), dann die Drohung blocken und erst dann gewinnen.


---

## 4d. Property-based Tests mit jqwik

Quelle: [`PerfectPlayerProperties.java`](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/players/PerfectPlayerProperties.java). jqwik ist eine eigene Engine auf der JUnit Platform: Statt fester Beispiele beschreibt eine `@Property` eine Eigenschaft, und jqwik generiert dafür standardmässig **1000 Eingaben**. Schlägt eine fehl, verkleinert jqwik sie automatisch zum einfachsten Gegenbeispiel (*Shrinking*). `@Provide`-Methoden liefern die Generatoren (`Arbitrary`).

### P1 – `neverLosesAgainstAnyOpponent(@ForAll List<Integer> preferences, @ForAll Stone ownColor)`

*"PerfectPlayerProperties > neverLosesAgainstAnyOpponent"* — [PerfectPlayerProperties.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/players/PerfectPlayerProperties.java)

| | |
|---|---|
| **GIVEN** | Eine zufällige Permutation der Felder 0–8 aus `Arbitraries.shuffle(…)` als Zugpräferenz des Gegners (er nimmt das erste freie Feld in dieser Reihenfolge) und eine zufällige eigene Farbe. |
| **WHEN** | Die Partie mit dem perfekten Spieler als X bzw. O gespielt wird. |
| **THEN** | Der Gewinner ist nie die Gegnerfarbe. |

**Warum dieser Test?** Ergänzt T22 um eine zweite, unabhängige Formulierung: 1000 Gegner mit beliebiger Zugreihenfolge.

### P2 – `playsToAFreeFieldWithoutChangingTheBoard(@ForAll Stone[] board, @ForAll Stone color)`

*"PerfectPlayerProperties > playsToAFreeFieldWithoutChangingTheBoard"* — [PerfectPlayerProperties.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/players/PerfectPlayerProperties.java)

| | |
|---|---|
| **GIVEN** | Ein zufälliges offenes Brett: jedes Feld `CROSS`, `CIRCLE` oder mit 40 % Wahrscheinlichkeit leer (`injectNull`), gefiltert auf mindestens ein freies Feld und keine bestehende Linie. |
| **WHEN** | `player.play(board, color)` aufgerufen wird. |
| **THEN** | Das gewählte Feld ist frei, und das übergebene Array ist danach unverändert. |

**Warum dieser Test?** Die Suche setzt Steine probeweise und nimmt sie zurück; ein vergessenes Zurücknehmen würde das Brett des Aufrufers beschädigen. Zufällige, auch unerreichbare Bretter decken Fälle ab, die keine Partie erzeugt.


---

## 5. Testergebnis: Alle Tests erfolgreich

Befehl:

```bash
./gradlew clean build pitest --console=plain
```

**Ergebnis vom 25.10.2026: 76 ausgeführte Tests, 0 Fehler** (29 Testmethoden, davon 8
parametrisiert und 2 jqwik-Properties à 1000 Durchläufe), Branch-Coverage **100 %**
(Verification ≥ 90 % bestanden), Mutation Score **96 %**, `BUILD SUCCESSFUL`.

| Testklasse | Ausgeführte Tests |
|---|---:|
| `DummySetupTest` | 2 |
| `StoneTest` | 1 |
| `TicTacToeMainTest` (`isWin` 41, `toString` 1, `play` 7, `main` 1) | 50 |
| `GreedyPlayerTest` | 4 |
| `HumanPlayerTest` | 3 |
| `PerfectPlayerTest` (`single moves` 9, `whole games` 5) | 14 |
| `PerfectPlayerProperties` | 2 |
| **Total** | **76** |

Auszug aus der Konsolenausgabe:

```
PerfectPlayer > whole games > never loses, whatever the opponent does > as CROSS PASSED
PerfectPlayer > whole games > never loses, whatever the opponent does > as CIRCLE PASSED
PerfectPlayer > whole games > ends in a draw against itself PASSED
HumanPlayer > plays to the position typed on the console PASSED
TicTacToeMain > main > lets a human play CROSS against the greedy player PASSED
...
>> Generated 137 mutations Killed 131 (96%)

BUILD SUCCESSFUL
```

### Screenshot

![Alle Tests erfolgreich](screenshots/tests-erfolgreich.png)

*(Screenshot vom ersten Stand mit 16 Tests; der aktuelle Lauf mit 76 Tests ist in der CI unter Actions → CI → Artefakt `test-report` einsehbar.)*

---

## 6. Testergebnis: Ein Test schlägt fehl

Um einen Fehlschlag zu demonstrieren, wird in
[`DummySetupTest.junitDummy()`](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/DummySetupTest.java#L17-L19)
die Assertion temporär invertiert:

```diff
- assertFalse(false, "if this test runs at all, JUnit Jupiter is wired up correctly");
+ assertFalse(true, "absichtlich fehlschlagender Test fuer den Screenshot");
```

Danach `./gradlew test --rerun-tasks --no-build-cache --console=plain` erneut ausführen.
Der Build bricht mit `Task :test FAILED` ab, alle übrigen Tests laufen weiterhin grün durch (Protokoll vom ersten Stand mit 16 Tests; das Verhalten ist heute mit 76 Tests identisch):

```
> Task :test FAILED

TicTacToeMain > play > rejects moves outside the board and onto occupied fields > playing to position -1 is rejected PASSED
...
TicTacToeMain > isWin > is false for an empty board and for a full board without a line PASSED

DummySetupTest > AssertJ assertions are available and run PASSED

DummySetupTest > JUnit 5 assertions are available and run FAILED
    org.opentest4j.AssertionFailedError at DummySetupTest.java:18

16 tests completed, 1 failed

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':test'.
> There were failing tests. See the report at: file:///C:/codingProjects/450-tictactest-mvk/build/reports/tests/test/index.html
```

Die vollständige Fehlermeldung
(`absichtlich fehlschlagender Test fuer den Screenshot ==> expected: <false> but was: <true>`)
steht im HTML-Report unter `build/reports/tests/test/index.html`.

Nach dem Screenshot wird die Änderung wieder rückgängig gemacht, damit der Build
auf `main` grün bleibt.

### Screenshot

![Ein Test schlägt fehl](screenshots/test-fehlgeschlagen.png)

---

## 7. Continuous Integration

Die Tests, die Coverage Verification und das Mutation Testing laufen bei jedem Push auf
jeden Branch automatisch in GitHub Actions; Test-, Coverage- und Mutation-Report werden
als Artefakte (`test-report`, `coverage-report`, `mutation-report`) hochgeladen:
[`.github/workflows/ci.yml`](https://github.com/TheGloo/450-tictactest-mvk/blob/main/.github/workflows/ci.yml)

Workflow-Läufe: <https://github.com/TheGloo/450-tictactest-mvk/actions>
