# Testdokumentation – TicTacToe (M450)

Dokumentation aller Unit-Tests nach dem **GIVEN / WHEN / THEN**-Pattern.

| | |
|---|---|
| **Projekt** | `450-tictactest-mvk` (TicTacToe) |
| **Test-Framework** | JUnit 5 (Jupiter) `5.14.0` |
| **Assertion-Library** | AssertJ `3.27.6` |
| **Build-Tool** | Gradle (`./gradlew test`) |
| **Umfang** | 3 Testklassen, 11 Testmethoden, **42 ausgeführte Tests** |
| **Repository** | <https://github.com/TheGloo/450-tictactest-mvk> |

Die Teststrategie, die nummerierten Testziele und die Zuordnung Ziel ↔ Testfall
stehen im [Testkonzept](TESTKONZEPT.md).

---

## 1. Setup

Die Test-Abhängigkeiten sind in [`build.gradle`](../build.gradle) deklariert:

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

## 2. Test-Code

| Testklasse | Zweck | Datei |
|---|---|---|
| `DummySetupTest` | Dummy-Tests für JUnit & AssertJ | [DummySetupTest.java](../src/test/java/ch/bbw/m450/tictactoe/DummySetupTest.java) |
| `TicTacToeMainTest` | Spielregeln: `isWin` und `play` | [TicTacToeMainTest.java](../src/test/java/ch/bbw/m450/tictactoe/TicTacToeMainTest.java) |
| `GreedyPlayerTest` | Computerspieler `GreedyPlayer` | [GreedyPlayerTest.java](../src/test/java/ch/bbw/m450/tictactoe/players/GreedyPlayerTest.java) |

### Test-Helper und Fixtures

Damit die Tests selbst nur noch das Szenario beschreiben, liegen Testdaten, Test-Doubles
und eigene Assertions im Paket
[`testsupport`](../src/test/java/ch/bbw/m450/tictactoe/testsupport):

| Klasse | Art | Aufgabe |
|---|---|---|
| [`Boards`](../src/test/java/ch/bbw/m450/tictactoe/testsupport/Boards.java) | Fixtures + Helper | Konstanten `EMPTY`, `DRAW`, `DRAW_MOVES_CROSS`, `DRAW_MOVES_CIRCLE`; der Builder `board(String)` (9-Zeichen-Muster → `Stone[]`); `render(…)` für lesbare Fehlermeldungen; die Datenquellen `winningLines()` und `boardsWithoutWinner()` für die parametrisierten Tests |
| [`TestPlayers`](../src/test/java/ch/bbw/m450/tictactoe/testsupport/TestPlayers.java) | Test-Doubles | `greedy()`, `alwaysPlayingTo(int)` und `playingSequence(int...)` — ein berechenbarer Gegner, ein „schummelnder" und einer, der eine feste Zugfolge abspielt. Nötig, weil `HumanPlayer` von stdin liest und im Test unbrauchbar ist |
| [`BoardAssert`](../src/test/java/ch/bbw/m450/tictactoe/testsupport/BoardAssert.java) | eigene AssertJ-Assertion | `assertThatBoard(…).isWonBy(color)` / `.hasNoWinner()`; bei einem Fehlschlag wird das betroffene Brett mit ausgegeben |
| [`ConsoleCapture`](../src/test/java/ch/bbw/m450/tictactoe/testsupport/ConsoleCapture.java) | JUnit-5-Fixture (Extension) | Leitet `System.out` pro Test um und stellt es danach wieder her. Hält die Brett-Ausgaben von `play(…)` aus der Build-Ausgabe heraus und macht sie für Assertions verfügbar |

Beispiel für eine Fehlermeldung von `BoardAssert`:

```
expected CROSS to have three in a line on board:

XX.
...
...
```

Das Brett ist ein eindimensionales Array der Länge 9. Die Muster in den Fixtures
werden zeilenweise von links oben nach rechts unten gelesen
(`X` = Kreuz, `O` = Kreis, alles andere = leeres Feld):

```
 0 | 1 | 2
---+---+---
 3 | 4 | 5
---+---+---
 6 | 7 | 8
```

---

## 3. Dummy-Tests (Setup-Nachweis)

Diese beiden Tests prüfen keine Fachlogik. Sie beweisen nur, dass das Test-Setup
funktioniert: dass JUnit 5 Tests findet und ausführt, und dass AssertJ auf dem
Test-Classpath liegt.

Quelle: [`DummySetupTest.java`](../src/test/java/ch/bbw/m450/tictactoe/DummySetupTest.java)

### D1 – `junitDummy()`

*"JUnit 5 assertions are available and run"*

| | |
|---|---|
| **GIVEN** | Das Projekt ist mit der JUnit-5-Jupiter-Abhängigkeit konfiguriert und `useJUnitPlatform()` ist im Gradle-`test`-Task aktiviert. |
| **WHEN** | Gradle führt den Test aus und dieser ruft die JUnit-Assertion `assertFalse(false)` auf. |
| **THEN** | Der Test wird überhaupt gefunden, ausgeführt und läuft grün durch — JUnit 5 ist korrekt eingebunden. |

```java
assertFalse(false, "if this test runs at all, JUnit Jupiter is wired up correctly");
```

> **Hinweis zur Aufgabenstellung:** Für den Fehlschlag-Screenshot (Kapitel 7) wird genau
> dieser Aufruf temporär auf `assertFalse(true)` geändert.

### D2 – `assertJDummy()`

*"AssertJ assertions are available and run"*

| | |
|---|---|
| **GIVEN** | Das Projekt hat `org.assertj:assertj-core` als `testImplementation`-Abhängigkeit. |
| **WHEN** | Der Test ruft die AssertJ-Fluent-Assertion `assertThat(true).isTrue()` auf. |
| **THEN** | Der Test kompiliert und läuft grün durch — AssertJ ist auf dem Test-Classpath verfügbar. |

---

## 4. Tests der Spielregeln (`TicTacToeMainTest`)

Quelle: [`TicTacToeMainTest.java`](../src/test/java/ch/bbw/m450/tictactoe/TicTacToeMainTest.java)

Die Tests sind mit `@Nested` nach der getesteten Methode gruppiert (`isWin` und `play`),
damit die Testausgabe die Struktur der Klasse widerspiegelt.

### T1 – `IsWin.detectsEveryWinningLine(Stone color, String line, String pattern)`

*"TicTacToeMain > isWin > detects every winning line for both colours"*

**Parametrisierter Test mit 16 Durchläufen:** 8 Gewinnlinien (3 Reihen, 3 Spalten,
2 Diagonalen) × 2 Farben.

| | |
|---|---|
| **GIVEN** | Ein Spielfeld aus der Fixture `Boards.winningLines()`, auf dem die übergebene Farbe genau eine der 8 möglichen Gewinnlinien besetzt. Die Fixture leitet die CIRCLE-Bretter aus denselben acht Linienmustern ab wie die CROSS-Bretter, sodass jede Linie im Testcode nur einmal existiert. |
| **WHEN** | Das Brett mit `assertThatBoard(pattern).isWonBy(color)` geprüft wird. Die Assertion ruft intern `TicTacToeMain.isWin` für beide Farben auf. |
| **THEN** | Für die gesetzte Farbe liefert `isWin` `true` (die Gewinnlinie wird erkannt), für die Gegenfarbe `false` (es wird kein falscher Gewinner gemeldet). |

```java
@ParameterizedTest(name = "{0} wins on the {1}")
@MethodSource("ch.bbw.m450.tictactoe.testsupport.Boards#winningLines")
void detectsEveryWinningLine(Stone color, String line, String pattern) {
    assertThatBoard(pattern).isWonBy(color);
}
```

**Warum dieser Test?** `isWin` besteht aus 8 hart kodierten Bedingungen. Ein Tippfehler
in einem einzigen Index würde eine Gewinnlinie unerkannt lassen. Der Test deckt jede
Linie einzeln ab — und zwar für beide Farben, weil der Parameter `color` in jede der
8 Bedingungen eingeht.

### T2 – `IsWin.isFalseWithoutThreeInALine(String constellation, String pattern)`

*"TicTacToeMain > isWin > reports no winner on boards without three in a line"*

**Parametrisierter Test mit 10 Durchläufen.**

| | |
|---|---|
| **GIVEN** | Zehn Bretter ohne Gewinnlinie aus der Fixture `Boards.boardsWithoutWinner()`: das leere Brett, zwei volle Bretter, „zwei in einer Reihe mit freiem drittem Feld", vier Linien, die von der Gegenfarbe blockiert sind (Reihe, Spalte, beide Diagonalen), drei verstreute Steine einer Farbe und ein Brett, auf dem beide Farben zwei nebeneinander haben. |
| **WHEN** | Jedes Brett mit `assertThatBoard(pattern).hasNoWinner()` geprüft wird, was `isWin` für `CROSS` und `CIRCLE` aufruft. |
| **THEN** | Alle Aufrufe liefern `false` — es wird kein Gewinner gemeldet, wo keiner ist. |

**Warum dieser Test?** Das leere Brett ist der wichtigste Grenzfall: Alle Felder sind
`null`. Ohne die vorgelagerte `b[x] == color`-Prüfung würde `null == null == null`
fälschlicherweise als Gewinn gelten. Die blockierten Linien sind die zweite
Fehlerquelle: Sie prüfen, dass alle drei Felder einer Linie tatsächlich verglichen
werden und nicht nur zwei.

### T3 – `Play.twoGreedyPlayersLetCrossWin()`

*"TicTacToeMain > play > lets CROSS win on 2-4-6 when two greedy players meet"*

| | |
|---|---|
| **GIVEN** | Zwei unabhängige `GreedyPlayer`-Instanzen aus `TestPlayers.greedy()`; der `GreedyPlayer` setzt immer auf das erste freie Feld von oben links. Die Fixture `ConsoleCapture` fängt `System.out` für die Dauer des Tests ab. |
| **WHEN** | Eine komplette Partie mit `TicTacToeMain.play(greedy(), greedy())` gespielt wird. Die Züge sind deterministisch: X→0, O→1, X→2, O→3, X→4, O→5, X→6. |
| **THEN** | Die Methode gibt `Stone.CROSS` zurück, denn `CROSS` besetzt nach dem 7. Zug die Nebendiagonale 2-4-6 und gewinnt — und die abgefangene Konsolenausgabe enthält `"...and the winner is: CROSS"`. |

**Warum dieser Test?** Der Happy-Path-Integrationstest: Er prüft die komplette
Spielschleife — Zugreihenfolge, Farbwechsel, Gewinnerkennung und Rückgabewert — in
einem vollständig deterministischen Szenario.

### T4 – `Play.fullBoardWithoutALineIsADraw()`

*"TicTacToeMain > play > returns null and reports a draw when all nine fields are filled"*

| | |
|---|---|
| **GIVEN** | Zwei gescriptete Spieler aus `TestPlayers.playingSequence(…)` mit den Zugfolgen `Boards.DRAW_MOVES_CROSS` (`0, 2, 3, 7, 8`) und `Boards.DRAW_MOVES_CIRCLE` (`1, 4, 5, 6`). Verschränkt füllen sie das Brett zu `Boards.DRAW`, ohne dass eine Farbe je drei in einer Linie hält — auch nicht zwischendurch. |
| **WHEN** | `TicTacToeMain.play(…)` die Partie über alle 9 Runden spielt. |
| **THEN** | Die Methode gibt `null` zurück, die Konsolenausgabe enthält `"it's a draw!"`, und das erreichte Brett `DRAW` hat für beide Farben keinen Gewinner. |

**Warum dieser Test?** Dies ist der einzige Testfall, der die Spielschleife regulär
zu Ende laufen lässt. Ohne ihn bliebe der `return null`-Pfad hinter der 9-Runden-Schleife
komplett ungetestet — eine Regression im Unentschieden-Fall wäre unentdeckt geblieben.

### T5 – `Play.rejectsTheSamePlayerTwice()`

*"TicTacToeMain > play > refuses to run a player against itself"*

| | |
|---|---|
| **GIVEN** | Eine einzige `TestPlayers.greedy()`-Instanz, die als beide Spieler übergeben werden soll. |
| **WHEN** | `TicTacToeMain.play(player, player)` mit derselben Referenz für X und O aufgerufen wird. |
| **THEN** | Es wird eine `IllegalArgumentException` mit der Nachricht `"players must differ"` geworfen; das Spiel startet gar nicht erst. |

**Warum dieser Test?** Die Spielschleife unterscheidet die beiden Spieler über einen
Referenzvergleich (`currentPlayer == xPlayer`). Wäre es dieselbe Instanz, würde die
Farbzuordnung brechen. Der Test sichert diese Vorbedingung ab.

### T6 – `Play.rejectsInvalidMoves(int position)`

*"TicTacToeMain > play > rejects moves outside the board and onto occupied fields"*

**Parametrisierter Test mit 3 Durchläufen:** `-1`, `9` und `0`.

| | |
|---|---|
| **GIVEN** | Ein regulärer `TestPlayers.greedy()` als X und `TestPlayers.alwaysPlayingTo(position)` als „schummelnder" O, der immer auf die ungültige Position `position` setzt. X eröffnet auf Feld `0`. |
| **WHEN** | `TicTacToeMain.play(greedy, cheater)` gespielt wird und der Schummler seinen Zug macht — mit `-1` (unterhalb des Bretts), `9` (oberhalb des Bretts) oder `0` (bereits von X belegt). |
| **THEN** | Es wird eine `IllegalStateException` mit der Nachricht `"cannot play to position <position>"` geworfen. |

**Warum dieser Test?** Er deckt alle drei Zweige der Zug-Validierung ab
(`playTo < 0`, `playTo >= 9`, `board[playTo] != null`) — beide Bereichsgrenzen und die
Belegt-Prüfung.

---

## 5. Tests des Computerspielers (`GreedyPlayerTest`)

Quelle: [`GreedyPlayerTest.java`](../src/test/java/ch/bbw/m450/tictactoe/players/GreedyPlayerTest.java)

### T7 – `takesTheFirstFreeField(String pattern, int expected)`

*"GreedyPlayer > always takes the first free field, top-left first"*

**Parametrisierter Test mit 6 Durchläufen** (`@CsvSource`).

| | |
|---|---|
| **GIVEN** | Sechs Brettkonstellationen mit dem jeweils erwarteten Feld: leeres Brett → `0`, erstes Feld belegt → `1`, Lücke ab der Mitte → `5`, nur letztes Feld frei → `8`, freies Feld hinter einer vollen Reihe → `8`, freies Feld ganz vorne → `0`. |
| **WHEN** | `player.play(board(pattern), Stone.CROSS)` aufgerufen wird. |
| **THEN** | Der Rückgabewert ist der Index des ersten freien Feldes von oben links. |

**Warum dieser Test?** `GreedyPlayer` ist nicht nur ausgelieferter Produktivcode,
sondern auch der berechenbare Gegner in T3. Die Konstellationen decken beide Ränder
des Bretts (`0` und `8`) sowie die Mitte ab.

### T8 – `ignoresTheColour()`

*"GreedyPlayer > plays the same field regardless of the colour it is asked to play"*

| | |
|---|---|
| **GIVEN** | Zweimal dasselbe leere Brett. |
| **WHEN** | Derselbe Spieler einmal für `CROSS` und einmal für `CIRCLE` befragt wird. |
| **THEN** | Beide Aufrufe liefern denselben Index — der Parameter `colorToPlay` beeinflusst die Wahl nicht. |

### T9 – `throwsOnAFullBoard()`

*"GreedyPlayer > throws when the board is full"*

| | |
|---|---|
| **GIVEN** | Das volle Brett `Boards.DRAW`, auf dem kein Feld mehr frei ist. |
| **WHEN** | `player.play(board(DRAW), Stone.CROSS)` aufgerufen wird. |
| **THEN** | Es wird eine `IllegalStateException` mit der Nachricht `"cannot play at all"` geworfen. |

**Warum dieser Test?** Er deckt den Pfad hinter der Suchschleife ab, den ein reguläres
Spiel nie erreicht, weil `play` nach dem 9. Zug endet.

---

## 6. Testergebnis: Alle Tests erfolgreich

Befehl:

```bash
./gradlew test --rerun-tasks --no-build-cache --console=plain
```

**Ergebnis: 42 ausgeführte Tests, 0 Fehler** (11 Testmethoden, davon 4 parametrisiert
mit 16, 10, 3 bzw. 6 Durchläufen).

| Testklasse / Gruppe | Testmethoden | Ausgeführte Tests |
|---|---|---|
| `DummySetupTest` | 2 | 2 |
| `TicTacToeMainTest > isWin` | 2 | 26 |
| `TicTacToeMainTest > play` | 4 | 6 |
| `GreedyPlayerTest` | 3 | 8 |
| **Total** | **11** | **42** |

```
> Task :test

DummySetupTest > AssertJ assertions are available and run PASSED
DummySetupTest > JUnit 5 assertions are available and run PASSED

TicTacToeMain > play > returns null and reports a draw when all nine fields are filled PASSED
TicTacToeMain > play > rejects moves outside the board and onto occupied fields > playing to position -1 is rejected PASSED
TicTacToeMain > play > rejects moves outside the board and onto occupied fields > playing to position 9 is rejected PASSED
TicTacToeMain > play > rejects moves outside the board and onto occupied fields > playing to position 0 is rejected PASSED
TicTacToeMain > play > lets CROSS win on 2-4-6 when two greedy players meet PASSED
TicTacToeMain > play > refuses to run a player against itself PASSED

TicTacToeMain > isWin > detects every winning line for both colours > CROSS wins on the top row PASSED
TicTacToeMain > isWin > detects every winning line for both colours > CROSS wins on the middle row PASSED
TicTacToeMain > isWin > detects every winning line for both colours > CROSS wins on the bottom row PASSED
TicTacToeMain > isWin > detects every winning line for both colours > CROSS wins on the left column PASSED
TicTacToeMain > isWin > detects every winning line for both colours > CROSS wins on the middle column PASSED
TicTacToeMain > isWin > detects every winning line for both colours > CROSS wins on the right column PASSED
TicTacToeMain > isWin > detects every winning line for both colours > CROSS wins on the main diagonal PASSED
TicTacToeMain > isWin > detects every winning line for both colours > CROSS wins on the anti diagonal PASSED
TicTacToeMain > isWin > detects every winning line for both colours > CIRCLE wins on the top row PASSED
TicTacToeMain > isWin > detects every winning line for both colours > CIRCLE wins on the middle row PASSED
TicTacToeMain > isWin > detects every winning line for both colours > CIRCLE wins on the bottom row PASSED
TicTacToeMain > isWin > detects every winning line for both colours > CIRCLE wins on the left column PASSED
TicTacToeMain > isWin > detects every winning line for both colours > CIRCLE wins on the middle column PASSED
TicTacToeMain > isWin > detects every winning line for both colours > CIRCLE wins on the right column PASSED
TicTacToeMain > isWin > detects every winning line for both colours > CIRCLE wins on the main diagonal PASSED
TicTacToeMain > isWin > detects every winning line for both colours > CIRCLE wins on the anti diagonal PASSED

TicTacToeMain > isWin > reports no winner on boards without three in a line > empty board PASSED
TicTacToeMain > isWin > reports no winner on boards without three in a line > full board ending in a draw PASSED
TicTacToeMain > isWin > reports no winner on boards without three in a line > full board without a line PASSED
TicTacToeMain > isWin > reports no winner on boards without three in a line > two in a row, third field still free PASSED
TicTacToeMain > isWin > reports no winner on boards without three in a line > row blocked by the opponent PASSED
TicTacToeMain > isWin > reports no winner on boards without three in a line > column blocked by the opponent PASSED
TicTacToeMain > isWin > reports no winner on boards without three in a line > main diagonal blocked by the opponent PASSED
TicTacToeMain > isWin > reports no winner on boards without three in a line > anti diagonal blocked by the opponent PASSED
TicTacToeMain > isWin > reports no winner on boards without three in a line > three stones of one colour, but not in a line PASSED
TicTacToeMain > isWin > reports no winner on boards without three in a line > both colours two in a row PASSED

GreedyPlayer > throws when the board is full PASSED
GreedyPlayer > plays the same field regardless of the colour it is asked to play PASSED
GreedyPlayer > always takes the first free field, top-left first > on "........." it plays to 0 PASSED
GreedyPlayer > always takes the first free field, top-left first > on "X........" it plays to 1 PASSED
GreedyPlayer > always takes the first free field, top-left first > on "XOXOX...." it plays to 5 PASSED
GreedyPlayer > always takes the first free field, top-left first > on "XOXOXOXO." it plays to 8 PASSED
GreedyPlayer > always takes the first free field, top-left first > on "XXXXXXXX." it plays to 8 PASSED
GreedyPlayer > always takes the first free field, top-left first > on ".XXXXXXXX" it plays to 0 PASSED

BUILD SUCCESSFUL
```

### Screenshot

![Alle Tests erfolgreich](screenshots/tests-erfolgreich.png)

---

## 7. Testergebnis: Ein Test schlägt fehl

Um einen Fehlschlag zu demonstrieren, wird in
[`DummySetupTest.junitDummy()`](../src/test/java/ch/bbw/m450/tictactoe/DummySetupTest.java)
die Assertion temporär invertiert:

```diff
- assertFalse(false, "if this test runs at all, JUnit Jupiter is wired up correctly");
+ assertFalse(true, "absichtlich fehlschlagender Test fuer den Screenshot");
```

Danach `./gradlew test --rerun-tasks --no-build-cache --console=plain` erneut ausführen.
Der Build bricht mit `Task :test FAILED` ab, die übrigen Tests laufen weiterhin grün durch:

```
> Task :test FAILED

DummySetupTest > AssertJ assertions are available and run PASSED

DummySetupTest > JUnit 5 assertions are available and run FAILED
    org.opentest4j.AssertionFailedError at DummySetupTest.java:18

42 tests completed, 1 failed

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':test'.
> There were failing tests. See the report at: file:///.../build/reports/tests/test/index.html
```

Die vollständige Fehlermeldung
(`absichtlich fehlschlagender Test fuer den Screenshot ==> expected: <false> but was: <true>`)
steht im HTML-Report unter `build/reports/tests/test/index.html`.

Nach dem Screenshot wird die Änderung wieder rückgängig gemacht, damit der Build
auf `main` grün bleibt.

### Screenshot

![Ein Test schlägt fehl](screenshots/test-fehlgeschlagen.png)

---

## 8. Continuous Integration

Die Tests laufen zusätzlich bei jedem Push auf `main` automatisch in GitHub Actions:
[`.github/workflows/ci.yml`](../.github/workflows/ci.yml)

Der CI-Job läuft im projekteigenen Container-Image
`ghcr.io/thegloo/450-tictactest-mvk:latest` (siehe [CONTAINER.md](CONTAINER.md)) und
lädt den HTML-Testreport als Artefakt hoch.

Workflow-Läufe: <https://github.com/TheGloo/450-tictactest-mvk/actions>
