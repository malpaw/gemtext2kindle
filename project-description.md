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

TODO: Add a proper project plan, technology description and other importand details below.
