/*
* You may only use this file in accordance with the terms and conditions
* outlined in the accompanying LICENSE file.
 */

package com.exit104.maurersmarbles.libgdx;

import com.badlogic.gdx.Gdx;

import com.exit104.maurersmarbles.libgdx.screens.SplashStageScreen;

/**
 *
 * @author Daniel Uppenkamp
 * @since 1.0.0
 */
public class MaurersMarblesGame extends com.badlogic.gdx.Game {

  /**
   * Creates a new MaurersMarblesGame.
   */
  public MaurersMarblesGame() {
    // do nothing
  }

  @Override
  public void create() {
    Gdx.graphics.setContinuousRendering(false);
    this.setScreen(new SplashStageScreen(this));
  }

}
