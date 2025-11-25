package si.um.feri.temelko;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class RoadGame extends Game {
    private SpriteBatch batch;
    private ShapeRenderer renderer;
    private AssetManager assetManager;
    private GameSettings settings;
    private Music backgroundMusic;
    private boolean musicLoaded = false;
    private String musicPath = null;

    @Override
    public void create() {
        batch = new SpriteBatch();
        renderer = new ShapeRenderer();
        assetManager = new AssetManager();
        settings = new GameSettings();

        // Load all assets
        loadAssets();

        // Wait for assets to finish loading
        assetManager.finishLoading();

        // Load music if available
        if (musicLoaded && musicPath != null) {
            try {
                backgroundMusic = assetManager.get(musicPath, Music.class);
                if (backgroundMusic != null) {
                    backgroundMusic.setLooping(true);
                    backgroundMusic.setVolume(settings.getMusicVolume());
                }
            } catch (Exception e) {
                // Music not available
                musicLoaded = false;
                backgroundMusic = null;
                musicPath = null;
            }
        }

        // Apply fullscreen setting
        if (settings.isFullscreen() != com.badlogic.gdx.Gdx.graphics.isFullscreen()) {
            if (settings.isFullscreen()) {
                com.badlogic.gdx.Gdx.graphics.setFullscreenMode(com.badlogic.gdx.Gdx.graphics.getDisplayMode());
            } else {
                com.badlogic.gdx.Gdx.graphics.setWindowedMode(1024, 768);
            }
        }

        // Start with IntroScreen
        setScreen(new IntroScreen(this));
    }

    private void loadAssets() {
        // Load images
        assetManager.load("images/car.png", Texture.class);
        assetManager.load("images/obstacle.png", Texture.class);
        assetManager.load("images/fuel.png", Texture.class);
        assetManager.load("images/power-up.png", Texture.class);
        assetManager.load("images/bullet.png", Texture.class);
        assetManager.load("images/background.png", Texture.class);

        // Load sounds
        assetManager.load("sounds/crash.wav", Sound.class);
        assetManager.load("sounds/power-up.wav", Sound.class);
        assetManager.load("sounds/collect.wav", Sound.class);
        assetManager.load("sounds/shoot.wav", Sound.class);

        // Load background music (optional - file may not exist)
        // Try to load OGG first (better for music), fallback to MP3
        if (com.badlogic.gdx.Gdx.files.internal("sounds/background-music.ogg").exists()) {
            this.musicPath = "sounds/background-music.ogg";
            musicLoaded = true;
        } else if (com.badlogic.gdx.Gdx.files.internal("sounds/background-music.mp3").exists()) {
            this.musicPath = "sounds/background-music.mp3";
            musicLoaded = true;
        }

        if (musicLoaded && this.musicPath != null) {
            assetManager.load(this.musicPath, Music.class);
        }

        // Load skin
        SkinLoader.SkinParameter params = new SkinLoader.SkinParameter("skins/mySkin/star-soldier-ui.atlas");
        assetManager.load("skins/mySkin/star-soldier-ui.json", Skin.class, params);
    }

    @Override
    public void dispose() {
        // Stop and dispose music first
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic = null;
        }
        // Dispose all resources
        batch.dispose();
        renderer.dispose();
        // AssetManager disposes all assets loaded through it (Textures, Sounds, Music, Skin, etc.)
        assetManager.dispose();
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public ShapeRenderer getRenderer() {
        return renderer;
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    /**
     * Creates a default font for game UI. This is created here since
     * default BitmapFont doesn't require file loading.
     */
    public BitmapFont createDefaultFont() {
        return new BitmapFont();
    }

    public GameSettings getSettings() {
        return settings;
    }

    /**
     * Starts playing background music if available.
     */
    public void playMusic() {
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.setVolume(settings.getMusicVolume());
            backgroundMusic.play();
        }
    }

    /**
     * Stops background music.
     */
    public void stopMusic() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.stop();
        }
    }

    /**
     * Updates music volume based on settings.
     */
    public void updateMusicVolume() {
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(settings.getMusicVolume());
        }
    }

}
