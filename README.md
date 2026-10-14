# aa_news

Google Digital Workshop - Android Advanced - News

MVVM, rxJava, retrofit, room and https://newsapi.org/

## Features

- **My News**: the latest articles about the topics you follow (add or remove them with the chips at the top).
  Results are cached, so the last ones are available offline.
- **Headlines**: top headlines for a country (Settings, defaults to the device country) and a category.
- **Bookmarks**: saved articles, stored locally. Removing one can be undone from the snackbar.
- **Search**: search all articles, sorted by date, relevance or popularity.
- Infinite scroll for headlines and search results.
- Articles open in an in-app browser, where they can be shared or bookmarked.
- Swipe an article to share or bookmark it, or swipe between tabs (Settings).
- Light / dark / system theme.

## Setup

1. Get a free API key at https://newsapi.org/register
2. Add it to `local.properties` in the project root (this file is not committed):

   ```properties
   newsApiKey=your_key_here
   ```

   The key can also be passed in the `NEWS_API_KEY` environment variable, e.g. on CI.
3. Open the project in Android Studio and run the `app` configuration.

Requirements: JDK 17+, Android SDK 36. The build uses Gradle 8.14 and Android Gradle Plugin 8.13.

## Tests

```shell
./gradlew testDebugUnitTest            # JVM unit tests
./gradlew connectedDebugAndroidTest    # instrumented tests (Room migrations), needs a device or emulator
```

## Notes

- The free NewsAPI (Developer) plan returns at most 100 results per query, allows 100 requests per day and
  is meant for development only.
- NewsAPI doesn't have top headlines for every country; if a country shows nothing, pick another one in Settings.
