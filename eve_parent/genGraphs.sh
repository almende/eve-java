#!/bin/bash

mvn -P graph graph:reactor -Dhide-scope=test -Dgraph.target=full-graph.png
mvn -P graph graph:reactor -Dhide-scope=test -Dhide-transitive=true -Dgraph.target=non-transitive-graph.png
