# Testkonzept – TicTacToe (M450)

Dieses Dokument beschreibt **was, warum und wie** getestet wird. Die konkrete
Umsetzung und die Ergebnisse stehen in der [Testdokumentation](TESTDOKUMENTATION.md).

| | |
|---|---|
| **Projekt** | `450-tictactest-mvk` (TicTacToe) |
| **Testart** | Automatisierte Entwicklertests (Unit- und Komponenten-/Integrationstests) |
| **Test-Framework** | JUnit 5 (Jupiter) `5.14.0` |
| **Assertion-Library** | AssertJ `3.27.6` |
| **Build-Tool** | Gradle Wrapper (`./gradlew test` bzw. `./gradlew build`) |
| **JDK** | Zulu 25 (in `gradle/gradle-daemon-jvm.properties` gepinnt) |
| **CI** | GitHub Actions (`.github/workflows/ci.yml`) bei jedem Push/PR auf `main` |

---

## 1. Ausgangslage und Testziele

Das Programm ist ein kleines TicTacToe mit einem Mensch- und einem einfachen
Computergegner. Die gesamte Spiellogik liegt in `TicTacToeMain`
(`isWin`, `play`, `toString`); die Spieler sind über das Interface
`TicTacToePlayer` austauschbar.

**Ziele der Testaktivitäten:**

1. **Regelkonformität sichern** – Gewinnerkennung, Zugvalidierung, Farbwechsel und
   Unentschieden müssen den TicTacToe-Regeln entsprechen.
2. **Regressionsschutz** – Änderungen an der Logik dürfen bestehende Regeln nicht
   brechen; die Tests laufen automatisch in der CI.
3. **Nachweis eines funktionierenden Test-Setups** – JUnit 5 und AssertJ sind
   eingebunden und werden vom Build ausgeführt (Modulanforderung M450).
4. **Lesbare, wartbare Tests** – jeder Test beschreibt genau ein Szenario; Fixtures,
   Test-Doubles und eigene Assertions liegen im Paket `testsupport`.

**Nicht-Ziele:** kein Test der „Spielstärke" eines Spielers, keine
UI-/Konsolen-Layout-Tests, keine Performance-/Lasttests (für ein 3×3-Brett irrelevant).

---

## 2. Testgegenstände (Was wird getestet?)

| # | Testobjekt | Art | Priorität | Begründung |
|---|---|---|---|---|
| A | `TicTacToeMain.isWin(Stone[] board, Stone color)` | reine Funktion | **hoch** | 8 hart kodierte Bedingungen mit je 3 Array-Indizes – hohe Tippfehler-Gefahr; zusätzlich Null-Behandlung beim leeren Brett |
| B | `TicTacToeMain.play(TicTacToePlayer, TicTacToePlayer)` | Spielschleife / Integration | **hoch** | zentrale Ablauflogik: Vorbedingung, Zugvalidierung, Farbwechsel, Gewinn-/Remis-Erkennung, Rückgabewert |
| C | `GreedyPlayer.play(...)` | Strategie-Implementierung | mittel | einfache Schleifenlogik, dient zugleich als berechenbarer Gegner in B |
| D | `TicTacToePlayer.Stone.opponent()` | Hilfsmethode | niedrig | trivial, wird indirekt über `BoardAssert` mitgetestet |
| E | `TicTacToeMain.toString(Stone[])` | Ausgabe-Formatierung | niedrig | nur kosmetisch (ANSI-Farben); wird über die Konsolenausgabe von `play` gestreift |
| F | `HumanPlayer.play(...)` | I/O-Adapter | **out of scope** | liest von `System.in` und nutzt `Integer.parseInt` ohne Fehlerbehandlung – nicht deterministisch testbar ohne stdin-Injektion; siehe Risiko R4 |

Die **Testbasis** (Grundlage der Testfälle) sind die JavaDoc-Verträge der Methoden,
die TicTacToe-Spielregeln und der Quellcode selbst.

---

## 3. Teststrategie und -methoden

### 3.1 Teststufen

| Stufe | Umfang | Beispiele |
|---|---|---|
| **Unit** | einzelne Methode isoliert | `isWin` gegen fixe Bretter (A) |
| **Komponenten-/Integrationstest** | `play` inkl. echter/simulierter Spieler | komplette Partie zweier `GreedyPlayer` (B, C) |
| **Setup-/Smoke-Test** | Test-Infrastruktur | `DummySetupTest` |

Systemtests (JAR starten, interaktiv spielen) erfolgen manuell und einmalig,
weil `HumanPlayer` stdin benötigt.

### 3.2 Testentwurfsverfahren

| Verfahren | Einsatz |
|---|---|
| **Äquivalenzklassen** | Zug-Position: `< 0` (ungültig) / `0..8` (im Brett) / `≥ 9` (ungültig); Feldzustand: frei / belegt |
| **Grenzwertanalyse** | Positionen `-1, 0` und `8, 9` an den Bereichsgrenzen des Bretts |
| **Entscheidungs-/Zweigüberdeckung** | `isWin`: jede der 8 Gewinnlinien einzeln + Negativfälle; `play`: alle drei Zweige der Zugvalidierung (`playTo < 0`, `playTo >= 9`, `board[playTo] != null`) |
| **Zustandsbasiertes Testen** | `play`-Schleife: Farbwechsel X→O→X, vorzeitiges Spielende bei Gewinn, Spielende nach 9 Zügen = Remis |
| **Fehlerfälle / Vorbedingungen** | `play` mit identischer Spielerreferenz → `IllegalArgumentException`; „schummelnder" Spieler → `IllegalStateException` |

### 3.3 Test-Doubles und Fixtures (`testsupport`)

| Baustein | Zweck |
|---|---|
| `Boards` | Bretter als 9-Zeichen-Muster (`board("XXX......")`), Konstanten `EMPTY`/`DRAW`, Datenquelle `winningLinesForCross()` für den parametrisierten Test, `render()` für Fehlermeldungen |
| `TestPlayers.greedy()` | frischer, berechenbarer `GreedyPlayer` (neue Instanz pro Aufruf – `play` lehnt dieselbe Instanz zweimal ab) |
| `TestPlayers.alwaysPlayingTo(int)` | Stub, der stets auf dieselbe – ggf. ungültige – Position setzt |
| `BoardAssert` | fachliche AssertJ-Assertion `assertThatBoard(...).isWonBy(CROSS)` / `.hasNoWinner()`, gibt bei Fehlschlag das Brett aus |
| `ConsoleCapture` | JUnit-5-Extension, leitet `System.out` pro Test um und stellt es wieder her; macht die Ausgabe von `play` prüfbar |

### 3.4 Namens- und Strukturkonvention

- Tests nach getesteter Methode mit `@Nested` gruppiert, sprechende `@DisplayName`.
- Ein Szenario pro Testmethode, Aufbau nach **GIVEN / WHEN / THEN**.
- Parametrisierte Tests, wo dieselbe Logik gegen viele Daten läuft.

---

## 4. Abgeleitete Testfälle (Überblick)

Detaillierte GIVEN/WHEN/THEN-Beschreibungen: siehe [Testdokumentation](TESTDOKUMENTATION.md).

| ID | Testobjekt | Kurzbeschreibung | Verfahren | Erwartetes Ergebnis |
|---|---|---|---|---|
| T1 | `isWin` | jede der 8 Gewinnlinien für `CROSS` (parametrisiert) | Zweigüberdeckung | `isWin(…, CROSS) == true`, `isWin(…, CIRCLE) == false` |
| T2 | `isWin` | leeres Brett und volles Brett ohne Linie | Grenzwert / Negativfall | für beide Farben `false` (Null-Vergleich schlägt nicht als Gewinn durch) |
| T3 | `play` | komplette Partie zweier `GreedyPlayer` | Happy Path / Zustandsübergänge | Rückgabe `CROSS` (Linie 2-4-6), Konsolenausgabe enthält `"...and the winner is: CROSS"` |
| T4 | `play` | derselbe Spieler als X und O | Vorbedingung | `IllegalArgumentException("players must differ")`, Spiel startet nicht |
| T5 | `play` | Zug auf `-1`, `9`, `0` (belegt) (parametrisiert) | Äquivalenzklassen / Grenzwert | `IllegalStateException("cannot play to position <p>")` |
| D1 | Setup | JUnit-5-Assertion läuft | Smoke | Test wird gefunden und grün |
| D2 | Setup | AssertJ auf dem Classpath | Smoke | Test kompiliert und grün |

**Mögliche Erweiterungen (derzeit nicht umgesetzt):**

- `play`: erzwungenes Remis über zwei Stub-Spieler, die das `DRAW`-Muster erzeugen
  (prüft den `return null`-Pfad nach 9 Runden explizit).
- `play`: `CIRCLE` gewinnt (bisher gewinnt in T3 immer `CROSS`).
- `GreedyPlayer`: volles Brett → `IllegalStateException("cannot play at all")`.
- `toString` / `Boards.board`: ungültige Musterlänge → `IllegalArgumentException`.

---

## 5. Testumgebung und Werkzeuge

| Bereich | Festlegung |
|---|---|
| Sprache / Laufzeit | Java, JDK 25 (Zulu), von Gradle-Toolchain verwaltet |
| Build & Testlauf | `./gradlew test` (nur Tests) bzw. `./gradlew build` (inkl. JAR) |
| Report | `build/reports/tests/test/index.html` (HTML), Konsolen-Log via `testLogging` |
| Isolierung | keine externen Systeme, keine DB, kein Netzwerk; `System.in`/`System.out` via Double bzw. Extension gekapselt |
| CI | GitHub Actions führt `./gradlew build` bei Push/PR auf `main` aus, lädt Test-Report und JAR als Artefakt hoch |
| Reproduzierbarkeit | fixe Abhängigkeitsversionen (JUnit-BOM, AssertJ), gepinnte JDK-Version, Gradle Wrapper |

---

## 6. Ein-/Austrittskriterien

**Eintrittskriterien (Testbeginn):**

- Code kompiliert (`./gradlew compileJava`).
- Testabhängigkeiten in `build.gradle` deklariert, `useJUnitPlatform()` aktiv.

**Austrittskriterien (Testabschluss):**

- Alle automatisierten Tests grün (`BUILD SUCCESSFUL`), aktuell **16 ausgeführte Tests**
  (7 Methoden, davon 2 parametrisiert mit 8 bzw. 3 Durchläufen).
- Jede Gewinnlinie von `isWin` und jeder Validierungszweig von `play` durch mindestens
  einen Testfall abgedeckt.
- CI-Lauf auf `main` grün.
- Testdokumentation aktualisiert (inkl. Screenshot eines erfolgreichen und eines
  fehlgeschlagenen Laufs gemäss Modulvorgabe).

---

## 7. Risiken und Massnahmen

| ID | Risiko | Auswirkung | Massnahme |
|---|---|---|---|
| R1 | Falscher Array-Index in einer der 8 `isWin`-Bedingungen | Gewinnlinie wird nicht/falsch erkannt | T1 prüft jede Linie einzeln, T2 sichert die Negativfälle |
| R2 | Leeres Brett: `null == null == null` würde ohne die `b[x] == color`-Vorprüfung als Gewinn gelten | Spiel meldet Phantom-Gewinner | T2 mit `Boards.EMPTY` |
| R3 | Reihenfolge der Zugprüfung (`playTo < 0` vor `board[playTo]`) – bei Umstellung droht `IndexOutOfBoundsException` statt sauberer Exception | Absturz statt kontrollierter Fehler | T5 deckt beide Bereichsgrenzen und den Belegt-Fall ab |
| R4 | `HumanPlayer` fängt `NumberFormatException` bei ungültiger Eingabe nicht ab | Absturz bei Fehleingabe im echten Spiel | bekannt und akzeptiert; `HumanPlayer` ist nicht automatisiert getestet, Hinweis hier dokumentiert |
| R5 | `play` vergleicht Spieler per Referenz (`==`); zwei „gleiche" Spieler-Instanzen sind erlaubt und gewollt | – | bewusst so; `TestPlayers.greedy()` liefert pro Aufruf eine neue Instanz |
| R6 | Remis-Pfad (`return null` nach 9 Runden) ist nicht durch einen eigenen Test abgedeckt | Regression im Remis-Fall bliebe unbemerkt | als Erweiterung in Kap. 4 vermerkt |
| R7 | Test verändert globalen Zustand (`System.out`) | Folgetests erhalten falschen Stream | `ConsoleCapture` stellt den Originalstream in `afterEach` immer wieder her |

---

## 8. Rollen und Verantwortlichkeiten

Schulprojekt (Einzelarbeit): Entwicklung, Testentwurf, Testdurchführung und
Dokumentation in einer Person. Review erfolgt über Pull Requests gegen `main`;
die CI ist das automatisierte Qualitätstor.
