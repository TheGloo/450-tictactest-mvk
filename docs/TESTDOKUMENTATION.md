# Testdokumentation – TicTacToe (M450)

Dokumentation aller Unit-Tests nach dem **GIVEN / WHEN / THEN**-Pattern.

| | |
|---|---|
| **Projekt** | `450-tictactest-mvk` (TicTacToe) |
| **Test-Framework** | JUnit 5 (Jupiter) `5.14.0` |
| **Assertion-Library** | AssertJ `3.27.6` |
| **Build-Tool** | Gradle (`./gradlew test`) |
| **Repository** | <https://github.com/TheGloo/450-tictactest-mvk> |

---

## 1. Setup

Die Test-Abhängigkeiten sind in [`build.gradle`](https://github.com/TheGloo/450-tictactest-mvk/blob/main/build.gradle) deklariert:

```gradle
dependencies {
    testImplementation platform('org.junit:junit-bom:5.14.0')
    testImplementation 'org.junit.jupiter:junit-jupiter'
    testImplementation 'org.assertj:assertj-core:3.27.6'
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
./gradlew test
```

---

## 2. Link zum Test-Code auf GitHub

| Testklasse | Zweck | Link |
|---|---|---|
| `DummySetupTest` | Dummy-Tests für JUnit & AssertJ | [DummySetupTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/DummySetupTest.java) |
| `TicTacToeMainTest` | 5 fachliche Tests des Spiels | [TicTacToeMainTest.java](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/TicTacToeMainTest.java) |

Gesamter Test-Ordner: <https://github.com/TheGloo/450-tictactest-mvk/tree/main/src/test/java/ch/bbw/m450/tictactoe>

### Test-Helper und Fixtures

Damit die Tests selbst nur noch das Szenario beschreiben, liegen Testdaten, Test-Doubles
und eigene Assertions im Paket
[`testsupport`](https://github.com/TheGloo/450-tictactest-mvk/tree/main/src/test/java/ch/bbw/m450/tictactoe/testsupport):

| Klasse | Art | Aufgabe |
|---|---|---|
| [`Boards`](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/testsupport/Boards.java) | Fixture + Helper | Konstanten `EMPTY` und `DRAW`, der Builder `board(String)` (9-Zeichen-Muster → `Stone[]`), `render(…)` für lesbare Fehlermeldungen und `winningLinesForCross()` als Datenquelle für den parametrisierten Test |
| [`TestPlayers`](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/testsupport/TestPlayers.java) | Test-Doubles | `greedy()` und `alwaysPlayingTo(int)` — ein berechenbarer bzw. ein „schummelnder" Gegner. Nötig, weil `HumanPlayer` von stdin liest und im Test unbrauchbar ist |
| [`BoardAssert`](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/testsupport/BoardAssert.java) | eigene AssertJ-Assertion | `assertThatBoard(…).isWonBy(CROSS)` / `.hasNoWinner()`; bei einem Fehlschlag wird das betroffene Brett mit ausgegeben |
| [`ConsoleCapture`](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/testsupport/ConsoleCapture.java) | JUnit-5-Fixture (Extension) | Leitet `System.out` pro Test um und stellt es danach wieder her. Hält die Brett-Ausgaben von `play(…)` aus der Build-Ausgabe heraus und macht sie für Assertions verfügbar |

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

Die Tests sind mit `@Nested` nach der getesteten Methode gruppiert (`isWin` und `play`),
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
| **GIVEN** | Zwei unabhängige `GreedyPlayer`-Instanzen aus `TestPlayers.greedy()`; der `GreedyPlayer` setzt immer auf das erste freie Feld von oben links. Die Fixture `ConsoleCapture` fängt `System.out` für die Dauer des Tests ab. |
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
| **THEN** | Es wird eine `IllegalStateException` mit der Nachricht `"cannot play to position <position>"` geworfen. |

**Warum dieser Test?** Er deckt alle drei Zweige der Zug-Validierung ab
(`playTo < 0`, `playTo >= 9`, `board[playTo] != null`) — beide Bereichsgrenzen und die
Belegt-Prüfung.

---

## 5. Testergebnis: Alle Tests erfolgreich

Befehl:

```bash
./gradlew test --rerun-tasks --no-build-cache --console=plain
```

**Ergebnis: 16 ausgeführte Tests, 0 Fehler** (7 Testmethoden, davon 2 parametrisiert
mit 8 bzw. 3 Durchläufen).

```
> Task :test

DummySetupTest > AssertJ assertions are available and run PASSED
DummySetupTest > JUnit 5 assertions are available and run PASSED

TicTacToeMain > play > rejects moves outside the board and onto occupied fields > playing to position -1 is rejected PASSED
TicTacToeMain > play > rejects moves outside the board and onto occupied fields > playing to position 9 is rejected PASSED
TicTacToeMain > play > rejects moves outside the board and onto occupied fields > playing to position 0 is rejected PASSED
TicTacToeMain > play > lets CROSS win on 2-4-6 when two greedy players meet PASSED
TicTacToeMain > play > refuses to run a player against itself PASSED

TicTacToeMain > isWin > detects all three rows, columns and diagonals > top row PASSED
TicTacToeMain > isWin > detects all three rows, columns and diagonals > middle row PASSED
TicTacToeMain > isWin > detects all three rows, columns and diagonals > bottom row PASSED
TicTacToeMain > isWin > detects all three rows, columns and diagonals > left column PASSED
TicTacToeMain > isWin > detects all three rows, columns and diagonals > middle column PASSED
TicTacToeMain > isWin > detects all three rows, columns and diagonals > right column PASSED
TicTacToeMain > isWin > detects all three rows, columns and diagonals > main diagonal PASSED
TicTacToeMain > isWin > detects all three rows, columns and diagonals > anti diagonal PASSED
TicTacToeMain > isWin > is false for an empty board and for a full board without a line PASSED

BUILD SUCCESSFUL in 2s
3 actionable tasks: 3 executed
```

### Screenshot

![Alle Tests erfolgreich](screenshots/tests-erfolgreich.png)

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
Der Build bricht mit `Task :test FAILED` ab, die übrigen 15 Tests laufen weiterhin grün durch:

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

Die Tests laufen zusätzlich bei jedem Push auf `main` automatisch in GitHub Actions:
[`.github/workflows/ci.yml`](https://github.com/TheGloo/450-tictactest-mvk/blob/main/.github/workflows/ci.yml)

Workflow-Läufe: <https://github.com/TheGloo/450-tictactest-mvk/actions>
