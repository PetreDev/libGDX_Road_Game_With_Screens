package si.um.feri.temelko;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameScreen extends ScreenAdapter {
    private final RoadGame game;
    private final AssetManager assetManager;
    private final GameSettings settings;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Texture carImg, obstacleImg, fuelImg, powerUpImg, backgroundImg, bulletImg;
    private Sound crashSound, powerUpSound, fuelSound, shootSound;
    private BitmapFont font;

    private Rectangle car;
    private List<Rectangle> obstacles, fuels, powerUps, bullets;
    private Random random;
    private float obstacleSpawnTimer, fuelSpawnTimer, powerUpSpawnTimer, shootCooldown;

    private int score;
    private float currentHealth = 100f;
    private boolean isInvincible = false;
    private float invincibleTimer = 0f;

    // --- Background scrolling ---
    private float backgroundY1 = 0f;
    private float backgroundY2;
    private float backgroundHeight;

    // --- Game speed ---
    private float gameSpeedMultiplier = 1f;
    private float difficultySpeedMultiplier = 1f;
    private float difficultySpawnRate = 1f;

    // --- Image scaling constants ---
    private static final float CAR_SCALE = 0.7f;
    private static final float OBSTACLE_SCALE = 0.3f;
    private static final float FUEL_SCALE = 0.15f;
    private static final float POWERUP_SCALE = 0.15f;
    private static final float BULLET_SCALE = 0.08f;

    // --- Base speeds ---
    private static final float CAR_SPEED = 500f;
    private static final float OBSTACLE_SPEED = 300f;
    private static final float FUEL_SPEED = 250f;
    private static final float POWER_UP_SPEED = 220f;
    private static final float BULLET_SPEED = 700f;
    private static final float SHOOT_COOLDOWN_TIME = 0.25f;

    // --- Scaled sizes ---
    private float carWidth, carHeight;
    private float obstacleWidth, obstacleHeight;
    private float fuelWidth, fuelHeight;
    private float powerUpWidth, powerUpHeight;
    private float bulletWidth, bulletHeight;

    private boolean isGameOver = false;
    private boolean scoreSaved = false;

    public GameScreen(RoadGame game) {
        this.game = game;
        this.assetManager = game.getAssetManager();
        this.settings = game.getSettings();
    }

    @Override
    public void show() {
        batch = game.getBatch();
        shapeRenderer = game.getRenderer();
        
        // Ensure keyboard input is active (no Stage input processor)
        Gdx.input.setInputProcessor(null);
        
        // Stop background music during gameplay
        game.stopMusic();
        
        // Load assets from AssetManager
        carImg = assetManager.get("images/car.png", Texture.class);
        obstacleImg = assetManager.get("images/obstacle.png", Texture.class);
        fuelImg = assetManager.get("images/fuel.png", Texture.class);
        powerUpImg = assetManager.get("images/power-up.png", Texture.class);
        bulletImg = assetManager.get("images/bullet.png", Texture.class);
        backgroundImg = assetManager.get("images/background.png", Texture.class);

        crashSound = assetManager.get("sounds/crash.wav", Sound.class);
        powerUpSound = assetManager.get("sounds/power-up.wav", Sound.class);
        fuelSound = assetManager.get("sounds/collect.wav", Sound.class);
        shootSound = assetManager.get("sounds/shoot.wav", Sound.class);

        // Create default font for game UI
        font = game.createDefaultFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(2f);

        setupScaledSizes();
        resetGame();
    }

    private void setupScaledSizes() {
        carWidth = carImg.getWidth() * CAR_SCALE;
        carHeight = carImg.getHeight() * CAR_SCALE;
        obstacleWidth = obstacleImg.getWidth() * OBSTACLE_SCALE;
        obstacleHeight = obstacleImg.getHeight() * OBSTACLE_SCALE;
        fuelWidth = fuelImg.getWidth() * FUEL_SCALE;
        fuelHeight = fuelImg.getHeight() * FUEL_SCALE;
        powerUpWidth = powerUpImg.getWidth() * POWERUP_SCALE;
        powerUpHeight = powerUpImg.getHeight() * POWERUP_SCALE;
        bulletWidth = bulletImg.getWidth() * BULLET_SCALE;
        bulletHeight = bulletImg.getHeight() * BULLET_SCALE;
    }

    private void resetGame() {
        car = new Rectangle(Gdx.graphics.getWidth() / 2f - carWidth / 2f, 100f, carWidth, carHeight);
        obstacles = new ArrayList<>();
        fuels = new ArrayList<>();
        powerUps = new ArrayList<>();
        bullets = new ArrayList<>();
        random = new Random();

        obstacleSpawnTimer = 0;
        fuelSpawnTimer = 0;
        powerUpSpawnTimer = 0;
        shootCooldown = 0;

        score = 0;
        currentHealth = 100f;
        isInvincible = false;
        invincibleTimer = 0f;
        gameSpeedMultiplier = 1f;
        isGameOver = false;
        scoreSaved = false;

        // Apply difficulty settings
        GameSettings.Difficulty difficulty = settings.getDifficulty();
        difficultySpeedMultiplier = difficulty.getSpeedMultiplier();
        difficultySpawnRate = difficulty.getObstacleSpawnRate();

        // Background setup
        float scale = (float) Gdx.graphics.getWidth() / backgroundImg.getWidth();
        backgroundHeight = backgroundImg.getHeight() * scale;
        backgroundY1 = 0f;
        backgroundY2 = backgroundHeight;
    }

    @Override
    public void render(float delta) {
        // Handle ESC key to return to menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
            return;
        }

        if (!isGameOver) {
            update(delta);
            if (currentHealth <= 0) {
                isGameOver = true;
                // Save score to leaderboard
                if (!scoreSaved) {
                    String playerName = settings.getPlayerName();
                    settings.addScore(playerName, score);
                    scoreSaved = true;
                }
            }
        } else {
            // Restart button
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                resetGame();
            }
            // Leaderboard button
            if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
                game.setScreen(new LeaderboardScreen(game));
                return;
            }
        }

        ScreenUtils.clear(0f, 0f, 0f, 0f);

        batch.begin();

        // Draw background
        batch.draw(backgroundImg, 0, backgroundY1, Gdx.graphics.getWidth(), backgroundHeight);
        batch.draw(backgroundImg, 0, backgroundY2, Gdx.graphics.getWidth(), backgroundHeight);

        if (!isGameOver) {
            // Draw game objects
            batch.draw(carImg, car.x, car.y, car.width, car.height);

            for (Rectangle obstacle : obstacles)
                batch.draw(obstacleImg, obstacle.x, obstacle.y, obstacle.width, obstacle.height);
            for (Rectangle fuel : fuels)
                batch.draw(fuelImg, fuel.x, fuel.y, fuel.width, fuel.height);
            for (Rectangle powerUp : powerUps)
                batch.draw(powerUpImg, powerUp.x, powerUp.y, powerUp.width, powerUp.height);
            for (Rectangle bullet : bullets)
                batch.draw(bulletImg, bullet.x, bullet.y, bullet.width, bullet.height);

            font.draw(batch, "Score: " + score, 20, Gdx.graphics.getHeight() - 20);
            font.draw(batch, "Speed x" + String.format("%.2f", gameSpeedMultiplier * difficultySpeedMultiplier), 20, Gdx.graphics.getHeight() - 140);
            
            // Show FPS if enabled
            if (settings.isShowFps()) {
                font.getData().setScale(1.5f);
                font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), Gdx.graphics.getWidth() - 120, Gdx.graphics.getHeight() - 20);
                font.getData().setScale(2f);
            }
        } else {
            // Game Over Screen
            font.getData().setScale(4f);
            font.draw(batch, "GAME OVER", Gdx.graphics.getWidth() / 2f - 175, Gdx.graphics.getHeight() / 2f + 100);
            font.getData().setScale(2.5f);
            font.draw(batch, "Final Score: " + score, Gdx.graphics.getWidth() / 2f - 110, Gdx.graphics.getHeight() / 2f + 20);
            font.getData().setScale(2f);
            font.draw(batch, "Press R to Restart", Gdx.graphics.getWidth() / 2f - 120, Gdx.graphics.getHeight() / 2f - 40);
            font.draw(batch, "Press L for Leaderboard", Gdx.graphics.getWidth() / 2f - 150, Gdx.graphics.getHeight() / 2f - 90);
        }

        batch.end();

        // Draw bars
        if (!isGameOver) {
            drawBars();
        }
    }

    private void drawBars() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Health bar
        float barX = 20;
        float barY = Gdx.graphics.getHeight() - 80;
        float barWidth = 200;
        float barHeight = 20;

        // Background
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        // Health fill
        float healthPercent = currentHealth / 100f;
        if (healthPercent > 0.6f) {
            shapeRenderer.setColor(Color.GREEN);
        } else if (healthPercent > 0.3f) {
            shapeRenderer.setColor(Color.YELLOW);
        } else {
            shapeRenderer.setColor(Color.RED);
        }
        shapeRenderer.rect(barX, barY, barWidth * healthPercent, barHeight);

        // Border
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        // Power-up bar
        if (isInvincible) {
            float powerBarY = Gdx.graphics.getHeight() - 120;

            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            // Background
            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.rect(barX, powerBarY, barWidth, barHeight);

            // Power-up fill
            float powerPercent = invincibleTimer / 3f;
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(barX, powerBarY, barWidth * powerPercent, barHeight);

            // Border
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(barX, powerBarY, barWidth, barHeight);
        }

        shapeRenderer.end();
    }

    private void update(float delta) {
        // Background movement (apply difficulty multiplier)
        float backgroundScrollSpeed = 200f;
        float totalSpeedMultiplier = gameSpeedMultiplier * difficultySpeedMultiplier;
        backgroundY1 -= backgroundScrollSpeed * delta * totalSpeedMultiplier;
        backgroundY2 -= backgroundScrollSpeed * delta * totalSpeedMultiplier;
        if (backgroundY1 + backgroundHeight <= 0) backgroundY1 = backgroundY2 + backgroundHeight;
        if (backgroundY2 + backgroundHeight <= 0) backgroundY2 = backgroundY1 + backgroundHeight;

        // Car movement (apply difficulty multiplier)
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            car.x -= CAR_SPEED * delta * totalSpeedMultiplier;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            car.x += CAR_SPEED * delta * totalSpeedMultiplier;
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE))
            shoot();

        car.x = Math.max(0, Math.min(Gdx.graphics.getWidth() - car.width, car.x));

        if (shootCooldown > 0) shootCooldown -= delta;

        // Spawning (apply difficulty spawn rate)
        obstacleSpawnTimer += delta;
        fuelSpawnTimer += delta;
        powerUpSpawnTimer += delta;

        float obstacleSpawnInterval = 1f / difficultySpawnRate;
        if (obstacleSpawnTimer > obstacleSpawnInterval) {
            obstacles.add(new Rectangle(random.nextInt(Gdx.graphics.getWidth() - (int)obstacleWidth),
                Gdx.graphics.getHeight(), obstacleWidth, obstacleHeight));
            obstacleSpawnTimer = 0f;
        }
        if (fuelSpawnTimer > 2f) {
            fuels.add(new Rectangle(random.nextInt(Gdx.graphics.getWidth() - (int)fuelWidth),
                Gdx.graphics.getHeight(), fuelWidth, fuelHeight));
            fuelSpawnTimer = 0f;
        }
        if (powerUpSpawnTimer > 5f) {
            powerUps.add(new Rectangle(random.nextInt(Gdx.graphics.getWidth() - (int)powerUpWidth),
                Gdx.graphics.getHeight(), powerUpWidth, powerUpHeight));
            powerUpSpawnTimer = 0f;
        }

        // Move obstacles (apply difficulty multiplier)
        for (Iterator<Rectangle> obstacleIterator = obstacles.iterator(); obstacleIterator.hasNext();) {
            Rectangle obstacle = obstacleIterator.next();
            obstacle.y -= OBSTACLE_SPEED * delta * totalSpeedMultiplier;
            if (obstacle.overlaps(car) && !isInvincible) {
                obstacleIterator.remove();
                currentHealth -= 20f;
                if (currentHealth < 0f) currentHealth = 0f;
                float volume = settings.getSoundVolume();
                crashSound.play(volume * 0.5f);
                // permanent boost after crash
                float speedIncreasePerCrash = 0.1f;
                gameSpeedMultiplier += speedIncreasePerCrash;
            }
            if (obstacle.y + obstacle.height < 0) obstacleIterator.remove();
        }

        // Move fuels (apply difficulty multiplier)
        for (Iterator<Rectangle> fuelIterator = fuels.iterator(); fuelIterator.hasNext();) {
            Rectangle fuel = fuelIterator.next();
            fuel.y -= FUEL_SPEED * delta * totalSpeedMultiplier;
            if (fuel.overlaps(car)) {
                fuelIterator.remove();
                currentHealth = Math.min(100f, currentHealth + 10f);
                score += 5;
                float volume = settings.getSoundVolume();
                fuelSound.play(volume * 0.5f);
            }
            if (fuel.y + fuel.height < 0) fuelIterator.remove();
        }

        // Move power-ups (apply difficulty multiplier)
        for (Iterator<Rectangle> powerUpIterator = powerUps.iterator(); powerUpIterator.hasNext();) {
            Rectangle powerUp = powerUpIterator.next();
            powerUp.y -= POWER_UP_SPEED * delta * totalSpeedMultiplier;
            if (powerUp.overlaps(car)) {
                powerUpIterator.remove();
                isInvincible = true;
                invincibleTimer = 3f;
                float volume = settings.getSoundVolume();
                powerUpSound.play(volume * 0.5f);
            }
            if (powerUp.y + powerUp.height < 0) powerUpIterator.remove();
        }

        if (isInvincible) {
            invincibleTimer -= delta;
            if (invincibleTimer <= 0f) isInvincible = false;
        }

        // Bullets (apply difficulty multiplier)
        for (Iterator<Rectangle> bulletIterator = bullets.iterator(); bulletIterator.hasNext();) {
            Rectangle bullet = bulletIterator.next();
            bullet.y += BULLET_SPEED * delta * totalSpeedMultiplier;
            if (bullet.y > Gdx.graphics.getHeight()) bulletIterator.remove();
            for (Iterator<Rectangle> obstacleIter = obstacles.iterator(); obstacleIter.hasNext();) {
                Rectangle obstacle = obstacleIter.next();
                if (bullet.overlaps(obstacle)) {
                    obstacleIter.remove();
                    bulletIterator.remove();
                    score += 10;
                    break;
                }
            }
        }
    }

    private void shoot() {
        if (shootCooldown <= 0f) {
            bullets.add(new Rectangle(car.x + car.width / 2f - bulletWidth / 2f, car.y + car.height,
                bulletWidth, bulletHeight));
            float volume = settings.getSoundVolume();
            shootSound.play(volume * 0.3f);
            shootCooldown = SHOOT_COOLDOWN_TIME;
        }
    }

    @Override
    public void hide() {
        // Resume background music when leaving game
        game.playMusic();
        // Reset input processor to prevent dangling references
        Gdx.input.setInputProcessor(null);
        dispose();
    }

    @Override
    public void dispose() {
        // Dispose font since it's not managed by AssetManager
        if (font != null) {
            font.dispose();
            font = null;
        }
        // All other resources (Textures, Sounds) are managed by AssetManager
        // Viewport is just a data structure and doesn't need disposal
    }
}

