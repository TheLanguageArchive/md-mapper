#!/bin/bash

# Script to execute the mapper. See README.txt for information.

JAR=md-mapper-${mapper_version}.jar

y=.
for x in lib/*.jar ; do
  y=${y}:${x}
done

nice java -cp ${y} -jar ${JAR} mapper_version=${mapper_version} $*
