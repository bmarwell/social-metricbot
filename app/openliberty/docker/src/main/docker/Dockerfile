FROM openliberty:21.0.0.11-kernel-slim-java17-openj9

COPY --chown=1001:0 config/jvm.options config/server.xml config/bootstrap.properties /config/

RUN features.sh

ARG ARTIFACT_NAME
COPY --chown=1001:0 ${ARTIFACT_NAME} /config/apps/${ARTIFACT_NAME}
COPY --chown=1001:0 h2 /config/

ENV OPENJ9_SCC=true
RUN configure.sh
