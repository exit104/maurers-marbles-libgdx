/*
 * You may only use this file in accordance with the terms and conditions
 * outlined in the accompanying LICENSE file.
 */

package com.exit104.maurersmarbles.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;

import com.exit104.maurersmarbles.libgdx.screens.SplashStageScreen;

/**
 *
 * @author Daniel Uppenkamp
 * @since 1.0.0
 */
public class MaurersMarblesGame extends com.badlogic.gdx.Game {

  /**
   * The asset manager for the game.
   */
  protected final transient AssetManager assetManager = new AssetManager();

  /**
   * Creates a new MaurersMarblesGame.
   */
  public MaurersMarblesGame() {
    // do nothing
  }

  /**
   * Returns the asset manager for the game.
   *
   * @return the asset manager for the game
   */
  public AssetManager getAssetManager() {
    return assetManager;
  }

  @Override
  public void create() {
    Gdx.graphics.setContinuousRendering(false);
    this.setScreen(new SplashStageScreen(this));
  }

  @Override
  public void dispose() {
    super.dispose();
    assetManager.dispose();
  }

}
