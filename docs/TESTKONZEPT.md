# Testkonzept – TicTacTest (M450)

> **Geltung:** Dieses Dokument beschreibt den **IST-Zustand** der Testaktivitäten im
> Projekt `450-tictactest-mvk` zum Stand **09.09.2026** (Commit `fdd5c44`).
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
| **Umfang** | 2 Testklassen, 7 Testmethoden, **16 ausgeführte Tests**, alle grün |

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
| `TicTacToeMain.isWin(Stone[], Stone)` | Gewinnprüfung, 8 hart kodierte Linienbedingungen | **ja** (T1, T2) |
| `TicTacToeMain.play(TicTacToePlayer, TicTacToePlayer)` | Spielschleife: Vorbedingung, Zugvalidierung, Farbwechsel, Gewinnerkennung, Rückgabewert | **ja** (T3, T4, T5) |
| `TicTacToeMain.toString(Stone[])` | Brettausgabe mit ANSI-Farben | indirekt (wird in T3 über die Konsolenausgabe ausgeführt) |
| `TicTacToePlayer.Stone.opponent()` | Gegenfarbe | indirekt (von `BoardAssert.isWonBy` aufgerufen) |
| `GreedyPlayer.play(...)` | setzt auf das erste freie Feld | indirekt (als realer Spieler in T3 und T5) |
| `HumanPlayer.play(...)` | liest den Zug von `System.in` | **nein** – siehe Kapitel 4.3 |

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
| **Z1** | **Vollständige Gewinnerkennung** – jede der 8 möglichen Gewinnlinien (3 Reihen, 3 Spalten, 2 Diagonalen) wird als Gewinn erkannt. | `isWin` besteht aus 8 unabhängigen Bedingungen mit je 3 Array-Indizes; ein einzelner Zahlendreher macht eine Linie blind. |
| **Z2** | **Keine falsch positiven Gewinner** – auf einem leeren Brett und auf einem vollen Brett ohne Linie meldet `isWin` für beide Farben keinen Gewinn. | Auf dem leeren Brett sind alle Felder `null`; ohne die vorgelagerte `b[x] == color`-Prüfung würde `null == null == null` als Gewinn durchgehen. |
| **Z3** | **Korrekter Spielablauf** – die Spielschleife wechselt die Farben, erkennt den Gewinn im richtigen Zug, beendet die Partie und liefert die Gewinnerfarbe zurück. | `play` ist die zentrale Ablauflogik und verbindet alle Einzelteile. |
| **Z4** | **Absicherung der Vorbedingung** – dieselbe Spielerinstanz als X und O wird abgelehnt. | Die Schleife unterscheidet die Spieler per Referenzvergleich (`currentPlayer == xPlayer`); bei einer identischen Instanz bräche die Farbzuordnung. |
| **Z5** | **Zugvalidierung** – Züge ausserhalb des Bretts und auf bereits belegte Felder werden abgelehnt. | Schützt vor `IndexOutOfBoundsException` und vor dem Überschreiben gesetzter Steine. |
| **Z6** | **Nachweis eines lauffähigen Test-Setups** – JUnit 5 findet und startet Tests, AssertJ liegt auf dem Test-Classpath. | Modulanforderung M450; ohne funktionierendes Setup sind alle übrigen Ergebnisse wertlos. |
| **Z7** | **Automatisierte Regressionsprüfung** – die gesamte Testsuite läuft bei jedem Push und Pull Request auf `main` unbeaufsichtigt durch. | Sichert die Ziele Z1–Z6 dauerhaft gegen Regressionen ab. |

---

## 4. Teststrategie

### 4.1 Teststufen und Testarten

| Stufe | Was geprüft wird | Vorhandene Umsetzung |
|---|---|---|
| **Unit-Test** | eine Methode isoliert, ohne Mitspieler | `isWin` gegen fest vorgegebene Bretter (T1, T2) |
| **Komponenten-/Integrationstest** | `play` im Zusammenspiel mit echten bzw. simulierten Spielern | vollständige Partie und Fehlerfälle (T3, T4, T5) |
| **Smoke-/Setup-Test** | die Testinfrastruktur selbst | `DummySetupTest` (D1, D2) |
| **CI-Lauf** | die gesamte Suite in einer sauberen Umgebung | GitHub Actions bei jedem Push/PR |

Alle Tests sind **White-Box-orientiert**: Die Testfälle wurden aus der Struktur des
Quellcodes abgeleitet (Anzahl der Bedingungen in `isWin`, Anzahl der Zweige in der
Zugvalidierung).

### 4.2 Angewandte Testentwurfsverfahren

| Verfahren | Konkrete Anwendung im Projekt |
|---|---|
| **Zweigüberdeckung** | T1 deckt jede der 8 Oder-Bedingungen von `isWin` mit einem eigenen Durchlauf ab; T5 deckt alle drei Zweige der Zugvalidierung ab (`playTo < 0`, `playTo >= 9`, `board[playTo] != null`). |
| **Äquivalenzklassenbildung** | Zug-Position in drei Klassen: `< 0` ungültig, `0..8` im Brett, `>= 9` ungültig. Feldzustand in zwei Klassen: frei / belegt. |
| **Grenzwertanalyse** | T5 prüft mit `-1` und `9` genau die beiden Werte direkt ausserhalb der Bereichsgrenzen `0` und `8`. |
| **Negativtest / Fehlerfall** | T2 (kein Gewinner vorhanden), T4 (verletzte Vorbedingung), T5 (regelwidriger Zug). |
| **Zustandsbasiertes Testen** | T3 durchläuft die Zustandsfolge der Spielschleife über 7 Züge inklusive Farbwechsel und vorzeitigem Spielende. |
| **Parametrisiertes Testen** | T1 (`@MethodSource`, 8 Datensätze) und T5 (`@ValueSource`, 3 Werte) prüfen dieselbe Logik gegen viele Eingaben ohne Codeverdopplung. |

### 4.3 Bewusste Abgrenzung

Was heute **nicht** durch automatisierte Tests abgedeckt ist, und warum:

| Bereich | Begründung |
|---|---|
| `HumanPlayer.play(...)` | liest über einen `Scanner` von `System.in` und ist ohne Injektion einer Eingabequelle nicht deterministisch ausführbar. Die Klasse enthält ausserdem keine Fehlerbehandlung für `Integer.parseInt` (siehe Risiko R4). |
| Remis-Pfad von `play` (`return null` nach 9 Runden) | wird von keinem der vorhandenen Testfälle durchlaufen; der `DRAW`-Brettzustand wird nur statisch über `isWin` geprüft (T2). |
| Exaktes Ausgabeformat von `toString` | die ANSI-Escape-Sequenzen sind rein kosmetisch; geprüft wird in T3 nur, dass die Gewinnmeldung in der Ausgabe erscheint. |
| Code-Coverage-Messung | im Projekt ist kein Coverage-Werkzeug (z. B. JaCoCo) konfiguriert; die Überdeckung wird über die Verfahren in 4.2 argumentativ, nicht metrisch belegt. |
| Nichtfunktionale Tests | bei einem 3×3-Brett ohne Persistenz und Netzwerk gegenstandslos. |

---

## 5. Teststruktur

### 5.1 Verzeichnisstruktur

```
src/test/java/ch/bbw/m450/tictactoe/
├── DummySetupTest.java          Setup-Nachweis (Z6)
├── TicTacToeMainTest.java       fachliche Tests (Z1–Z5)
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
│   ├── detects all three rows, columns and diagonals   (8 Durchläufe)
│   └── is false for an empty board and for a full board without a line
└── play                            @Nested class Play
    ├── lets CROSS win on 2-4-6 when two greedy players meet
    ├── refuses to run a player against itself
    └── rejects moves outside the board and onto occupied fields  (3 Durchläufe)
```

Jede Testmethode prüft genau ein Szenario und ist innerlich nach
**GIVEN / WHEN / THEN** aufgebaut. Sprechende `@DisplayName`-Annotationen beschreiben
das erwartete Verhalten in ganzen Sätzen.

### 5.3 Testinfrastruktur (`testsupport`)

| Klasse | Art | Aufgabe |
|---|---|---|
| `Boards` | Fixture + Builder | Bretter als 9-Zeichen-Muster (`board("XXX......")`); Konstanten `EMPTY` und `DRAW`; `winningLinesForCross()` als `@MethodSource` für T1; `render()` für lesbare Fehlermeldungen |
| `TestPlayers` | Test-Doubles | `greedy()` liefert einen berechenbaren `GreedyPlayer` (bewusst je Aufruf eine neue Instanz, weil `play` dieselbe Referenz zweimal ablehnt); `alwaysPlayingTo(int)` ist ein Stub, der stets dieselbe – auch ungültige – Position spielt |
| `BoardAssert` | eigene AssertJ-Assertion | `assertThatBoard(...).isWonBy(CROSS)` / `.hasNoWinner()`; gibt bei einem Fehlschlag das betroffene Brett mit aus |
| `ConsoleCapture` | JUnit-5-Extension | leitet `System.out` je Testmethode in einen Puffer um und stellt den Originalstream in `afterEach` wieder her; macht die Ausgabe von `play` prüfbar und hält sie aus dem Build-Log heraus |

### 5.4 Konventionen

- Testklasse `<KlasseUnterTest>Test`, Methodenname beschreibt das erwartete Verhalten.
- Eine fachliche Aussage pro Testmethode; keine Abhängigkeiten zwischen Tests.
- Assertions über AssertJ (`assertThat…`), fachliche Prüfungen über `BoardAssert`.
- Kein Zugriff auf Dateisystem, Netzwerk oder Datenbank; globaler Zustand
  (`System.out`) wird ausschliesslich über `ConsoleCapture` verändert.

---

## 6. Testfälle und Zuordnung zu den Testzielen

Ausführliche GIVEN/WHEN/THEN-Beschreibungen: [TESTDOKUMENTATION.md](TESTDOKUMENTATION.md).

| ID | Testfall | Testklasse / Methode | Verfahren | Erwartetes Ergebnis | Ziel |
|---|---|---|---|---|---|
| **T1** | jede der 8 Gewinnlinien für `CROSS` | `TicTacToeMainTest.IsWin.detectsEveryWinningLine` (8 Durchläufe) | Zweigüberdeckung, parametrisiert | `isWin(…, CROSS) == true` und `isWin(…, CIRCLE) == false` | **Z1** |
| **T2** | leeres Brett und volles Brett ohne Linie | `TicTacToeMainTest.IsWin.isFalseWithoutThreeInALine` | Negativtest, Grenzfall | für beide Farben `false` | **Z2** |
| **T3** | vollständige Partie zweier `GreedyPlayer` | `TicTacToeMainTest.Play.twoGreedyPlayersLetCrossWin` | zustandsbasiert, Happy Path | Rückgabe `Stone.CROSS` (Linie 2-4-6); Konsolenausgabe enthält `"...and the winner is: CROSS"` | **Z3** |
| **T4** | dieselbe Spielerinstanz als X und O | `TicTacToeMainTest.Play.rejectsTheSamePlayerTwice` | Vorbedingung / Negativtest | `IllegalArgumentException("players must differ")` | **Z4** |
| **T5** | Zug auf `-1`, `9` und `0` (belegt) | `TicTacToeMainTest.Play.rejectsInvalidMoves` (3 Durchläufe) | Äquivalenzklassen + Grenzwerte, parametrisiert | `IllegalStateException("cannot play to position <p>")` | **Z5** |
| **D1** | JUnit-5-Assertion wird gefunden und ausgeführt | `DummySetupTest.junitDummy` | Smoke-Test | Test läuft grün | **Z6** |
| **D2** | AssertJ liegt auf dem Test-Classpath | `DummySetupTest.assertJDummy` | Smoke-Test | Test kompiliert und läuft grün | **Z6** |

### Rückwärtsverfolgung: jedes Ziel ist belegt

| Ziel | belegt durch | Anzahl Testausführungen |
|---|---|---|
| Z1 | T1 | 8 |
| Z2 | T2 | 1 |
| Z3 | T3 | 1 |
| Z4 | T4 | 1 |
| Z5 | T5 | 3 |
| Z6 | D1, D2 | 2 |
| Z7 | alle Testfälle, ausgeführt durch `.github/workflows/ci.yml` | 16 pro CI-Lauf |
| | **Summe** | **16** |

Jedes Testziel Z1–Z7 ist durch mindestens einen Testfall abgedeckt, und jeder
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

**Ergebnis vom 09.09.2026: 16 ausgeführte Tests, 0 Fehler, `BUILD SUCCESSFUL`.**

| Testklasse | Testmethoden | Ausgeführte Tests |
|---|---|---|
| `DummySetupTest` | 2 | 2 |
| `TicTacToeMainTest` | 5 (davon 2 parametrisiert) | 14 |
| **Total** | **7** | **16** |

Die vollständigen Testprotokolle sowie die Screenshots eines erfolgreichen und eines
absichtlich fehlgeschlagenen Laufs stehen in
[TESTDOKUMENTATION.md](TESTDOKUMENTATION.md), Kapitel 5 und 6.

---

## 9. Testkriterien

**Eintrittskriterien** – erfüllt, bevor die Suite ausgeführt wird:

- Der Produktivcode kompiliert (`./gradlew compileJava`).
- Die Testabhängigkeiten sind in `build.gradle` deklariert und `useJUnitPlatform()`
  ist im `test`-Task aktiviert.

**Austrittskriterien** – im aktuellen Stand sämtlich erfüllt:

- Alle 16 Tests laufen grün (`BUILD SUCCESSFUL`).
- Jede der 8 Gewinnlinien von `isWin` und jeder der 3 Validierungszweige von `play`
  ist durch mindestens einen Testfall abgedeckt (Kapitel 6).
- Jedes Testziel Z1–Z7 ist einem Testfall zugeordnet.
- Der CI-Workflow auf `main` läuft grün durch.
- Die Testdokumentation ist auf dem Stand des Codes.

---

## 10. Risiken

| ID | Risiko | Auswirkung | Heutige Absicherung |
|---|---|---|---|
| **R1** | Falscher Array-Index in einer der 8 Bedingungen von `isWin` | eine Gewinnlinie wird nicht erkannt | T1 prüft jede Linie in einem eigenen Durchlauf (Z1) |
| **R2** | Leeres Brett: `null == null == null` würde ohne die `b[x] == color`-Vorprüfung als Gewinn gelten | das Spiel meldet einen Gewinner, wo keiner ist | T2 mit `Boards.EMPTY` (Z2) |
| **R3** | Die Zugprüfung stützt sich auf die Kurzschluss-Auswertung: `playTo < 0` und `playTo >= 9` stehen vor dem Array-Zugriff `board[playTo]` | bei umgestellter Reihenfolge `IndexOutOfBoundsException` statt kontrolliertem Fehler | T5 prüft beide Bereichsgrenzen und den Belegt-Fall (Z5) |
| **R4** | `HumanPlayer` behandelt keine `NumberFormatException` bei nicht-numerischer Eingabe | Absturz im interaktiven Spiel | **nicht durch Tests abgesichert** – die Klasse ist bewusst ausserhalb des Testumfangs (Kapitel 4.3) |
| **R5** | Der Remis-Pfad von `play` wird von keinem Testfall durchlaufen | eine Regression im Unentschieden-Fall bliebe unbemerkt | **offene Lücke**; teilweise mittelbar durch T2 (`Boards.DRAW` ergibt keinen Gewinner) |
| **R6** | Ein Test verändert mit `System.out` globalen Zustand | Folgetests würden in einen fremden Stream schreiben | `ConsoleCapture` stellt den Originalstream in `afterEach` immer wieder her – auch nach einem fehlgeschlagenen Test |
| **R7** | Unterschiedliche JDK-Versionen auf Entwickler- und CI-Maschine | Tests verhalten sich lokal anders als in der CI | JDK-Version in `gradle/gradle-daemon-jvm.properties` gepinnt; die CI läuft im eigenen Container-Image mit genau diesem JDK |

R4 und R5 sind erkannt, bewertet und bewusst offen – sie sind hier dokumentiert,
damit die Grenzen der Aussagekraft der Testsuite nachvollziehbar bleiben.

---

## 11. Rollen und Verantwortlichkeiten

Es handelt sich um eine Einzelarbeit: Entwicklung, Testentwurf, Testdurchführung und
Testdokumentation liegen in einer Hand. Die Qualitätssicherung erfolgt zusätzlich
technisch über die CI-Pipeline, die jeden Push und Pull Request auf `main`
automatisch baut und testet und den Build bei einem fehlschlagenden Test rot färbt.
