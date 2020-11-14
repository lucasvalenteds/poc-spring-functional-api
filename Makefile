DATABASE_PORT = 27017
DATABASE_NAME = person

CONTAINER_NAME = poc_spring_declarative

provision:
	@docker run --detach \
			--publish=$(DATABASE_PORT):27017 \
			--env MONGO_INITDB_DATABASE=$(DATABASE_NAME) \
			--name $(CONTAINER_NAME) \
			mongo

destroy:
	@docker stop $(CONTAINER_NAME)
	@docker rm $(CONTAINER_NAME)

API_PORT ?= 8080
API_URL = http://localhost:$(API_PORT)

persist:
	@curl --request POST \
			--silent \
			--data '{"name":"John Smith"}' \
			--header 'Content-Type: application/json' \
			$(API_URL)/person && echo

find-all:
	@curl --request GET \
			--silent \
			--header 'Content-Type: application/json' \
			$(API_URL)/person && echo

remove:
	@curl --request DELETE \
			--verbose \
			$(API_URL)/person/$(id) && echo

