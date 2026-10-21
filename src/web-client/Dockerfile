# syntax=docker/dockerfile:1

#
# Builds the web client's image, from this directory alone.
#
# See: docs > decisions > 0016.
#

# Build stage. Compiles the TypeScript.
FROM node:24-trixie-slim AS build

WORKDIR /build

COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci

COPY tsconfig.json ./
COPY src ./src
RUN npm run build

# Runtime stage. Production dependencies and compiled code only.
FROM node:24-trixie-slim

ENV NODE_ENV=production

WORKDIR /app
COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci --omit=dev

COPY --from=build /build/dist ./dist

# The `node` user (uid 1000) comes with the base image.
USER 1000:1000
EXPOSE 3000

CMD ["node", "dist/server.js"]
