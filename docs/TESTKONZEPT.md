# Testkonzept – TicTacTest (M450)

> **Geltung:** Dieses Dokument beschreibt den **IST-Zustand** der Testaktivitäten im
> Projekt `450-tictactest-mvk` zum Stand **09.09.2026**.
> Es dokumentiert ausschliesslich, was heute im Repository tatsächlich vorhanden
> ist – keine Planungen und keine Absichtserklärungen.

| | |
|---|---|
| **Projekt** | `450-tictactest-mvk` (TicTacToe) |
| **Repository** | <https://github.com/TheGloo/450-tictactest-mvk> |
| **Testart** | automatisierte Entwicklertests (Unit- und Komponententests) |
| **Test-Framework** | JUnit 5 (Jupiter) `5.14.0` |
| **Assertion-Library** | AssertJ `3.27.6` |
| **Build-Tool** | Gradle Wrapper `9.7.0` (`./gradlew test`) |
| **JDK** | Zulu 25 (gepinnt in `gradle/gradle-daemon-jvm.properties`) |
| **Umfang** | 3 Testklassen, 11 Testmethoden, **42 ausgeführte Tests**, alle grün |

---

## 1. Einleitung und Geltungsbereich

Der Testgegenstand ist ein konsolenbasiertes TicTacToe-Spiel. Die Spielregeln liegen
vollständig in der Klasse `TicTacToeMain`; die Spieler sind über das Interface
`TicTacToePlayer` austauschbar implementiert.

Dieses Testkonzept umfasst die im Repository vorhandenen automatisierten Tests unter
`src/test/java`. Manuelle Tests, Abnahmetests und nichtfunktionale Tests
(Performance, Last, Sicherheit) sind im Projekt nicht vorhanden und daher nicht
Gegenstand dieses Konzepts.

**Mitgeltende Dokumente**

| Dokument | Inhalt |
|---|---|
| [TESTDOKUMENTATION.md](TESTDOKUMENTATION.md) | GIVEN/WHEN/THEN-Beschreibung jedes einzelnen Testfalls, Testprotokolle, Screenshots |
| [CONTAINER.md](CONTAINER.md) | Container-Image, in dem die Tests in der CI ausgeführt werden |

---

## 2. Testgegenstand

Getestet wird der Produktivcode unter `src/main/java/ch/bbw/m450/tictactoe`.

| Klasse / Methode | Rolle | Im Test abgedeckt |
|---|---|---|
| `TicTacToeMain.isWin(Stone[], Stone)` | Gewinnprüfung, 8 hart kodierte Linienbedingungen | **direkt** (T1, T2) |
| `TicTacToeMain.play(TicTacToePlayer, TicTacToePlayer)` | Spielschleife: Vorbedingung, Zugvalidierung, Farbwechsel, Gewinn- und Remis-Erkennung, Rückgabewert | **direkt** (T3–T6) |
| `GreedyPlayer.play(Stone[], Stone)` | Computerspieler: setzt auf das erste freie Feld | **direkt** (T7, T8, T9) |
| `TicTacToeMain.toString(Stone[])` | Brettausgabe mit ANSI-Farben | indirekt (wird in T3 und T4 über die Konsolenausgabe ausgeführt) |
| `TicTacToePlayer.Stone.opponent()` | Gegenfarbe | indirekt (von `BoardAssert.isWonBy` in T1 aufgerufen) |
| `HumanPlayer.play(Stone[], Stone)` | liest den Zug von `System.in` | **nein** – siehe Kapitel 4.3 |

Das Spielfeld ist ein eindimensionales Array der Länge 9:

```
 0 | 1 | 2
---+---+---
 3 | 4 | 5
---+---+---
 6 | 7 | 8
```

**Testbasis** (Grundlage der Testfallermittlung): die JavaDoc-Verträge der Methoden,
die TicTacToe-Spielregeln und der Quellcode selbst.

---

## 3. Testziele

Die folgenden Ziele werden von den heute vorhandenen Tests verfolgt. Die Zuordnung
zu den konkreten Testfällen steht in Kapitel 6.

| ID | Testziel | Begründung |
|---|---|---|
| **Z1** | **Vollständige Gewinnerkennung** – jede der 8 möglichen Gewinnlinien (3 Reihen, 3 Spalten, 2 Diagonalen) wird **für beide Farben** als Gewinn erkannt, ohne dass gleichzeitig die Gegenfarbe als Gewinner gemeldet wird. | `isWin` besteht aus 8 unabhängigen Bedingungen mit je 3 Array-Indizes; ein einzelner Zahlendreher macht eine Linie blind. |
| **Z2** | **Keine falsch positiven Gewinner** – auf leeren, teilweise gefüllten und vollen Brettern ohne durchgehende Linie meldet `isWin` für beide Farben keinen Gewinn. | Auf dem leeren Brett sind alle Felder `null`; ohne die vorgelagerte `b[x] == color`-Prüfung würde `null == null == null` als Gewinn durchgehen. Blockierte Linien und „zwei in einer Reihe" sind die typischen Beinahe-Treffer. |
| **Z3** | **Korrekter Spielablauf bis zum Gewinn** – die Spielschleife wechselt die Farben, erkennt den Gewinn im richtigen Zug, beendet die Partie vorzeitig und liefert die Gewinnerfarbe zurück. | `play` ist die zentrale Ablauflogik und verbindet alle Einzelteile. |
| **Z4** | **Korrektes Spielende bei Unentschieden** – füllen beide Spieler das Brett ohne Gewinnlinie, läuft die Schleife alle 9 Runden, meldet ein Remis und gibt `null` zurück. | Der Remis-Pfad ist der einzige Ausgang, der die Schleife regulär beendet; er wird von keinem Gewinn-Szenario mitgetestet. |
| **Z5** | **Absicherung der Vorbedingung** – dieselbe Spielerinstanz als X und O wird abgelehnt. | Die Schleife unterscheidet die Spieler per Referenzvergleich (`currentPlayer == xPlayer`); bei einer identischen Instanz bräche die Farbzuordnung. |
| **Z6** | **Zugvalidierung** – Züge ausserhalb des Bretts und auf bereits belegte Felder werden mit einer aussagekräftigen Meldung abgelehnt. | Schützt vor `IndexOutOfBoundsException` und vor dem Überschreiben gesetzter Steine. |
| **Z7** | **Determinismus des Computerspielers** – `GreedyPlayer` wählt auf jeder Brettkonstellation genau das erste freie Feld, unabhängig von der eigenen Farbe, und meldet auf einem vollen Brett einen Fehler. | Der Spieler wird ausgeliefert *und* dient als berechenbarer Gegner in T3; ändert sich seine Wahl, brechen die Spielablauf-Tests aus unklarem Grund. |
| **Z8** | **Nachweis eines lauffähigen Test-Setups** – JUnit 5 findet und startet Tests, AssertJ liegt auf dem Test-Classpath. | Modulanforderung M450; ohne funktionierendes Setup sind alle übrigen Ergebnisse wertlos. |
| **Z9** | **Automatisierte Regressionsprüfung** – die gesamte Testsuite läuft bei jedem Push und Pull Request auf `main` unbeaufsichtigt durch. | Sichert die Ziele Z1–Z8 dauerhaft gegen Regressionen ab. |

---

## 4. Teststrategie

### 4.1 Teststufen und Testarten

| Stufe | Was geprüft wird | Vorhandene Umsetzung |
|---|---|---|
| **Unit-Test** | eine Methode isoliert, ohne Mitspieler | `isWin` gegen fest vorgegebene Bretter (T1, T2); `GreedyPlayer.play` gegen vorgegebene Bretter (T7–T9) |
| **Komponenten-/Integrationstest** | `play` im Zusammenspiel mit echten bzw. simulierten Spielern | vollständige Partien und Fehlerfälle (T3–T6) |
| **Smoke-/Setup-Test** | die Testinfrastruktur selbst | `DummySetupTest` (D1, D2) |
| **CI-Lauf** | die gesamte Suite in einer sauberen Umgebung | GitHub Actions bei jedem Push/PR |

Alle Tests sind **White-Box-orientiert**: Die Testfälle wurden aus der Struktur des
Quellcodes abgeleitet (Anzahl der Bedingungen in `isWin`, Anzahl der Zweige in der
Zugvalidierung, die beiden Ausgänge der Spielschleife).

### 4.2 Angewandte Testentwurfsverfahren

| Verfahren | Konkrete Anwendung im Projekt |
|---|---|
| **Zweigüberdeckung** | T1 deckt jede der 8 Oder-Bedingungen von `isWin` ab – zweifach, einmal je Farbe; T6 deckt alle drei Zweige der Zugvalidierung ab (`playTo < 0`, `playTo >= 9`, `board[playTo] != null`); T3 und T4 decken die beiden Ausgänge der Spielschleife ab (vorzeitiger Gewinn / vollständige 9 Runden). |
| **Äquivalenzklassenbildung** | Zug-Position in drei Klassen: `< 0` ungültig, `0..8` im Brett, `>= 9` ungültig. Feldzustand in zwei Klassen: frei / belegt. Brettkonstellationen in zwei Klassen: mit Gewinnlinie (T1) / ohne Gewinnlinie (T2). |
| **Grenzwertanalyse** | T6 prüft mit `-1` und `9` genau die beiden Werte direkt ausserhalb der Bereichsgrenzen `0` und `8`. T7 prüft das erste freie Feld an beiden Rändern des Bretts (`0` und `8`) sowie in der Mitte. |
| **Negativtest / Fehlerfall** | T2 (kein Gewinner vorhanden), T5 (verletzte Vorbedingung), T6 (regelwidriger Zug), T9 (volles Brett). |
| **Zustandsbasiertes Testen** | T3 durchläuft die Zustandsfolge der Spielschleife über 7 Züge inklusive Farbwechsel und vorzeitigem Spielende; T4 über alle 9 Züge bis zum Remis. |
| **Parametrisiertes Testen** | Vier der elf Testmethoden sind parametrisiert und erzeugen zusammen **35 der 42 Testausführungen**: T1 (`@MethodSource`, 16 Bretter), T2 (`@MethodSource`, 10 Bretter), T6 (`@ValueSource`, 3 Positionen), T7 (`@CsvSource`, 6 Bretter). |

### 4.3 Bewusste Abgrenzung

Was heute **nicht** durch automatisierte Tests abgedeckt ist, und warum:

| Bereich | Begründung |
|---|---|
| `HumanPlayer.play(...)` | liest über einen `Scanner` von `System.in` und ist ohne Injektion einer Eingabequelle nicht deterministisch ausführbar. Die Klasse enthält ausserdem keine Fehlerbehandlung für `Integer.parseInt` (siehe Risiko R4). |
| Exaktes Ausgabeformat von `toString` | die ANSI-Escape-Sequenzen sind rein kosmetisch; geprüft wird in T3 und T4 nur, dass die Gewinn- bzw. Remis-Meldung in der Ausgabe erscheint. |
| Code-Coverage-Messung | im Projekt ist kein Coverage-Werkzeug (z. B. JaCoCo) konfiguriert; die Überdeckung wird über die Verfahren in 4.2 argumentativ, nicht metrisch belegt. |
| Nichtfunktionale Tests | bei einem 3×3-Brett ohne Persistenz und Netzwerk gegenstandslos. |

---

## 5. Teststruktur

### 5.1 Verzeichnisstruktur

```
src/test/java/ch/bbw/m450/tictactoe/
├── DummySetupTest.java          Setup-Nachweis (Z8)
├── TicTacToeMainTest.java       Spielregeln (Z1–Z6)
├── players/
│   └── GreedyPlayerTest.java    Computerspieler (Z7)
└── testsupport/                 Testinfrastruktur, enthält keine Tests
    ├── Boards.java
    ├── TestPlayers.java
    ├── BoardAssert.java
    └── ConsoleCapture.java
```

Die Testklassen spiegeln das Paket der getesteten Klassen. Wiederverwendbare
Bausteine liegen im eigenen Paket `testsupport`, damit die Testmethoden selbst nur
noch das jeweilige Szenario beschreiben.

### 5.2 Aufbau der Testklassen

`TicTacToeMainTest` ist mit `@Nested` nach der getesteten Methode gegliedert, sodass
die Testausgabe die Struktur der Klasse widerspiegelt:

```
TicTacToeMain                       @DisplayName auf der Testklasse
├── isWin                           @Nested class IsWin
│   ├── detects every winning line for both colours          (16 Durchläufe)
│   └── reports no winner on boards without three in a line  (10 Durchläufe)
└── play                            @Nested class Play
    ├── lets CROSS win on 2-4-6 when two greedy players meet
    ├── returns null and reports a draw when all nine fields are filled
    ├── refuses to run a player against itself
    └── rejects moves outside the board and onto occupied fields  (3 Durchläufe)

GreedyPlayer                        @DisplayName auf GreedyPlayerTest
├── always takes the first free field, top-left first         (6 Durchläufe)
├── plays the same field regardless of the colour it is asked to play
└── throws when the board is full
```

Jede Testmethode prüft genau ein Szenario und ist innerlich nach
**GIVEN / WHEN / THEN** aufgebaut. Sprechende `@DisplayName`-Annotationen beschreiben
das erwartete Verhalten in ganzen Sätzen; die `name`-Attribute der parametrisierten
Tests benennen zusätzlich jeden einzelnen Durchlauf
(z. B. *„CIRCLE wins on the main diagonal"*).

### 5.3 Testinfrastruktur (`testsupport`)

| Klasse | Art | Aufgabe |
|---|---|---|
| `Boards` | Fixtures + Builder | Bretter als 9-Zeichen-Muster (`board("XXX......")`); Konstanten `EMPTY`, `DRAW`, `DRAW_MOVES_CROSS`, `DRAW_MOVES_CIRCLE`; die `@MethodSource`-Fixtures `winningLines()` (16 Bretter) und `boardsWithoutWinner()` (10 Bretter); `render()` für lesbare Fehlermeldungen |
| `TestPlayers` | Test-Doubles | `greedy()` liefert einen berechenbaren `GreedyPlayer` (bewusst je Aufruf eine neue Instanz, weil `play` dieselbe Referenz zweimal ablehnt); `alwaysPlayingTo(int)` spielt stets dieselbe – auch ungültige – Position; `playingSequence(int...)` spielt eine vorgegebene Zugfolge ab und steuert die Partie so in eine exakte Endstellung |
| `BoardAssert` | eigene AssertJ-Assertion | `assertThatBoard(...).isWonBy(color)` / `.hasNoWinner()`; gibt bei einem Fehlschlag das betroffene Brett mit aus |
| `ConsoleCapture` | JUnit-5-Extension | leitet `System.out` je Testmethode in einen Puffer um und stellt den Originalstream in `afterEach` wieder her; macht die Ausgabe von `play` prüfbar und hält sie aus dem Build-Log heraus |

Die 16 Bretter von `winningLines()` werden aus **einer** Liste von acht Linienmustern
für beide Farben abgeleitet. Damit existiert jede Gewinnlinie im Testcode nur einmal;
eine Korrektur wirkt automatisch für CROSS und CIRCLE.

### 5.4 Konventionen

- Testklasse `<KlasseUnterTest>Test`, Methodenname beschreibt das erwartete Verhalten.
- Eine fachliche Aussage pro Testmethode; keine Abhängigkeiten zwischen Tests.
- Testdaten stehen nicht im Test, sondern als Fixture in `Boards`; der Test nennt sie
  nur beim Namen.
- Assertions über AssertJ (`assertThat…`), fachliche Prüfungen über `BoardAssert`.
- Kein Zugriff auf Dateisystem, Netzwerk oder Datenbank; globaler Zustand
  (`System.out`) wird ausschliesslich über `ConsoleCapture` verändert.

---

## 6. Testfälle und Zuordnung zu den Testzielen

Ausführliche GIVEN/WHEN/THEN-Beschreibungen: [TESTDOKUMENTATION.md](TESTDOKUMENTATION.md).

| ID | Testfall | Testklasse / Methode | Verfahren | Erwartetes Ergebnis | Ziel |
|---|---|---|---|---|---|
| **T1** | jede der 8 Gewinnlinien, je einmal für CROSS und CIRCLE | `TicTacToeMainTest.IsWin.detectsEveryWinningLine` (16 Durchläufe) | Zweigüberdeckung, parametrisiert | `isWin(…, farbe) == true` und `isWin(…, gegenfarbe) == false` | **Z1** |
| **T2** | 10 Bretter ohne Gewinnlinie: leer, 2× voll, „zwei in einer Reihe", 4× blockierte Linie, verstreute Steine | `TicTacToeMainTest.IsWin.isFalseWithoutThreeInALine` (10 Durchläufe) | Äquivalenzklassen, Negativtest, parametrisiert | für beide Farben `false` | **Z2** |
| **T3** | vollständige Partie zweier `GreedyPlayer` | `TicTacToeMainTest.Play.twoGreedyPlayersLetCrossWin` | zustandsbasiert, Happy Path | Rückgabe `Stone.CROSS` (Linie 2-4-6); Ausgabe enthält `"...and the winner is: CROSS"` | **Z3** |
| **T4** | gescriptete Partie, die das Brett ohne Gewinnlinie füllt | `TicTacToeMainTest.Play.fullBoardWithoutALineIsADraw` | zustandsbasiert, Grenzfall (9. Runde) | Rückgabe `null`; Ausgabe enthält `"it's a draw!"` | **Z4** |
| **T5** | dieselbe Spielerinstanz als X und O | `TicTacToeMainTest.Play.rejectsTheSamePlayerTwice` | Vorbedingung / Negativtest | `IllegalArgumentException("players must differ")` | **Z5** |
| **T6** | Zug auf `-1`, `9` und `0` (belegt) | `TicTacToeMainTest.Play.rejectsInvalidMoves` (3 Durchläufe) | Äquivalenzklassen + Grenzwerte, parametrisiert | `IllegalStateException("cannot play to position <p>")` | **Z6** |
| **T7** | 6 Brettkonstellationen → erwartetes Feld | `GreedyPlayerTest.takesTheFirstFreeField` (6 Durchläufe) | Grenzwerte, parametrisiert | der Index des ersten freien Feldes | **Z7** |
| **T8** | dasselbe Brett, beide Farben angefragt | `GreedyPlayerTest.ignoresTheColour` | Äquivalenzklasse | identischer Rückgabewert | **Z7** |
| **T9** | volles Brett | `GreedyPlayerTest.throwsOnAFullBoard` | Negativtest | `IllegalStateException("cannot play at all")` | **Z7** |
| **D1** | JUnit-5-Assertion wird gefunden und ausgeführt | `DummySetupTest.junitDummy` | Smoke-Test | Test läuft grün | **Z8** |
| **D2** | AssertJ liegt auf dem Test-Classpath | `DummySetupTest.assertJDummy` | Smoke-Test | Test kompiliert und läuft grün | **Z8** |

### Rückwärtsverfolgung: jedes Ziel ist belegt

| Ziel | belegt durch | Testausführungen |
|---|---|---|
| Z1 | T1 | 16 |
| Z2 | T2 | 10 |
| Z3 | T3 | 1 |
| Z4 | T4 | 1 |
| Z5 | T5 | 1 |
| Z6 | T6 | 3 |
| Z7 | T7, T8, T9 | 8 |
| Z8 | D1, D2 | 2 |
| Z9 | alle Testfälle, ausgeführt durch `.github/workflows/ci.yml` | 42 pro CI-Lauf |
| | **Summe** | **42** |

Jedes Testziel Z1–Z9 ist durch mindestens einen Testfall abgedeckt, und jeder
Testfall zahlt auf genau ein Ziel ein – es gibt weder unbelegte Ziele noch Testfälle
ohne Zielbezug.

---

## 7. Testumgebung und Werkzeuge

| Bereich | IST-Zustand |
|---|---|
| Sprache / Laufzeit | Java, JDK 25 (Azul Zulu), über die Gradle-Toolchain gepinnt |
| Testframework | JUnit 5 Jupiter `5.14.0` (via `junit-bom`), aktiviert mit `useJUnitPlatform()` |
| Assertions | AssertJ `3.27.6` |
| Build | Gradle Wrapper `9.7.0`; Testlauf mit `./gradlew test`, Gesamtbuild mit `./gradlew build` |
| Testberichte | `build/reports/tests/test/index.html`; Konsolenausgabe über `testLogging { events 'passed', 'skipped', 'failed' }` |
| Isolation | keine externen Systeme, keine Datenbank, kein Netzwerk; `System.in` wird durch Test-Doubles ersetzt, `System.out` durch `ConsoleCapture` gekapselt |
| CI | GitHub Actions (`.github/workflows/ci.yml`) bei jedem Push und PR auf `main`; der Job läuft im projekteigenen Container-Image `ghcr.io/thegloo/450-tictactest-mvk:latest` und lädt den Testreport als Artefakt hoch |
| Reproduzierbarkeit | feste Abhängigkeitsversionen, gepinnte JDK-Version, Gradle Wrapper im Repository |

---

## 8. Testdurchführung und Ergebnis

Ausgeführter Befehl:

```bash
./gradlew test --rerun-tasks --no-build-cache --console=plain
```

**Ergebnis vom 09.09.2026: 42 ausgeführte Tests, 0 Fehler, `BUILD SUCCESSFUL`.**

| Testklasse / Gruppe | Testmethoden | Ausgeführte Tests |
|---|---|---|
| `DummySetupTest` | 2 | 2 |
| `TicTacToeMainTest > isWin` | 2 (beide parametrisiert) | 26 |
| `TicTacToeMainTest > play` | 4 (davon 1 parametrisiert) | 6 |
| `GreedyPlayerTest` | 3 (davon 1 parametrisiert) | 8 |
| **Total** | **11** | **42** |

Die vollständigen Testprotokolle sowie die Screenshots eines erfolgreichen und eines
absichtlich fehlgeschlagenen Laufs stehen in
[TESTDOKUMENTATION.md](TESTDOKUMENTATION.md).

---

## 9. Testkriterien

**Eintrittskriterien** – erfüllt, bevor die Suite ausgeführt wird:

- Der Produktivcode kompiliert (`./gradlew compileJava`).
- Die Testabhängigkeiten sind in `build.gradle` deklariert und `useJUnitPlatform()`
  ist im `test`-Task aktiviert.

**Austrittskriterien** – im aktuellen Stand sämtlich erfüllt:

- Alle 42 Tests laufen grün (`BUILD SUCCESSFUL`).
- Jede der 8 Gewinnlinien von `isWin` ist für beide Farben abgedeckt, jeder der
  3 Validierungszweige und beide Ausgänge von `play` sind durch mindestens einen
  Testfall abgedeckt (Kapitel 6).
- Jedes Testziel Z1–Z9 ist einem Testfall zugeordnet.
- Der CI-Workflow auf `main` läuft grün durch.
- Die Testdokumentation ist auf dem Stand des Codes.

---

## 10. Risiken

| ID | Risiko | Auswirkung | Heutige Absicherung |
|---|---|---|---|
| **R1** | Falscher Array-Index in einer der 8 Bedingungen von `isWin` | eine Gewinnlinie wird nicht erkannt | T1 prüft jede Linie in einem eigenen Durchlauf, für beide Farben (Z1) |
| **R2** | Leeres Brett: `null == null == null` würde ohne die `b[x] == color`-Vorprüfung als Gewinn gelten | das Spiel meldet einen Gewinner, wo keiner ist | T2 mit `Boards.EMPTY` sowie 9 weiteren Brettern ohne Linie (Z2) |
| **R3** | Die Zugprüfung stützt sich auf die Kurzschluss-Auswertung: `playTo < 0` und `playTo >= 9` stehen vor dem Array-Zugriff `board[playTo]` | bei umgestellter Reihenfolge `IndexOutOfBoundsException` statt kontrolliertem Fehler | T6 prüft beide Bereichsgrenzen und den Belegt-Fall (Z6) |
| **R4** | `HumanPlayer` behandelt keine `NumberFormatException` bei nicht-numerischer Eingabe | Absturz im interaktiven Spiel | **nicht durch Tests abgesichert** – die Klasse ist bewusst ausserhalb des Testumfangs (Kapitel 4.3) |
| **R5** | Der Remis-Pfad von `play` wird nur erreicht, wenn die Schleife alle 9 Runden durchläuft | eine Regression im Unentschieden-Fall bliebe unbemerkt | T4 fährt die Partie mit `TestPlayers.playingSequence` gezielt bis in die 9. Runde (Z4) |
| **R6** | Ein Test verändert mit `System.out` globalen Zustand | Folgetests würden in einen fremden Stream schreiben | `ConsoleCapture` stellt den Originalstream in `afterEach` immer wieder her – auch nach einem fehlgeschlagenen Test |
| **R7** | Unterschiedliche JDK-Versionen auf Entwickler- und CI-Maschine | Tests verhalten sich lokal anders als in der CI | JDK-Version in `gradle/gradle-daemon-jvm.properties` gepinnt; die CI läuft im eigenen Container-Image mit genau diesem JDK |
| **R8** | `GreedyPlayer` ist gleichzeitig Produktivcode und der Gegner in T3 | eine Änderung an seiner Strategie liesse T3 fehlschlagen, ohne dass `play` fehlerhaft wäre | T7–T9 prüfen den Spieler direkt, sodass die Ursache eines solchen Fehlschlags sofort erkennbar ist (Z7) |

R4 ist erkannt, bewertet und bewusst offen – dokumentiert, damit die Grenzen der
Aussagekraft der Testsuite nachvollziehbar bleiben.

---

## 11. Rollen und Verantwortlichkeiten

Es handelt sich um eine Einzelarbeit: Entwicklung, Testentwurf, Testdurchführung und
Testdokumentation liegen in einer Hand. Die Qualitätssicherung erfolgt zusätzlich
technisch über die CI-Pipeline, die jeden Push und Pull Request auf `main`
automatisch baut und testet und den Build bei einem fehlschlagenden Test rot färbt.
