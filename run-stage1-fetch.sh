#!/bin/bash

# Configuration
FEEDS_TXT="data/feeds.txt"
VISITED_TXT="data/visited.2.txt"
BOOKMARKS_INI="data/bookmarks.ini"
Q1="queue/01-downloaded"

CP="target/gemtext2kindle-1.0-SNAPSHOT.jar:$(mvn dependency:build-classpath | grep -v '\[INFO\]' | tr '\n' ':')"

# Stage 1: Fetch
echo "--- Stage 1: Fetching ---"
java -cp "$CP" com.example.gemtext2kindle.stage1.FetcherMain "$FEEDS_TXT" "$VISITED_TXT" "$Q1" "$BOOKMARKS_INI"
