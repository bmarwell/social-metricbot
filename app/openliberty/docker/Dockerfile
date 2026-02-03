FROM icr.io/appcafe/open-liberty:kernel-slim-java25-openj9-ubi

COPY --chown=1001:0 maven/h2 /config/h2/
COPY --chown=1001:0 maven/config/* /config/

RUN features.sh

COPY --chown=1001:0 maven/*.war /config/apps/

ENV OPENJ9_SCC=true
RUN configure.sh && rm -r /output/tranlog
