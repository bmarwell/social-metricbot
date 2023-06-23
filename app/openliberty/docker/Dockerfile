FROM open-liberty:kernel-slim-java17-openj9

COPY --chown=1001:0 maven/h2 /config/h2/
COPY --chown=1001:0 maven/config/* /config/

RUN features.sh

ARG ARTIFACT_NAME
COPY --chown=1001:0 maven/*.war /config/apps/


ENV OPENJ9_SCC=true
RUN configure.sh
