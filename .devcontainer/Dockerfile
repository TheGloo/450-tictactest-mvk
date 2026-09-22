# DevContainer- und CI-Image für TicTacTest (M450).
#
# Ein Image für alles: VS Code (devcontainer.json) und alle GitHub-Actions-Jobs
# laufen in derselben Umgebung. Gebaut und veröffentlicht wird es von
# .github/workflows/devcontainer.yml, Details in docs/DEVCONTAINER.md.
#
# Build-Kontext ist das Projektwurzelverzeichnis (der Gradle-Wrapper wird
# mitkopiert):  docker build -f .devcontainer/Dockerfile .
#
# Alpine + Zulu 25: Zulu ist exakt der Hersteller, auf den der Gradle-Daemon
# gepinnt ist (gradle/gradle-daemon-jvm.properties), es wird also keine
# Toolchain heruntergeladen.
FROM azul/zulu-openjdk-alpine:25

LABEL org.opencontainers.image.source=https://github.com/TheGloo/450-tictactest-mvk
LABEL org.opencontainers.image.description="Alpine DevContainer/CI image for the M450 TicTacToe project: Zulu JDK 25, Gradle, JUnit"

ARG USERNAME=dev
ARG USER_UID=1000
ARG USER_GID=1000
ARG JUNIT_PLATFORM_VERSION=1.14.0

# bash, git, openssh-client  VS Code und actions/checkout
# libstdc++, libgcc          VS Code Server und das Node.js des Actions-Runners (musl-Build)
# tar, zstd, gzip            GNU tar für den Gradle-Cache von actions/cache (BusyBox-tar reicht nicht)
# github-cli                 gh für den Release-Workflow
RUN apk add --no-cache \
		bash ca-certificates curl git github-cli gzip libgcc libstdc++ \
		openssh-client procps tar unzip zstd \
	&& addgroup -g "$USER_GID" "$USERNAME" \
	&& adduser -D -u "$USER_UID" -G "$USERNAME" -s /bin/bash "$USERNAME"

# JUnit: Console Launcher, um Tests auch ohne Gradle starten zu können
# (junit --class-path build/classes/java/test:build/classes/java/main --scan-class-path).
# Die Version gehört zur junit-bom 5.14.0 aus build.gradle.
RUN mkdir -p /opt/junit \
	&& curl -fsSL -o /opt/junit/junit-platform-console-standalone.jar \
		"https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/${JUNIT_PLATFORM_VERSION}/junit-platform-console-standalone-${JUNIT_PLATFORM_VERSION}.jar" \
	&& printf '#!/bin/sh\nexec java -jar /opt/junit/junit-platform-console-standalone.jar "$@"\n' > /usr/local/bin/junit \
	&& chmod +x /usr/local/bin/junit

# Gradle: Wrapper-Distribution und alle Projekt-Abhängigkeiten (JUnit, AssertJ,
# JaCoCo) vorab ins Image laden. Der Wrapper-Download wird zugleich als globales
# "gradle" verlinkt, damit beide dieselbe Version sind.
ENV GRADLE_USER_HOME=/opt/gradle-home
WORKDIR /tmp/warmup
COPY gradlew settings.gradle build.gradle gradle.properties ./
COPY gradle ./gradle
RUN ./gradlew --no-daemon --quiet dependencies > /dev/null \
	&& ln -s "$(find "$GRADLE_USER_HOME/wrapper/dists" -mindepth 3 -maxdepth 3 -type d -name 'gradle-*')/bin/gradle" /usr/local/bin/gradle \
	&& gradle --version \
	&& rm -rf /tmp/warmup "$GRADLE_USER_HOME/daemon" \
	&& chown -R "$USER_UID:$USER_GID" "$GRADLE_USER_HOME"

# Standardbenutzer 1000:1000. CI-Jobs starten den Container mit --user root,
# weil der Runner das Workspace-Verzeichnis mit seiner eigenen UID einhängt.
USER $USERNAME
WORKDIR /home/$USERNAME

CMD ["bash"]
