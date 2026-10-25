# Testkonzept – TicTacTest (M450)

> **Geltung:** Dieses Dokument beschreibt den **IST-Zustand** der Testaktivitäten im
> Projekt `450-tictactest-mvk` zum Stand **25.10.2026**. Es dokumentiert
> ausschliesslich, was heute im Repository tatsächlich vorhanden ist – keine Planungen
> und keine Absichtserklärungen.

| | |
|---|---|
| **Projekt** | `450-tictactest-mvk` (TicTacToe) |
| **Repository** | <https://github.com/TheGloo/450-tictactest-mvk> |
| **Testart** | automatisierte Entwicklertests (Unit-, Komponenten- und Property-based Tests), Mutation Testing |
| **Test-Frameworks** | JUnit 6 (Jupiter) `6.1.3`, JUnit Pioneer `2.3.0`, jqwik `1.10.2` |
| **Assertion-Library** | AssertJ `3.27.7` |
| **Coverage / Mutation** | JaCoCo `0.8.15`, PIT `1.30.0` |
| **Build-Tool** | Gradle Wrapper `9.8.2` (`./gradlew build`) |
| **JDK** | Zulu 25 (gepinnt in `gradle/gradle-daemon-jvm.properties`) |
| **Umfang** | 7 Testklassen, 29 Testmethoden, **76 ausgeführte Tests**, alle grün; Branch-Coverage **100 %**, Mutation Score **96 %** |

---

## 1. Einleitung und Geltungsbereich

Der Testgegenstand ist ein konsolenbasiertes TicTacToe-Spiel. Die Spielregeln liegen
vollständig in der Klasse `TicTacToeMain`; die Spieler sind über das Interface
`TicTacToePlayer` austauschbar implementiert. Neben den beiden mitgelieferten Spielern
(`GreedyPlayer`, `HumanPlayer`) gibt es den im Rahmen des Auftrags ergänzten
`PerfectPlayer`.

Dieses Testkonzept umfasst die im Repository vorhandenen automatisierten Tests unter
`src/test/java` sowie die Qualitätsmetriken, die der Build erzwingt (Branch-Coverage,
Mutation Score). Manuelle Tests, Abnahmetests und nichtfunktionale Tests (Performance,
Last, Sicherheit) sind im Projekt nicht vorhanden und daher nicht Gegenstand dieses
Konzepts.

**Mitgeltende Dokumente**

| Dokument | Inhalt |
|---|---|
| [TESTDOKUMENTATION.md](TESTDOKUMENTATION.md) | GIVEN/WHEN/THEN-Beschreibung jedes einzelnen Testfalls, Testprotokolle, Screenshots |
| [COVERAGE.md](COVERAGE.md) | Coverage-Messung mit JaCoCo, Coverage Verification, Time-Series und Coverage Gate |
| [MUTATIONTESTING.md](MUTATIONTESTING.md) | Mutation Testing mit PIT, Analyse der überlebenden Mutanten |
| [PIPELINES.md](PIPELINES.md) | alle GitHub-Actions-Workflows |
| [DEVCONTAINER.md](DEVCONTAINER.md) | DevContainer-Image, in dem die Tests lokal und in der CI ausgeführt werden |

---

## 2. Testgegenstand

Getestet wird der Produktivcode unter `src/main/java/ch/bbw/m450/tictactoe`.

| Klasse / Methode | Rolle | Im Test abgedeckt |
|---|---|---|
| `TicTacToeMain.isWin(Stone[], Stone)` | Gewinnprüfung, 8 hart kodierte Linienbedingungen | **ja** (T1, T2, T6, T7) |
| `TicTacToeMain.play(TicTacToePlayer, TicTacToePlayer)` | Spielschleife: Vorbedingung, Zugvalidierung, Farbwechsel, Gewinnerkennung, Remis, Rückgabewert | **ja** (T3, T4, T5, T8, T9) |
| `TicTacToeMain.toString(Stone[])` | Brettausgabe mit ANSI-Farben | **ja** (T10, exaktes Format) |
| `TicTacToeMain.main(String[])` | verdrahtet `HumanPlayer` (X) gegen `GreedyPlayer` (O) | **ja** (T17), siehe Kapitel 4.3 zur Line-Coverage |
| `TicTacToePlayer.Stone.opponent()` | Gegenfarbe | **ja** (T13) |
| `GreedyPlayer.play(...)` | setzt auf das erste freie Feld | **ja** (T11, T12) |
| `HumanPlayer.play(...)` | liest den Zug von `System.in`, Prompt auf `System.out` | **ja** (T14–T16, mit JUnit Pioneer) |
| `PerfectPlayer.play(...)` | Minimax über den ganzen Spielbaum, verliert nie | **ja** (T18–T25, P1, P2) |

Das Spielfeld ist ein eindimensionales Array der Länge 9:

```
 0 | 1 | 2
---+---+---
 3 | 4 | 5
---+---+---
 6 | 7 | 8
```

**Testbasis** (Grundlage der Testfallermittlung): die JavaDoc-Verträge der Methoden,
die TicTacToe-Spielregeln, der Quellcode selbst und die überlebenden Mutanten des
ersten PIT-Laufs (siehe [MUTATIONTESTING.md](MUTATIONTESTING.md), Kapitel 2).

---

## 3. Testziele

Die folgenden Ziele werden von den heute vorhandenen Tests verfolgt. Die Zuordnung
zu den konkreten Testfällen steht in Kapitel 6.

| ID | Testziel | Begründung |
|---|---|---|
| **Z1** | **Vollständige Gewinnerkennung** – jede der 8 Gewinnlinien wird für beide Farben erkannt. | `isWin` besteht aus 8 unabhängigen Bedingungen mit je 3 Array-Indizes; ein einzelner Zahlendreher macht eine Linie blind. |
| **Z2** | **Keine falsch positiven Gewinner** – leeres Brett, volles Brett ohne Linie und jede Linie mit nur zwei eigenen Steinen ergeben keinen Gewinn. | Ohne die `b[x] == color`-Vorprüfung ginge `null == null == null` als Gewinn durch; ohne die dritte Gleichheit reichten zwei Steine. PIT hat genau diese Lücke gezeigt. |
| **Z3** | **Korrekter Spielablauf** – Farbwechsel, Gewinn im richtigen Zug für beide Farben, Remis nach neun Zügen, Rückgabewert und Konsolenmeldung. | `play` ist die zentrale Ablauflogik und verbindet alle Einzelteile. |
| **Z4** | **Absicherung der Vorbedingung** – dieselbe Spielerinstanz als X und O wird abgelehnt. | Die Schleife unterscheidet die Spieler per Referenzvergleich. |
| **Z5** | **Zugvalidierung** – Züge ausserhalb des Bretts und auf belegte Felder werden abgelehnt, das Brett wird vorher angezeigt. | Schützt vor `IndexOutOfBoundsException` und vor dem Überschreiben gesetzter Steine. |
| **Z6** | **Nachweis eines lauffähigen Test-Setups** – JUnit findet und startet Tests, AssertJ liegt auf dem Test-Classpath. | Ohne funktionierendes Setup sind alle übrigen Ergebnisse wertlos. |
| **Z7** | **Automatisierte Regressionsprüfung** – Tests, Coverage-Gate und Mutation Testing laufen bei jedem Push unbeaufsichtigt durch. | Sichert alle übrigen Ziele dauerhaft gegen Regressionen ab. |
| **Z8** | **Exakte Brettausgabe** – `toString` rendert Steine fett, leere Felder als graue Indizes, drei pro Zeile. | Der Mensch sieht nur diese Ausgabe; PIT zeigte, dass sie vorher ungeprüft war (`return ""` blieb unbemerkt). |
| **Z9** | **Korrekte mitgelieferte Spieler** – `GreedyPlayer` nimmt das erste freie Feld und scheitert kontrolliert auf vollem Brett; `Stone.opponent()` liefert die Gegenfarbe. | Beide werden von Spielschleife und `PerfectPlayer` vorausgesetzt. |
| **Z10** | **Konsolenspieler über stdin/stdout** – `HumanPlayer` liest die getippte Position, zeigt Brett und Prompt und scheitert bei Nicht-Zahlen; `main` verdrahtet ihn als X. | Einziger Berührungspunkt mit dem Benutzer; testbar erst durch JUnit Pioneer. |
| **Z11** | **Perfekter Spieler** – `PerfectPlayer` verliert in keiner möglichen Partie, nutzt Gewinnchancen sofort, blockt Drohungen und verändert das übergebene Brett nicht. | Kernanforderung des Auftrags; ein Minimax mit einem Vorzeichenfehler verliert sofort, was nur ein erschöpfender Test zuverlässig zeigt. |

---

## 4. Teststrategie

### 4.1 Teststufen und Testarten

| Stufe | Was geprüft wird | Vorhandene Umsetzung |
|---|---|---|
| **Unit-Test** | eine Methode isoliert | `isWin`, `toString`, `Stone.opponent`, einzelne Züge von `GreedyPlayer`, `HumanPlayer`, `PerfectPlayer` |
| **Komponenten-/Integrationstest** | `play` im Zusammenspiel mit echten bzw. simulierten Spielern | vollständige Partien, Remis, Fehlerfälle, `main` |
| **Erschöpfender Test** | alle Partien, die ein Spieler erreichen kann | T22: der Gegner probiert in jedem Zug jedes freie Feld |
| **Property-based Test** (jqwik) | Eigenschaften, die für zufällige Eingaben gelten müssen | P1, P2 mit je 1000 generierten Gegnern bzw. Brettern |
| **Smoke-/Setup-Test** | die Testinfrastruktur selbst | `DummySetupTest` (D1, D2) |
| **Mutation Testing** (PIT) | ob die Tests Fehler im Code bemerken würden | `./gradlew pitest`, Schwellenwert 90 % |
| **CI-Lauf** | die gesamte Suite in einer sauberen Umgebung | GitHub Actions bei jedem Push/PR |

Die Tests sind **White-Box-orientiert**: Die Testfälle wurden aus der Struktur des
Quellcodes abgeleitet und mit Mutation Testing gegen den Code verifiziert.

### 4.2 Angewandte Testentwurfsverfahren

| Verfahren | Konkrete Anwendung im Projekt |
|---|---|
| **Zweigüberdeckung** | T1/T6 decken jede der 8 Oder-Bedingungen von `isWin` für beide Farben ab; T5 alle drei Zweige der Zugvalidierung. Ergebnis: 100 % Branch-Coverage, vom Build erzwungen (≥ 90 %). |
| **Äquivalenzklassenbildung** | Zug-Position: `< 0`, `0..8`, `>= 9`; Feldzustand frei / belegt; Eingabe des `HumanPlayer`: Zahl / keine Zahl. |
| **Grenzwertanalyse** | T5 prüft `-1` und `9` direkt ausserhalb der Grenzen `0` und `8`; T11/T12 den ersten und letzten freien Platz sowie das volle Brett. |
| **Negativtest / Fehlerfall** | T2, T7 (kein Gewinner), T4 (Vorbedingung), T5 (regelwidriger Zug), T12/T21 (volles Brett), T16 (Nicht-Zahl). |
| **Zustandsbasiertes Testen** | T3, T8, T9, T23–T25 durchlaufen ganze Partien mit Farbwechsel, vorzeitigem Ende und Remis. |
| **Erschöpfendes Testen** | T22 spielt gegen jeden möglichen Gegnerzug; auf dem 3×3-Brett ist das in Millisekunden möglich. |
| **Property-based Testing** | P1: „verliert gegen keinen zufälligen Gegner“, P2: „spielt immer auf ein freies Feld und verändert das Brett nicht“ – jqwik generiert die Eingaben und verkleinert Gegenbeispiele automatisch. |
| **Parametrisiertes Testen** | `@MethodSource` (8 + 8 + 24 Bretter), `@CsvSource` (Spielerzüge), `@ValueSource`, `@EnumSource` – dieselbe Logik gegen viele Eingaben ohne Codeverdopplung. |
| **Mutation Testing** | PIT hat 23 Lücken in der ersten Suite aufgedeckt (u. a. ungeprüftes `toString`, fehlendes Remis, fehlender CIRCLE-Sieg); für jede wurde ein Test ergänzt. |

### 4.3 Bewusste Abgrenzung

Was heute **nicht** durch automatisierte Tests abgedeckt ist, und warum:

| Bereich | Begründung |
|---|---|
| Vollständige Partie über `main` | `HumanPlayer` erzeugt pro Zug einen neuen `Scanner`; der erste verschluckt bei gepufferter Eingabe (Pipe, Pioneer) alle weiteren Zeilen. T17 beendet die Partie deshalb nach dem ersten (ungültigen) Zug. **JaCoCo zeigt die zwei Zeilen von `main` als nicht abgedeckt**, obwohl der Test sie ausführt: Die Sonde am Blockende wird wegen der Exception nie erreicht. Ein Umbau des Originalcodes unterbleibt gemäss Auftrag. |
| Impliziter Konstruktor von `TicTacToeMain` | reine Utility-Klasse, wird nie instanziiert (1 Zeile Line-Coverage). |
| Nichtfunktionale Tests | bei einem 3×3-Brett ohne Persistenz und Netzwerk gegenstandslos. |

---

## 5. Teststruktur

### 5.1 Verzeichnisstruktur

```
src/test/java/ch/bbw/m450/tictactoe/
├── DummySetupTest.java              Setup-Nachweis (Z6)
├── StoneTest.java                   Z9
├── TicTacToeMainTest.java           isWin, toString, play, main (Z1–Z5, Z8, Z10)
├── players/
│   ├── GreedyPlayerTest.java        Z9
│   ├── HumanPlayerTest.java         Z10, JUnit Pioneer @StdIo
│   ├── PerfectPlayerTest.java       Z11
│   └── PerfectPlayerProperties.java Z11, jqwik
└── testsupport/                     Testinfrastruktur, enthält keine Tests
    ├── Boards.java
    ├── TestPlayers.java
    └── BoardAssert.java
```

Die Testklassen spiegeln das Paket der getesteten Klassen. Wiederverwendbare
Bausteine liegen im eigenen Paket `testsupport`, damit die Testmethoden selbst nur
noch das jeweilige Szenario beschreiben.

### 5.2 Aufbau der Testklassen

Die Testklassen sind mit `@Nested` nach der getesteten Methode bzw. nach Einzelzug und
ganzer Partie gegliedert, sodass die Testausgabe die Struktur der Klasse widerspiegelt:

```
TicTacToeMain
├── isWin
│   ├── detects all three rows, columns and diagonals              (8 Durchläufe)
│   ├── detects all three rows, columns and diagonals of CIRCLE    (8 Durchläufe)
│   ├── is false when one field of the line holds the other colour (24 Durchläufe)
│   └── is false for an empty board and for a full board without a line
├── toString
│   └── renders stones in bold and empty fields as their grey index, three per line
├── play
│   ├── lets CROSS win on 2-4-6 when two greedy players meet
│   ├── lets CIRCLE win and announces it
│   ├── ends in a draw after nine moves without a line
│   ├── refuses to run a player against itself
│   └── rejects moves outside the board and onto occupied fields   (3 Durchläufe)
└── main
    └── lets a human play CROSS against the greedy player

PerfectPlayer
├── single moves
│   ├── completes its own line as soon as it can                   (4 Durchläufe)
│   ├── blocks the opponent's two in a line when it cannot win itself (3 Durchläufe)
│   ├── wins at once instead of blocking first, ...
│   └── refuses to play on a full board
└── whole games
    ├── never loses, whatever the opponent does                    (als CROSS, als CIRCLE)
    ├── ends in a draw against itself
    ├── beats the greedy player as CROSS
    └── beats the greedy player as CIRCLE
```

Jede Testmethode prüft genau ein Szenario und ist innerlich nach
**GIVEN / WHEN / THEN** aufgebaut. Sprechende `@DisplayName`-Annotationen beschreiben
das erwartete Verhalten in ganzen Sätzen.

### 5.3 Testinfrastruktur (`testsupport`) und Frameworks

| Baustein | Art | Aufgabe |
|---|---|---|
| `Boards` | Fixture + Builder | Bretter als 9-Zeichen-Muster (`board("XXX......")`); Konstanten `EMPTY` und `DRAW`; `winningLinesForCross()`, `winningLinesForCircle()`, `nearlyWinningLinesForCross()` als `@MethodSource`; `render()` für lesbare Fehlermeldungen |
| `TestPlayers` | Test-Doubles (Lambdas, kein Mocking-Framework) | `greedy()` liefert einen frischen `GreedyPlayer`; `alwaysPlayingTo(int)` spielt stur dieselbe (auch ungültige) Position; `playingInOrder(int...)` spielt eine Partie nach Drehbuch |
| `BoardAssert` | eigene AssertJ-Assertion | `assertThatBoard(...).isWonBy(CROSS)` / `.hasNoWinner()` / `.isNotWinFor(...)`; gibt bei einem Fehlschlag das betroffene Brett mit aus |
| JUnit Pioneer `@StdIo` | Extension | ersetzt `System.in` durch die angegebenen Zeilen und reicht `System.out` als `StdOut` in den Test; stellt beide Streams danach wieder her |
| jqwik `@Property` / `@Provide` | eigene Test-Engine | generiert Eingaben (`Arbitraries.shuffle`, `injectNull`, `filter`) und führt jede Property 1000-mal aus |

### 5.4 Konventionen

- Testklasse `<KlasseUnterTest>Test` bzw. `<KlasseUnterTest>Properties` für jqwik;
  Methodenname beschreibt das erwartete Verhalten.
- Eine fachliche Aussage pro Testmethode; keine Abhängigkeiten zwischen Tests.
- Assertions über AssertJ (`assertThat…`), fachliche Prüfungen über `BoardAssert`.
- Lokale Variablen mit `var`.
- Kein Zugriff auf Dateisystem, Netzwerk oder Datenbank; `System.in`/`System.out`
  werden ausschliesslich über Pioneer (`@StdIo`) bzw. in den jqwik-Properties über
  `@BeforeProperty`/`@AfterProperty` verändert und immer wiederhergestellt.

---

## 6. Testfälle und Zuordnung zu den Testzielen

Ausführliche GIVEN/WHEN/THEN-Beschreibungen: [TESTDOKUMENTATION.md](TESTDOKUMENTATION.md).

| ID | Testfall | Testklasse / Methode | Verfahren | Erwartetes Ergebnis | Ziel |
|---|---|---|---|---|---|
| **T1** | jede der 8 Gewinnlinien für `CROSS` | `TicTacToeMainTest.IsWin.detectsEveryWinningLine` (8) | Zweigüberdeckung, parametrisiert | `isWin(…, CROSS) == true`, `isWin(…, CIRCLE) == false` | Z1 |
| **T6** | jede der 8 Gewinnlinien für `CIRCLE` | `IsWin.detectsEveryWinningLineOfCircle` (8) | Zweigüberdeckung, parametrisiert | spiegelbildlich zu T1 | Z1 |
| **T2** | leeres Brett und volles Brett ohne Linie | `IsWin.isFalseWithoutThreeInALine` | Negativtest, Grenzfall | für beide Farben `false` | Z2 |
| **T7** | jede Linie mit einem Feld vom Gegner besetzt | `IsWin.isFalseForTwoInALine` (24) | Negativtest, parametrisiert | kein Gewinner | Z2 |
| **T3** | vollständige Partie zweier `GreedyPlayer` | `Play.twoGreedyPlayersLetCrossWin` | zustandsbasiert, Happy Path | `CROSS`, Ausgabe `"...and the winner is: CROSS"` | Z3 |
| **T8** | gescriptete Partie, CIRCLE gewinnt | `Play.circleCanWin` | zustandsbasiert | `CIRCLE`, entsprechende Meldung | Z3 |
| **T9** | gescriptete Partie bis zum Remis | `Play.fullBoardWithoutLineIsADraw` | zustandsbasiert, Grenzfall (9 Züge) | `null`, Ausgabe `"it's a draw!"` | Z3 |
| **T4** | dieselbe Spielerinstanz als X und O | `Play.rejectsTheSamePlayerTwice` | Vorbedingung / Negativtest | `IllegalArgumentException("players must differ")` | Z4 |
| **T5** | Zug auf `-1`, `9` und `0` (belegt) | `Play.rejectsInvalidMoves` (3) | Äquivalenzklassen + Grenzwerte | `IllegalStateException("cannot play to position <p>")`, Brett wird vorher ausgegeben | Z5 |
| **T10** | Brett `X.O.X...O` rendern | `ToString.rendersTheBoard` | exakter Vergleich | drei Zeilen, Steine fett, Indizes grau | Z8 |
| **T11** | erstes freies Feld auf drei Brettern | `GreedyPlayerTest.takesTheFirstFreeField` (3) | Grenzwerte, parametrisiert | 0, 2, 8 | Z9 |
| **T12** | volles Brett | `GreedyPlayerTest.failsOnAFullBoard` | Negativtest | `IllegalStateException("cannot play at all")` | Z9 |
| **T13** | Gegenfarbe beider Steine | `StoneTest.opponentIsTheOtherColour` | Unit | `CROSS ↔ CIRCLE` | Z9 |
| **T14** | Eingabe `4` auf der Konsole | `HumanPlayerTest.playsToTheTypedPosition` | Pioneer `@StdIo("4")` | Rückgabe `4` | Z10 |
| **T15** | Brett und Prompt vor dem Lesen | `HumanPlayerTest.showsTheBoardAndAsksForTheColour` | Pioneer `StdOut` | Ausgabe beginnt mit dem Brett, enthält `"next CIRCLE? (0-8):"` | Z10 |
| **T16** | Eingabe `middle` | `HumanPlayerTest.rejectsNonNumericInput` | Negativtest, Pioneer | `NumberFormatException` | Z10 |
| **T17** | `main` mit Eingabe `9` | `TicTacToeMainTest.Main.humanPlaysCrossAgainstTheGreedyPlayer` | Integration, Pioneer | Prompt für `CROSS`, dann `IllegalStateException("cannot play to position 9")` | Z10 |
| **T18** | eigene Linie vollenden (4 Bretter, beide Farben) | `PerfectPlayerTest.SingleMoves.takesTheImmediateWin` (4) | parametrisiert | der Gewinnzug | Z11 |
| **T19** | gegnerische Drohung blocken (3 Bretter) | `SingleMoves.blocksTheOpponentsThreat` (3) | parametrisiert | der Blockzug | Z11 |
| **T20** | Sofortsieg vs. langsamer Zwangssieg | `SingleMoves.prefersTheFasterWin` | gezielter Einzelfall | Feld 7 (sofort) statt 3 | Z11 |
| **T21** | volles Brett | `SingleMoves.failsOnAFullBoard` | Negativtest | `IllegalStateException("cannot play at all")` | Z11 |
| **T22** | alle Partien gegen jeden Gegnerzug, als X und als O | `WholeGames.neverLosesAgainstAnyOpponent` (2) | erschöpfend, `@EnumSource` | der Gegner hat auf keinem erreichten Brett drei in einer Linie | Z11 |
| **T23** | perfekt gegen perfekt | `WholeGames.drawsAgainstItself` | zustandsbasiert | Remis | Z11 |
| **T24** | perfekt (X) gegen greedy | `WholeGames.beatsTheGreedyPlayerAsCross` | zustandsbasiert | `CROSS` gewinnt | Z11 |
| **T25** | greedy gegen perfekt (O) | `WholeGames.beatsTheGreedyPlayerAsCircle` | zustandsbasiert | `CIRCLE` gewinnt (2-4-6) | Z11 |
| **P1** | 1000 zufällige Gegner (Zugpräferenz = Permutation der Felder), zufällige eigene Farbe | `PerfectPlayerProperties.neverLosesAgainstAnyOpponent` | property-based (jqwik) | Gewinner ist nie der Gegner | Z11 |
| **P2** | 1000 zufällige offene Bretter | `PerfectPlayerProperties.playsToAFreeFieldWithoutChangingTheBoard` | property-based (jqwik) | Zug auf ein freies Feld, Brett danach unverändert | Z11 |
| **D1** | JUnit-Assertion wird gefunden und ausgeführt | `DummySetupTest.junitDummy` | Smoke-Test | grün | Z6 |
| **D2** | AssertJ liegt auf dem Test-Classpath | `DummySetupTest.assertJDummy` | Smoke-Test | grün | Z6 |

### Rückwärtsverfolgung: jedes Ziel ist belegt

| Ziel | belegt durch | Anzahl Testausführungen |
|---|---|---|
| Z1 | T1, T6 | 16 |
| Z2 | T2, T7 | 25 |
| Z3 | T3, T8, T9 | 3 |
| Z4 | T4 | 1 |
| Z5 | T5 | 3 |
| Z6 | D1, D2 | 2 |
| Z8 | T10 | 1 |
| Z9 | T11, T12, T13 | 5 |
| Z10 | T14, T15, T16, T17 | 4 |
| Z11 | T18–T25, P1, P2 | 16 (+ 2 × 1000 jqwik-Durchläufe) |
| Z7 | alle Testfälle, Coverage-Verification und PIT, ausgeführt durch `.github/workflows/ci.yml` | 76 pro CI-Lauf |
| | **Summe** | **76** |

Jedes Testziel Z1–Z11 ist durch mindestens einen Testfall abgedeckt, und jeder
Testfall zahlt auf genau ein Ziel ein.

---

## 7. Testumgebung und Werkzeuge

| Bereich | IST-Zustand |
|---|---|
| Sprache / Laufzeit | Java, JDK 25 (Azul Zulu), über die Gradle-Toolchain gepinnt |
| Testframework | JUnit 6 Jupiter `6.1.3` (via `junit-bom`), aktiviert mit `useJUnitPlatform()` |
| Weitere Frameworks | JUnit Pioneer `2.3.0` (`@StdIo` für stdin/stdout), jqwik `1.10.2` (Property-based Testing, eigene Platform-Engine) |
| Assertions | AssertJ `3.27.7` |
| Build | Gradle Wrapper `9.8.2`; Testlauf mit `./gradlew test`, Gesamtbuild inkl. Coverage-Gate mit `./gradlew build`, Mutation Testing mit `./gradlew pitest` |
| Testberichte | `build/reports/tests/test/index.html`; Konsolenausgabe über `testLogging { events 'passed', 'skipped', 'failed' }` |
| Isolation | keine externen Systeme, keine Datenbank, kein Netzwerk; `System.in`/`System.out` werden über Pioneer ersetzt und wiederhergestellt; keine Mocking-Frameworks |
| Coverage | JaCoCo `0.8.15`; Report unter `build/reports/jacoco/test/` (HTML, XML, CSV); `jacocoTestCoverageVerification` erzwingt **≥ 90 % Branch-Coverage** (Ist: 100 %), siehe [COVERAGE.md](COVERAGE.md) |
| Mutation Testing | PIT `1.30.0` mit `STRONGER`-Mutatoren; Schwellenwert 90 % (Ist: 96 %), siehe [MUTATIONTESTING.md](MUTATIONTESTING.md) |
| CI | GitHub Actions (`.github/workflows/ci.yml`) bei jedem Push und bei PRs auf `main`; Jobs laufen im DevContainer-Image `ghcr.io/thegloo/450-tictactest-mvk` und laden Test-, Coverage- und Mutation-Report als Artefakte hoch. Das Coverage Gate (`coverage-gate.yml`) verhindert, dass ein PR die Coverage von `main` senkt |
| Reproduzierbarkeit | feste Abhängigkeitsversionen, gepinnte JDK-Version, Gradle Wrapper im Repository, identisches Container-Image lokal und in der CI |

---

## 8. Testdurchführung und Ergebnis

Ausgeführter Befehl:

```bash
./gradlew clean build pitest --console=plain
```

**Ergebnis vom 25.10.2026: 76 ausgeführte Tests, 0 Fehler, Branch-Coverage 100 %
(104/104), Line-Coverage 96 % (80/83), 131 von 137 Mutanten getötet (96 %),
`BUILD SUCCESSFUL`.**

| Testklasse | Testmethoden | Ausgeführte Tests |
|---|---|---|
| `DummySetupTest` | 2 | 2 |
| `StoneTest` | 1 | 1 |
| `TicTacToeMainTest` | 11 (davon 4 parametrisiert) | 50 |
| `GreedyPlayerTest` | 2 (davon 1 parametrisiert) | 4 |
| `HumanPlayerTest` | 3 | 3 |
| `PerfectPlayerTest` | 8 (davon 3 parametrisiert) | 14 |
| `PerfectPlayerProperties` | 2 Properties à 1000 Durchläufe | 2 |
| **Total** | **29** | **76** |

Die Testprotokolle und Screenshots stehen in
[TESTDOKUMENTATION.md](TESTDOKUMENTATION.md), Kapitel 5 und 6.

---

## 9. Testkriterien

**Eintrittskriterien** – erfüllt, bevor die Suite ausgeführt wird:

- Der Produktivcode kompiliert (`./gradlew compileJava`).
- Die Testabhängigkeiten sind in `build.gradle` deklariert und `useJUnitPlatform()`
  ist im `test`-Task aktiviert.

**Austrittskriterien** – im aktuellen Stand sämtlich erfüllt und vom Build erzwungen:

- Alle 76 Tests laufen grün (`BUILD SUCCESSFUL`).
- Branch-Coverage ≥ 90 % (`jacocoTestCoverageVerification`, Teil von `./gradlew build`).
- Mutation Score ≥ 90 % (`mutationThreshold` in `./gradlew pitest`).
- Jedes Testziel Z1–Z11 ist einem Testfall zugeordnet.
- Der CI-Workflow auf `main` läuft grün durch.
- Das Coverage Gate meldet für jeden Pull Request PASS.
- Die Testdokumentation ist auf dem Stand des Codes.

---

## 10. Risiken

| ID | Risiko | Auswirkung | Heutige Absicherung |
|---|---|---|---|
| **R1** | Falscher Array-Index in einer der 8 Bedingungen von `isWin` | eine Gewinnlinie wird nicht erkannt | T1/T6 prüfen jede Linie für beide Farben (Z1) |
| **R2** | Leeres Brett: `null == null == null` würde ohne die `b[x] == color`-Vorprüfung als Gewinn gelten; zwei Steine würden ohne die dritte Gleichheit reichen | das Spiel meldet einen Gewinner, wo keiner ist | T2 und T7 (Z2); PIT bestätigt, dass jede dieser Bedingungen einen Test hat |
| **R3** | Die Zugprüfung stützt sich auf die Kurzschluss-Auswertung: `playTo < 0` und `playTo >= 9` stehen vor dem Array-Zugriff | bei umgestellter Reihenfolge `IndexOutOfBoundsException` statt kontrolliertem Fehler | T5 prüft beide Bereichsgrenzen und den Belegt-Fall (Z5) |
| **R4** | `HumanPlayer` behandelt keine `NumberFormatException` bei nicht-numerischer Eingabe | Absturz im interaktiven Spiel | **dokumentiertes Verhalten**, durch T16 festgehalten; eine Fehlerbehandlung wäre eine Änderung am Originalcode |
| **R5** | `HumanPlayer` legt pro Zug einen neuen `Scanner` an | bei gepufferter Eingabe (Pipe) geht ab dem zweiten Zug die Eingabe verloren | bekannt und in Kapitel 4.3 beschrieben; im interaktiven Terminal ohne Auswirkung |
| **R6** | Ein Test verändert mit `System.in`/`System.out` globalen Zustand | Folgetests würden in einen fremden Stream schreiben | Pioneer stellt beide Streams nach jedem Test wieder her, die jqwik-Klasse in `@AfterProperty` |
| **R7** | Unterschiedliche JDK-/Gradle-Versionen auf Entwickler- und CI-Maschine | Tests verhalten sich lokal anders als in der CI | JDK gepinnt, Wrapper im Repository, lokal und in der CI dasselbe Container-Image |
| **R8** | `PerfectPlayer` wertet mit Vorzeichen (Negamax): ein Vorzeichenfehler lässt ihn aktiv auf die Niederlage spielen | der „perfekte“ Spieler verliert | T22 spielt erschöpfend gegen jeden Gegnerzug, P1 zusätzlich gegen 1000 Zufallsgegner (Z11) |
| **R9** | Äquivalente Mutanten im `PerfectPlayer` (andere, aber ebenfalls eindeutige Schlüsselkodierung; Tie-Break-Reihenfolge; Cache-Umgehung) | Mutation Score unter 100 %, obwohl kein Verhalten ungeprüft ist | analysiert und in [MUTATIONTESTING.md](MUTATIONTESTING.md) dokumentiert; Schwellenwert deshalb 90 % |

---

## 11. Rollen und Verantwortlichkeiten

Es handelt sich um eine Einzelarbeit: Entwicklung, Testentwurf, Testdurchführung und
Testdokumentation liegen in einer Hand. Die Qualitätssicherung erfolgt zusätzlich
technisch über die CI-Pipeline, die jeden Push und Pull Request automatisch baut,
testet, die Branch-Coverage und den Mutation Score prüft und den Build bei einer
Verletzung rot färbt.
