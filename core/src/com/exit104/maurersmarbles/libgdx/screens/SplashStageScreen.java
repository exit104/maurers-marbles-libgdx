/*
 * You may only use this file in accordance with the terms and conditions
 * outlined in the accompanying LICENSE file.
 */

package com.exit104.maurersmarbles.libgdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Align;

import com.exit104.maurersmarbles.libgdx.MaurersMarblesGame;

/**
 * The SplashStageScreen class extends the StageScreen class to display a splash screen when the
 * game is first loaded.
 *
 * @author Daniel Uppenkamp
 * @since 1.0.0
 */
public class SplashStageScreen extends StageScreen {

  /**
   * The amount of time to display the splash screen (not including fade in/out).
   */
  protected static final float DISPLAY_DURATION = 2.0f;
  /**
   * The amount of time to fade in/out.
   */
  protected static final float FADE_DURATION = 0.75f;

  /**
   * Creates a new SplashStageScreen.
   *
   * @param maurersMarblesGame the game for this screen
   */
  public SplashStageScreen(MaurersMarblesGame maurersMarblesGame) {
    super(maurersMarblesGame);
  }

  @Override
  public void pause() {
    // TODO
  }

  @Override
  public void render(float delta) {

    stage.getCamera().update();
    Gdx.gl.glClearColor(0, 0, 0, 1);
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

    // TODO Add graphic logo
    Label label = new Label("Exit104", new LabelStyle(new BitmapFont(), null));
    label.setPosition(0, 0, Align.center);
    stage.addActor(label);

    label.setColor(new Color(1f, 1f, 1f, 0f));
    label.addAction(Actions.sequence(
        Actions.fadeIn(FADE_DURATION),
        Actions.delay(DISPLAY_DURATION),
        Actions.fadeOut(FADE_DURATION),
        Actions.run(new Runnable() {
          @Override
          public void run() {
            maurersMarblesGame.setScreen(new MainMenuStageScreen(maurersMarblesGame));
          }
        })));

  }

}
