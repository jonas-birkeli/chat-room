#!/bin/bash

mvn clean install -U exec:java -Dexec.mainClass="clientside.backend.Client"