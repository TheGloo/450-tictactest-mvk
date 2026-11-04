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

## Start

Im Repo-Root:

```bash
docker compose up --build        # Image bauen und starten (Ctrl+C beendet)
```

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

Standard ist das Release-Tag `v1.0.0`. Eine andere Version wird beim Build gewählt:

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

**Lokale Datei:** Jar nach `webconsole/jar/` legen, dann wird genau diese Datei
verwendet (die Build-Args für Repo/Version werden ignoriert):

```bash
gh release download v1.2.3 --pattern '*.jar' --dir webconsole/jar
docker compose up --build
```

Danach `webconsole/jar/*.jar` wieder löschen, sonst bleibt es bei dieser Datei.

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
