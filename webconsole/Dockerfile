# Local web console, same setup as on EC2 (infra/cloud-init.yaml.tftpl): ttyd starts the
# unchanged release jar for every connection. The jar is mounted from ./app, the image
# contains only Java and ttyd.
FROM amazoncorretto:25-al2023-headless

ARG TTYD_VERSION=1.7.7
ARG TTYD_SHA256=8a217c968aba172e0dbf3f34447218dc015bc4d5e59bf51db2f2cd12b7be4f55

ADD --checksum=sha256:${TTYD_SHA256} --chmod=755 \
    https://github.com/tsl0922/ttyd/releases/download/${TTYD_VERSION}/ttyd.x86_64 /usr/local/bin/ttyd

# the game needs no rights: own user without a home directory or shell
RUN dnf install -y shadow-utils && dnf clean all \
    && useradd --system --no-create-home --shell /sbin/nologin tictactest
USER tictactest

# only the jar, never a shell; reachable only through the nginx container (compose.yaml)
ENTRYPOINT ["ttyd", "--port", "7681", "--writable", "--max-clients", "10", \
            "--client-option", "titleFixed=TicTacTest", \
            "java", "-XX:+UseSerialGC", "-Xmx32m", "-jar", "/opt/tictactest/current/tictactoe.jar"]
