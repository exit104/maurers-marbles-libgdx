/*
 * You may only use this file in accordance with the terms and conditions
 * outlined in the accompanying LICENSE file.
 */

package com.exit104.maurersmarbles.libgdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import com.exit104.maurersmarbles.GameStats;
import com.exit104.maurersmarbles.libgdx.MaurersMarblesGame;

/**
 *
 * @author Daniel Uppenkamp
 * @since 1.0.0
 */
public class GameOverStageScreen extends StageScreen {

  protected final transient GameStats gameStats;

  /**
   * Creates a new GameOverStageScreen.
   *
   * @param maurersMarblesGame the game for this screen
   * @param gameStats the game stats from the game
   */
  public GameOverStageScreen(MaurersMarblesGame maurersMarblesGame, GameStats gameStats) {
    super(maurersMarblesGame);
    this.gameStats = gameStats;
  }

  @Override
  public void pause() {
    // TODO
  }

  @Override
  public void render(float delta) {

    stage.getCamera().update();
    Gdx.gl.glClearColor(1, 1, 1, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    stage.act();
    stage.draw();

  }

  @Override
  public void resume() {
    // TODO
  }

  @Override
  public void show() {

    super.show();

    BitmapFont bitmapFont = new BitmapFont();
    Label label = new Label(gameStats.toString(), new LabelStyle(bitmapFont, Color.BLACK));
    label.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        maurersMarblesGame.setScreen(new MainMenuStageScreen(maurersMarblesGame));
      }
    });
    stage.addActor(label);

  }

}
