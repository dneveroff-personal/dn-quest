FRONT_DIR=frontend
BACK_DIR=src/main/resources
STATIC_DIR=$(BACK_DIR)/static

clean:
	cd $(FRONT_DIR) && rm -rf node_modules package-lock.json

build-static:
	cd $(FRONT_DIR) && npm install && npm run build
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
