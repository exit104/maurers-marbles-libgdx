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
import com.badlogic.gdx.utils.Align;

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

    BitmapFont bitmapFont = new BitmapFont();

    String[] texts = new String[]{"Four", "Six", "Eight", "Ten", "Twelve"};
    int[] values = new int[]{4, 6, 8, 10, 12};

    int y = 90;
    for (int i = 0; i < texts.length; i++) {
      Label label = new Label(texts[i], new LabelStyle(bitmapFont, Color.BLACK));
      label.setPosition(0, y -= 30, Align.center);
      final int numberOfPlayers = values[i];
      label.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          maurersMarblesGame.setScreen(new GameStageScreen(maurersMarblesGame, numberOfPlayers));
        }
      });
      stage.addActor(label);
    }

  }

}
