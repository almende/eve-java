#!/bin/bash

mvn -P graph graph:reactor -Dhide-scope=test -Dgraph.target=graphs/full-graph.png
mvn -P graph graph:reactor -Dhide-scope=test -Dhide-transitive=true -Dgraph.target=graphs/non-transitive-graph.png
