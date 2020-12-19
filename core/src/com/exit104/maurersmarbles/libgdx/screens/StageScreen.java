/*
 * You may only use this file in accordance with the terms and conditions
 * outlined in the accompanying LICENSE file.
 */

package com.exit104.maurersmarbles.libgdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.exit104.maurersmarbles.libgdx.MaurersMarblesGame;

/**
 * The StageScreen class implements the Screen interface to provide the methods for using a stage in
 * a screen.
 *
 * @author Daniel Uppenkamp
 * @since 1.0.0
 */
public abstract class StageScreen implements Screen {

  /**
   * The game for this screen.
   */
  protected final transient MaurersMarblesGame maurersMarblesGame;
  /**
   * The stage used to animate the actors.
   */
  protected final transient Stage stage;
  /**
   * The viewport for the stage.
   */
  protected final transient Viewport viewport;

  /**
   * Creates a new StageScreen.
   *
   * @param maurersMarblesGame the game for this screen
   */
  public StageScreen(MaurersMarblesGame maurersMarblesGame) {

    // TODO Preconditons?
    this.maurersMarblesGame = maurersMarblesGame;

    viewport = new ScreenViewport();
    //viewport = new ExtendViewport(800, 480, 854, 600);
    //viewport = new ExtendViewport(1280, 720, 1440, 720);
    //viewport = new ExtendViewport(720, 1280, 720, 1440);
    stage = new Stage(viewport);

    ((OrthographicCamera) stage.getCamera()).setToOrtho(true, viewport.getWorldWidth(),
        viewport.getWorldHeight());

  }

  @Override
  public void dispose() {
    stage.dispose();
  }

  @Override
  public void hide() {
    // TODO Is this needed?
    Gdx.input.setInputProcessor(null);
  }

  @Override
  public void resize(int width, int height) {
    stage.getViewport().update(width, height);
    ((OrthographicCamera) stage.getCamera()).setToOrtho(true, viewport.getWorldWidth(),
        viewport.getWorldHeight());
  }

  @Override
  public void show() {
    Gdx.input.setInputProcessor(stage);
  }

}
