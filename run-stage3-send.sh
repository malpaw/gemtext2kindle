#!/bin/bash

# Configuration
Q2="queue/02-epubs"
ARCHIVE_EPUB="archive/sent-epubs"

# SMTP Configuration (Set these or use env vars)
SMTP_HOST=${SMTP_HOST:-"smtp.gmail.com"}
SMTP_PORT=${SMTP_PORT:-"587"}
SMTP_USER=${SMTP_USER:-"user@example.com"}
SMTP_PASS=${SMTP_PASS:-"password"}
KINDLE_EMAIL=${KINDLE_EMAIL:-"mykindle@kindle.com"}

CP="target/gemtext2kindle-1.0-SNAPSHOT.jar:$(mvn dependency:build-classpath | grep -v '\[INFO\]' | tr '\n' ':')"

# Stage 3: Send
echo "--- Stage 3: Sending ---"
java -cp "$CP" com.example.gemtext2kindle.stage3.MailerMain "$Q2" "$ARCHIVE_EPUB" "$SMTP_HOST" "$SMTP_PORT" "$SMTP_USER" "$SMTP_PASS" "$KINDLE_EMAIL" "New Gemini Articles"
