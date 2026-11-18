# Changelog

Alle wesentlichen Änderungen an TicTacTest (Spiel, Frontend, Deployment). Format nach
[Keep a Changelog](https://keepachangelog.com/de/1.1.0/), Versionen nach
[Semantic Versioning](https://semver.org/lang/de/), siehe [RELEASE.md](RELEASE.md).
Die Releases mit ihren Artefakten: <https://github.com/TheGloo/450-tictactest-mvk/releases>

## [Unreleased]

### Hinzugefügt
- `RELEASE.md` mit Release- und Versionierungskonzept, dieser Changelog.
- `main` ist geschützt: Änderungen nur per Pull Request mit grünen Checks.

### Dokumentation
- `docs/deployment.md`: Secrets-Konzept, Nachweise aller Release-Fälle, Reflexion und
  Promotion-Strategie, Ergebnis des Experiments mit ersetzter Instanz.
- README verlinkt die Web-App.

## [1.6.0] – 2026-11-04

### Hinzugefügt
- Jar-Manifest enthält `Implementation-Vendor`.
- Spielregeln im Frontend nennen den Gegner (`GreedyPlayer`).

## [1.5.0] – 2026-11-04

### Hinzugefügt
- Jar-Manifest enthält `Implementation-Title`.

### Behoben
- `release.yml` wiederholt das Anlegen des Releases bei Serverfehlern von GitHub und
  entfernt dabei halbfertige Drafts.

## [1.4.0] – 2026-11-04 – unvollständig

### Hinzugefügt
- Jar-Manifest enthält `Implementation-Version`.

### Bekanntes Problem
- Wegen einer Störung bei GitHub wurde der Release **ohne Jar** veröffentlicht, nur mit
  dem Frontend-Archiv. Das Deployment hat das erkannt und nichts verändert. Nicht
  verwenden, die Änderung ist in 1.5.0 enthalten.

## [1.3.0] – 2026-11-04

### Geändert
- Frontend: grüner Header.

### Behoben
- Smoke-Test beim Backend-Deployment kann das Jar lesen (Rechte des temporären
  Verzeichnisses).
- Frontend-Deployment überspringt keine geänderten Dateien mehr gleicher Grösse
  (`unzip -DD`).

## [1.2.0] – 2026-11-04

### Hinzugefügt
- Frontend: Footer.

### Geändert
- Der Release überspringt das Deployment, solange die Infrastruktur nicht in den
  GitHub Variables eingetragen ist.

## [1.1.0] – 2026-11-04

### Hinzugefügt
- Frontend (`frontend/`): Spielregeln, eingebettetes Terminal, Anzeige von Frontend- und
  Backend-Version, URL der Web-Konsole aus `config.js`.
- Release enthält zusätzlich `tictactest-frontend-X.Y.Z.zip`.
- Infrastruktur mit OpenTofu: EC2 mit Corretto 25, ttyd als systemd-Dienst hinter nginx
  mit `/version`, Elastic IP, privater Artifact-Bucket, Frontend als S3 Static Website.
- Deployment nur der Teile, die sich seit dem vorherigen Release geändert haben;
  manuelles Deployment einer beliebigen Version (Rollback).
- Lokaler Start nur aus Release-Assets (`webconsole/start-local.sh`).

## [1.0.0] – 2026-11-04

Erster Release: Stand der Abgabe M450 plus erste Web-Konsole.

### Hinzugefügt
- Konsolen-TicTacToe mit `HumanPlayer`, `GreedyPlayer` und `PerfectPlayer` (Minimax).
- Testsuite mit JUnit 6, AssertJ, JUnit Pioneer, jqwik; JaCoCo (Branch-Coverage ≥ 90 %),
  PIT (Mutation Score ≥ 90 %).
- CI, Coverage Gate, Coverage-Time-Series, DevContainer-Pipeline, Release-Workflow.

[Unreleased]: https://github.com/TheGloo/450-tictactest-mvk/compare/v1.6.0...main
[1.6.0]: https://github.com/TheGloo/450-tictactest-mvk/releases/tag/v1.6.0
[1.5.0]: https://github.com/TheGloo/450-tictactest-mvk/releases/tag/v1.5.0
[1.4.0]: https://github.com/TheGloo/450-tictactest-mvk/releases/tag/v1.4.0
[1.3.0]: https://github.com/TheGloo/450-tictactest-mvk/releases/tag/v1.3.0
[1.2.0]: https://github.com/TheGloo/450-tictactest-mvk/releases/tag/v1.2.0
[1.1.0]: https://github.com/TheGloo/450-tictactest-mvk/releases/tag/v1.1.0
[1.0.0]: https://github.com/TheGloo/450-tictactest-mvk/releases/tag/v1.0.0
