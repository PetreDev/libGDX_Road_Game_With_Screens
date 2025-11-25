package si.um.feri.temelko;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class IntroScreen extends ScreenAdapter {
    public static final float INTRO_DURATION_IN_SEC = 3f;   // duration of the (intro) animation

    private final RoadGame game;
    private final AssetManager assetManager;
    private Texture carTexture;
    private Texture roadTexture;
    private Viewport viewport;
    private float duration = 0f;
    private Stage stage;

    public IntroScreen(RoadGame game) {
        this.game = game;
        this.assetManager = game.getAssetManager();
    }

    @Override
    public void show() {
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage = new Stage(viewport, game.getBatch());

        // Load textures from AssetManager
        carTexture = assetManager.get("images/car.png", Texture.class);
        roadTexture = assetManager.get("images/background.png", Texture.class);

        stage.addActor(createRoadBackground());
        stage.addActor(createAnimation());
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 0f);
        duration += delta;

        // go to the MenuScreen after INTRO_DURATION_IN_SEC seconds
        if (duration > INTRO_DURATION_IN_SEC) {
            game.setScreen(new MenuScreen(game));
        }

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void hide() {
        // Reset input processor if set (IntroScreen doesn't set one, but good practice)
        Gdx.input.setInputProcessor(null);
        dispose();
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        // Don't dispose textures - they're managed by AssetManager
        // Viewport is just a data structure and doesn't need disposal
    }

    private Actor createRoadBackground() {
        Image road = new Image(roadTexture);
        // Scale to fit viewport
        float scale = viewport.getWorldWidth() / road.getWidth();
        road.setSize(road.getWidth() * scale, road.getHeight() * scale);
        // Position the image to center
        road.setPosition(viewport.getWorldWidth() / 2f - road.getWidth() / 2f,
            viewport.getWorldHeight() / 2f - road.getHeight() / 2f);
        road.setColor(1f, 1f, 1f, 0.5f); // Semi-transparent
        return road;
    }

    private Actor createAnimation() {
        Image car = new Image(carTexture);
        // Scale the car for the intro
        car.setSize(car.getWidth() * 0.5f, car.getHeight() * 0.5f);
        // Set initial position (starting from bottom left, coming into view)
        float startX = -car.getWidth();
        float startY = viewport.getWorldHeight() * 0.3f;
        car.setPosition(startX, startY);
        
        // Set positions x, y to center the image to the center of the window
        float posX = (viewport.getWorldWidth() / 2f) - car.getWidth() / 2f;
        float posY = (viewport.getWorldHeight() / 2f) - car.getHeight() / 2f;
        
        car.setOrigin(Align.center);

        car.addAction(
            /* animationDuration = Actions.sequence + Actions.parallel + Actions.scaleTo
                                 = 1.5 + 1 + 0.5 = 3 sec */
            Actions.sequence(
                Actions.parallel(
                    Actions.moveTo(posX, posY, 1.5f),   // move car to the center of the window
                    Actions.scaleTo(0.7f, 0.7f, 1.5f)   // scale up the car
                ),
                Actions.rotateBy(360, 1f),  // rotate the car for 360 degrees
                Actions.scaleTo(0, 0, 0.5f),    // "minimize"/"hide" car
                Actions.removeActor()   // remove car
            )
        );

        return car;
    }
}

