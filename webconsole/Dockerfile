# Web console for TicTacTest: ttyd (https://github.com/tsl0922/ttyd) serves a terminal
# in the browser and starts the unchanged release jar for every connection. The jar is
# not part of the image, compose.yaml mounts it from ./app, so a new release only
# replaces that file and the image stays as it is.
FROM eclipse-temurin:25-jre

ARG TTYD_VERSION=1.7.7
ARG TTYD_SHA256=8a217c968aba172e0dbf3f34447218dc015bc4d5e59bf51db2f2cd12b7be4f55

ADD --checksum=sha256:${TTYD_SHA256} --chmod=755 \
    https://github.com/tsl0922/ttyd/releases/download/${TTYD_VERSION}/ttyd.x86_64 /usr/local/bin/ttyd

# the game needs no rights: own user without a home directory or shell
RUN useradd --system --no-create-home --shell /usr/sbin/nologin player
USER player

EXPOSE 7681

# --writable      the browser may type (moves 0-8)
# --max-clients   one JVM per player, keeps a t3.micro alive
# small heap and serial GC: the game needs a few MB, the default would take 1/4 of the RAM
ENTRYPOINT ["ttyd", "--port", "7681", "--writable", "--max-clients", "10", \
            "--client-option", "titleFixed=TicTacTest", \
            "java", "-XX:+UseSerialGC", "-Xmx32m", "-jar", "/app/tictactoe.jar"]
