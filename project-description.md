# Vision

Let's plan a new exciting project. We'll use this file to store our plan.
First, a little bit of context. There are a protocol and file format, both called Gemini. It's something between Gopher and a simple HTML/HTTP. And there is a whole vibrant community using it as a web alternative. Basically, peope host their blogs just like in the old good days.
There is also a browser for Gemini protocol/format: Lagrange. Lagrange can bookmark sites and subscribe to feeds.

The goal of this project is to take the feeds.txt file (look in data/ folder), check the latest posts and deliver them to my ebook reader in EPUB format.

We will use simple tools: Java, some Bash scripts as orchestrators, minimal set of libraries. We will rely on unit and integration tests (TDD is important here, start with tests!). We will split the project into stages, each stage exchanging data with the next one using text files (a separate folder for each channel /queue).

The stages will be:
1. Get the list of feeds from the feeds.txt, download the newest articles from each of the feeds, store them in a file queue in .gmi format, mark the feeds as read in feeds.txt. If downloading article(s) was not possible for any reason, keep that feed "unread".
2. For each .gmi file from the queue, convert it into HTML and assemble an EPUB file. Store it to the next queue. Move .gmi to a "processed" folder once processed successfully.
3. For each .epub file from the queue, send it to a given email addres (my Kindle inbox). Move that .ebup file to a "processed" folder once email sent successfully.

The project will run in a container on my NAS, with the Lagrange feed file synchronized to it from my computer (I will take care about the synchronization myself - just assume the file is there).
Describe the project as best as you can below. We will execute it later together, implementing and testing one stage at a time and moving to the next stage only when the current one is thoroughly tested and working fine.

## Project Plan: gemtext2kindle

### 1. Overview
The project is a multi-stage pipeline designed to bridge the Gemini/Gopher "smolweb" with modern ebook readers (Kindle). It automates the discovery of new content from a Lagrange-compatible `feeds.txt`, converts it into the Kindle-friendly EPUB format, and delivers it via email.

### 2. Technology Stack
*   **Language:** Java 17+ (Core logic, parsing, and conversion).
*   **Orchestration:** Bash scripts (Stage transitions and file management).
*   **Build Tool:** Maven (Minimal dependencies).
*   **Key Libraries:**
    *   `JUnit 5` & `AssertJ` for TDD.
    *   Simple Socket programming for Gemini/Gopher.
    *   Manual EPUB assembly (OEBPS structure + ZIP compression).
    *   `Jakarta Mail` for Kindle inbox delivery.
*   **Deployment:** Docker (Alpine-based for NAS compatibility).

### 3. Data Flow & Queue Structure
We will use a directory-based queue system to decouple stages:
*   `data/feeds.txt`: Source of truth for subscriptions and read state.
*   `queue/01-downloaded/`: New `.gmi` or `.txt` files from feeds.
*   `queue/02-epubs/`: Assembled `.epub` files ready for sending.
*   `archive/processed-gmi/`: History of downloaded articles.
*   `archive/sent-epubs/`: History of delivered ebooks.

### 4. Implementation Roadmap
1.  **Stage 1: The Fetcher**
    *   Parse `feeds.txt` to identify unread entries.
    *   Implement Gemini (TLS) and Gopher (Plaintext) protocol handlers.
    *   Download content, store in `queue/01-downloaded/`.
    *   Update `feeds.txt` read status ONLY on successful download.
2.  **Stage 2: The Converter**
    *   Parse Gemtext (`.gmi`) and convert to basic HTML5.
    *   Generate necessary EPUB metadata and structure.
    *   Bundle into EPUB and move to `queue/02-epubs/`.
    *   Move source files to `archive/processed-gmi/`.
3.  **Stage 3: The Deliverer**
    *   Pick up EPUBs from `queue/02-epubs/`.
    *   Send to Kindle email address via SMTP.
    *   Move files to `archive/sent-epubs/` on success.

### 5. Success Criteria
*   Integration test: Mock Gemini/Gopher server → Pipeline → Mock SMTP server.
*   Update to `feeds.txt` is atomic and robust.
*   EPUBs pass validation and are readable on a Kindle device.
