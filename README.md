# Road Game

A 2D road survival game built with [libGDX](https://libgdx.com/), featuring car controls, shooting mechanics, power-ups, and difficulty settings.

## Overview

Road Game is an endless runner-style game where you control a car on a road. Navigate obstacles, collect fuel to restore health, grab power-ups for invincibility, and shoot bullets to destroy obstacles. The game speed increases over time, making survival increasingly challenging.

## Features

### Gameplay
- **Car Controls**: Move left/right using arrow keys
- **Shooting**: Press SPACE to shoot bullets that destroy obstacles
- **Health System**: Start with 100 HP; collisions reduce health by 20
- **Fuel Collection**: Collect fuel items to restore 10 HP (max 100)
- **Power-ups**: Temporary invincibility (3 seconds) to pass through obstacles safely
- **Dynamic Difficulty**: Game speed increases permanently after each crash
- **Scoring**: Earn points by collecting fuel (+5) and destroying obstacles with bullets (+10)

### Game Screens
- **Intro Screen**: Animated introduction sequence
- **Main Menu**: Navigate to Play, Settings, or Quit
- **Settings Screen**: Customize game preferences
- **Game Screen**: Main gameplay with health bar, score, and speed indicator

### Settings
- **Difficulty Levels**: Easy, Normal, Hard (affects speed multiplier and obstacle spawn rate)
- **Sound Volume**: Adjust sound effect volume (0.0 - 1.0)
- **Music Volume**: Adjust background music volume (0.0 - 1.0)
- **Fullscreen Mode**: Toggle between windowed and fullscreen
- **FPS Display**: Show/hide frames per second counter

### Technical Features
- Persistent settings storage using Preferences API
- Background music with looping support
- Sound effects for various game events
- Scrolling background animation
- Resource management with AssetManager

## Project Structure

- `core/`: Main module with shared game logic
  - `si/um/feri/temelko/`: Main game package
    - `RoadGame.java`: Main game class and asset management
    - `GameScreen.java`: Core gameplay implementation
    - `MenuScreen.java`: Main menu UI
    - `IntroScreen.java`: Introduction animation
    - `SettingsScreen.java`: Settings configuration UI
    - `GameSettings.java`: Settings management and persistence
- `lwjgl3/`: Desktop launcher for Windows/Linux/Mac
- `assets/`: Game resources (images, sounds, UI skins)

## Requirements

- **Java**: Version 17 or higher
- **Gradle**: Included wrapper (gradlew/gradlew.bat)
- **libGDX**: Managed via Gradle dependencies

## Building and Running

### Using Gradle Wrapper

**Build the project:**
```bash
# Windows
gradlew.bat build

# Linux/Mac
./gradlew build
```

**Run the game:**
```bash
# Windows
gradlew.bat lwjgl3:run

# Linux/Mac
./gradlew lwjgl3:run
```

**Create a runnable JAR:**
```bash
# Windows
gradlew.bat lwjgl3:jar

# Linux/Mac
./gradlew lwjgl3:jar
```

The JAR file will be created at `lwjgl3/build/libs/`.

### Useful Gradle Tasks

- `--continue`: Continue building even if errors occur
- `--daemon`: Use Gradle daemon for faster builds
- `--offline`: Use cached dependencies only
- `--refresh-dependencies`: Force refresh of all dependencies
- `clean`: Remove build folders
- `test`: Run unit tests (if any)

## Controls

### In-Game
- **Left Arrow** / **A**: Move car left
- **Right Arrow** / **D**: Move car right
- **Space**: Shoot bullet
- **ESC**: Return to main menu
- **R**: Restart game (when game over)

### Menu Navigation
- Mouse click on buttons
- Keyboard navigation (if supported)

## Assets

Game assets are located in the `assets/` directory:
- `images/`: Sprites (car, obstacles, fuel, power-ups, bullets, background)
- `sounds/`: Sound effects and background music
- `skins/`: UI skin files for Scene2D

## Development

This project was generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff) template.

### Project Configuration

- **Source Compatibility**: Java 17
- **Desktop Platform**: LWJGL3
- **Window Size**: Default 1024x768 (configurable in settings)

## License

This project is part of the Computer Game Development course (Task 4).
