# Web-Konsole für TicTacTest: das unveränderte Release-Jar hinter ttyd.
#
# Jede Browser-Verbindung startet über ttyd einen eigenen `java -jar`-Prozess,
# also eine eigene Spielsitzung. Im Browser läuft nur das Spiel, keine Shell.
#
# Build-Kontext ist dieses Verzeichnis:  docker build webconsole
# Gestartet wird über docker-compose.yml im Repo-Root, Details in README.md.

# Java 25: das Jar wird mit Zulu JDK 25 gebaut (gradle/gradle-daemon-jvm.properties,
# .devcontainer/Dockerfile). Ubuntu Noble liefert ttyd als Paket (universe).
FROM eclipse-temurin:25-jre-noble

# Repository und Release-Tag, aus denen das Jar geholt wird. Der Release-Workflow
# (.github/workflows/release.yml) hängt das Jar als tictactoe-<Version ohne v>.jar an,
# deshalb braucht es ein festes Tag (releases/latest/download geht mit dem
# versionierten Dateinamen nicht).
ARG TICTACTEST_REPO=TheGloo/450-tictactest-mvk
ARG TICTACTEST_VERSION=v1.0.0
# Optional: eigene Download-URL (überschreibt Repo/Version) und erwartete SHA256.
ARG JAR_URL=
ARG JAR_SHA256=

# ttyd: Web-Terminal; curl + ca-certificates: Download des Jars
RUN apt-get update \
	&& apt-get install -y --no-install-recommends ca-certificates curl ttyd \
	&& rm -rf /var/lib/apt/lists/* \
	&& useradd --system --uid 10001 --user-group --no-create-home \
		--shell /usr/sbin/nologin tictactest

# Liegt ein Jar in webconsole/jar/, wird genau diese Datei verwendet (z. B. für
# private Repos oder offline), sonst wird das Release-Asset heruntergeladen.
COPY jar/ /tmp/jar/
RUN set -eu; \
	mkdir -p /app; \
	local_jar="$(find /tmp/jar -maxdepth 1 -name '*.jar' | head -n 1)"; \
	if [ -n "$local_jar" ]; then \
		echo "Using local jar: $local_jar"; \
		cp "$local_jar" /app/tictactest.jar; \
	else \
		url="${JAR_URL:-https://github.com/${TICTACTEST_REPO}/releases/download/${TICTACTEST_VERSION}/tictactoe-${TICTACTEST_VERSION#v}.jar}"; \
		echo "Downloading $url"; \
		curl -fsSL -o /app/tictactest.jar "$url"; \
	fi; \
	rm -rf /tmp/jar; \
	if [ -n "$JAR_SHA256" ]; then \
		echo "$JAR_SHA256  /app/tictactest.jar" | sha256sum -c -; \
	fi; \
	sha256sum /app/tictactest.jar; \
	chmod 0444 /app/tictactest.jar

USER tictactest
WORKDIR /app

EXPOSE 7681

# Exec-Form ohne Shell: ttyd startet pro Verbindung nur das Spiel.
# -W erlaubt Eingaben aus dem Browser.
ENTRYPOINT ["ttyd", "-W", "-p", "7681", "java", "-jar", "/app/tictactest.jar"]
