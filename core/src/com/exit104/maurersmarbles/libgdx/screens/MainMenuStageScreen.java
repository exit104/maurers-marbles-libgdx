/*
 * You may only use this file in accordance with the terms and conditions
 * outlined in the accompanying LICENSE file.
 */

package com.exit104.maurersmarbles.libgdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

import com.exit104.maurersmarbles.libgdx.MaurersMarblesGame;

/**
 * The MainMenuStageScreen class extends the StageScreen class to display the game's main menu.
 *
 * @author Daniel Uppenkamp
 * @since 1.0.0
 */
public class MainMenuStageScreen extends StageScreen {

  /**
   * Creates a new MainMenuStageScreen.
   *
   * @param maurersMarblesGame the game for this screen
   */
  public MainMenuStageScreen(MaurersMarblesGame maurersMarblesGame) {
    super(maurersMarblesGame);
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
    // TODO
    maurersMarblesGame.setScreen(new GameStageScreen(maurersMarblesGame, 4));
  }

}
