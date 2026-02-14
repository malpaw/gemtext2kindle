# Lagrange Feed Fetching and Unread State Logic

This document describes the logic for fetching articles from feeds and maintaining their read/unread state, as implemented in `data/feeds.c` and `data/visited.c`.

## 1. Core Data Structures

### Feed Entries (`iFeedEntry`)
Defined in `feeds.c`, an entry represents a single article or update in a feed.
- **URL**: The unique identifier for the entry.
- **Posted**: The timestamp when the entry was published (parsed from the feed).
- **Discovered**: The timestamp when Lagrange first saw the entry.
- **Bookmark ID**: The ID of the feed (subscription) this entry belongs to.
- **Is Heading**: A flag indicating if the entry was generated from a page heading rather than a link.

### Visited URLs (`iVisitedUrl`)
Defined in `visited.c`, tracks history and unread status.
- **URL**: The visited address.
- **When**: Timestamp of the latest visit.
- **Flags**: 
    - `transient_VisitedUrlFlag`: Not stored in long-term history.
    - `kept_VisitedUrlFlag`: Prevents the entry from being purged even if it's old, as long as it's still in an active feed.

## 2. Feed Fetching and Parsing

### The Fetching Process
1.  **Subscription Listing**: The system identifies all bookmarks with the `subscribed_BookmarkFlag`.
2.  **Job Queue**: A `FeedJob` is created for each subscription.
3.  **Concurrent Requests**: Up to 10 concurrent Gemini requests (`GmRequest`) are processed.

### Parsing Logic (`parseResult_FeedJob_`)
Lagrange supports two ways of discovering entries in a Gemtext file:

1.  **Link-based Entries**:
    - Scans for lines starting with `=>`.
    - Looks for a date pattern: `YYYY-MM-DD`.
    - Format: `=> URL YYYY-MM-DD Title`
    - The date is captured as the `posted` time (set to noon UTC).

2.  **Heading-based Entries**:
    - If the bookmark has the `headings_BookmarkFlag` enabled, every heading line (`#`, `##`, etc.) is treated as a feed entry.
    - The URL is constructed as `FeedURL#URL_Encoded_Heading_Text`.
    - These are useful for tracking updates to a single page that doesn't use standard Gemini feed links.

## 3. Unread State Logic

The unread state is determined by the `isUnread_FeedEntry` function in `feeds.c`. There are two distinct algorithms:

### Algorithm A: Standard Link Entries
If the URL does **not** contain a fragment (`#`):
- **Unread** if the URL is **not present** in the `visited` database.
- **Read** if the URL **is present** in the `visited` database.

### Algorithm B: Heading/Fragment Entries
If the URL **contains** a fragment (`#`):
- The system strips the fragment to get the base page URL.
- It retrieves the `visTime` (last visit time) for that base URL from the `visited` database.
- **Unread** if `visTime` is **earlier** than the entry's `posted` time.
- **Read** if `visTime` is **later** or equal to the entry's `posted` time.
- *Note*: If the base URL hasn't been visited, `visTime` is zero, making the entry unread.

## 4. State Maintenance

### Updating Entries (`updateEntries_Feeds_`)
- When new entries are fetched, they are merged into the global `entries` array.
- If a URL is already known but its title or date has changed, it is updated, and the URL is **removed from the visited database** to mark it as unread again.
- **Kept Status**: For every entry found in a feed, the `setUrlKept_Visited` function is called to set the `kept_VisitedUrlFlag`. This ensures the read/unread status is preserved even for old entries that would otherwise be purged from history.

### Cleanup and Persistence
- **Max Age**: Standard history entries are purged after 6 months (`maxAge_Visited`), unless the `kept` flag is set.
- **Persistence**:
    - `feeds.txt`: Stores the list of discovered entries, including discovery/posted timestamps and source bookmark IDs.
    - `visited.2.txt`: Stores the visit history and flags.
- **Syncing Kept Flags**: After a feed refresh, the system iterates through all "kept" visited URLs. If a URL is no longer found in any active feed entry, the `kept` flag is cleared, allowing it to eventually expire from history.
