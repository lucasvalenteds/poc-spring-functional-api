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
