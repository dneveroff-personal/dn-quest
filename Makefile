FRONT_DIR=frontend
BACK_DIR=src/main/resources
STATIC_DIR=$(BACK_DIR)/static

# Принудительно используем Node.js v24
NODE_CMD = cd $(FRONT_DIR) && . ~/.nvm/nvm.sh && nvm use 24 &&

clean:
	docker compose down --remove-orphans
	cd $(FRONT_DIR) && rm -rf node_modules package-lock.json

build-static:
	$(NODE_CMD) npm install && npm run build
	rm -rf $(STATIC_DIR)
	mkdir -p $(STATIC_DIR)
	cp -r $(FRONT_DIR)/dist/* $(STATIC_DIR)/

docker-upd:
	docker compose down --remove-orphans
	docker compose up -d --build --remove-orphans

build-back:
	docker compose down --remove-orphans
	./gradlew build
	docker compose up -d --build --remove-orphans

build-front: build-static docker-upd

all: clean build-static build-back
