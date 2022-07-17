# Minecraft Server Creator
An application for making Minecraft Server creation as easy as never.

## Mod Repositories
MSC supports downloading mods directly from the GUI, without having to download the jar files and copying them manually,
as well as resolving dependencies. The supported mod repositories are currently as follows, but feel free to open a new
issue with a new repository, or create a new PR with the repository by yourself.

[![Modrinth Supported][modrinth-badge]](https://modrinth.com/)
[![Curseforge Planned][curseforge-badge]](https://www.curseforge.com/minecraft/mc-mods)

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



[modrinth-badge]: https://img.shields.io/static/v1?style=for-the-badge&label=Modrinth&message=Supported&color=success&logo=data:image/png;base64,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
[curseforge-badge]: https://img.shields.io/static/v1?style=for-the-badge&label=Curseforge&message=Planned&color=orange&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAACXBIWXMAACdeAAAnXgHPwViOAAAAGXRFWHRTb2Z0d2FyZQB3d3cuaW5rc2NhcGUub3Jnm+48GgAAABB0RVh0VGl0bGUAQ3Vyc2VGb3JnZcZ21YQAAAMHSURBVHic7dlLqFVVHMfxj+deekhm9EASi1tEQaQg0QOVciCCg4rCQAgSs4EDCdKBSpQDaSKUBDoJ6g60IAgLcVBNgl4QUZgKTYJIcaKhdC2R9N7jYO0Dp3P3vvc81jp3R+sLC85jr9/+/f/rudcmk8lkMplMJpPJZDL/P+ZF0LgOSyLozAkxEnATLmA0gtbQaUTQ+As/R9CZE2IkAL6NpDN06pqAq/gUH+BcZO1/MYp3ccuAOgsjeGlxDM9jHd4RZ56akQ/RrEk5jztwMy4nuscEDmMLlsFSXKlB8E28VzTKYwm0j2Mj5ivhjRoE38T+ws9YRM0z2GCW+a6B92uQgBNtnr6MoHdID/NbA3swOQeB/4rPis8rCj8P4lKfelPY0W3gnazCj0NOwLgwNldjcZuXV/rUe7Xf4Fs08BS+ENbk1Ak4VuFjBL/1qPX2oMF3cjtexlFhz58qCVtL7n0Y//Sg8Quujxf6dBrC2NyEvfgEJ/W3Zl/E923frwjDr8VIoftTD/pPpAi6GxpCT7kPj2Atfigx2Cpn8AA+7vh9X4fuctytu573earg+uUl1WafK67pTMBrJTqP40/hibOWrV/FAvyt3OwNxTWPtl1zQWjtduYJQ6CJ9ap3rMcTxjEQB003OyWM7xZjeBH3ltR/uKgziTvxUYleE9uSuI/AGuWGH+qy/j34AzuL79sr9O6KZzkuDZwy3fCBPvV2lWidmLFGDXhT+TB4oQ+tr0q09saxmY77lXfbKaEnLOhSZ0OFzprIfpPwnerla0I4B9io/Hh9EXYr3xFOCocntec2YZY/ZOa1/CpeL+qsxzfKn0UmhI3U2LACiMWTutvS7p/l//FhG4/BKG7Fad0lYabyrIqjrTqz2eCBt5evh2t/cI6Im4BJYYL8TzBf9XPBIGXToMZivRmajbXSjNlnEmgmYVz81m8Kh6a1nwxHcFaaBDTx9CDmhjEEVgqvu1JR+2HwlnSt3xQel9vPF3oi+ZtX4SDjxsT3+F1YFjOZTCaTyWQymUwmk+mCayxM8Vhk0mWuAAAAAElFTkSuQmCC
