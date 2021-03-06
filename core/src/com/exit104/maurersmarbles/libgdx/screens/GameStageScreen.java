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

import com.exit104.maurersmarbles.Card;
import com.exit104.maurersmarbles.CardDeck;
import com.exit104.maurersmarbles.Game;
import com.exit104.maurersmarbles.Game.State;
import com.exit104.maurersmarbles.GameStats;
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
import com.exit104.maurersmarbles.ui.BoardView;
import com.exit104.maurersmarbles.ui.CardHandView;
import com.exit104.maurersmarbles.ui.DefaultScreenLayout;
import com.exit104.maurersmarbles.ui.GridBoardView;
import com.exit104.maurersmarbles.ui.HorizontalCardHandView;
import com.exit104.maurersmarbles.ui.ScreenLayout;

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
   * The board actor for the game board.
   */
  protected final transient BoardActor boardActor;
  /**
   * The board view used to draw the game board.
   */
  protected final transient BoardView boardView;
  /**
   * The card value used to indicate no card.
   */
  protected static final Card NO_CARD = null;
  /**
   * The user card that is currently selected.
   */
  protected transient Card selectedCard = NO_CARD;
  /**
   * The array of card actors for the split cards (when a seven is split). The index into the array
   * is the split value - 1 and the value is the card actor for that split card.
   */
  protected final transient CardActor[] splitCardActors;
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
   * The user player number.
   */
  protected static final int USER_PLAYER_NUMBER = 0;
  /**
   * The split value that is currently selected by the user.
   */
  protected transient int selectedSplitValue = UserPlay.NO_SPLIT_VALUE;
  /**
   * The list of cards from the cannot play game event.
   */
  protected final transient List<Card> cannotPlayPlayerCards = new ArrayList<>();
  /**
   * The list of events that were fired since the last game state change.
   */
  protected final transient List<Event> queuedEvents = new ArrayList<>();
  /**
   * The map that contains the card actors for the cards. The key into the map is the card string
   * (toString()), and the value is the card actor for that card.
   */
  protected final transient Map<String, CardActor> cardActors = new TreeMap<>();
  /**
   * The array of marble actors for the marbles. The index into the first array is the player number
   * and the index into the second array is the marble number and the value is the marble actor for
   * that marble.
   */
  protected final transient MarbleActor[][] marbleActors;
  /**
   * The first marble that is currently selected by the user.
   */
  protected transient Marble selectedMarble1 = UserPlay.NO_MARBLE;
  /**
   * The second marble that is currently selected by the user.
   */
  protected transient Marble selectedMarble2 = UserPlay.NO_MARBLE;
  /**
   * The array of player actors. the index into the array is the player number and the value is the
   * player actor for that player.
   */
  protected final transient PlayerActor[] playerActors;
  /**
   * The set of card actors in the discard pile.
   */
  protected final transient Set<CardActor> discardPile = new LinkedHashSet<>();

  // debugging/working
  protected transient Label mainMenuLabel;
  boolean firstResize = true;
  boolean portrait = false;
  ScreenLayout screenLayout;
  CardHandView cardHandView;

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

    screenLayout = new DefaultScreenLayout(game.getNumberOfPlayers());
    cardHandView = new HorizontalCardHandView();
    ((HorizontalCardHandView) cardHandView).setPaddingBottomScaleFactor(0.01f);
    ((HorizontalCardHandView) cardHandView).setPaddingLeftScaleFactor(0.01f);
    ((HorizontalCardHandView) cardHandView).setPaddingRightScaleFactor(0.01f);
    ((HorizontalCardHandView) cardHandView).setPaddingTopScaleFactor(0.01f);
    ((HorizontalCardHandView) cardHandView).setSpaceBetweenCardsScaleFactor(-0.05f);

    // create the board view and board actor
    boardView = new GridBoardView(game.getBoard());
    boardActor = new BoardActor();
    stage.addActor(boardActor);

    // create the actors for the marbles
    marbleActors = new MarbleActor[numberOfPlayers][Game.NUMBER_OF_MARBLES_PER_PLAYER];
    for (int playerNumber = 0; playerNumber < numberOfPlayers; playerNumber++) {
      for (int marbleNumber = 0; marbleNumber < Game.NUMBER_OF_MARBLES_PER_PLAYER; marbleNumber++) {
        MarbleActor marbleActor = new MarbleActor(playerNumber);
        marbleActors[playerNumber][marbleNumber] = marbleActor;
        boardActor.addActor(marbleActor);
        final int finalPlayerNumber = playerNumber;
        final int finalMarbleNumber = marbleNumber;
        marbleActor.addListener(new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            clickedMarble(finalPlayerNumber, finalMarbleNumber);
          }
        });
      }
    }

    // create the actors for the cards
    for (int i = 0; i < CardDeck.NUMBER_OF_CARDS_IN_FULL_DECK; i++) {
      Card card = game.getCardDeck().getUndealtCards().get(i);
      CardActor cardActor = new CardActor(card);
      cardActors.put(card.toString(), cardActor);
      stage.addActor(cardActor);
    }

    // create the actors for the split cards
    splitCardActors = new CardActor[7];
    int splitValue = 1;
    for (Card.Rank rank : new Card.Rank[]{Card.Rank.ACE, Card.Rank.TWO, Card.Rank.THREE,
      Card.Rank.FOUR, Card.Rank.FIVE, Card.Rank.SIX, Card.Rank.SEVEN}) {
      // TODO need images for split cards
      CardActor cardActor = new CardActor(new Card(rank, Card.Suit.CLUBS));
      cardActor.setVisible(false);
      final int finalSplitValue = splitValue;
      cardActor.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          clickedSplitCard(finalSplitValue);
        }
      });
      splitCardActors[splitValue++ - 1] = cardActor;
      stage.addActor(cardActor);
    }

    // create the actors for the players
    playerActors = new PlayerActor[numberOfPlayers];
    for (int playerNumber = 0; playerNumber < numberOfPlayers; playerNumber++) {
      PlayerActor playerActor = new PlayerActor(playerNumber);
      playerActors[playerNumber] = playerActor;
      stage.addActor(playerActor);
    }

    mainMenuLabel = new Label("Main Menu", new LabelStyle(new BitmapFont(true), Color.GOLD));
    mainMenuLabel.setPosition(0, 0);
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
      for (CardActor cardActor : splitCardActors) {
        cardActor.setVisible(false);
      }
    }
  }

  protected Action getActionToDealCard(DealtCardGameEvent dealtCardGameEvent,
      final boolean faceDown) {

    Card card = dealtCardGameEvent.getCard();
    final CardActor cardActor = cardActors.get(card.toString());

    Image boardSpaceImage = boardActor.boardSpaceImages[game.getBoard().getHomeMinBoardIndex(
        dealtCardGameEvent.getRecipientPlayerNumber()) + 1];
    Vector2 stageCoordinates = boardActor.localToStageCoordinates(new Vector2(
        boardSpaceImage.getX() + (boardSpaceImage.getWidth() / 2.0f),
        boardSpaceImage.getY() + (boardSpaceImage.getHeight() / 2.0f)));

    float toX = stageCoordinates.x - (cardActor.getWidth() / 2.0f);
    float toY = stageCoordinates.y - (cardActor.getHeight() / 2.0f);

    // apply a minor random rotation
    float toAngle = (float) (Math.random() * 20.0f * 2.0f) - 20.0f;

    // apply random x,y offset
    toX += (float) ((Math.random() - 0.5f) * cardActor.getWidth() * 0.25f);
    toY += (float) ((Math.random() - 0.5f) * cardActor.getWidth() * 0.25f);

    SequenceAction sequenceAction = Actions.sequence();

    RunnableAction runnableAction = Actions.run(new Runnable() {
      @Override
      public void run() {
        cardActor.toFront();
        cardActor.setFaceDown(faceDown);
      }
    });
    sequenceAction.addAction(runnableAction);

    ParallelAction parallelAction = new ParallelAction();

    MoveToAction moveToAction = Actions.action(MoveToAction.class);
    moveToAction.setPosition(toX, toY);
    moveToAction.setDuration(DURATION_DEAL_CARD);
    moveToAction.setActor(cardActor);
    parallelAction.addAction(moveToAction);

    RotateToAction rotateToAction = Actions.rotateTo(toAngle);
    rotateToAction.setUseShortestDirection(true);
    rotateToAction.setDuration(DURATION_DEAL_CARD);
    rotateToAction.setActor(cardActor);
    parallelAction.addAction(rotateToAction);

    sequenceAction.addAction(parallelAction);

    return sequenceAction;

  }

  protected Action getActionToMoveMarble(MovedMarbleGameEvent movedMarbleGameEvent) {

    Rectangle rectangle = boardView.getBoundsForMarble(movedMarbleGameEvent.getNewBoardIndex());
    float toX = rectangle.getX();
    float toY = rectangle.getY();

    MarbleActor marbleActor = marbleActors[movedMarbleGameEvent
        .getPlayerNumber()][movedMarbleGameEvent.getMarbleNumber()];
    marbleActor.toFront();

    ParallelAction parallelAction = new ParallelAction();

    MoveToAction moveToAction = Actions.action(MoveToAction.class);
    moveToAction.setPosition(toX, toY);
    moveToAction.setDuration(DURATION_MOVE_MARBLE);
    moveToAction.setActor(marbleActor);
    parallelAction.addAction(moveToAction);

    RotateToAction rotateToAction = Actions.rotateTo(90.0f - boardView.getAngleForBoardIndex(
        movedMarbleGameEvent.getNewBoardIndex()) * 180.0f / (float) Math.PI);
    rotateToAction.setUseShortestDirection(true);
    rotateToAction.setDuration(DURATION_MOVE_MARBLE);
    rotateToAction.setActor(marbleActor);
    parallelAction.addAction(rotateToAction);

    return parallelAction;

  }

  protected Action getActionToPlayCard(PlayedCardGameEvent playedCardGameEvent,
      final boolean faceDown) {

    Card card = playedCardGameEvent.getCard();
    final CardActor cardActor = cardActors.get(card.toString());

    // TODO update to include random offsets in discard pile?
    // TODO determine where to get the discard pile rectangle
    Rectangle rectangleTo = new Rectangle(0, 0, 0, 0);
    float toX = rectangleTo.getX();
    float toY = rectangleTo.getY();

    SequenceAction sequenceAction = Actions.sequence();

    RunnableAction runnableAction = Actions.run(new Runnable() {
      @Override
      public void run() {
        cardActor.toFront();
        cardActor.setFaceDown(faceDown);
      }
    });
    sequenceAction.addAction(runnableAction);

    ParallelAction parallelAction = new ParallelAction();

    MoveToAction moveToAction = Actions.action(MoveToAction.class);
    moveToAction.setPosition(toX, toY);
    moveToAction.setDuration(DURATION_PLAY_CARD);
    moveToAction.setActor(cardActor);
    parallelAction.addAction(moveToAction);

    SizeToAction sizeToAction = Actions.sizeTo(rectangleTo.getWidth(), rectangleTo.getHeight());
    sizeToAction.setDuration(DURATION_PLAY_CARD);
    sizeToAction.setActor(cardActor);
    parallelAction.addAction(sizeToAction);

    RotateToAction rotateToAction = Actions.rotateTo(0.0f);
    rotateToAction.setUseShortestDirection(true);
    rotateToAction.setDuration(DURATION_PLAY_CARD);
    rotateToAction.setActor(cardActor);
    parallelAction.addAction(rotateToAction);

    sequenceAction.addAction(parallelAction);

    runnableAction = Actions.run(new Runnable() {
      @Override
      public void run() {
        discardPile.add(cardActor);
      }
    });
    sequenceAction.addAction(runnableAction);

    return sequenceAction;

  }

  protected final Color getColorForPlayer(int playerNumber) {
    // TODO move elsewhere
    Color[] playerColors = new Color[]{Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW};
    return playerColors[playerNumber];
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
            // TODO add animation to move cards to player areas?
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
                  ((CannotPlayGameEvent) queuedEvent).getPlayerNumber(), card),
                  ((CannotPlayGameEvent) queuedEvent).getPlayerNumber() != USER_PLAYER_NUMBER));
            }
            cannotPlayPlayerCards.clear();
            sequenceAction.addAction(parallelAction);
          } else if (queuedEvent instanceof MovedMarbleGameEvent) {
            sequenceAction.addAction(getActionToMoveMarble((MovedMarbleGameEvent) queuedEvent));
          } else if (queuedEvent instanceof PlayedCardGameEvent) {
            sequenceAction.addAction(getActionToPlayCard((PlayedCardGameEvent) queuedEvent, false));
          }
        }

        if (exitedStateGameEvent.getState() == State.PLAYER_TURN && waitForUserInput) {

          updateCardActors();

          for (Card card : game.getPlayers().get(USER_PLAYER_NUMBER).getCards()) {
            final Card finalCard = card;
            CardActor cardActor = cardActors.get(card.toString());
            cardActor.clearListeners();
            cardActor.addListener(new ClickListener() {
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
    updateCardActors();
  }

  public void setSelectedCard(Card card) {

    if (selectedCard == card) {
      resetUserInput();
    } else {
      selectedCard = card;
    }

    if (selectedCard != NO_CARD && selectedCard.getRank().equals(Card.Rank.SEVEN)) {

      CardActor selectedCardActor = cardActors.get(selectedCard.toString());
      float y = selectedCardActor.getY();
      float centerX = selectedCardActor.getX() + (selectedCardActor.getWidth() / 2.0f);
      // vertical stack
      for (int i = splitCardActors.length - 1; i >= 0; i--) {
        CardActor cardActor = splitCardActors[i];
        float newX = centerX;
        float newY = y - (i * cardActor.getHeight() * 0.20f);
        cardActor.setPosition(newX - (cardActor.getWidth() / 2.0f), newY);
        cardActor.setVisible(true);
        cardActor.toFront();
      }
      // spiral
      /*float width = (selectedCardActor.getWidth()
          + (4 * selectedCardActor.getWidth() * (1.0f - CARD_OVERLAP))) / 2.0f;
      for (int i = 0; i < splitCardActors.length; i++) {
        CardActor cardActor = splitCardActors[i];
        float angle = (float) Math.PI / 8.0f * (float) (7 - i);
        float newX = centerX + width * (float) Math.cos(angle);
        float newY = y + width * (float) Math.sin(angle);
        cardActor.setPosition(newX - (cardActor.getWidth() / 2.0f),
            newY - (cardActor.getHeight() / 2.0f));
        cardActor.setOrigin(Align.center);
        cardActor.setRotation(((-(float)Math.PI / 2.0f)+angle) * 180.0f / (float)Math.PI);
        cardActor.setVisible(true);
        cardActor.toFront();
      }*/

    } else {
      for (CardActor cardActor : splitCardActors) {
        cardActor.setVisible(false);
      }
    }

    // reset all cards to not be highlighted
    for (Card playerCard : game.getPlayers().get(USER_PLAYER_NUMBER).getCards()) {
      cardActors.get(playerCard.toString()).setSelected(false);
    }

    // update the selected card to be highlighted
    if (selectedCard != NO_CARD) {
      cardActors.get(selectedCard.toString()).setSelected(true);
    }

  }

  protected void resetUserInput() {
    setSelectedCard(NO_CARD);
    selectedMarble1 = UserPlay.NO_MARBLE;
    selectedMarble2 = UserPlay.NO_MARBLE;
    selectedSplitValue = UserPlay.NO_SPLIT_VALUE;
  }

  public void updateBoardActors() {
    boardActor.update();
  }

  public void updateCardActors() {

    // update the player cards
    for (int playerNumber = 0; playerNumber < game.getNumberOfPlayers(); playerNumber++) {

      Rectangle playerRectangle = screenLayout.getBoundsForPlayer(playerNumber);
      cardHandView.update(playerRectangle.getWidth(), playerRectangle.getHeight());

      if (playerNumber == 0) {
        // update the split cards
        Rectangle cardRectangle = cardHandView.getBoundsForCard(0, splitCardActors.length,
            splitCardActors.length);
        for (CardActor cardActor : splitCardActors) {
          cardActor.setSize(cardRectangle.getWidth(), cardRectangle.getHeight());
        }
      }

      List<Card> playerCards = game.getPlayers().get(playerNumber).getCards();
      for (int cardNumber = 0; cardNumber < playerCards.size(); cardNumber++) {

        // TODO determine max number of cards based on number of players
        Rectangle cardRectangle = cardHandView.getBoundsForCard(cardNumber, playerCards.size(), 5);

        Card card = playerCards.get(cardNumber);

        CardActor cardActor = cardActors.get(card.toString());
        cardActor.setPosition(playerRectangle.getX() + cardRectangle.getX(),
            playerRectangle.getY() + cardRectangle.getY());
        cardActor.setSize(cardRectangle.getWidth(), cardRectangle.getHeight());
        cardActor.setOrigin(Align.center);
        cardActor.setRotation(0.0f);

        cardActor.setFaceDown(playerNumber != USER_PLAYER_NUMBER);

      }

    }

    // update the discard pile
    Rectangle rectangle = new Rectangle(0, 0, 0, 0);
    for (CardActor cardActor : discardPile) {
      cardActor.setPosition(rectangle.getX(), rectangle.getY());
      cardActor.setSize(rectangle.getWidth(), rectangle.getHeight());
      cardActor.setOrigin(Align.center);
      cardActor.setRotation(0.0f);
    }

    // update the card deck (undealt cards)
    rectangle = new Rectangle(0, 0, 0, 0);
    for (Card card : game.getCardDeck().getUndealtCards()) {
      CardActor cardActor = cardActors.get(card.toString());
      cardActor.setPosition(rectangle.getX(), rectangle.getY());
      cardActor.setSize(rectangle.getWidth(), rectangle.getHeight());
      cardActor.setOrigin(Align.center);
      cardActor.setRotation(0.0f);
      cardActor.setFaceDown(true);
    }

  }

  public void updateMarbleActors() {

    for (Player player : game.getPlayers()) {
      for (Marble marble : player.getMarbles()) {
        Rectangle rectangle = boardView.getBoundsForMarble(marble.getBoardIndex());
        MarbleActor marbleActor = marbleActors[player.getPlayerNumber()][marble.getMarbleNumber()];
        marbleActor.setPosition(rectangle.getX(), rectangle.getY());
        marbleActor.setSize(rectangle.getWidth(), rectangle.getHeight());
        marbleActor.setOrigin(Align.center);
        marbleActor.setRotation(90.0f - boardView.getAngleForBoardIndex(marble.getBoardIndex())
            * 180.0f / (float) Math.PI);
      }
    }

  }

  public void updatePlayerActors() {
    for (int playerNumber = 0; playerNumber < game.getNumberOfPlayers(); playerNumber++) {
      playerActors[playerNumber].update();
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
    Gdx.gl.glClearColor(0.75f, 0.75f, 0.75f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    stage.act();
    stage.draw();

  }

  @Override
  public void resize(int width, int height) {

    super.resize(width, height);

    screenLayout.update(viewport.getWorldWidth(), viewport.getWorldHeight());

    updateBoardActors();
    updateMarbleActors();
    updatePlayerActors();
    updateCardActors();

    // TODO do we want to keep this logic?
    if (firstResize) {
      firstResize = false;
      // start the game
      game.advance();
    }

  }

  @Override
  public void resume() {
    // TODO
  }

  @Override
  public void show() {
    super.show();
  }

  protected class BoardActor extends Group {

    /**
     * The image used as the background for the game board.
     */
    protected final transient Image boardBackgroundImage;
    /**
     * The array of images for the spaces on the board. The index into the array is the board index
     * and the value is the image for that board space.
     */
    protected final transient Image[] boardSpaceImages;

    public BoardActor() {

      // create the image for the board background
      boardBackgroundImage = maurersMarblesGame.createImage("board_background.png");
      boardBackgroundImage.setColor(0.65f, 0.65f, 0.65f, 1);
      addActor(boardBackgroundImage);

      // create the images for the board spaces
      boardSpaceImages = new Image[game.getBoard().getNumberOfPlayableSpaces()];
      for (int i = 0; i < boardSpaceImages.length; i++) {
        boardSpaceImages[i] = maurersMarblesGame.createImage("arrow.png");
        boardSpaceImages[i].setColor(0.5f, 0.5f, 0.5f, 1);
        addActor(boardSpaceImages[i]);
      }

      // set the colors for the player board spaces
      for (int playerNumber = 0; playerNumber < game.getNumberOfPlayers(); playerNumber++) {
        boardSpaceImages[game.getBoard().getSafeBoardIndex(playerNumber)].setColor(
            getColorForPlayer(playerNumber));
        for (int boardIndex : game.getBoard().getHomeBoardIndexes(playerNumber)) {
          boardSpaceImages[boardIndex].setColor(getColorForPlayer(playerNumber));
        }
        for (int boardIndex : game.getBoard().getStartBoardIndexes(playerNumber)) {
          boardSpaceImages[boardIndex].setColor(getColorForPlayer(playerNumber));
        }
      }

    }

    public void update() {

      Rectangle rectangle = screenLayout.getBoundsForBoard();
      setPosition(rectangle.getX(), rectangle.getY());
      setSize(rectangle.getWidth(), rectangle.getHeight());
      boardBackgroundImage.setSize(getWidth(), getHeight());

      // update the board spaces
      boardView.update(rectangle.getWidth(), rectangle.getHeight());
      for (int boardIndex = 0; boardIndex < boardSpaceImages.length; boardIndex++) {
        rectangle = boardView.getBoundsForSpace(boardIndex);
        boardSpaceImages[boardIndex].setPosition(rectangle.getX(), rectangle.getY());
        boardSpaceImages[boardIndex].setSize(rectangle.getWidth(), rectangle.getHeight());
        boardSpaceImages[boardIndex].setOrigin(Align.center);
        boardSpaceImages[boardIndex].setRotation(90.0f
            - boardView.getAngleForBoardIndex(boardIndex) * 180.0f / (float) Math.PI);
      }

    }

  }

  protected class CardActor extends Group {

    Image backImage;
    Image frontImage;

    public CardActor(Card card) {
      backImage = maurersMarblesGame.createImage("card_back.png");
      backImage.setColor(Color.SKY);
      frontImage = maurersMarblesGame.createImage("card_" + card.toString().toLowerCase() + ".png");
      addActor(backImage);
      addActor(frontImage);
    }

    public void setFaceDown(boolean faceDown) {
      if (faceDown) {
        backImage.setVisible(true);
        frontImage.setVisible(false);
      } else {
        backImage.setVisible(false);
        frontImage.setVisible(true);
      }
    }

    public void setSelected(boolean selected) {
      // TODO change to hide/show selected image on top?
      if (selected) {
        frontImage.setColor(Color.YELLOW);
      } else {
        frontImage.setColor(Color.WHITE);
      }
    }

    @Override
    public void setHeight(float height) {
      super.setHeight(height);
      backImage.setHeight(height);
      frontImage.setHeight(height);
    }

    @Override
    public void setSize(float width, float height) {
      super.setSize(width, height);
      backImage.setSize(width, height);
      frontImage.setSize(width, height);
    }

    @Override
    public void setWidth(float width) {
      super.setWidth(width);
      backImage.setWidth(width);
      frontImage.setWidth(width);
    }

  }

  protected class MarbleActor extends Group {

    Image marbleImage;

    public MarbleActor(int playerNumber) {
      marbleImage = maurersMarblesGame.createImage("arrow.png");
      marbleImage.setColor(getColorForPlayer(playerNumber));
      addActor(marbleImage);
    }

    @Override
    public void setSize(float width, float height) {
      super.setSize(width, height);
      marbleImage.setSize(width, height);
    }

  }

  protected class PlayerActor extends Group {

    Image backgroundImage;
    int playerNumber;

    public PlayerActor(int playerNumber) {

      this.playerNumber = playerNumber;

      backgroundImage = maurersMarblesGame.createImage("player_background.png");
      backgroundImage.setColor(getColorForPlayer(playerNumber));
      addActor(backgroundImage);

    }

    public void update() {

      Rectangle rectangle = screenLayout.getBoundsForPlayer(playerNumber);
      setPosition(rectangle.getX(), rectangle.getY());
      setSize(rectangle.getWidth(), rectangle.getHeight());

      backgroundImage.setSize(getWidth(), getHeight());

    }

  }

  protected class UserPlaySelector extends PlaySelector {

    Set<Play> plays;

    @Override
    public void setAvailablePlays(Set<Play> plays) {
      super.setAvailablePlays(plays);
      this.plays = plays;
      waitForUserInput = true;
    }

  }

}
