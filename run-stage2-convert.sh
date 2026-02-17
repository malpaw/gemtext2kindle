#!/bin/bash

# Configuration
Q1="queue/01-downloaded"
Q2="queue/02-epubs"
ARCHIVE_GMI="archive/processed-gmi"

CP="target/gemtext2kindle-1.0-SNAPSHOT.jar:$(mvn dependency:build-classpath | grep -v '\[INFO\]' | tr '\n' ':')"

# Stage 2: Convert
echo "--- Stage 2: Converting ---"
java -cp "$CP" com.example.gemtext2kindle.stage2.ConverterMain "$Q1" "$Q2" "$ARCHIVE_GMI"
