#!/usr/bin/env bash

java -jar target/jira-cli-1.0-SNAPSHOT-jar-with-dependencies.jar --action get-e2es --source "$1" --recursive