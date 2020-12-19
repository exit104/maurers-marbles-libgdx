/*
 * You may only use this file in accordance with the terms and conditions
 * outlined in the accompanying LICENSE file.
 */

package com.exit104.maurersmarbles.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

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

  public Image createImage(String fileName) {
    TextureRegion textureRegion = new TextureRegion(assetManager.get(fileName, Texture.class));
    textureRegion.flip(false, true);
    return new Image(textureRegion);
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
