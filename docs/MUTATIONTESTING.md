# Mutation Testing mit PIT (M450)

Code Coverage zeigt nur, **welcher Code ausgeführt** wird, aber nicht, ob die Tests
einen Fehler darin auch **bemerken würden**. Mutation Testing prüft genau das: Das Werkzeug
[PIT](https://pitest.org/) baut absichtlich kleine Fehler in den kompilierten Code ein
(*Mutanten*) und lässt für jeden Mutanten die Tests laufen.

| Ergebnis | Bedeutung |
|---|---|
| **KILLED** | mindestens ein Test schlägt fehl → die Tests erkennen diesen Fehler ✅ |
| **SURVIVED** | alle Tests bleiben grün → eine Lücke in den Assertions ❌ |
| **NO_COVERAGE** | kein Test führt die mutierte Zeile überhaupt aus ❌ |

**Mutation Score** = getötete Mutanten / alle Mutanten.

---

## 1. Einrichtung

PIT ist über das Gradle-Plugin [`info.solidsoft.pitest`](https://gradle-pitest-plugin.solidsoft.info/)
in [`build.gradle`](../build.gradle) eingebunden:

| Einstellung | Wert | Warum |
|---|---|---|
| `pitestVersion` | `1.30.0` | aktuelle Version, läuft mit Java 25 |
| `junit5PluginVersion` | `1.2.3` | PIT führt die JUnit-5-Tests aus |
| `targetClasses` / `targetTests` | `ch.bbw.m450.tictactoe.*` | ganzer Produktivcode, alle Tests |
| `excludedClasses` | `HumanPlayer` | liest von `System.in`, nicht sinnvoll testbar |
| `excludedMethods` | `main` | startet nur ein Spiel mit dem `HumanPlayer` |
| `mutators` | `STRONGER` | Standardmutatoren plus *Remove Conditionals* (z. B. `a == b` → `true`/`false`) |
| `mutationThreshold` | `90` | der Build schlägt fehl, wenn der Score unter 90 % fällt |
| `outputFormats` | `HTML`, `XML` | Report für Menschen und für die Zusammenfassung in der CI |

Ausführen:

```bash
./gradlew pitest
```

Report: `build/reports/pitest/index.html` (pro Klasse und Zeile markiert, welche
Mutanten überlebt haben).

### In der Pipeline

Der Job **Mutation Testing (PIT)** in [`ci.yml`](../.github/workflows/ci.yml) läuft bei
jedem Push und Pull Request parallel zum Build im DevContainer-Image. Er

- führt `./gradlew pitest` aus und **schlägt fehl, wenn der Score unter 90 % liegt**,
- schreibt Score, Anzahl Mutanten, getötete, überlebende und nicht abgedeckte Mutanten in
  die Zusammenfassung des Workflow-Laufs,
- lädt den HTML-Report als Artefakt **`mutation-report`** hoch.

---

## 2. Erster Lauf: 76 %

Mit den bestehenden Tests erzeugte PIT **94 Mutanten, davon 71 getötet (76 %)**, obwohl
die Line Coverage der mutierten Klassen bei 85 % lag. Die 23 übrigen Mutanten zeigen
konkrete Lücken:

| Stelle | Überlebende Mutanten | Ursache (fehlender Test) |
|---|---|---|
| `isWin` | 7× „Vergleich durch `true`/`false` ersetzt“ | Es wurden nur Bretter geprüft, auf denen CROSS **eine ganze** Linie hat. Es fehlten Siege von CIRCLE und Bretter mit **zwei** Steinen in einer Linie, deren dritter Stein vom Gegner ist. So blieb z. B. `b[0] == b[1]` → `true` unbemerkt. |
| `play`, Rundenschleife | `round < 9` → `round <= 9` und → `true` | Kein Test spielte ein **Unentschieden**, die Schleife lief also nie bis zum Ende. |
| `play`, Farbwahl | `currentPlayer == xPlayer` → `true` | Kein Test liess **CIRCLE gewinnen**. Hätten beide Spieler immer CROSS gesetzt, wäre das nicht aufgefallen. |
| `play`, ungültiger Zug | `println(board)` entfernt | Die Ausgabe des Bretts vor dem Abbruch wurde nicht geprüft. |
| `play`, Unentschieden | `println("it's a draw!")` **NO_COVERAGE** | siehe Unentschieden |
| `toString` | 8 Mutanten (u. a. `return ""`) | `toString` wurde **gar nicht** geprüft, nur ausgeführt. |
| `GreedyPlayer.play` | `i < 9` → `i <= 9` und → `true` | Kein Test mit **vollem Brett**. Statt `IllegalStateException` käme eine `ArrayIndexOutOfBoundsException`. |
| `Stone.opponent` | `this == CROSS` → `true` | Nur `CROSS.opponent()` wurde indirekt verwendet, `CIRCLE.opponent()` nie. |

Gerade `toString` zeigt den Unterschied zur Coverage: Die Methode war vollständig
abgedeckt (die Spieltests geben das Brett aus), aber kein Test hätte bemerkt, wenn sie
einen leeren String liefert.

---

## 3. Verbesserte Tests: 100 %

Für jede Lücke wurde ein gezielter Test ergänzt:

| Neuer Test | tötet |
|---|---|
| `IsWin.detectsEveryWinningLineOfCircle` – alle 8 Linien für CIRCLE | Mutanten in `isWin`, die nur für CROSS zufällig stimmen |
| `IsWin.isFalseForTwoInALine` – 24 Bretter: jede Linie mit einem Feld vom Gegner besetzt (Fixture `Boards.nearlyWinningLinesForCross`) | `b[x] == b[y]` → `true` in `isWin` |
| `ToString.rendersTheBoard` – exakter String inkl. ANSI-Farben | alle 8 Mutanten in `toString` |
| `Play.circleCanWin` – gescriptete Partie, CIRCLE gewinnt die mittlere Reihe (Test-Double `TestPlayers.playingInOrder`) | Farbwahl in `play` |
| `Play.fullBoardWithoutLineIsADraw` – gescriptete Partie bis zum Unentschieden | Rundenschleife, `"it's a draw!"` |
| `Play.rejectsInvalidMoves` – prüft zusätzlich die Ausgabe des Bretts | entferntes `println` |
| `GreedyPlayerTest` – erstes freies Feld, volles Brett | Schleifengrenze in `GreedyPlayer` |
| `StoneTest.opponentIsTheOtherColour` | `Stone.opponent` |

Ergebnis:

```
>> Line Coverage (for mutated classes only): 38/41 (93%)
>> Generated 94 mutations Killed 94 (100%)
```

| | vorher | nachher |
|---|---:|---:|
| Mutanten | 94 | 94 |
| getötet | 71 | 94 |
| **Mutation Score** | **76 %** | **100 %** |
| Line Coverage (mutierte Klassen) | 85 % | 93 % |

---

## 4. Schwellenwert

Der Schwellenwert liegt bewusst bei **90 %** und nicht bei 100 %: Bei neuem Code können
*äquivalente Mutanten* entstehen, also Änderungen, die das Verhalten gar nicht
verändern und darum von keinem Test getötet werden können. Diese sollen den Build nicht
blockieren. Ein deutlicher Rückgang, zum Beispiel neuer Code ohne aussagekräftige
Assertions, lässt die CI dagegen fehlschlagen.
