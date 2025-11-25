package si.um.feri.temelko;

import com.badlogic.gdx.Preferences;

/**
 * Manages game settings and persists them using Preferences.
 */
public class GameSettings {
    private static final String PREFS_NAME = "road_game_settings";

    // Setting keys
    private static final String KEY_SOUND_VOLUME = "sound_volume";
    private static final String KEY_MUSIC_VOLUME = "music_volume";
    private static final String KEY_DIFFICULTY = "difficulty";
    private static final String KEY_FULLSCREEN = "fullscreen";
    private static final String KEY_SHOW_FPS = "show_fps";
    
    // Default values
    private static final float DEFAULT_SOUND_VOLUME = 0.7f;
    private static final float DEFAULT_MUSIC_VOLUME = 0.5f;
    private static final int DEFAULT_DIFFICULTY = 1; // 0=Easy, 1=Normal, 2=Hard
    private static final boolean DEFAULT_FULLSCREEN = false;
    private static final boolean DEFAULT_SHOW_FPS = false;

    private final Preferences prefs;

    public enum Difficulty {
        EASY("Easy"),
        NORMAL("Normal"),
        HARD("Hard");

        private final String displayName;

        Difficulty(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public float getSpeedMultiplier() {
            return switch (this) {
                case EASY -> 0.8f;
                case NORMAL -> 1.0f;
                case HARD -> 1.3f;
            };
        }

        public float getObstacleSpawnRate() {
            return switch (this) {
                case EASY -> 1.5f; // slower spawn
                case NORMAL -> 1.0f;
                case HARD -> 0.7f; // faster spawn
            };
        }
    }

    public GameSettings() {
        prefs = com.badlogic.gdx.Gdx.app.getPreferences(PREFS_NAME);
        // Load defaults if preferences are empty
        if (!prefs.contains(KEY_SOUND_VOLUME)) {
            resetToDefaults();
        }
    }

    public void resetToDefaults() {
        prefs.putFloat(KEY_SOUND_VOLUME, DEFAULT_SOUND_VOLUME);
        prefs.putFloat(KEY_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME);
        prefs.putInteger(KEY_DIFFICULTY, DEFAULT_DIFFICULTY);
        prefs.putBoolean(KEY_FULLSCREEN, DEFAULT_FULLSCREEN);
        prefs.putBoolean(KEY_SHOW_FPS, DEFAULT_SHOW_FPS);
        prefs.flush();
    }

    public float getSoundVolume() {
        return prefs.getFloat(KEY_SOUND_VOLUME, DEFAULT_SOUND_VOLUME);
    }

    public void setSoundVolume(float volume) {
        prefs.putFloat(KEY_SOUND_VOLUME, Math.max(0f, Math.min(1f, volume)));
        prefs.flush();
    }
    
    public float getMusicVolume() {
        return prefs.getFloat(KEY_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME);
    }
    
    public void setMusicVolume(float volume) {
        prefs.putFloat(KEY_MUSIC_VOLUME, Math.max(0f, Math.min(1f, volume)));
        prefs.flush();
    }
    
    public Difficulty getDifficulty() {
        int difficultyIndex = prefs.getInteger(KEY_DIFFICULTY, DEFAULT_DIFFICULTY);
        Difficulty[] difficulties = Difficulty.values();
        if (difficultyIndex >= 0 && difficultyIndex < difficulties.length) {
            return difficulties[difficultyIndex];
        }
        return Difficulty.NORMAL;
    }

    public void setDifficulty(Difficulty difficulty) {
        prefs.putInteger(KEY_DIFFICULTY, difficulty.ordinal());
        prefs.flush();
    }

    public boolean isFullscreen() {
        return prefs.getBoolean(KEY_FULLSCREEN, DEFAULT_FULLSCREEN);
    }

    public void setFullscreen(boolean fullscreen) {
        prefs.putBoolean(KEY_FULLSCREEN, fullscreen);
        prefs.flush();
    }

    public boolean isShowFps() {
        return prefs.getBoolean(KEY_SHOW_FPS, DEFAULT_SHOW_FPS);
    }

    public void setShowFps(boolean showFps) {
        prefs.putBoolean(KEY_SHOW_FPS, showFps);
        prefs.flush();
    }

    public void save() {
        prefs.flush();
    }
}

