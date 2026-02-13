#!/bin/bash

# Configuration
FEEDS_TXT="data/feeds.txt"
Q1="queue/01-downloaded"
Q2="queue/02-epubs"
ARCHIVE_GMI="archive/processed-gmi"
ARCHIVE_EPUB="archive/sent-epubs"

# SMTP Configuration (Set these or use env vars)
SMTP_HOST=${SMTP_HOST:-"smtp.gmail.com"}
SMTP_PORT=${SMTP_PORT:-"587"}
SMTP_USER=${SMTP_USER:-"user@example.com"}
SMTP_PASS=${SMTP_PASS:-"password"}
KINDLE_EMAIL=${KINDLE_EMAIL:-"mykindle@kindle.com"}

# Build the project
mvn clean package -DskipTests

CP="target/gemtext2kindle-1.0-SNAPSHOT.jar:$(mvn dependency:build-classpath | grep -v '\[INFO\]' | tr '\n' ':')"

# Stage 1: Fetch
echo "--- Stage 1: Fetching ---"
java -cp "$CP" com.example.gemtext2kindle.stage1.FetcherMain "$FEEDS_TXT" "$Q1"

# Stage 2: Convert
echo "--- Stage 2: Converting ---"
java -cp "$CP" com.example.gemtext2kindle.stage2.ConverterMain "$Q1" "$Q2" "$ARCHIVE_GMI"

# Stage 3: Send
echo "--- Stage 3: Sending ---"
java -cp "$CP" com.example.gemtext2kindle.stage3.MailerMain "$Q2" "$ARCHIVE_EPUB" "$SMTP_HOST" "$SMTP_PORT" "$SMTP_USER" "$SMTP_PASS" "$KINDLE_EMAIL" "New Gemini Articles"

echo "--- Finished ---"
