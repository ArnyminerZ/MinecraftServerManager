# Minecraft Server Creator
An application for making Minecraft Server creation as easy as never.

# Development
## Localization
### Initialization
Strings are localized using the `LangManager` class, which loads them, and helps to select the desired language.

First, you have to initialize the class with `LangManager.initialize`, which asks for a fallback language, in case of
trying to select a non-existing language; and a language to select at the moment, which by default is set to the
fallback language. For example, you can initialize the `LangManager` class with:
```kotlin
LangManager.initialize(Locale.ENGLISH)
```
Note that in this example we are trying to load the English language. For this to work correctly we need to have a
directory called `lang` in our resources' path, and inside a JSON file with the language code as name, in this case
`en`. Take into account the language you are passing when selecting one, since if you have initialized the `LangManager`
class with country code, for example:
```kotlin
LangManager.initialize(Locale("en-US"))
```
The file should be named `en-US.json` instead of `en.json`.

Once you have this ready, the `LangManager.initialize` method has already loaded the contents of the file, and are
available through the read functions.

## Fetching
To get a string from the resources' directory you can call the static method called `getString`, which requires a key,
and accepts arguments. For example, if you have the following `en.json` file in the `resources/lang` directory:
```json
{
  "hello-world": "Hello world!",
  "formatted": "Welcome %s!",
  "formatted-int": "You have %d points."
}
```
You first have to initialize it as seen before with:
```kotlin
LangManager.initialize(Locale.ENGLISH)
```
And now you are ready to fetch strings, for example, the following is returned from these calls:
```kotlin
// Getting raw strings
getString("hello-world")             // -> "Hello world!"
getString("formatted")               // -> "Welcome %s!"

// Getting formatted strings
getString("formatted", "User")       // -> "Welcome User!"
getString("formatted", "New Player") // -> "Welcome New Player!"
getString("formatted-int", 10)       // -> "You have 10 points."

// Invalid keys
getString("non-existing")            // throws IllegalArgumentException
```
If you don't initialize the `LangManager` class, or try to load strings from an empty or non-existing language,
`IllegalStateException` is thrown on the call of `getString`.
