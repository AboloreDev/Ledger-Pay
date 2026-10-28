.PHONY: help build run test package clean docker-up docker-down

help: 
	@echo "Available commands:"
	@echo "  build       Build the server binary"
	@echo "  run         Run the server"
	@echo "  docker-up   Start running the container"
	@echo "  docker-down   Rollback running of the container"

build: 
	./mvnw compile

run: 
	./mvnw spring-boot:run

test:
	./mvnw test

test-one:
	./mvnw -Dtest=$(TEST) test

# test-one:
#   	@test -n "$(TEST)" || (echo "Usage: make test-one TEST=ClassName" && exit 1)
#   	./mvnw -Dtest=$(TEST) test

package:
	./mvnw clean package -DskipTests

clean:
	./mvnw clean


docker-up:
	docker-compose -p ledger_pay --env-file .env -f docker/docker-compose.yml up -d

docker-down:
	docker-compose -p ledger_pay --env-file .env -f docker/docker-compose.yml down

db-check:
	docker compose -p ledger_pay -f docker/docker-compose.yml exec postgres psql -U ledger_pay -d ledger_pay_db -c "SELECT current_database(), current_user;"

db-describe:
	docker compose -p ledger_pay -f docker/docker-compose.yml exec postgres psql -U ledger_pay -d ledger_pay_db -c "\d $(TABLE)"

db-shell:
	docker compose -p ledger_pay -f docker/docker-compose.yml exec postgres psql -U ledger_pay -d ledger_pay_db