#
# Debian 12, with Git, an SSH client, sudo, and other common tools, and a
# non-root `vscode` user.
#

# https://github.com/devcontainers/images/tree/main/src/base-debian
FROM mcr.microsoft.com/devcontainers/base:1-bookworm

ENV DEBIAN_FRONTEND=noninteractive

USER root

# JDK 25, for the API service's Gradle build.
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"
COPY --from=eclipse-temurin:25-jdk /opt/java/openjdk /opt/java/openjdk

# Node.js 24 and npm, for the web client and the test packages.
COPY --from=node:24-bookworm-slim /usr/local/bin/node /usr/local/bin/node
COPY --from=node:24-bookworm-slim /usr/local/lib/node_modules /usr/local/lib/node_modules
RUN ln -s ../lib/node_modules/npm/bin/npm-cli.js /usr/local/bin/npm \
    && ln -s ../lib/node_modules/npm/bin/npx-cli.js /usr/local/bin/npx

# The system libraries that Playwright's Chromium needs, for the acceptance
# tests. Keep the version in step with `tests/acceptance/package.json`.
ARG PLAYWRIGHT_VERSION=1.64.1
RUN apt-get update \
    && npx --yes "playwright@${PLAYWRIGHT_VERSION}" install-deps chromium \
    && rm -rf /var/lib/apt/lists/* /root/.npm

# Drop back to the non-root user, which the base image provides. Everything
# above needs root to install into system paths; everything from here on,
# including the running container, MUST NOT. Bind-mounted project files are
# then created with the host user's ownership (the base image's `vscode` user
# has UID 1000) instead of root's. `remoteUser` in `devcontainer.json` names
# this same user, so keep the two in step.
USER vscode
