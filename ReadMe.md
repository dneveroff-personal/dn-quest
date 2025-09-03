# Welcome to the Quest engine!

### To run Swagger in UI run 
http://localhost:8080/swagger-ui/index.html

### Ping
http://localhost:8080/ping

### To run PgAdmin
http://localhost:5050

Email: admin@localhost.com
Password: admin

### To run Front
http://localhost:5173/


# Docker Compose support

## Commands:

### To build new jar
./gradlew bootJar

### To build new docker container
docker compose build

### To run docker 
docker compose up -d --build --remove-orphans

### To stop docker 
docker compose down --remove-orphans

### To see LOGS
docker logs -f quest-app


# Vue + Vite frontend in /frontend folder

### To build files for static to use in backend 
npm run build

### To build files static into backend directly (already setup in package.json)
npm run build:static


# MAKEFILE

### Make Docker down and up
make docker-upd

### Make docker down, build backend, docker up again
make build-back

### Make run npm front build, copy to backend and restart docker
make build-front: