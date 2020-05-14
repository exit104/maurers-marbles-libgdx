/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.exit104.maurersmarbles.libgdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import com.exit104.maurersmarbles.BoardLayout;
import com.exit104.maurersmarbles.Game;
import com.exit104.maurersmarbles.Game.State;
import com.exit104.maurersmarbles.GameStats;
import com.exit104.maurersmarbles.GridBoardLayout;
import com.exit104.maurersmarbles.Marble;
import com.exit104.maurersmarbles.Play;
import com.exit104.maurersmarbles.PlaySelector;
import com.exit104.maurersmarbles.Player;
import com.exit104.maurersmarbles.event.Event;
import com.exit104.maurersmarbles.event.EventListener;
import com.exit104.maurersmarbles.event.ExitedStateGameEvent;
import com.exit104.maurersmarbles.event.MovedMarbleGameEvent;
import com.exit104.maurersmarbles.libgdx.MaurersMarblesGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Daniel Uppenkamp
 * @since 1.0.0
 */
public class GameStageScreen extends StageScreen implements EventListener {

  protected transient boolean waitForUserInput = false;
  protected final transient BoardLayout boardLayout;
  protected final transient BitmapFont bitmapFont = new BitmapFont();
  protected final transient Game game;
  protected final transient GameStats gameStats;
  protected final transient Group boardGroup = new Group();
  protected final transient Image boardBackgroundImage;
  protected final transient Image[] spaceImages;
  protected final transient Image[][] marbleImages;
  protected final transient Label[] spaceLabels;
  protected final transient List<Event> events = new ArrayList<>();

  class UserPlaySelector extends PlaySelector {

    Set<Play> plays;

    @Override
    public void setAvailablePlays(Set<Play> plays) {
      super.setAvailablePlays(plays);
      this.plays = plays;
      waitForUserInput = true;
    }

  }

  /**
   * Creates a new GameStageScreen.
   *
   * @param maurersMarblesGame the game for this screen
   * @param numberOfPlayers the number of players in the game
   */
  public GameStageScreen(MaurersMarblesGame maurersMarblesGame, int numberOfPlayers) {

    super(maurersMarblesGame);

    game = new Game(numberOfPlayers);
    game.getPlayers().get(0).setPlaySelector(new UserPlaySelector());

    gameStats = new GameStats(game);
    game.addEventListener(this);

    boardLayout = new GridBoardLayout(game.getBoard());

    boardGroup.setOrigin(Align.center);
    stage.addActor(boardGroup);

    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.WHITE);
    pixmap.fillRectangle(0, 0, 1, 1);
    Texture texture = new Texture(pixmap);

    // create the image for the board background
    boardBackgroundImage = new Image(texture);
    boardBackgroundImage.setColor(0.75f, 0.75f, 0.75f, 1);
    boardGroup.addActor(boardBackgroundImage);

    // create the images for the board spaces
    spaceImages = new Image[game.getBoard().getNumberOfPlayableSpaces()];
    spaceLabels = new Label[game.getBoard().getNumberOfPlayableSpaces()];
    for (int i = 0; i < spaceImages.length; i++) {
      spaceImages[i] = new Image(texture);
      spaceImages[i].setColor(0.5f, 0.5f, 0.5f, 1);
      boardGroup.addActor(spaceImages[i]);
      spaceLabels[i] = new Label(String.valueOf(i), new LabelStyle(bitmapFont, Color.GOLD));
      boardGroup.addActor(spaceLabels[i]);
    }

    // TODO Determine colors for 4, 6, 8, 10, and 12 players
    Color[] colors = new Color[12];
    colors[0] = Color.BLUE;
    colors[1] = Color.GREEN;
    colors[2] = Color.RED;
    colors[3] = Color.YELLOW;
    colors[4] = Color.BLACK;
    colors[5] = Color.WHITE;
    colors[6] = Color.BROWN;
    colors[7] = Color.GOLDENROD;
    colors[8] = Color.CORAL;
    colors[9] = Color.CYAN;
    colors[10] = Color.PURPLE;
    colors[11] = Color.LIME;

    marbleImages = new Image[game.getNumberOfPlayers()][4];
    for (int i = 0; i < game.getNumberOfPlayers(); i++) {
      for (int j = 0; j < 4; j++) {

        marbleImages[i][j] = new Image(texture);
        marbleImages[i][j].setColor(colors[i]);
        boardGroup.addActor(marbleImages[i][j]);

        if (i == 0) {

          marbleImages[i][j].addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

              if (waitForUserInput) {
                waitForUserInput = false;
                UserPlaySelector userPlaySelector
                    = (UserPlaySelector) game.getPlayers().get(0).getPlaySelector();
                userPlaySelector.setSelectedPlay(userPlaySelector.plays.iterator().next());
                userPlaySelector.plays = null;
                game.advance();
              }

            }
          });

        }

      }
    }

    // start the game
    game.advance();

  }

  @Override
  public void handleEvent(Event event) {

    if (event instanceof ExitedStateGameEvent) {

      SequenceAction sequenceAction = new SequenceAction();
      for (Event event1 : events) {
        if (event1 instanceof MovedMarbleGameEvent) {
          MovedMarbleGameEvent movedMarbleGameEvent = (MovedMarbleGameEvent) event1;
          com.exit104.maurersmarbles.Rectangle rectangle = boardLayout.getBoundsForMarble(
              movedMarbleGameEvent.getNewBoardIndex());
          MoveToAction moveToAction = Actions.action(MoveToAction.class);
          moveToAction.setPosition(rectangle.getX() * boardGroup.getWidth(),
              (1.0f - rectangle.getY()) * boardGroup.getHeight()
              - rectangle.getHeight() * boardGroup.getHeight());
          moveToAction.setDuration(0.01f);
          moveToAction.setActor(
              marbleImages[movedMarbleGameEvent.getPlayerNumber()][movedMarbleGameEvent.getMarbleNumber()]);
          sequenceAction.addAction(moveToAction);
        }
      }
      events.clear();

      if (((ExitedStateGameEvent) event).getState() == State.GAME_OVER) {
        maurersMarblesGame.setScreen(new GameOverStageScreen(maurersMarblesGame, gameStats));
      } else if (((ExitedStateGameEvent) event).getState() == State.PLAYER_TURN
          && waitForUserInput) {
        System.out.printf("Waiting for user input...\n");
      } else {
        RunnableAction runnableAction = new RunnableAction();
        runnableAction.setRunnable(new Runnable() {
          @Override
          public void run() {
            game.advance();
          }
        });
        sequenceAction.addAction(runnableAction);
      }

      stage.addAction(sequenceAction);
      Gdx.graphics.requestRendering();

    } else {
      events.add(event);
    }

  }

  @Override
  public void pause() {
    // TODO
  }

  @Override
  public void render(float delta) {

    stage.getCamera().update();
    Gdx.gl.glClearColor(1, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    stage.act();
    stage.draw();

  }

  @Override
  public void resize(int width, int height) {

    float size = height;
    if (width < height) {
      size = width;
    }

    boardGroup.setSize(size, size);
    boardGroup.setPosition(-boardGroup.getWidth() / 2.0f,
        (height / 2.0f) - boardGroup.getHeight());
    boardBackgroundImage.setSize(boardGroup.getWidth(), boardGroup.getHeight());

    for (int i = 0; i < spaceImages.length; i++) {
      com.exit104.maurersmarbles.Rectangle rectangle = boardLayout.getBoundsForSpace(i);
      float spaceHeight = rectangle.getHeight() * boardGroup.getHeight();
      spaceImages[i].setPosition(rectangle.getX() * boardGroup.getWidth(),
          (1.0f - rectangle.getY()) * boardGroup.getHeight() - spaceHeight);
      spaceImages[i].setSize(rectangle.getWidth() * boardGroup.getWidth(), spaceHeight);
      spaceLabels[i].setPosition(spaceImages[i].getX(), spaceImages[i].getY());
    }

    for (Player player : game.getPlayers()) {
      for (Marble marble : player.getMarbles()) {
        com.exit104.maurersmarbles.Rectangle rectangle = boardLayout.getBoundsForMarble(
            marble.getBoardIndex());
        float marbleHeight = rectangle.getHeight() * boardGroup.getHeight();
        marbleImages[player.getPlayerNumber()][marble.getMarbleNumber()].setPosition(
            rectangle.getX() * boardGroup.getWidth(),
            (1.0f - rectangle.getY()) * boardGroup.getHeight() - marbleHeight);
        marbleImages[player.getPlayerNumber()][marble.getMarbleNumber()].setSize(
            rectangle.getWidth() * boardGroup.getWidth(), marbleHeight);
      }
    }

    stage.getViewport().update(width, height);

  }

  @Override
  public void resume() {
    // TODO
  }

}