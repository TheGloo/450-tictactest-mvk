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

Alle Tests benutzen die Hilfsmethode `board(String pattern)`, die ein Spielfeld aus
einem 9-Zeichen-Muster baut (`X` = Kreuz, `O` = Kreis, alles andere = leeres Feld).
Das Brett ist ein eindimensionales Array der Länge 9:

```
 0 | 1 | 2
---+---+---
 3 | 4 | 5
---+---+---
 6 | 7 | 8
```

### T1 – `isWinDetectsEveryWinningLine(String pattern)`

*"isWin detects all three rows, columns and diagonals"* — [Zeile 31–40](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/TicTacToeMainTest.java#L31-L40)

Parametrisierter Test mit 8 Durchläufen (3 Reihen, 3 Spalten, 2 Diagonalen).

| | |
|---|---|
| **GIVEN** | Ein Spielfeld, auf dem `CROSS` genau eine der 8 möglichen Gewinnlinien besetzt (z.B. `"XXX......"` für die obere Reihe oder `"X...X...X"` für die Hauptdiagonale). |
| **WHEN** | `TicTacToeMain.isWin(board, Stone.CROSS)` bzw. `isWin(board, Stone.CIRCLE)` aufgerufen wird. |
| **THEN** | Für `CROSS` liefert die Methode `true` (die Gewinnlinie wird erkannt), für `CIRCLE` liefert sie `false` (es wird kein falscher Gewinner gemeldet). |

**Warum dieser Test?** `isWin` besteht aus 8 hart kodierten Bedingungen. Ein Tippfehler in
einem einzigen Index würde eine Gewinnlinie unerkannt lassen. Der Test deckt jede Linie
einzeln ab.

### T2 – `isWinIsFalseWithoutThreeInALine()`

*"isWin is false for an empty board and for a full board without a line"* — [Zeile 42–53](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/TicTacToeMainTest.java#L42-L53)

| | |
|---|---|
| **GIVEN** | Zwei Spielfelder ohne Gewinnlinie: (a) ein komplett leeres Brett `"........."`, (b) ein volles Unentschieden-Brett `"XOXXOOOXX"`. |
| **WHEN** | `isWin` für beide Farben (`CROSS` und `CIRCLE`) aufgerufen wird. |
| **THEN** | Alle Aufrufe liefern `false` — es wird kein Gewinner gemeldet, wo keiner ist. |

**Warum dieser Test?** Das leere Brett ist der wichtigste Grenzfall: Alle Felder sind `null`.
Ohne die vorgelagerte `b[x] == color`-Prüfung würde `null == null == null` fälschlicherweise
als Gewinn gelten. Der Test sichert diesen Negativfall ab.

### T3 – `twoGreedyPlayersLetCrossWin()`

*"two greedy players fill the board left to right, so CROSS wins on 2-4-6"* — [Zeile 55–61](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/TicTacToeMainTest.java#L55-L61)

| | |
|---|---|
| **GIVEN** | Zwei unabhängige `GreedyPlayer`-Instanzen. Der `GreedyPlayer` setzt immer auf das erste freie Feld von oben links. |
| **WHEN** | Eine komplette Partie mit `TicTacToeMain.play(greedy1, greedy2)` gespielt wird. Die Züge sind deterministisch: X→0, O→1, X→2, O→3, X→4, O→5, X→6. |
| **THEN** | Die Methode gibt `Stone.CROSS` zurück, denn `CROSS` besetzt nach dem 7. Zug die Nebendiagonale 2-4-6 und gewinnt. |

**Warum dieser Test?** Dies ist der Happy-Path-Integrationstest: Er prüft die komplette
Spielschleife — Zugreihenfolge, Farbwechsel, Gewinnerkennung und Rückgabewert — in einem
vollständig deterministischen Szenario.

### T4 – `playRejectsTheSamePlayerTwice()`

*"play refuses to run a player against itself"* — [Zeile 63–69](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/TicTacToeMainTest.java#L63-L69)

| | |
|---|---|
| **GIVEN** | Eine einzige `GreedyPlayer`-Instanz, die als beide Spieler übergeben werden soll. |
| **WHEN** | `TicTacToeMain.play(player, player)` mit derselben Referenz für X und O aufgerufen wird. |
| **THEN** | Es wird eine `IllegalArgumentException` mit der Nachricht `"players must differ"` geworfen; das Spiel startet gar nicht erst. |

**Warum dieser Test?** Die Spielschleife unterscheidet die beiden Spieler über einen
Referenzvergleich (`currentPlayer == xPlayer`). Wäre es dieselbe Instanz, würde die
Farbzuordnung brechen. Der Test sichert diese Vorbedingung ab.

### T5 – `playRejectsInvalidMoves(int position)`

*"play rejects moves outside the board and onto occupied fields"* — [Zeile 71–79](https://github.com/TheGloo/450-tictactest-mvk/blob/main/src/test/java/ch/bbw/m450/tictactoe/TicTacToeMainTest.java#L71-L79)

Parametrisierter Test mit 3 Durchläufen: `-1`, `9` und `0`.

| | |
|---|---|
| **GIVEN** | Ein regulärer `GreedyPlayer` als X und ein "schummelnder" Spieler als O (Lambda), der immer auf die ungültige Position `position` setzt. X eröffnet auf Feld `0`. |
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

TicTacToeMainTest > play rejects moves outside the board and onto occupied fields > playing to position -1 is rejected PASSED
TicTacToeMainTest > play rejects moves outside the board and onto occupied fields > playing to position 9 is rejected PASSED
TicTacToeMainTest > play rejects moves outside the board and onto occupied fields > playing to position 0 is rejected PASSED
TicTacToeMainTest > play refuses to run a player against itself PASSED
TicTacToeMainTest > isWin is false for an empty board and for a full board without a line PASSED
TicTacToeMainTest > isWin detects all three rows, columns and diagonals > board "XXX......" is a win for CROSS PASSED
TicTacToeMainTest > isWin detects all three rows, columns and diagonals > board "...XXX..." is a win for CROSS PASSED
TicTacToeMainTest > isWin detects all three rows, columns and diagonals > board "......XXX" is a win for CROSS PASSED
TicTacToeMainTest > isWin detects all three rows, columns and diagonals > board "X..X..X.." is a win for CROSS PASSED
TicTacToeMainTest > isWin detects all three rows, columns and diagonals > board ".X..X..X." is a win for CROSS PASSED
TicTacToeMainTest > isWin detects all three rows, columns and diagonals > board "..X..X..X" is a win for CROSS PASSED
TicTacToeMainTest > isWin detects all three rows, columns and diagonals > board "X...X...X" is a win for CROSS PASSED
TicTacToeMainTest > isWin detects all three rows, columns and diagonals > board "..X.X.X.." is a win for CROSS PASSED
TicTacToeMainTest > two greedy players fill the board left to right, so CROSS wins on 2-4-6 PASSED

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

DummySetupTest > AssertJ assertions are available and run PASSED

DummySetupTest > JUnit 5 assertions are available and run FAILED
    org.opentest4j.AssertionFailedError at DummySetupTest.java:18

TicTacToeMainTest > play rejects moves outside the board and onto occupied fields > playing to position -1 is rejected PASSED
...
TicTacToeMainTest > two greedy players fill the board left to right, so CROSS wins on 2-4-6 PASSED

16 tests completed, 1 failed

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':test'.
> There were failing tests.
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
