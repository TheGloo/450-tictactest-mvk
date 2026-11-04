# Web-Konsole

Spielt das **unveränderte Release-Jar** von TicTacTest im Browser. Der Container
startet [ttyd](https://github.com/tsl0922/ttyd), ein Web-Terminal, das für jede
Browser-Verbindung `java -jar /app/tictactest.jar` startet. Im Browser läuft nur das
Spiel, keine Shell. Das Jar wird nicht neu gebaut, sondern aus dem GitHub-Release
geholt (oder als Datei hineinkopiert).

| Datei | Inhalt |
|---|---|
| [`Dockerfile`](Dockerfile) | `eclipse-temurin:25-jre-noble` + ttyd, Jar nach `/app/tictactest.jar`, läuft als User `tictactest` (UID 10001) |
| [`../docker-compose.yml`](../docker-compose.yml) | Service `webconsole`, Port `127.0.0.1:8080` → `7681` |
| `jar/` | optional: lokales Jar statt Download (Inhalt wird nicht committet) |

## Voraussetzungen

- Docker mit Compose-Plugin (`docker compose version`)
- Ein GitHub-Release mit angehängtem Jar, erzeugt vom
  [Release-Workflow](../docs/PIPELINES.md#3-release) (Tag `v1.2.3` → Asset
  `tictactoe-1.2.3.jar`). Alternativ ein lokales Jar, siehe unten.
- Für das private Repo: [GitHub CLI](https://cli.github.com/) mit Zugriff aufs Repo
  (`gh auth status`), um das Jar herunterzuladen.

## Start

Im Repo-Root. `TheGloo/450-tictactest-mvk` ist ein **privates** Repository, der
Download ohne Token schlägt dort fehl. Deshalb ist der Standardweg: Release-Jar mit
der GitHub CLI (eigenes Login, kein Token im Image) nach `webconsole/jar/` holen und
dann bauen:

```bash
gh release download v1.0.0 --repo TheGloo/450-tictactest-mvk --pattern '*.jar' --dir webconsole/jar
docker compose up --build        # Image bauen und starten (Ctrl+C beendet)
```

Bei einem **öffentlichen** Repository (z. B. Fork) entfällt der erste Schritt, dann
lädt der Build das Asset selbst herunter. Fehlt das Jar und scheitert der Download,
bricht der Build mit einem Hinweis auf diesen Ablauf ab.

Dann im Browser **http://localhost:8080** öffnen und spielen: Feldnummer (0–8)
eingeben, Enter. Der Port ist nur an `127.0.0.1` gebunden, also nur auf dem eigenen
Rechner erreichbar.

- **Jeder Tab ist eine eigene Sitzung:** Jede Browser-Verbindung startet einen eigenen
  `java`-Prozess mit einem eigenen Spiel. Kontrolle:
  `docker compose exec webconsole ps aux` zeigt pro offenem Tab einen `java`-Prozess.
- **Spielende:** Ist die Partie zu Ende (oder ungültige Eingabe), beendet sich das
  Spiel und mit ihm die Sitzung. Seite neu laden (oder Enter bei *Press ⏎ to
  Reconnect*) startet ein neues Spiel.

Stoppen und aufräumen: `docker compose down`.

## Version wechseln

Standard ist das Release-Tag `v1.0.0`.

**Privates Repo (Standard):** Jar in `webconsole/jar/` austauschen. Dort darf genau
ein Jar liegen, sonst bricht der Build ab. `TICTACTEST_VERSION` setzt dann nur noch
das Image-Tag.

```bash
rm -f webconsole/jar/*.jar
gh release download v1.2.3 --repo TheGloo/450-tictactest-mvk --pattern '*.jar' --dir webconsole/jar
TICTACTEST_VERSION=v1.2.3 docker compose up --build
```

**Öffentliches Repo (Download im Build):** Die Version wird per Build-Arg gewählt:

```bash
TICTACTEST_VERSION=v1.2.3 docker compose up --build
# oder nur bauen
docker compose build --build-arg TICTACTEST_VERSION=v1.2.3
```

Dauerhaft ändern: Default in [`../docker-compose.yml`](../docker-compose.yml)
(`TICTACTEST_VERSION`) oder eine Datei `.env` im Repo-Root mit
`TICTACTEST_VERSION=v1.2.3`. Ein Fork wird mit `TICTACTEST_REPO=owner/repo` gewählt.

Das Jar wird von
`https://github.com/<TICTACTEST_REPO>/releases/download/<TICTACTEST_VERSION>/tictactoe-<Version ohne v>.jar`
geholt. `latest` funktioniert nicht, weil der Dateiname die Version enthält.

## Eigenes Jar (privates Repo, offline, andere Quelle)

Der Download ohne Token klappt nur bei einem **öffentlichen** Repository. Tokens kommen
nicht ins Image und nicht ins Repo. Stattdessen:

**Lokale Datei:** Liegt genau ein Jar in `webconsole/jar/`, wird diese Datei
verwendet (siehe [Start](#start)). Die Build-Args für Repo, Version und `JAR_URL`
werden dann ignoriert. Der Build gibt dazu eine `WARNING` aus, ebenso, wenn der
Dateiname nicht zu `TICTACTEST_VERSION` passt (erwartet `tictactoe-<Version ohne v>.jar`),
damit kein veraltetes Jar unbemerkt im Image landet. Für den Download im Build `webconsole/jar/*.jar` wieder
löschen. Die Datei wird nicht committet (`.gitignore`).

**Eigene URL:** `JAR_URL` überschreibt die Release-URL:

```bash
JAR_URL=https://example.org/tictactoe-1.2.3.jar docker compose up --build
```

**Prüfsumme:** Mit `JAR_SHA256=<sha256>` bricht der Build ab, wenn das Jar nicht
byte-identisch ist. Der Build gibt die SHA256 des Jars im Image immer aus; prüfen
lässt sie sich auch im laufenden Container:

```bash
docker compose exec webconsole sha256sum /app/tictactest.jar
sha256sum tictactoe-1.2.3.jar     # heruntergeladenes Release-Asset
```

## Sicherheit

ttyd gibt ein interaktives Terminal frei. Deshalb startet der Container nur `java` in
Exec-Form (kein Shell-Zugang), läuft nicht als root und ist nur lokal gebunden. Für
einen öffentlichen Betrieb fehlen noch Authentifizierung und TLS.
