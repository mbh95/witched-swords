package com.comp460.tactics;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.comp460.MainGame;
import com.comp460.assets.FontManager;
import com.comp460.common.GameScreen;
import com.comp460.tactics.components.unit.UnitStatsComponent;
import com.comp460.tactics.factories.CursorFactory;
import com.comp460.tactics.systems.ai.AiSystem;
import com.comp460.tactics.systems.core.CameraTrackingSystem;
import com.comp460.tactics.systems.core.SnapToParentSystem;
import com.comp460.tactics.systems.core.SpriteAnimationSystem;
import com.comp460.tactics.systems.core.SpriteRenderingSystem;
import com.comp460.tactics.components.unit.AIControlledComponent;
import com.comp460.tactics.components.unit.PlayerControlledComponent;
import com.comp460.tactics.components.unit.ReadyToMoveComponent;
import com.comp460.tactics.systems.cursor.CursorManagementSystem;
import com.comp460.tactics.systems.game.EndConditionSystem;
import com.comp460.tactics.systems.ui.HoverRenderingSystem;
import com.comp460.tactics.systems.cursor.MapCursorMovementSystem;
import com.comp460.tactics.systems.unit.MapManagementSystem;
import com.comp460.tactics.systems.game.TurnManagementSystem;
import com.comp460.tactics.systems.map.MapRenderingSystem;
import com.comp460.tactics.systems.map.MapToScreenSystem;
import com.comp460.tactics.systems.cursor.MapCursorSelectionSystem;
import com.comp460.tactics.systems.map.MovesRenderingSystem;
import com.comp460.tactics.systems.map.SelectionRenderingSystem;

/**
 * Created by matthewhammond on 1/15/17.
 */
public class TacticsScreen extends GameScreen {

    public enum TacticsState {PLAYER_TURN_TRANSITION, PLAYER_TURN, AI_TURN_TRANSITION, AI_TURN, PLAYER_WIN, AI_WIN}

    private static final Family unitsFamily = Family.all(UnitStatsComponent.class).get();
    private static final Family playerUnitsFamily = Family.all(PlayerControlledComponent.class).get();
    private static final Family aiUnitsFamily = Family.all(AIControlledComponent.class).get();

    private static final BitmapFont playerTurnFont = FontManager.getFont(FontManager.KEN_VECTOR_FUTURE, 16, Color.BLUE);
    private static final BitmapFont aiTurnFont = FontManager.getFont(FontManager.KEN_VECTOR_FUTURE, 16, Color.RED);


    public Engine engine;

    private TacticsMap map;

    public TacticsState curState;

    private float timer;

    public TacticsScreen(MainGame game, GameScreen prevScreen, TiledMap tiledMap) {
        super(game, prevScreen);

        this.engine = new PooledEngine();

        this.map = new TacticsMap(tiledMap);

        engine.addSystem(new MapRenderingSystem(this));

        engine.addSystem(new SpriteRenderingSystem(batch, camera));
        engine.addSystem(new SpriteAnimationSystem());
        engine.addSystem(new CameraTrackingSystem());
        engine.addSystem(new SnapToParentSystem());

        engine.addSystem(new MapToScreenSystem(this));
        engine.addSystem(new MovesRenderingSystem(this));
        engine.addSystem(new SelectionRenderingSystem(this));
        engine.addSystem(new HoverRenderingSystem(this));

        engine.addSystem(new CursorManagementSystem());
        engine.addSystem(new MapManagementSystem(this));
        engine.addSystem(new TurnManagementSystem(this));
        engine.addSystem(new EndConditionSystem(this));

        engine.addSystem(new MapCursorSelectionSystem(this));
        engine.addSystem(new MapCursorMovementSystem(this));

        engine.addSystem(new AiSystem());


        this.map.populate(engine);

        engine.addEntity(CursorFactory.makeCursor(this));

        startPlayerTurn();
    }

    public TacticsMap getMap() {
        return this.map;
    }

    public SpriteBatch getBatch() {
        return this.batch;
    }

    public OrthographicCamera getCamera() {
        return this.camera;
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        engine.update(delta);

        switch (curState) {
            case PLAYER_TURN_TRANSITION:
                renderPlayerTurnTransition(delta);
                break;
            case AI_TURN_TRANSITION:
                renderAiTurnTransition(delta);
                break;
            case PLAYER_TURN:
                break;
            case AI_TURN:
                break;
            case PLAYER_WIN:
                renderPlayerWin(delta);
                break;
            case AI_WIN:
                renderAiWin(delta);
                break;

        }

    }

    private void renderPlayerWin(float delta) {
        timer -= delta;
        if (timer <= 0) {
            dispose();
            this.previousScreen();
        }
        uiBatch.begin();
        playerTurnFont.draw(uiBatch, "You Win!", 0, 16);
        uiBatch.end();
    }

    private void renderAiWin(float delta) {
        timer -= delta;
        if (timer <= 0) {
            dispose();
            this.previousScreen();
        }
        uiBatch.begin();
        aiTurnFont.draw(uiBatch, "Computer Wins", 0, 16);
        uiBatch.end();
    }

    public void renderPlayerTurnTransition(float delta) {
        timer -= delta;
        if (timer <= 0) {
            startPlayerTurn();
        }
        uiBatch.begin();
        playerTurnFont.draw(uiBatch, "Player Turn", 0, 16);
        uiBatch.end();
    }

    public void renderAiTurnTransition(float delta) {
        timer -= delta;
        if (timer <= 0) {
            startAiTurn();
        }
        uiBatch.begin();
        aiTurnFont.draw(uiBatch, "Computer Turn", 0, 16);
        uiBatch.end();

    }

    public void startTransitionToPlayerTurn() {
        timer = 1f;
        curState = TacticsState.PLAYER_TURN_TRANSITION;
    }

    public void startTransitionToAiTurn() {
        timer = 1f;
        curState = TacticsState.AI_TURN_TRANSITION;
    }

    public void startPlayerTurn() {
        engine.getSystem(AiSystem.class).setProcessing(false);

        engine.getEntitiesFor(playerUnitsFamily).forEach(e -> {
            e.add(new ReadyToMoveComponent());
        });
        this.curState = TacticsState.PLAYER_TURN;

        engine.getSystem(MapCursorSelectionSystem.class).setProcessing(true);
        engine.getSystem(MapCursorMovementSystem.class).setProcessing(true);

    }

    public void startAiTurn() {
        engine.getSystem(MapCursorSelectionSystem.class).setProcessing(false);
        engine.getSystem(MapCursorMovementSystem.class).setProcessing(false);

        engine.getEntitiesFor(aiUnitsFamily).forEach(e -> {
            e.add(new ReadyToMoveComponent());
        });
        this.curState = TacticsState.AI_TURN;

        engine.getSystem(AiSystem.class).setProcessing(true);
    }

    public void playerWins() {
        timer = 2f;
        this.curState = TacticsState.PLAYER_WIN;
    }

    public void aiWins() {
        timer = 2f;
        this.curState = TacticsState.AI_WIN;
    }

    @Override
    public void show() {
        super.show();
        engine.getEntitiesFor(unitsFamily).forEach(e -> {
            UnitStatsComponent stats = e.getComponent(UnitStatsComponent.class);
            if (stats.base.curHP <= 0) {
                engine.removeEntity(e);
            }
        });
    }

    @Override
    public void hide() {
        super.hide();
    }
}