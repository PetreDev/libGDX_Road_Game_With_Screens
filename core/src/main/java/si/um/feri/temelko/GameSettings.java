package si.um.feri.temelko;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

/**
 * Manages game settings and persists them using Preferences.
 */
public class GameSettings {
    private static final String PREFS_NAME = "road_game_settings";
    private static final int MAX_LEADERBOARD_ENTRIES = 10;

    // Setting keys
    private static final String KEY_SOUND_VOLUME = "sound_volume";
    private static final String KEY_MUSIC_VOLUME = "music_volume";
    private static final String KEY_DIFFICULTY = "difficulty";
    private static final String KEY_FULLSCREEN = "fullscreen";
    private static final String KEY_SHOW_FPS = "show_fps";
    private static final String KEY_LEADERBOARD = "leaderboard";
    private static final String KEY_PLAYER_NAME = "player_name";
    
    // Default values
    private static final float DEFAULT_SOUND_VOLUME = 0.7f;
    private static final float DEFAULT_MUSIC_VOLUME = 0.5f;
    private static final int DEFAULT_DIFFICULTY = 1; // 0=Easy, 1=Normal, 2=Hard
    private static final boolean DEFAULT_FULLSCREEN = false;
    private static final boolean DEFAULT_SHOW_FPS = false;
    private static final String DEFAULT_PLAYER_NAME = "Player";

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
        prefs.putString(KEY_PLAYER_NAME, DEFAULT_PLAYER_NAME);
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

    // --- Player Name ---
    public String getPlayerName() {
        return prefs.getString(KEY_PLAYER_NAME, DEFAULT_PLAYER_NAME);
    }

    public void setPlayerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            name = DEFAULT_PLAYER_NAME;
        }
        prefs.putString(KEY_PLAYER_NAME, name.trim());
        prefs.flush();
    }

    // --- Leaderboard ---

    /**
     * Represents a single leaderboard entry with player name and score.
     */
    public static class LeaderboardEntry {
        public String name;
        public int score;
        public String difficulty;

        /** Required for JSON deserialization via reflection. */
        @SuppressWarnings("unused")
        private LeaderboardEntry() {}

        public LeaderboardEntry(String name, int score, String difficulty) {
            this.name = name;
            this.score = score;
            this.difficulty = difficulty;
        }
    }

    /**
     * Get all leaderboard entries, sorted by score descending.
     */
    public Array<LeaderboardEntry> getLeaderboard() {
        String json = prefs.getString(KEY_LEADERBOARD, "[]");
        Json jsonParser = new Json();
        try {
            @SuppressWarnings("unchecked")
            Array<LeaderboardEntry> entries = jsonParser.fromJson(Array.class, LeaderboardEntry.class, json);
            if (entries == null) {
                return new Array<>();
            }
            return entries;
        } catch (Exception e) {
            return new Array<>();
        }
    }

    /**
     * Add a new score to the leaderboard.
     * Each player name only appears once - only their best score is kept.
     */
    public void addScore(String playerName, int score) {
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = DEFAULT_PLAYER_NAME;
        }
        playerName = playerName.trim();

        Array<LeaderboardEntry> entries = getLeaderboard();

        // Check if player already exists in leaderboard
        LeaderboardEntry existingEntry = null;
        for (LeaderboardEntry entry : entries) {
            if (entry.name != null && entry.name.equalsIgnoreCase(playerName)) {
                existingEntry = entry;
                break;
            }
        }

        if (existingEntry != null) {
            // Player exists - only update if new score is higher
            if (score > existingEntry.score) {
                existingEntry.score = score;
                existingEntry.difficulty = getDifficulty().toString();
            }
        } else {
            // New player - add entry
            LeaderboardEntry newEntry = new LeaderboardEntry(
                playerName,
                score,
                getDifficulty().toString()
            );
            entries.add(newEntry);
        }

        // Sort by score descending
        entries.sort((a, b) -> Integer.compare(b.score, a.score));

        // Keep only top entries
        while (entries.size > MAX_LEADERBOARD_ENTRIES) {
            entries.removeIndex(entries.size - 1);
        }

        // Save to preferences
        Json jsonParser = new Json();
        String json = jsonParser.toJson(entries, Array.class, LeaderboardEntry.class);
        prefs.putString(KEY_LEADERBOARD, json);
        prefs.flush();
    }

    /**
     * Clear all leaderboard entries.
     */
    public void clearLeaderboard() {
        prefs.putString(KEY_LEADERBOARD, "[]");
        prefs.flush();
    }
}

