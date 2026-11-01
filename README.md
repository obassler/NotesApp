# NotesApp

Simple Android notes app written in Kotlin with Jetpack Compose.

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack-Compose-4285F4?logo=jetpackcompose&logoColor=white)

> School project, University of Ostrava.

## Features

- Add and delete notes
- Reorder notes
- Notes persist between launches (SharedPreferences + kotlinx.serialization JSON)
- ViewModel-based state, Material 3 theme

## Structure

```
app/src/main/java/com/osu/notes/
├── MainActivity.kt
├── Note.kt            # data model
├── NoteManager.kt     # ViewModel: add / delete / move, load & save
├── NoteListScreen.kt  # Compose UI
└── ui/theme/
```

## Running

Open the project in Android Studio and run the `app` configuration on an emulator or device.
