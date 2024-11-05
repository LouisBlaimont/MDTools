# Stage 1: Build the SvelteKit app
FROM node:18 AS build

# Set working directory in the container
WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm install

# Copy the entire SvelteKit project
COPY . ./

# Build the application
RUN npm run build

# Stage 2: Create a minimal container for the production server
FROM node:18-slim AS runner

# Set the UID and GID
ENV USER_ID=2000
ENV GROUP_ID=2000

# Switch to the non-root user
USER ${USER_ID}:${GROUP_ID}

# Set working directory in the container
WORKDIR /app


# Copy the built application and node_modules from the build stage
COPY --from=build /app/build ./build
COPY --from=build /app/node_modules ./node_modules
COPY --from=build /app/package*.json ./

# Expose the port SvelteKit uses by default
EXPOSE 3000

# Start the SvelteKit server
CMD ["node", "./build/index.js"]
