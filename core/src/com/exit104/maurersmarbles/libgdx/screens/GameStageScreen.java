/*
 * You may only use this file in accordance with the terms and conditions
 * outlined in the accompanying LICENSE file.
 */

package com.exit104.maurersmarbles.libgdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.actions.ParallelAction;
import com.badlogic.gdx.scenes.scene2d.actions.RotateToAction;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import com.exit104.maurersmarbles.BoardLayout;
import com.exit104.maurersmarbles.Card;
import com.exit104.maurersmarbles.CardDeck;
import com.exit104.maurersmarbles.Game;
import com.exit104.maurersmarbles.Game.State;
import com.exit104.maurersmarbles.GameStats;
import com.exit104.maurersmarbles.GridBoardLayout;
import com.exit104.maurersmarbles.InvalidPlayException;
import com.exit104.maurersmarbles.Marble;
import com.exit104.maurersmarbles.Play;
import com.exit104.maurersmarbles.PlaySelector;
import com.exit104.maurersmarbles.Player;
import com.exit104.maurersmarbles.Rectangle;
import com.exit104.maurersmarbles.ScoreBasedPlaySelector;
import com.exit104.maurersmarbles.UserPlay;
import com.exit104.maurersmarbles.event.CannotPlayGameEvent;
import com.exit104.maurersmarbles.event.DealtCardGameEvent;
import com.exit104.maurersmarbles.event.Event;
import com.exit104.maurersmarbles.event.EventListener;
import com.exit104.maurersmarbles.event.ExitedStateGameEvent;
import com.exit104.maurersmarbles.event.MovedMarbleGameEvent;
import com.exit104.maurersmarbles.event.PlayedCardGameEvent;
import com.exit104.maurersmarbles.libgdx.MaurersMarblesGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Daniel Uppenkamp
 * @since 1.0.0
 */
public class GameStageScreen extends StageScreen implements EventListener {

  /**
   * Whether or not to wait for user input before advancing the game.
   */
  protected transient boolean waitForUserInput = false;
  /**
   * The board layout used to draw the game board.
   */
  protected final transient BoardLayout boardLayout;
  /**
   * The card value used to indicate no card.
   */
  protected static final Card NO_CARD = null;
  /**
   * The user card that is currently selected.
   */
  protected transient Card selectedCard = NO_CARD;
  /**
   * The length of time (in seconds) to deal a card.
   */
  protected static final float DURATION_DEAL_CARD = 0.1f;
  /**
   * The length of time (in seconds) to move a marble.
   */
  protected static final float DURATION_MOVE_MARBLE = 0.5f;
  /**
   * The length of time (in seconds) to play a card.
   */
  protected static final float DURATION_PLAY_CARD = 0.5f;
  /**
   * The game.
   */
  protected final transient Game game;
  /**
   * The game stats.
   */
  protected final transient GameStats gameStats;
  /**
   * The group used to manage the actors on the game board.
   */
  protected final transient Group boardGroup = new Group();
  /**
   * The image used as the background for the game board.
   */
  protected final transient Image boardBackgroundImage;
  /**
   * The array of images for the spaces on the board. The index into the array is the board index
   * and the value is the image for that board space.
   */
  protected final transient Image[] spaceImages;
  /**
   * The array of images for the split cards (when a seven is split). The index into the array is
   * the split value - 1 and the value is the image for that split card.
   */
  protected final transient Image[] splitCardImages;
  /**
   * The array of images for the marbles. The index into the first array is the player number and
   * the index into the second array is the image for that marble.
   */
  protected final transient Image[][] marbleImages;
  /**
   * The user player number.
   */
  protected static final int USER_PLAYER_NUMBER = 0;
  /**
   * The split value that is currently selected by the user.
   */
  protected transient int selectedSplitValue = UserPlay.NO_SPLIT_VALUE;
  /**
   * The list of events that were fired since the last game state change.
   */
  protected final transient List<Event> queuedEvents = new ArrayList<>();
  /**
   * The map that contains the images for the cards. The key into the map is the card string
   * (toString()), and the value is the image for that card.
   */
  protected final transient Map<String, Image> cardImages = new TreeMap<>();
  /**
   * The first marble that is currently selected by the user.
   */
  protected transient Marble selectedMarble1 = UserPlay.NO_MARBLE;
  /**
   * The second marble that is currently selected by the user.
   */
  protected transient Marble selectedMarble2 = UserPlay.NO_MARBLE;

  // debugging
  protected transient Label mainMenuLabel;

  /**
   * Creates a new GameStageScreen.
   *
   * @param maurersMarblesGame the game for this screen
   * @param numberOfPlayers the number of players in the game
   */
  public GameStageScreen(final MaurersMarblesGame maurersMarblesGame, int numberOfPlayers) {

    super(maurersMarblesGame);

    // TODO move elsewhere?
    maurersMarblesGame.getAssetManager().load("board_background.png", Texture.class);
    maurersMarblesGame.getAssetManager().load("board_space.png", Texture.class);
    maurersMarblesGame.getAssetManager().load("card_back.png", Texture.class);
    for (Card.Rank rank : Card.Rank.values()) {
      for (Card.Suit suit : Card.Suit.values()) {
        maurersMarblesGame.getAssetManager().load("card_" + rank.toString().toLowerCase()
            + suit.toString().toLowerCase() + ".png", Texture.class);
      }
    }
    maurersMarblesGame.getAssetManager().load("marble.png", Texture.class);
    maurersMarblesGame.getAssetManager().finishLoading();

    // TODO Determine colors for 4, 6, 8, 10, and 12 players
    // TODO keep colors for teams consistent
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

    // create the game and initialize the players
    game = new Game(numberOfPlayers);
    // TODO add ability to be all AI players
    game.getPlayers().get(USER_PLAYER_NUMBER).setPlaySelector(new UserPlaySelector());
    for (int i = 1; i < game.getNumberOfPlayers(); i++) {
      // TODO add the ability to set the AI difficulty
      game.getPlayers().get(i).setPlaySelector(new ScoreBasedPlaySelector(game, i));
    }

    // initialize the game stats which are displayed at the end of the game
    gameStats = new GameStats(game);
    game.addEventListener(this);

    // create the board layout
    boardLayout = new GridBoardLayout(game.getBoard());
    stage.addActor(boardGroup);

    // create the image for the board background
    boardBackgroundImage = new Image(maurersMarblesGame.getAssetManager().get(
        "board_background.png", Texture.class));
    boardBackgroundImage.setColor(0.75f, 0.75f, 0.75f, 1);
    boardGroup.addActor(boardBackgroundImage);

    // create the images for the board spaces
    spaceImages = new Image[game.getBoard().getNumberOfPlayableSpaces()];
    for (int i = 0; i < spaceImages.length; i++) {
      spaceImages[i] = new Image(maurersMarblesGame.getAssetManager().get("board_space.png",
          Texture.class));
      spaceImages[i].setColor(0.5f, 0.5f, 0.5f, 1);
      boardGroup.addActor(spaceImages[i]);
    }

    // set the colors for the player board spaces
    for (int playerNumber = 0; playerNumber < numberOfPlayers; playerNumber++) {
      spaceImages[game.getBoard().getSafeBoardIndex(playerNumber)].setColor(colors[playerNumber]);
      for (int boardIndex : game.getBoard().getHomeBoardIndexes(playerNumber)) {
        spaceImages[boardIndex].setColor(colors[playerNumber]);
      }
      for (int boardIndex : game.getBoard().getStartBoardIndexes(playerNumber)) {
        spaceImages[boardIndex].setColor(colors[playerNumber]);
      }
    }

    // create the images for the marbles
    marbleImages = new Image[numberOfPlayers][Game.NUMBER_OF_MARBLES_PER_PLAYER];
    for (int playerNumber = 0; playerNumber < numberOfPlayers; playerNumber++) {
      for (int marbleNumber = 0; marbleNumber < 4; marbleNumber++) {
        marbleImages[playerNumber][marbleNumber] = new Image(
            maurersMarblesGame.getAssetManager().get("board_space.png", Texture.class));
        marbleImages[playerNumber][marbleNumber].setColor(colors[playerNumber]);
        boardGroup.addActor(marbleImages[playerNumber][marbleNumber]);
        final int finalPlayerNumber = playerNumber;
        final int finalMarbleNumber = marbleNumber;
        marbleImages[playerNumber][marbleNumber].addListener(new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            clickedMarble(finalPlayerNumber, finalMarbleNumber);
          }
        });
      }
    }

    // create the images for the cards
    for (int i = 0; i < CardDeck.NUMBER_OF_CARDS_IN_FULL_DECK; i++) {
      Card card = game.getCardDeck().getUndealtCards().get(i);
      Image image = new Image(maurersMarblesGame.getAssetManager().get("card_"
          + card.toString().toLowerCase() + ".png", Texture.class));
      image.setVisible(false);
      cardImages.put(card.toString(), image);
      boardGroup.addActor(image);
    }

    // create the images for the split cards
    splitCardImages = new Image[7];
    int splitValue = 1;
    for (Card.Rank rank : new Card.Rank[]{Card.Rank.ACE, Card.Rank.TWO, Card.Rank.THREE,
      Card.Rank.FOUR, Card.Rank.FIVE, Card.Rank.SIX, Card.Rank.SEVEN}) {
      // TODO need images for split cards
      Image image = new Image(maurersMarblesGame.getAssetManager().get("card_"
          + rank.toString().toLowerCase() + "c.png", Texture.class));
      image.setVisible(false);
      final int finalSplitValue = splitValue;
      image.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          clickedSplitCard(finalSplitValue);
        }
      });
      splitCardImages[splitValue++ - 1] = image;
      boardGroup.addActor(image);
    }

    mainMenuLabel = new Label("Main Menu", new LabelStyle(new BitmapFont(), Color.GOLD));
    stage.addActor(mainMenuLabel);
    mainMenuLabel.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        maurersMarblesGame.setScreen(new MainMenuStageScreen(maurersMarblesGame));
      }
    });

  }

  protected void clickedCard(Card card) {
    if (waitForUserInput && game.getPlayers().get(USER_PLAYER_NUMBER).getCards().contains(card)) {
      setSelectedCard(card);
    }
  }

  protected void clickedMarble(int playerNumber, int marbleNumber) {

    if (!waitForUserInput) {
      return;
    }

    if (selectedCard != NO_CARD) {

      UserPlaySelector userPlaySelector = (UserPlaySelector) game.getPlayers()
          .get(USER_PLAYER_NUMBER).getPlaySelector();

      UserPlay userPlay;
      if (selectedCard.getRank().equals(Card.Rank.JACK)
          || (selectedCard.getRank().equals(Card.Rank.SEVEN) && selectedSplitValue != 7)) {

        if (selectedMarble1 == UserPlay.NO_MARBLE) {
          selectedMarble1 = game.getPlayers().get(playerNumber).getMarbles().get(marbleNumber);
          return;
        }

        selectedMarble2 = game.getPlayers().get(playerNumber).getMarbles().get(marbleNumber);
        userPlay = UserPlay.builder(0, selectedCard, selectedMarble1)
            .setMarble2(selectedMarble2)
            .setSplitValue1(selectedSplitValue)
            .build();

      } else {
        selectedMarble1 = game.getPlayers().get(playerNumber).getMarbles().get(marbleNumber);
        userPlay = UserPlay.builder(0, selectedCard, selectedMarble1).build();
      }

      try {
        Play play = game.validate(userPlay);
        waitForUserInput = false;
        userPlaySelector.setSelectedPlay(play);
        userPlaySelector.plays = null;
        resetUserInput();
        game.advance();
      } catch (InvalidPlayException ex) {
        // TODO display message to user
        resetUserInput();
      }

    }

  }

  protected void clickedSplitCard(int splitValue) {
    if (waitForUserInput) {
      selectedSplitValue = splitValue;
      for (Image image : splitCardImages) {
        image.setVisible(false);
      }
    }
  }

  protected Action getActionToDealCard(DealtCardGameEvent dealtCardGameEvent, boolean faceDown) {

    Card card = dealtCardGameEvent.getCard();
    Image image = cardImages.get(card.toString());

    // TODO decide where cards will come from
    Rectangle rectangleFrom = boardLayout.getBoundsForBoardIndex(game.getBoard()
        .getSafeBoardIndex(dealtCardGameEvent.getDealerPlayerNumber()));
    float fromX = rectangleFrom.getX() * boardGroup.getWidth();
    float fromY = (1.0f - rectangleFrom.getY()) * boardGroup.getHeight();

    Rectangle rectangleTo = boardLayout.getBoundsForBoardIndex(game.getBoard()
        .getHomeMinBoardIndex(dealtCardGameEvent.getRecipientPlayerNumber()) + 1);
    float toX = (rectangleTo.getX() + (rectangleTo.getWidth() / 2.0f))
        * boardGroup.getWidth() - (image.getWidth() / 2.0f);
    float toY = (1.0f - (rectangleTo.getY() + (rectangleTo.getHeight() / 2.0f)))
        * boardGroup.getHeight() - (image.getHeight() / 2.0f);

    float angle = boardLayout.getAngleForBoardIndex(game.getBoard().getHomeEntryBoardIndex(
        dealtCardGameEvent.getDealerPlayerNumber()));
    angle = 270.0f - (float) (angle * 180.0 / Math.PI);

    image.toFront();
    image.setOrigin(Align.center);
    image.setPosition(fromX, fromY);
    image.setRotation(angle);
    image.setVisible(true);
    if (faceDown) {
      // TODO need to figure out different image
      image.setColor(Color.FIREBRICK);
    }

    // apply a minor random rotation
    float toAngle = angle + (float) (Math.random() * 20.0f * 2.0f) - 20.0f;

    // apply random x,y offset
    toX += (float) ((Math.random() - 0.5f) * rectangleTo.getWidth() * boardGroup.getWidth() * 0.5f);
    toY += (float) ((Math.random() - 0.5f) * rectangleTo.getWidth() * boardGroup.getWidth() * 0.5f);

    ParallelAction parallelAction = new ParallelAction();

    MoveToAction moveToAction = Actions.action(MoveToAction.class);
    moveToAction.setPosition(toX, toY);
    moveToAction.setDuration(DURATION_DEAL_CARD);
    moveToAction.setActor(image);
    parallelAction.addAction(moveToAction);

    RotateToAction rotateToAction = Actions.rotateTo(toAngle);
    rotateToAction.setUseShortestDirection(true);
    rotateToAction.setDuration(DURATION_DEAL_CARD);
    rotateToAction.setActor(image);
    parallelAction.addAction(rotateToAction);

    return parallelAction;

  }

  protected Action getActionToMoveMarble(MovedMarbleGameEvent movedMarbleGameEvent) {

    Rectangle rectangle = boardLayout.getBoundsForMarble(
        movedMarbleGameEvent.getNewBoardIndex());
    float toX = rectangle.getX() * boardGroup.getWidth();
    float toY = (1.0f - rectangle.getY()) * boardGroup.getHeight()
        - rectangle.getHeight() * boardGroup.getHeight();

    ParallelAction parallelAction = new ParallelAction();

    MoveToAction moveToAction = Actions.action(MoveToAction.class);
    moveToAction.setPosition(toX, toY);
    moveToAction.setDuration(DURATION_MOVE_MARBLE);
    moveToAction.setActor(marbleImages[movedMarbleGameEvent
        .getPlayerNumber()][movedMarbleGameEvent.getMarbleNumber()]);
    parallelAction.addAction(moveToAction);

    RotateToAction rotateToAction = Actions.rotateTo(270.0f
        - (float) (boardLayout.getAngleForBoardIndex(
            movedMarbleGameEvent.getNewBoardIndex()) * 180.0 / Math.PI));
    rotateToAction.setUseShortestDirection(true);
    rotateToAction.setDuration(DURATION_MOVE_MARBLE);
    rotateToAction.setActor(marbleImages[movedMarbleGameEvent
        .getPlayerNumber()][movedMarbleGameEvent.getMarbleNumber()]);
    parallelAction.addAction(rotateToAction);

    return parallelAction;

  }

  protected Action getActionToPlayCard(PlayedCardGameEvent playedCardGameEvent) {

    Card card = playedCardGameEvent.getCard();
    Image image = cardImages.get(card.toString());

    Rectangle rectangleTo = boardLayout.getBoundsForDiscardPile();
    float toX = rectangleTo.getX() * boardGroup.getWidth();
    float toY = (1.0f - rectangleTo.getY()) * boardGroup.getHeight()
        - rectangleTo.getHeight() * boardGroup.getHeight();

    image.setColor(Color.WHITE);
    image.toFront();
    image.setVisible(true);

    ParallelAction parallelAction = new ParallelAction();

    MoveToAction moveToAction = Actions.action(MoveToAction.class);
    moveToAction.setPosition(toX, toY);
    moveToAction.setDuration(DURATION_PLAY_CARD);
    moveToAction.setActor(image);
    parallelAction.addAction(moveToAction);

    RotateToAction rotateToAction = Actions.rotateTo(0.0f);
    rotateToAction.setUseShortestDirection(true);
    rotateToAction.setDuration(DURATION_PLAY_CARD);
    rotateToAction.setActor(image);
    parallelAction.addAction(rotateToAction);

    return parallelAction;

  }

  protected void handleCannotPlayGameEvent(CannotPlayGameEvent cannotPlayGameEvent) {
    for (Card card : game.getPlayers().get(cannotPlayGameEvent.getPlayerNumber()).getCards()) {
      cardImages.get(card.toString()).setVisible(false);
    }
  }

  protected void handleExitedStateGameEvent(ExitedStateGameEvent exitedStateGameEvent) {

    SequenceAction sequenceAction = new SequenceAction();

    switch (exitedStateGameEvent.getState()) {

      case DETERMINE_DEALER:
      // falls through
      case DEAL_CARDS: {

        // TODO reset card deck?
        for (Event queuedEvent : queuedEvents) {
          if (queuedEvent instanceof DealtCardGameEvent) {
            sequenceAction.addAction(getActionToDealCard((DealtCardGameEvent) queuedEvent,
                exitedStateGameEvent.getState().equals(State.DEAL_CARDS)));
          }
        }

        RunnableAction runnableAction = new RunnableAction();
        runnableAction.setRunnable(new Runnable() {
          @Override
          public void run() {
            // TODO can change this to update cards once we figure out the discard pile
            for (Image image : cardImages.values()) {
              image.setVisible(false);
            }
            game.advance();
          }
        });
        sequenceAction.addAction(runnableAction);

        break;

      }
      case PLAYER_TURN: {

        for (Event queuedEvent : queuedEvents) {
          if (queuedEvent instanceof PlayedCardGameEvent) {
            sequenceAction.addAction(getActionToPlayCard((PlayedCardGameEvent) queuedEvent));
          } else if (queuedEvent instanceof MovedMarbleGameEvent) {
            sequenceAction.addAction(getActionToMoveMarble((MovedMarbleGameEvent) queuedEvent));
          }
        }

        if (exitedStateGameEvent.getState() == State.PLAYER_TURN && waitForUserInput) {

          updateCards();

          for (Card card : game.getPlayers().get(USER_PLAYER_NUMBER).getCards()) {
            final Card finalCard = card;
            Image image = cardImages.get(card.toString());
            image.clearListeners();
            image.addListener(new ClickListener() {
              @Override
              public void clicked(InputEvent event, float x, float y) {
                clickedCard(finalCard);
              }
            });

          }

        } else {

          updateCards();
          RunnableAction runnableAction = new RunnableAction();
          runnableAction.setRunnable(new Runnable() {
            @Override
            public void run() {
              game.advance();
            }
          });
          sequenceAction.addAction(runnableAction);

        }

        break;
      }

      case GAME_OVER: {
        maurersMarblesGame.setScreen(new GameOverStageScreen(maurersMarblesGame, gameStats));
        break;
      }

    }

    queuedEvents.clear();

    // add the action
    stage.addAction(sequenceAction);
    Gdx.graphics.requestRendering();

  }

  public void setSelectedCard(Card card) {

    if (selectedCard == card) {
      resetUserInput();
    } else {
      selectedCard = card;
    }

    if (selectedCard != NO_CARD && selectedCard.getRank().equals(Card.Rank.SEVEN)) {

      // TODO move to above the seven card
      float x = boardGroup.getWidth();
      for (Image image : splitCardImages) {
        image.setPosition(x, 0);
        image.setVisible(true);
        x += 40;
      }

    } else {
      for (Image image : splitCardImages) {
        image.setVisible(false);
      }
    }

    // reset all cards to not be highlighted
    // TODO Can this be just the player cards?
    for (Card c : game.getPlayers().get(USER_PLAYER_NUMBER).getCards()) {
      cardImages.get(c.toString()).setColor(Color.WHITE);
    }

    // update the selected card to be highlighted
    if (selectedCard != NO_CARD) {
      cardImages.get(selectedCard.toString()).setVisible(true);
      cardImages.get(selectedCard.toString()).setColor(Color.YELLOW);
    }

  }

  protected void resetUserInput() {
    setSelectedCard(NO_CARD);
    selectedMarble1 = UserPlay.NO_MARBLE;
    selectedMarble2 = UserPlay.NO_MARBLE;
    selectedSplitValue = UserPlay.NO_SPLIT_VALUE;
  }

  protected void updateCards() {

    // reset all of the cards
    // TODO need to revisit to make more efficient
    for (Image image : cardImages.values()) {
      image.setRotation(0.0f);
      image.setColor(Color.WHITE);
    }

    // update the player cards
    for (int playerNumber = 0; playerNumber < game.getNumberOfPlayers(); playerNumber++) {

      float angle = boardLayout.getAngleForBoardIndex(game.getBoard().getHomeEntryBoardIndex(
          playerNumber));

      List<Card> playerCards = game.getPlayers().get(playerNumber).getCards();
      for (int i = 0; i < playerCards.size(); i++) {

        Card card = playerCards.get(i);

        Image image = cardImages.get(card.toString());
        float centerX = ((0.5f * boardGroup.getWidth()) + (image.getHeight() / 2.0f))
            * (float) Math.cos(angle + Math.PI) + (boardGroup.getWidth() / 2.0f)
            - (image.getWidth() / 2.0f);
        float centerY = ((-0.5f * boardGroup.getHeight()) - (image.getHeight() / 2.0f))
            * (float) Math.sin(angle + Math.PI) + (boardGroup.getHeight() / 2.0f)
            - (image.getHeight() / 2.0f);

        // horizontal hand
        /*float x = centerX + (i * image.getWidth() - (image.getWidth() * playerCards.size() / 2.0f)
            + image.getWidth() / 2.0f) * (float) Math.cos(angle + Math.PI / 2.0f);
        float y = centerY - (i * image.getWidth() - (image.getWidth() * playerCards.size() / 2.0f)
            + image.getWidth() / 2.0f) * (float) Math.sin(angle + Math.PI / 2.0f);*/
        // compact hand
        float visible = 0.3f;
        float width = image.getWidth();
        if (playerCards.size() > 1) {
          width += image.getWidth() * visible * (playerCards.size() - 1);
        }
        float x = centerX + (((image.getWidth() / 2.0f) - (width / 2.0f)
            + (i * image.getWidth() * visible)) * (float) Math.cos(angle + Math.PI / 2.0f));
        float y = centerY - (((image.getWidth() / 2.0f) - (width / 2.0f)
            + (i * image.getWidth() * visible)) * (float) Math.sin(angle + Math.PI / 2.0f));

        image.setColor(playerNumber == 0 ? Color.WHITE : Color.FIREBRICK);
        image.setPosition(x, y);
        image.setOrigin(Align.center);
        image.setRotation(270.0f - (float) (angle * 180.0 / Math.PI));
        image.setVisible(true);

      }

    }

  }

  @Override
  public void handleEvent(Event event) {
    if (event instanceof CannotPlayGameEvent) {
      handleCannotPlayGameEvent((CannotPlayGameEvent) event);
    } else if (event instanceof ExitedStateGameEvent) {
      handleExitedStateGameEvent((ExitedStateGameEvent) event);
    } else {
      queuedEvents.add(event);
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

    // TODO Discard pile does not get updated on resize, keep a list of discarded cards?
    mainMenuLabel.setPosition(-width / 2.0f, -height / 2.0f);

    float size = height;
    if (width < height) {
      size = width;
    }
    boardGroup.setSize(size * 0.9f, size * 0.9f);
    boardGroup.setPosition(-boardGroup.getWidth() / 2.0f,
        (size * 0.95f / 2.0f) - boardGroup.getHeight());
    boardBackgroundImage.setSize(boardGroup.getWidth(), boardGroup.getHeight());

    for (int i = 0; i < spaceImages.length; i++) {
      com.exit104.maurersmarbles.Rectangle rectangle = boardLayout.getBoundsForSpace(i);
      float spaceHeight = rectangle.getHeight() * boardGroup.getHeight();
      spaceImages[i].setPosition(rectangle.getX() * boardGroup.getWidth(),
          (1.0f - rectangle.getY()) * boardGroup.getHeight() - spaceHeight);
      spaceImages[i].setSize(rectangle.getWidth() * boardGroup.getWidth(), spaceHeight);
      spaceImages[i].setOrigin(Align.center);
      spaceImages[i].setRotation(270.0f - (float) (boardLayout.getAngleForBoardIndex(i)
          * 180.0 / Math.PI));
    }

    for (Player player : game.getPlayers()) {
      for (Marble marble : player.getMarbles()) {
        com.exit104.maurersmarbles.Rectangle rectangle = boardLayout.getBoundsForMarble(
            marble.getBoardIndex());
        float marbleHeight = rectangle.getHeight() * boardGroup.getHeight();
        marbleImages[player.getPlayerNumber()][marble.getMarbleNumber()].setSize(
            rectangle.getWidth() * boardGroup.getWidth(), marbleHeight);
        marbleImages[player.getPlayerNumber()][marble.getMarbleNumber()].setPosition(
            rectangle.getX() * boardGroup.getWidth(),
            (1.0f - rectangle.getY()) * boardGroup.getHeight() - marbleHeight);
        marbleImages[player.getPlayerNumber()][marble.getMarbleNumber()].setOrigin(Align.center);
        marbleImages[player.getPlayerNumber()][marble.getMarbleNumber()].setRotation(270.0f
            - (float) (boardLayout.getAngleForBoardIndex(marble.getBoardIndex())
            * 180.0 / Math.PI));
      }
    }

    Rectangle rectangle = boardLayout.getBoundsForDiscardPile();
    for (Map.Entry<String, Image> entry : cardImages.entrySet()) {
      entry.getValue().setSize(rectangle.getWidth() * boardGroup.getWidth(),
          rectangle.getHeight() * boardGroup.getHeight());
      entry.getValue().setSize(entry.getValue().getWidth(), entry.getValue().getHeight());
    }

    for (Image image : splitCardImages) {
      image.setSize(rectangle.getWidth() * boardGroup.getWidth(),
          rectangle.getHeight() * boardGroup.getHeight());
    }

    updateCards();

    stage.getViewport().update(width, height);

  }

  @Override
  public void resume() {
    // TODO
  }

  @Override
  public void show() {

    super.show();

    // TODO need to account for leaving screen and coming back
    // start the game
    game.advance();

  }

  protected class UserPlaySelector extends PlaySelector {

    Set<Play> plays;

    @Override
    public void setAvailablePlays(Set<Play> plays) {
      super.setAvailablePlays(plays);
      //setSelectedPlay(plays.iterator().next());
      this.plays = plays;
      waitForUserInput = true;
    }

  }

}
