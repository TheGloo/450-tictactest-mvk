# TicTacTest (M450)

Ein konsolenbasiertes TicTacToe in Java mit einer vollständigen Testsuite: JUnit 6,
AssertJ, JUnit Pioneer, jqwik, JaCoCo und PIT. Gebaut und geprüft wird in einem eigenen
DevContainer-Image, lokal wie in GitHub Actions.

## Schnellstart

```bash
./gradlew build      # kompilieren, alle Tests, Coverage-Report, Coverage-Gate (Branch >= 90 %)
./gradlew pitest     # Mutation Testing mit PIT
./gradlew test       # nur die Tests
java -jar build/libs/tictactoe.jar   # spielen (Mensch gegen GreedyPlayer)
```

| Report | Pfad |
|---|---|
| Tests | `build/reports/tests/test/index.html` |
| Coverage (JaCoCo) | `build/reports/jacoco/test/html/index.html` |
| Mutation Testing (PIT) | `build/reports/pitest/index.html` |

Alle drei Reports lädt die CI bei jedem Commit als Artefakte hoch (`test-report`,
`coverage-report`, `mutation-report`).

## Anforderungen des Auftrags und ihre Umsetzung

| # | Anforderung | Umsetzung |
|---|---|---|
| 1 | Unit Testing mit AssertJ, fluent, Hilfsstrukturen | Alle Prüfungen über `assertThat(...)`-Ketten; eigene AssertJ-Assertion [`BoardAssert`](src/test/java/ch/bbw/m450/tictactoe/testsupport/BoardAssert.java) (`assertThatBoard("XXX......").isWonBy(CROSS)`), Fixtures [`Boards`](src/test/java/ch/bbw/m450/tictactoe/testsupport/Boards.java), Test-Doubles [`TestPlayers`](src/test/java/ch/bbw/m450/tictactoe/testsupport/TestPlayers.java) |
| 2 | ParameterizedTest, keine Code-Wiederholung | `@MethodSource` (Gewinnlinien, 8 + 8 + 24 Bretter), `@CsvSource` (Spielerzüge), `@ValueSource` (ungültige Züge), `@EnumSource` (beide Farben); 76 ausgeführte Tests aus 29 Testmethoden |
| 3 | JaCoCo Report + Verification mit 90 % Branch-Coverage | `jacocoTestReport` (HTML/XML/CSV) nach jedem Testlauf; `jacocoTestCoverageVerification` mit `BRANCH COVEREDRATIO >= 0.90` hängt an `check`, der Build schlägt darunter fehl. Stand: **100 % Branch**, 96 % Line. Siehe [docs/COVERAGE.md](docs/COVERAGE.md) |
| 4 | StdIn/StdOut-Tests mit JUnit Pioneer | `@StdIo` / `StdOut` in [`HumanPlayerTest`](src/test/java/ch/bbw/m450/tictactoe/players/HumanPlayerTest.java) (Eingabe über `System.in`, Prompt auf `System.out`) und in allen Spieltests, die die Konsolenausgabe des Spiels prüfen |
| 5 | CI mit allen Reports als Artefakte, Build grün | [`ci.yml`](.github/workflows/ci.yml): Build + Tests + Coverage + Mutation Testing bei jedem Push; Artefakte `test-report`, `coverage-report`, `mutation-report`, `tictactoe-jar`. Dazu Coverage Gate für PRs, Coverage-Time-Series, Release- und DevContainer-Pipeline, siehe [docs/PIPELINES.md](docs/PIPELINES.md) |
| 6 | Zusätzlicher, perfekt spielender Player | [`PerfectPlayer`](src/main/java/ch/bbw/m450/tictactoe/players/PerfectPlayer.java): Minimax (Negamax) über den vollständigen Spielbaum mit Memoisierung; bevorzugt den schnellsten Sieg. Nachweis in [`PerfectPlayerTest`](src/test/java/ch/bbw/m450/tictactoe/players/PerfectPlayerTest.java): verliert in **keiner** möglichen Partie (der Gegner probiert in jedem Zug jedes freie Feld), Remis gegen sich selbst, schlägt den `GreedyPlayer` als X und als O |
| 7 | Zusätzliches Testframework mit Beispieltest | **jqwik** (Property-based Testing, eigene JUnit-Platform-Engine): [`PerfectPlayerProperties`](src/test/java/ch/bbw/m450/tictactoe/players/PerfectPlayerProperties.java), je 1000 zufällige Gegner bzw. Bretter pro Property |
| 8 | Mutation Testing mit PIT | `./gradlew pitest`, Schwellenwert 90 %, eigener CI-Job. Stand: **96 %** (131 von 137 Mutanten getötet, die 6 übrigen sind äquivalent). Siehe [docs/MUTATIONTESTING.md](docs/MUTATIONTESTING.md) |
| 9 | Keine SonarLint-Warnings, `var`, aktuelle Versionen | JUnit `6.1.3`, AssertJ `3.27.7`, Pioneer `2.3.0`, jqwik `1.10.2`, JaCoCo `0.8.15`, PIT `1.30.0`, Gradle `9.8.2`, Zulu JDK 25. `var` für alle lokalen Variablen. Einzige Änderung am Originalcode: `String color` → `var color` in `TicTacToeMain.toString` |
| 10 | Abgabe als privates GitHub-Projekt | Repository `TheGloo/450-tictactest-mvk` |

Es werden **keine Mocking-Frameworks** verwendet; Spieler-Doubles sind von Hand
geschriebene Lambdas in `TestPlayers`.

## Projektstruktur

```
src/main/java/ch/bbw/m450/tictactoe/
├── TicTacToeMain.java          Spielregeln und Spielschleife (Originalcode)
├── TicTacToePlayer.java        Spieler-Interface und enum Stone (Originalcode)
└── players/
    ├── GreedyPlayer.java       erstes freies Feld (Originalcode)
    ├── HumanPlayer.java        liest den Zug von der Konsole (Originalcode)
    └── PerfectPlayer.java      Minimax, verliert nie (neu)

src/test/java/ch/bbw/m450/tictactoe/
├── DummySetupTest.java         Setup-Nachweis JUnit + AssertJ
├── StoneTest.java
├── TicTacToeMainTest.java      isWin, toString, play, main (@Nested)
├── players/
│   ├── GreedyPlayerTest.java
│   ├── HumanPlayerTest.java    JUnit Pioneer @StdIo
│   ├── PerfectPlayerTest.java
│   └── PerfectPlayerProperties.java   jqwik
└── testsupport/
    ├── Boards.java             Bretter als 9-Zeichen-Muster, Fixtures für @MethodSource
    ├── BoardAssert.java        eigene AssertJ-Assertion
    └── TestPlayers.java        Spieler-Doubles
```

## Dokumentation

| Dokument | Inhalt |
|---|---|
| [docs/TESTKONZEPT.md](docs/TESTKONZEPT.md) | Testziele, Strategie, Testfälle, Werkzeuge, Risiken |
| [docs/TESTDOKUMENTATION.md](docs/TESTDOKUMENTATION.md) | jeder Testfall als GIVEN / WHEN / THEN, Testprotokolle |
| [docs/COVERAGE.md](docs/COVERAGE.md) | JaCoCo-Report, Coverage Verification, Time-Series, Coverage Gate |
| [docs/MUTATIONTESTING.md](docs/MUTATIONTESTING.md) | PIT: Einrichtung, gefundene Lücken, äquivalente Mutanten |
| [docs/PIPELINES.md](docs/PIPELINES.md) | alle GitHub-Actions-Workflows |
| [docs/DEVCONTAINER.md](docs/DEVCONTAINER.md) | DevContainer-Image, Versionierung, Continuous Deployment |

## Hinweise

- **Line-Coverage von `main()`:** Der Test `TicTacToeMainTest.Main` führt `main` aus
  (geprüft werden der Prompt des Konsolenspielers und die Exception nach dem ungültigen
  Zug). JaCoCo zeigt die zwei Zeilen trotzdem als nicht abgedeckt, weil seine Sonde am
  Blockende durch die Exception nie erreicht wird. Vollständig abdecken liesse sich
  `main` nur mit einem Umbau von `HumanPlayer` (ein `Scanner` pro Zug verschluckt
  gepufferte Eingaben), der nach Auftrag unterbleibt. Die Branch-Coverage ist davon
  nicht betroffen.
- **Entwicklungsumgebung:** Repository in VS Code öffnen und *Reopen in Container*
  wählen, siehe [docs/DEVCONTAINER.md](docs/DEVCONTAINER.md).
