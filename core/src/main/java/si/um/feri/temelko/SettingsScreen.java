package si.um.feri.temelko;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class SettingsScreen extends ScreenAdapter {
    private final RoadGame game;
    private final AssetManager assetManager;
    private final GameSettings settings;
    private Viewport viewport;
    private Stage stage;
    private Skin skin;
    private Texture background;

    private Slider soundVolumeSlider;
    private Label soundVolumeLabel;
    private Slider musicVolumeSlider;
    private Label musicVolumeLabel;
    private SelectBox<GameSettings.Difficulty> difficultySelectBox;
    private CheckBox fullscreenCheckBox;
    private CheckBox showFpsCheckBox;

    public SettingsScreen(RoadGame game) {
        this.game = game;
        this.assetManager = game.getAssetManager();
        this.settings = game.getSettings();
    }

    @Override
    public void show() {
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage = new Stage(viewport, game.getBatch());

        skin = assetManager.get("skins/mySkin/star-soldier-ui.json", Skin.class);
        background = assetManager.get("images/background.png", Texture.class);

        stage.addActor(createUi());

        // Start/resume background music in settings
        game.playMusic();

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 0f);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void hide() {
        // Save settings when leaving screen
        settings.save();
        // Reset input processor to prevent dangling references
        Gdx.input.setInputProcessor(null);
        dispose();
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        // Don't dispose skin or background - they're managed by AssetManager
        // Viewport is just a data structure and doesn't need disposal
    }

    private Actor createUi() {
        Table table = new Table();
        table.defaults().pad(10);
        table.setBackground(new TextureRegionDrawable(background));

        Label titleLabel = new Label("Settings", skin, "title");
        titleLabel.setFontScale(1.8f);

        Table settingsTable = new Table();
        settingsTable.defaults().pad(10).left();

        // Sound Volume
        Label soundLabel = new Label("Sound Volume:", skin);
        soundLabel.setFontScale(1.1f);
        settingsTable.add(soundLabel).width(180);

        soundVolumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
        soundVolumeSlider.setValue(settings.getSoundVolume());
        soundVolumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float volume = soundVolumeSlider.getValue();
                settings.setSoundVolume(volume);
                updateSoundVolumeLabel(volume);
            }
        });
        settingsTable.add(soundVolumeSlider).width(280).padLeft(15);

        soundVolumeLabel = new Label(String.format("%.0f%%", settings.getSoundVolume() * 100), skin);
        soundVolumeLabel.setFontScale(1f);
        settingsTable.add(soundVolumeLabel).width(80).padLeft(10);
        settingsTable.row();

        // Music Volume
        Label musicLabel = new Label("Music Volume:", skin);
        musicLabel.setFontScale(1.1f);
        settingsTable.add(musicLabel).width(180);

        musicVolumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
        musicVolumeSlider.setValue(settings.getMusicVolume());
        musicVolumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float volume = musicVolumeSlider.getValue();
                settings.setMusicVolume(volume);
                game.updateMusicVolume();
                updateMusicVolumeLabel(volume);
            }
        });
        settingsTable.add(musicVolumeSlider).width(280).padLeft(15);

        musicVolumeLabel = new Label(String.format("%.0f%%", settings.getMusicVolume() * 100), skin);
        musicVolumeLabel.setFontScale(1f);
        settingsTable.add(musicVolumeLabel).width(80).padLeft(10);
        settingsTable.row();

        // Difficulty
        Label difficultyLabel = new Label("Difficulty:", skin);
        difficultyLabel.setFontScale(1.1f);
        settingsTable.add(difficultyLabel).width(180);

        difficultySelectBox = new SelectBox<>(skin);
        Array<GameSettings.Difficulty> difficulties = new Array<>(GameSettings.Difficulty.values());
        difficultySelectBox.setItems(difficulties);
        difficultySelectBox.setSelected(settings.getDifficulty());
        difficultySelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                settings.setDifficulty(difficultySelectBox.getSelected());
            }
        });
        settingsTable.add(difficultySelectBox).width(280).padLeft(15).fillX();
        settingsTable.row();

        // Fullscreen
        Label fullscreenLabel = new Label("Fullscreen:", skin);
        fullscreenLabel.setFontScale(1.1f);
        settingsTable.add(fullscreenLabel).width(180);

        fullscreenCheckBox = new CheckBox("", skin);
        fullscreenCheckBox.setChecked(settings.isFullscreen());
        fullscreenCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean fullscreen = fullscreenCheckBox.isChecked();
                settings.setFullscreen(fullscreen);
                // Apply fullscreen change
                if (fullscreen) {
                    Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                } else {
                    Gdx.graphics.setWindowedMode(1024, 768);
                }
            }
        });
        settingsTable.add(fullscreenCheckBox).padLeft(15);
        settingsTable.row();

        // Show FPS
        Label fpsLabel = new Label("Show FPS:", skin);
        fpsLabel.setFontScale(1.1f);
        settingsTable.add(fpsLabel).width(180);

        showFpsCheckBox = new CheckBox("", skin);
        showFpsCheckBox.setChecked(settings.isShowFps());
        showFpsCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                settings.setShowFps(showFpsCheckBox.isChecked());
            }
        });
        settingsTable.add(showFpsCheckBox).padLeft(15);
        settingsTable.row();

        // Controls Info
        Label controlsTitle = new Label("Controls:", skin);
        controlsTitle.setFontScale(1.2f);
        settingsTable.add(controlsTitle).colspan(3).padTop(15).row();

        Label controlsLabel = new Label(
            """
                LEFT/RIGHT ARROWS - Move car
                SPACE - Shoot
                ESC - Return to menu
                R - Restart (after game over)""",
            skin);
        controlsLabel.setFontScale(0.9f);
        settingsTable.add(controlsLabel).colspan(3).padTop(8).left().row();

        // Buttons
        TextButton backButton = new TextButton("Back to Menu", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });

        TextButton resetButton = new TextButton("Reset to Defaults", skin);
        resetButton.getLabel().setFontScale(0.9f); // Scale down font to fit better
        resetButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settings.resetToDefaults();
                // Update UI to reflect defaults
                soundVolumeSlider.setValue(settings.getSoundVolume());
                updateSoundVolumeLabel(settings.getSoundVolume());
                musicVolumeSlider.setValue(settings.getMusicVolume());
                updateMusicVolumeLabel(settings.getMusicVolume());
                game.updateMusicVolume();
                difficultySelectBox.setSelected(settings.getDifficulty());
                fullscreenCheckBox.setChecked(settings.isFullscreen());
                showFpsCheckBox.setChecked(settings.isShowFps());

                // Apply fullscreen change
                if (settings.isFullscreen() != Gdx.graphics.isFullscreen()) {
                    if (settings.isFullscreen()) {
                        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                    } else {
                        Gdx.graphics.setWindowedMode(1024, 768);
                    }
                }
            }
        });

        Table buttonTable = new Table();
        buttonTable.defaults()
            .width(400)
            .height(70)
            .padLeft(15)
            .padRight(15);
        buttonTable.add(resetButton).fillX().padBottom(8).row();
        buttonTable.add(backButton).fillX();

        // Create scrollable content
        Table scrollContent = new Table();
        scrollContent.defaults().pad(8);
        scrollContent.add(titleLabel).padBottom(10).row();
        scrollContent.add(settingsTable).padBottom(10).row();
        scrollContent.add(buttonTable).padTop(5);

        ScrollPane scrollPane = new ScrollPane(scrollContent, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(false, false);
        scrollPane.setOverscroll(false, false);

        table.add(scrollPane).fill().expand();
        table.setFillParent(true);

        return table;
    }

    private void updateSoundVolumeLabel(float volume) {
        soundVolumeLabel.setText(String.format("%.0f%%", volume * 100));
    }

    private void updateMusicVolumeLabel(float volume) {
        musicVolumeLabel.setText(String.format("%.0f%%", volume * 100));
    }
}
