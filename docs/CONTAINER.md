# Eigenes Container-Image für die CI/CD-Pipeline (M450)

**Ziel:** Die GitHub-Actions-Workflows laufen nicht mehr auf einem nackten
`ubuntu-latest`-Runner mit nachträglich installiertem JDK, sondern **in unserem
eigenen Image** aus der GitHub Container Registry (GHCR).

Das Image ([`Dockerfile`](../Dockerfile) im Projektwurzelverzeichnis) enthält:

- **Zulu 25** – exakt die JVM, auf die der Gradle-Daemon gepinnt ist
  (`gradle/gradle-daemon-jvm.properties`) → kein Toolchain-Download in der CI
- die **Gradle-Distribution** des Wrappers (vorab „aufgewärmt")
- `git` (für `actions/checkout`) und `gh` (für den Release-Workflow)

Image-Name: `ghcr.io/thegloo/450-tictactest-mvk:latest`

---

## Schritt 1 + 2 – Image bauen und nach GHCR laden

### Variante A: über GitHub Actions (empfohlen, kein lokaler Docker nötig)

Der Workflow [`publish-image.yml`](../.github/workflows/publish-image.yml) baut das
`Dockerfile` und pusht `:latest` nach GHCR. Er meldet sich mit dem eingebauten
`GITHUB_TOKEN` an (`permissions: packages: write`) – **kein PAT erforderlich**.

Einmalig auslösen:

1. Diesen Branch nach `main` bringen (Workflow muss auf `main` liegen).
2. GitHub → **Actions → Publish image → Run workflow**.
3. Nach dem grünen Lauf erscheint das Paket unter
   **Profil → Packages → `450-tictactest-mvk`**.

Danach baut der Workflow das Image automatisch neu, sobald sich `Dockerfile`,
`.dockerignore` oder der Gradle-Wrapper ändert.

### Variante B: von Hand mit lokalem Docker

Im Projektwurzelverzeichnis (dort liegt das `Dockerfile`):

```bash
# Schritt 1 – bauen und mit :latest taggen
docker build -t ghcr.io/thegloo/450-tictactest-mvk:latest .

# Schritt 2 – anmelden (PAT mit write:packages) und pushen
export CR_PAT=ghp_dein_token_hier
echo "$CR_PAT" | docker login ghcr.io -u TheGloo --password-stdin
docker push ghcr.io/thegloo/450-tictactest-mvk:latest
```

Kurz prüfen:

```bash
docker run --rm ghcr.io/thegloo/450-tictactest-mvk:latest java -version
```

> Der Image-Name **muss** klein geschrieben sein und mit
> `ghcr.io/<owner>/<name>` beginnen (`TheGloo` → `thegloo`).

### Paket sichtbar machen (bei beiden Varianten)

Das `LABEL org.opencontainers.image.source` im `Dockerfile` verknüpft das Paket
automatisch mit dem Repo. Unter **Package settings**:

- *Change visibility → Public* → die CI braucht dann keine Zugangsdaten, **oder**
- privat lassen → die Workflows melden sich per `GITHUB_TOKEN` an (`credentials`-Block).

---

## Schritt 3 – CI/CD-Pipeline auf das Image umgestellt

Erledigt in [`ci.yml`](../.github/workflows/ci.yml) und
[`release.yml`](../.github/workflows/release.yml):

```yaml
permissions:
  contents: read
  packages: read          # Image aus GHCR ziehen dürfen

jobs:
  build:
    runs-on: ubuntu-latest
    container:
      image: ghcr.io/thegloo/450-tictactest-mvk:latest
      credentials:                                 # nur nötig, wenn das Paket privat ist
        username: ${{ github.actor }}
        password: ${{ secrets.GITHUB_TOKEN }}
    steps:
      - uses: actions/checkout@v4
      - uses: gradle/actions/setup-gradle@v4        # nur noch fürs Dependency-Caching
      - run: ./gradlew build --stacktrace
```

Der Schritt **„Set up JDK 25 (Zulu)"** ist entfallen – die JVM kommt aus dem Image.

---

## Reihenfolge beim ersten Mal

`ci.yml` und `release.yml` referenzieren das Image. Läuft **`publish-image`** noch
nicht durch, findet die CI das Image nicht und schlägt fehl. Deshalb:

1. Branch mergen → `publish-image` einmal manuell starten (Variante A).
2. Erst wenn das Paket in GHCR liegt, laufen CI und Release grün.
