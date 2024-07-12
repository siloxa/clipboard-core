#!/bin/bash
  case $1 in
    -u|up)
      docker-compose -f src/main/docker/postgresql.yml up -d
      exit 0
      ;;
    -d|down)
      docker-compose -f src/main/docker/postgresql.yml down
      exit 0
      ;;
    -r|reset)
      docker-compose -f src/main/docker/postgresql.yml down && docker-compose -f src/main/docker/postgresql.yml up -d
      exit 0
      ;;
  esac

echo "Handling docker for Project"
echo "-u | up       start docker"
echo "-d | down     destroy docker"
echo "-r | reset    destroy and recreate docker"
