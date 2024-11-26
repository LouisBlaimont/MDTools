# Start from the official PostgreSQL image
#FROM postgres:17.0
FROM gitlab.uliege.be:5050/speam/2024-2025/team3/mdtools/postgres_ghcr_cache:17.0


# Set the UID and GID
ENV USER_ID=1000
ENV GROUP_ID=1000

ARG HOMEDIR="/home/postgres_user"
ENV PGDATA="/home/postgres_user/data"

# Install necessary packages and create a non-root user and group with the specified UID:GID
# RUN groupadd -g ${GROUP_ID} postgres_group
# RUN useradd -m -u ${USER_ID} -g postgres_group -s /bin/bash postgres
RUN mkdir -p ${PGDATA}
# RUN chown -R postgres_user:postgres_group ${HOMEDIR}
RUN chown -R ${USER_ID}:${GROUP_ID} ${HOMEDIR}

USER ${USER_ID}:${GROUP_ID}


COPY *.sql /docker-entrypoint-initdb.d/

# Switch to the non-root user for running PostgreSQL
# USER postgres_user:postgres_group

# Run PostgreSQL
CMD ["postgres"]