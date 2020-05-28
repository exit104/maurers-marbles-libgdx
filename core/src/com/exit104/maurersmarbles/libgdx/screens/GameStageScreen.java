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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.actions.ParallelAction;
import com.badlogic.gdx.scenes.scene2d.actions.RotateToAction;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.actions.SizeToAction;
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
import com.exit104.maurersmarbles.event.ShuffledCardDeckGameEvent;
import com.exit104.maurersmarbles.libgdx.MaurersMarblesGame;

import java.util.ArrayList;
import java.util.LinkedHashSet;
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
   * The percent of the card width to overlap the player cards.
   */
  protected static final float CARD_OVERLAP = 0.25f;
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
   * The groups used to manage the player display areas.
   */
  protected final transient Group[] playerGroups;
  /**
   * The image used as the background for the game board.
   */
  protected final transient Image boardBackgroundImage;
  /**
   * The array of images for the spaces on the board. The index into the array is the board index
   * and the value is the image for that board space.
   */
  protected final transient Image[] boardSpaceImages;
  /**
   * The array of images for the player background. The index into the array is the player number
   * and the value is the image for that player.
   */
  protected final transient Image[] playerBackgroundImages;
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
   * The labels for the player names. The index into the array is the player number and the value is
   * the label for that player.
   */
  protected final transient Label[] playerLabels;
  /**
   * The list of cards from the cannot play game event.
   */
  protected final transient List<Card> cannotPlayPlayerCards = new ArrayList<>();
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
  /**
   * The set of card images in the discard pile.
   */
  protected final transient Set<Image> discardPile = new LinkedHashSet<>();

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
    // TODO define const image names
    maurersMarblesGame.getAssetManager().load("arrow.png", Texture.class);
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
    maurersMarblesGame.getAssetManager().load("player_background.png", Texture.class);
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
    boardSpaceImages = new Image[game.getBoard().getNumberOfPlayableSpaces()];
    for (int i = 0; i < boardSpaceImages.length; i++) {
      boardSpaceImages[i] = new Image(maurersMarblesGame.getAssetManager().get("board_space.png",
          Texture.class));
      boardSpaceImages[i].setColor(0.5f, 0.5f, 0.5f, 1);
      boardGroup.addActor(boardSpaceImages[i]);
    }

    // set the colors for the player board spaces
    for (int playerNumber = 0; playerNumber < numberOfPlayers; playerNumber++) {
      boardSpaceImages[game.getBoard().getSafeBoardIndex(playerNumber)].setColor(
          colors[playerNumber]);
      for (int boardIndex : game.getBoard().getHomeBoardIndexes(playerNumber)) {
        boardSpaceImages[boardIndex].setColor(colors[playerNumber]);
      }
      for (int boardIndex : game.getBoard().getStartBoardIndexes(playerNumber)) {
        boardSpaceImages[boardIndex].setColor(colors[playerNumber]);
      }
    }

    // create the images for the marbles
    marbleImages = new Image[numberOfPlayers][Game.NUMBER_OF_MARBLES_PER_PLAYER];
    for (int playerNumber = 0; playerNumber < numberOfPlayers; playerNumber++) {
      for (int marbleNumber = 0; marbleNumber < Game.NUMBER_OF_MARBLES_PER_PLAYER; marbleNumber++) {
        marbleImages[playerNumber][marbleNumber] = new Image(
            maurersMarblesGame.getAssetManager().get("marble.png", Texture.class));
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
      cardImages.put(card.toString(), image);
      stage.addActor(image);
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
      stage.addActor(image);
    }

    // create the player display areas
    playerGroups = new Group[numberOfPlayers];
    playerBackgroundImages = new Image[numberOfPlayers];
    playerLabels = new Label[numberOfPlayers];
    for (int i = 0; i < numberOfPlayers; i++) {
      playerGroups[i] = new Group();
      stage.addActor(playerGroups[i]);
      playerBackgroundImages[i] = new Image(maurersMarblesGame.getAssetManager().get(
          "player_background.png", Texture.class));
      playerBackgroundImages[i].setColor(colors[i]);
      playerGroups[i].addActor(playerBackgroundImages[i]);
      playerLabels[i] = new Label(" Player " + i, new Label.LabelStyle(new BitmapFont(),
          Color.GOLD));
      playerGroups[i].addActor(playerLabels[i]);
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

    float fromX = playerGroups[dealtCardGameEvent.getDealerPlayerNumber()].getX();
    float fromY = playerGroups[dealtCardGameEvent.getDealerPlayerNumber()].getY();

    Image boardSpaceImage = boardSpaceImages[game.getBoard().getHomeMinBoardIndex(
        dealtCardGameEvent.getRecipientPlayerNumber()) + 1];
    Vector2 stageCoordinates = boardGroup.localToStageCoordinates(new Vector2(
        boardSpaceImage.getX() + (boardSpaceImage.getWidth() / 2.0f),
        boardSpaceImage.getY() + (boardSpaceImage.getHeight() / 2.0f)));

    float toX = stageCoordinates.x - (image.getWidth() / 2.0f);
    float toY = stageCoordinates.y - (image.getHeight() / 2.0f);

    image.toFront();
    image.setPosition(fromX, fromY);
    image.setVisible(true);

    if (faceDown) {
      // TODO need to figure out different image
      image.setColor(Color.FIREBRICK);
    }

    // apply a minor random rotation
    float toAngle = (float) (Math.random() * 20.0f * 2.0f) - 20.0f;

    // apply random x,y offset
    toX += (float) ((Math.random() - 0.5f) * image.getWidth() * 0.25f);
    toY += (float) ((Math.random() - 0.5f) * image.getWidth() * 0.25f);

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

    Image image = marbleImages[movedMarbleGameEvent.getPlayerNumber()][movedMarbleGameEvent
        .getMarbleNumber()];
    image.toFront();

    ParallelAction parallelAction = new ParallelAction();

    MoveToAction moveToAction = Actions.action(MoveToAction.class);
    moveToAction.setPosition(toX, toY);
    moveToAction.setDuration(DURATION_MOVE_MARBLE);
    moveToAction.setActor(image);
    parallelAction.addAction(moveToAction);

    RotateToAction rotateToAction = Actions.rotateTo(270.0f
        - (float) (boardLayout.getAngleForBoardIndex(
            movedMarbleGameEvent.getNewBoardIndex()) * 180.0 / Math.PI));
    rotateToAction.setUseShortestDirection(true);
    rotateToAction.setDuration(DURATION_MOVE_MARBLE);
    rotateToAction.setActor(image);
    parallelAction.addAction(rotateToAction);

    return parallelAction;

  }

  protected Action getActionToPlayCard(PlayedCardGameEvent playedCardGameEvent) {

    Card card = playedCardGameEvent.getCard();
    final Image image = cardImages.get(card.toString());

    Rectangle rectangleTo = boardLayout.getBoundsForDiscardPile();
    float toX = rectangleTo.getX() * boardGroup.getWidth() + boardGroup.getX();
    float toY = (1.0f - rectangleTo.getY()) * boardGroup.getHeight()
        - rectangleTo.getHeight() * boardGroup.getHeight() + boardGroup.getY();

    // TODO should this be here or in the action?
    image.toFront();
    image.setVisible(true);

    ParallelAction parallelAction = new ParallelAction();

    MoveToAction moveToAction = Actions.action(MoveToAction.class);
    moveToAction.setPosition(toX, toY);
    moveToAction.setDuration(DURATION_PLAY_CARD);
    moveToAction.setActor(image);
    parallelAction.addAction(moveToAction);

    SizeToAction sizeToAction = Actions.sizeTo(rectangleTo.getWidth() * boardGroup.getWidth(),
        rectangleTo.getHeight() * boardGroup.getHeight());
    sizeToAction.setDuration(DURATION_PLAY_CARD);
    sizeToAction.setActor(image);
    parallelAction.addAction(sizeToAction);

    RotateToAction rotateToAction = Actions.rotateTo(0.0f);
    rotateToAction.setUseShortestDirection(true);
    rotateToAction.setDuration(DURATION_PLAY_CARD);
    rotateToAction.setActor(image);
    parallelAction.addAction(rotateToAction);

    RunnableAction runnableAction = Actions.run(new Runnable() {
      @Override
      public void run() {
        discardPile.add(image);
      }
    });

    SequenceAction sequenceAction = Actions.sequence();
    sequenceAction.addAction(parallelAction);
    sequenceAction.addAction(runnableAction);

    return sequenceAction;

  }

  protected void handleCannotPlayGameEvent(CannotPlayGameEvent cannotPlayGameEvent) {
    cannotPlayPlayerCards.addAll(game.getPlayers().get(
        cannotPlayGameEvent.getPlayerNumber()).getCards());
  }

  protected void handleExitedStateGameEvent(ExitedStateGameEvent exitedStateGameEvent) {

    SequenceAction sequenceAction = new SequenceAction();

    switch (exitedStateGameEvent.getState()) {

      case DETERMINE_DEALER:
      // falls through
      case DEAL_CARDS: {

        updateCardActors();

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
            updateCardActors();
            game.advance();
          }
        });
        sequenceAction.addAction(runnableAction);

        break;

      }
      case PLAYER_TURN: {

        for (Event queuedEvent : queuedEvents) {
          if (queuedEvent instanceof CannotPlayGameEvent) {
            ParallelAction parallelAction = Actions.parallel();
            for (Card card : cannotPlayPlayerCards) {
              parallelAction.addAction(getActionToPlayCard(new PlayedCardGameEvent(game,
                  ((CannotPlayGameEvent) queuedEvent).getPlayerNumber(), card)));
            }
            cannotPlayPlayerCards.clear();
            sequenceAction.addAction(parallelAction);
          } else if (queuedEvent instanceof MovedMarbleGameEvent) {
            sequenceAction.addAction(getActionToMoveMarble((MovedMarbleGameEvent) queuedEvent));
          } else if (queuedEvent instanceof PlayedCardGameEvent) {
            sequenceAction.addAction(getActionToPlayCard((PlayedCardGameEvent) queuedEvent));
          }
        }

        if (exitedStateGameEvent.getState() == State.PLAYER_TURN && waitForUserInput) {

          updateCardActors();

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

          updateCardActors();
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

  protected void handleShuffledCardDeckGameEvent(
      ShuffledCardDeckGameEvent shuffledCardDeckGameEvent) {
    discardPile.clear();
  }

  public void setSelectedCard(Card card) {

    if (selectedCard == card) {
      resetUserInput();
    } else {
      selectedCard = card;
    }

    if (selectedCard != NO_CARD && selectedCard.getRank().equals(Card.Rank.SEVEN)) {

      Image selectedCardImage = cardImages.get(selectedCard.toString());
      float y = selectedCardImage.getY() + selectedCardImage.getHeight();
      float x = selectedCardImage.getX();
      for (Image image : splitCardImages) {
        image.setPosition(x, y);
        image.setVisible(true);
        image.toFront();
        x += selectedCardImage.getWidth() * (1.0f - CARD_OVERLAP);
      }

    } else {
      for (Image image : splitCardImages) {
        image.setVisible(false);
      }
    }

    // reset all cards to not be highlighted
    for (Card playerCard : game.getPlayers().get(USER_PLAYER_NUMBER).getCards()) {
      cardImages.get(playerCard.toString()).setColor(Color.WHITE);
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

  public void updateBoardActors() {

    // update the size and position of the group
    boardGroup.setSize(viewport.getMinWorldHeight(), viewport.getMinWorldHeight());
    boardGroup.setPosition(-boardGroup.getWidth() / 2.0f, -boardGroup.getHeight() / 2.0f);

    // rotate the board to align with the player locations on the screen
    float angle0 = boardLayout.getAngleForBoardIndex(game.getBoard().getHomeEntryBoardIndex(0));
    float angle1 = boardLayout.getAngleForBoardIndex(game.getBoard().getHomeEntryBoardIndex(1));
    boardGroup.setOrigin(Align.center);
    boardGroup.setRotation((angle0 - angle1) / 2.0f * 180.0f / (float) Math.PI);

    // update the board background image, unrotate the image so it is still a square
    boardBackgroundImage.setSize(boardGroup.getWidth(), boardGroup.getHeight());
    boardBackgroundImage.setOrigin(Align.center);
    boardBackgroundImage.setRotation(-boardGroup.getRotation());

    // update the board spaces
    for (int boardIndex = 0; boardIndex < boardSpaceImages.length; boardIndex++) {
      Rectangle rectangle = boardLayout.getBoundsForSpace(boardIndex);
      float boardSpaceHeight = rectangle.getHeight() * boardGroup.getHeight();
      boardSpaceImages[boardIndex].setSize(rectangle.getWidth() * boardGroup.getWidth(),
          boardSpaceHeight);
      boardSpaceImages[boardIndex].setPosition(rectangle.getX() * boardGroup.getWidth(),
          (1.0f - rectangle.getY()) * boardGroup.getHeight() - boardSpaceHeight);
      boardSpaceImages[boardIndex].setOrigin(Align.center);
      boardSpaceImages[boardIndex].setRotation(270.0f
          - (float) (boardLayout.getAngleForBoardIndex(boardIndex) * 180.0 / Math.PI));
    }

  }

  public void updateCardActors() {

    float maxCardWidth = playerGroups[0].getWidth() * 0.95f;
    float cardWidth = maxCardWidth / (1 + (4 * (1.0f - CARD_OVERLAP)));
    float cardHeight = cardWidth * (Card.HEIGHT / Card.WIDTH);

    // update the player cards
    for (int playerNumber = 0; playerNumber < game.getNumberOfPlayers(); playerNumber++) {

      List<Card> playerCards = game.getPlayers().get(playerNumber).getCards();
      for (int cardNumber = 0; cardNumber < playerCards.size(); cardNumber++) {

        Card card = playerCards.get(cardNumber);

        Image image = cardImages.get(card.toString());

        image.setSize(cardWidth, cardHeight);

        float playerCardsWidth = image.getWidth()
            + ((playerCards.size() - 1) * image.getWidth() * (1.0f - CARD_OVERLAP));
        float x = playerGroups[playerNumber].getX()
            + (playerGroups[playerNumber].getWidth() / 2.0f) - (playerCardsWidth / 2.0f)
            + (cardNumber * image.getWidth() * (1.0f - CARD_OVERLAP));
        float y = playerGroups[playerNumber].getY() + playerGroups[playerNumber].getHeight()
            - (playerLabels[playerNumber].getHeight()) - cardHeight;
        image.setPosition(x, y);

        image.setOrigin(Align.center);
        image.setRotation(0.0f);

        image.setColor(playerNumber == 0 ? Color.WHITE : Color.FIREBRICK);
        image.setVisible(true);

      }

    }

    for (Image image : splitCardImages) {
      image.setSize(cardWidth, cardHeight);
    }

    Rectangle rectangle = boardLayout.getBoundsForDiscardPile();
    for (Image image : discardPile) {
      image.setSize(rectangle.getWidth() * boardGroup.getWidth(),
          rectangle.getHeight() * boardGroup.getHeight());
      image.setPosition(rectangle.getX() * boardGroup.getWidth() + boardGroup.getX(),
          (1.0f - rectangle.getY()) * boardGroup.getHeight()
          - rectangle.getHeight() * boardGroup.getHeight() + boardGroup.getY());
    }

    for (Card card : game.getCardDeck().getUndealtCards()) {
      Image image = cardImages.get(card.toString());
      image.setPosition(playerGroups[game.getCurrentDealer()].getX(),
          playerGroups[game.getCurrentDealer()].getY());
      image.setSize(cardWidth, cardHeight);
      image.setRotation(0.0f);
      image.setColor(Color.WHITE);
    }

    // update the depth order of the player actors and cards
    for (int i = game.getNumberOfTeams(); i < game.getNumberOfPlayers(); i++) {
      playerGroups[i].toFront();
      for (Card card : game.getPlayers().get(i).getCards()) {
        Image image = cardImages.get(card.toString());
        image.toFront();
      }
    }
    for (int i = game.getNumberOfTeams() - 1; i >= 0; i--) {
      playerGroups[i].toFront();
      for (Card card : game.getPlayers().get(i).getCards()) {
        Image image = cardImages.get(card.toString());
        image.toFront();
      }
    }

  }

  public void updateMarbleActors() {

    for (Player player : game.getPlayers()) {

      for (Marble marble : player.getMarbles()) {

        Rectangle rectangle = boardLayout.getBoundsForMarble(marble.getBoardIndex());
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

  }

  public void updatePlayerActors() {

    for (Player player : game.getPlayers()) {

      int playerNumber = player.getPlayerNumber();

      playerGroups[playerNumber].setSize(
          (viewport.getWorldWidth() - boardGroup.getWidth()) / 2.0f,
          viewport.getMinWorldHeight() / game.getNumberOfTeams());
      float playerGroupX, playerGroupY;
      if (playerNumber < game.getNumberOfTeams()) {
        // left side of board
        playerGroupX = -viewport.getWorldWidth() / 2.0f;
        playerGroupY = -(viewport.getMinWorldHeight() / 2.0f)
            + (playerGroups[playerNumber].getHeight() * playerNumber);
      } else {
        // right side of board
        playerGroupX = viewport.getWorldWidth() / 2.0f - playerGroups[playerNumber].getWidth();
        playerGroupY = (viewport.getMinWorldHeight() / 2.0f)
            - (playerGroups[playerNumber].getHeight()
            * (playerNumber - game.getNumberOfTeams() + 1));
      }
      playerGroups[playerNumber].setPosition(playerGroupX, playerGroupY);

      playerBackgroundImages[playerNumber].setSize(playerGroups[playerNumber].getWidth(),
          playerGroups[playerNumber].getHeight());

      playerLabels[playerNumber].setY(playerGroups[playerNumber].getHeight()
          - playerLabels[playerNumber].getHeight());

    }

  }

  @Override
  public void handleEvent(Event event) {
    if (event instanceof CannotPlayGameEvent) {
      handleCannotPlayGameEvent((CannotPlayGameEvent) event);
      queuedEvents.add(event);
    } else if (event instanceof ExitedStateGameEvent) {
      handleExitedStateGameEvent((ExitedStateGameEvent) event);
    } else if (event instanceof ShuffledCardDeckGameEvent) {
      handleShuffledCardDeckGameEvent((ShuffledCardDeckGameEvent) event);
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
    Gdx.gl.glClearColor(3.0f / 255.0f, 105.0f / 255.0f, 42.0f / 255.0f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    stage.act();
    stage.draw();

  }

  @Override
  public void resize(int width, int height) {

    super.resize(width, height);

    // TODO remove main menu label
    mainMenuLabel.setPosition(-viewport.getWorldHeight() / 2.0f, -viewport.getWorldHeight() / 2.0f);

    updateBoardActors();
    updateMarbleActors();
    updatePlayerActors();
    updateCardActors();

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
