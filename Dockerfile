# Build/CI image for the TicTacToe project.
#
# It ships the exact JDK the Gradle daemon is pinned to (Zulu 25, see
# gradle/gradle-daemon-jvm.properties) plus the tools a GitHub Actions job needs
# when it runs *inside* a container, and it bakes in the Gradle distribution so CI
# does not download it on every run.
FROM azul/zulu-openjdk:25

LABEL org.opencontainers.image.source=https://github.com/TheGloo/450-tictactest-mvk
LABEL org.opencontainers.image.description="JDK 25 (Zulu) build image for the M450 TicTacToe project"

# git            - actions/checkout uses it for a real clone instead of the REST fallback
# ca-certificates - HTTPS for the few things Gradle still fetches
# gh             - the release workflow publishes the GitHub Release with it
RUN apt-get update \
	&& apt-get install -y --no-install-recommends git ca-certificates curl gnupg \
	&& mkdir -p -m 755 /etc/apt/keyrings \
	&& curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg -o /etc/apt/keyrings/githubcli-archive-keyring.gpg \
	&& chmod go+r /etc/apt/keyrings/githubcli-archive-keyring.gpg \
	&& echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" > /etc/apt/sources.list.d/github-cli.list \
	&& apt-get update \
	&& apt-get install -y --no-install-recommends gh \
	&& rm -rf /var/lib/apt/lists/*

# JAVA_HOME is already set by the base image to the Zulu 25 install; Gradle's
# daemon-JVM discovery picks it up, so no toolchain is downloaded.
ENV GRADLE_USER_HOME=/opt/gradle-home

WORKDIR /app

# Warm the Gradle wrapper distribution into the image.
COPY gradlew ./
COPY gradle ./gradle
RUN ./gradlew --version --no-daemon

CMD ["bash"]
