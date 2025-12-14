package si.um.feri.temelko;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class LeaderboardScreen extends ScreenAdapter {
    private final RoadGame game;
    private final AssetManager assetManager;
    private final GameSettings settings;
    private Viewport viewport;
    private Stage stage;
    private Skin skin;
    private Texture background;

    public LeaderboardScreen(RoadGame game) {
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

        // Start/resume background music
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
        Gdx.input.setInputProcessor(null);
        dispose();
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
    }

    private Actor createUi() {
        Table table = new Table();
        table.defaults().pad(10);
        table.setBackground(new TextureRegionDrawable(background));

        // Title - smaller scale to fit on screen
        Label titleLabel = new Label("Leaderboard", skin, "title");
        titleLabel.setFontScale(1.2f);

        // Leaderboard table
        Table leaderboardTable = createLeaderboardTable();

        // Buttons
        TextButton backButton = new TextButton("Back to Menu", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });

        TextButton clearButton = new TextButton("Clear Scores", skin);
        clearButton.getLabel().setFontScale(0.9f);
        clearButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settings.clearLeaderboard();
                // Refresh the screen
                game.setScreen(new LeaderboardScreen(game));
            }
        });

        Table buttonTable = new Table();
        buttonTable.defaults()
            .width(300)
            .height(60)
            .padLeft(10)
            .padRight(10);
        buttonTable.add(clearButton).fillX().padBottom(8).row();
        buttonTable.add(backButton).fillX();

        // Create scrollable content - centered layout
        Table scrollContent = new Table();
        scrollContent.defaults().pad(5);
        scrollContent.add(titleLabel).expandX().center().padBottom(15).row();
        scrollContent.add(leaderboardTable).expandX().center().padBottom(15).row();
        scrollContent.add(buttonTable).expandX().center().padTop(10);

        ScrollPane scrollPane = new ScrollPane(scrollContent, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setOverscroll(false, false);

        table.add(scrollPane).expand().fill().center();
        table.setFillParent(true);

        return table;
    }

    private Table createLeaderboardTable() {
        Table table = new Table();
        table.defaults().pad(3).padLeft(10).padRight(10);

        // Header row
        Label rankHeader = new Label("Rank", skin);
        rankHeader.setFontScale(1f);
        rankHeader.setColor(Color.GOLD);

        Label nameHeader = new Label("Player", skin);
        nameHeader.setFontScale(1f);
        nameHeader.setColor(Color.GOLD);

        Label scoreHeader = new Label("Score", skin);
        scoreHeader.setFontScale(1f);
        scoreHeader.setColor(Color.GOLD);

        Label diffHeader = new Label("Difficulty", skin);
        diffHeader.setFontScale(1f);
        diffHeader.setColor(Color.GOLD);

        table.add(rankHeader).width(60).center();
        table.add(nameHeader).width(150).center();
        table.add(scoreHeader).width(80).center();
        table.add(diffHeader).width(100).center();
        table.row();

        // Separator
        table.add().height(8).colspan(4).row();

        // Get leaderboard entries
        Array<GameSettings.LeaderboardEntry> entries = settings.getLeaderboard();

        if (entries.size == 0) {
            Label emptyLabel = new Label("No scores yet!", skin);
            emptyLabel.setFontScale(1f);
            emptyLabel.setColor(Color.LIGHT_GRAY);
            table.add(emptyLabel).colspan(4).center().padTop(20).padBottom(20);
        } else {
            int rank = 1;
            for (GameSettings.LeaderboardEntry entry : entries) {
                // Determine color based on rank
                Color rowColor;
                if (rank == 1) {
                    rowColor = Color.GOLD;
                } else if (rank == 2) {
                    rowColor = Color.LIGHT_GRAY;
                } else if (rank == 3) {
                    rowColor = new Color(0.8f, 0.5f, 0.2f, 1f); // Bronze
                } else {
                    rowColor = Color.WHITE;
                }

                Label rankLabel = new Label(getRankDisplay(rank), skin);
                rankLabel.setFontScale(0.9f);
                rankLabel.setColor(rowColor);

                Label nameLabel = new Label(truncateName(entry.name), skin);
                nameLabel.setFontScale(0.9f);
                nameLabel.setColor(rowColor);

                Label scoreLabel = new Label(String.valueOf(entry.score), skin);
                scoreLabel.setFontScale(0.9f);
                scoreLabel.setColor(rowColor);

                Label diffLabel = new Label(entry.difficulty != null ? entry.difficulty : "Normal", skin);
                diffLabel.setFontScale(0.85f);
                diffLabel.setColor(rowColor);

                table.add(rankLabel).width(60).center();
                table.add(nameLabel).width(150).center();
                table.add(scoreLabel).width(80).center();
                table.add(diffLabel).width(100).center();
                table.row();

                rank++;
            }
        }

        return table;
    }

    private String getRankDisplay(int rank) {
        return switch (rank) {
            case 1 -> "1st";
            case 2 -> "2nd";
            case 3 -> "3rd";
            default -> rank + "th";
        };
    }

    private String truncateName(String name) {
        if (name == null) return "Unknown";
        if (name.length() > 15) {
            return name.substring(0, 12) + "...";
        }
        return name;
    }
}
