.PHONY: build

.DEFAULT_GOAL = help

## Runs build and generates pack
build:
	sbt clean compile test pack

## Runs postgress container
run-postgres:
	docker run -p 5432:5432 -e POSTGRES_USER=admin postgres:9.5

## Runs server
run-app:
	sh target/pack/bin/run-service.sh ./src/main/resources/config.yaml

## Show help screen.
help:
	@echo "Please use \`make <target>' where <target> is one of\n\n"
	@awk '/^[a-zA-Z\-\_0-9]+:/ { \
		helpMessage = match(lastLine, /^## (.*)/); \
		if (helpMessage) { \
			helpCommand = substr($$1, 0, index($$1, ":")-1); \
			helpMessage = substr(lastLine, RSTART + 3, RLENGTH); \
			printf "%-30s %s\n", helpCommand, helpMessage; \
		} \
	} \
	{ lastLine = $$0 }' $(MAKEFILE_LIST)
