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
   * The board actor for the game board.
   */
  protected final transient BoardActor boardActor;
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
   * The array of card actors for the split cards (when a seven is split). The index into the array
   * is the split value - 1 and the value is the card actor for that split card.
   */
  protected final transient CardActor[] splitCardActors;
  /**
   * The colors for each player. The index into the array is the player number and the value is the
   * color for that player.
   */
  protected static final Color[] PLAYER_COLORS = new Color[12];
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
   * The set of card actors in the discard pile.
   */
  protected final transient Set<CardActor> discardPile = new LinkedHashSet<>();

  // debugging/working
  protected transient Label mainMenuLabel;
  boolean firstResize = true;
  boolean portrait = false;

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

    // initialize the player colors
    PLAYER_COLORS[0] = Color.BLUE;
    PLAYER_COLORS[1] = Color.RED;
    PLAYER_COLORS[2] = Color.WHITE;
    PLAYER_COLORS[3] = Color.GREEN;
    PLAYER_COLORS[4] = Color.YELLOW;
    PLAYER_COLORS[5] = Color.MAGENTA;
    PLAYER_COLORS[6] = Color.NAVY;
    PLAYER_COLORS[7] = Color.FIREBRICK;
    PLAYER_COLORS[8] = Color.BLACK;
    PLAYER_COLORS[9] = Color.FOREST;
    PLAYER_COLORS[10] = Color.GOLD;
    PLAYER_COLORS[11] = Color.PURPLE;

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

    // create the board layout and board actor
    boardLayout = new GridBoardLayout(game.getBoard());
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

    // create the groups for the cards
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

    mainMenuLabel = new Label("Main Menu", new LabelStyle(new BitmapFont(), Color.GOLD));
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

    Rectangle rectangle = boardLayout.getBoundsForMarble(
        movedMarbleGameEvent.getNewBoardIndex());
    float toX = rectangle.getX() * boardActor.getWidth();
    float toY = (1.0f - rectangle.getY()) * boardActor.getHeight()
        - rectangle.getHeight() * boardActor.getHeight();

    MarbleActor marbleActor = marbleActors[movedMarbleGameEvent
        .getPlayerNumber()][movedMarbleGameEvent.getMarbleNumber()];
    marbleActor.toFront();

    ParallelAction parallelAction = new ParallelAction();

    MoveToAction moveToAction = Actions.action(MoveToAction.class);
    moveToAction.setPosition(toX, toY);
    moveToAction.setDuration(DURATION_MOVE_MARBLE);
    moveToAction.setActor(marbleActor);
    parallelAction.addAction(moveToAction);

    RotateToAction rotateToAction = Actions.rotateTo(270.0f
        - (float) (boardLayout.getAngleForBoardIndex(
            movedMarbleGameEvent.getNewBoardIndex()) * 180.0 / Math.PI));
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

    // TODO keep logic below or update board layout to have discard pile and deck pile
    // TODO update to include random offsets in discard pile?
    Rectangle rectangleTo = boardLayout.getBoundsForDiscardPile();
    float toX = rectangleTo.getX() * boardActor.getWidth()
        + (rectangleTo.getWidth() * boardActor.getWidth() / 1.8f) + boardActor.getX();
    float toY = (1.0f - rectangleTo.getY()) * boardActor.getHeight()
        - rectangleTo.getHeight() * boardActor.getHeight() + boardActor.getY();

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

    SizeToAction sizeToAction = Actions.sizeTo(rectangleTo.getWidth() * boardActor.getWidth(),
        rectangleTo.getHeight() * boardActor.getHeight());
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
    return PLAYER_COLORS[(playerNumber / game.getNumberOfTeams() * 6)
        + (playerNumber % game.getNumberOfTeams())];
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
      float y = selectedCardActor.getY() + selectedCardActor.getHeight();
      float x = selectedCardActor.getX();
      for (CardActor cardActor : splitCardActors) {
        cardActor.setPosition(x, y);
        cardActor.setVisible(true);
        cardActor.toFront();
        x += selectedCardActor.getWidth() * (1.0f - CARD_OVERLAP);
      }

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

    Rectangle rectangleLeft = boardLayout.getBoundsForBoardIndex(
        game.getBoard().getSafeBoardIndex(0));
    Rectangle rectangleRight = boardLayout.getBoundsForBoardIndex(
        game.getBoard().getHomeEntryBoardIndex(0) - 2);
    float playerWidth = (rectangleRight.getX() * boardActor.getWidth())
        + (rectangleRight.getWidth() * boardActor.getWidth())
        - (rectangleLeft.getX() * boardActor.getWidth());

    float maxCardWidth = playerWidth * 0.95f;
    float cardWidth = maxCardWidth / (1 + (4 * (1.0f - CARD_OVERLAP)));
    float cardHeight = cardWidth * (Card.HEIGHT / Card.WIDTH);

    // update the player cards
    for (int playerNumber = 1; playerNumber < game.getNumberOfPlayers(); playerNumber++) {

      float angle = boardLayout.getAngleForBoardIndex(game.getBoard().getHomeEntryBoardIndex(
          playerNumber));

      List<Card> playerCards = game.getPlayers().get(playerNumber).getCards();
      for (int i = 0; i < playerCards.size(); i++) {

        Card card = playerCards.get(i);
        CardActor cardActor = cardActors.get(card.toString());

        cardActor.setSize(cardWidth, cardHeight);

        float centerX = boardActor.getX() + ((0.49f * boardActor.getWidth())
            + (cardActor.getHeight() / 2.0f)) * (float) Math.cos(angle + Math.PI)
            + (boardActor.getWidth() / 2.0f) - (cardActor.getWidth() / 2.0f);
        float centerY = boardActor.getY() + ((-0.49f * boardActor.getHeight())
            - (cardActor.getHeight() / 2.0f)) * (float) Math.sin(angle + Math.PI)
            + (boardActor.getHeight() / 2.0f) - (cardActor.getHeight() / 2.0f);

        float width = cardActor.getWidth();
        if (playerCards.size() > 1) {
          width += cardActor.getWidth() * (1.0f - CARD_OVERLAP) * (playerCards.size() - 1);
        }
        float x = centerX + (((cardActor.getWidth() / 2.0f) - (width / 2.0f)
            + (i * cardActor.getWidth() * (1.0f - CARD_OVERLAP)))
            * (float) Math.cos(angle + Math.PI / 2.0f));
        float y = centerY - (((cardActor.getWidth() / 2.0f) - (width / 2.0f)
            + (i * cardActor.getWidth() * (1.0f - CARD_OVERLAP)))
            * (float) Math.sin(angle + Math.PI / 2.0f));
        cardActor.setPosition(x, y);

        cardActor.setOrigin(Align.center);
        cardActor.setRotation(270.0f - (float) (angle * 180.0 / Math.PI));

        cardActor.setFaceDown(true);

      }

    }

    if (portrait) {
      maxCardWidth = viewport.getWorldWidth();
    } else {
      maxCardWidth = (viewport.getWorldWidth() - boardActor.getWidth());
    }
    cardWidth = maxCardWidth / (1 + (4 * (1.0f - CARD_OVERLAP)));
    cardHeight = cardWidth * (Card.HEIGHT / Card.WIDTH);

    // update the user cards
    int playerNumber = 0;
    List<Card> playerCards = game.getPlayers().get(playerNumber).getCards();
    for (int i = 0; i < playerCards.size(); i++) {

      Card card = playerCards.get(i);
      CardActor cardActor = cardActors.get(card.toString());

      cardActor.setSize(cardWidth, cardHeight);

      float centerX;
      float centerY;
      if (portrait) {
        centerX = (viewport.getWorldWidth() / 2.0f);
        centerY = (viewport.getWorldHeight() - boardActor.getHeight()) / 2.0f;
      } else {
        centerX = (viewport.getWorldWidth() - boardActor.getWidth()) / 2.0f
            + boardActor.getX() + boardActor.getWidth();
        centerY = (viewport.getWorldHeight() / 2.0f);
      }

      float width = cardActor.getWidth();
      if (playerCards.size() > 1) {
        width += cardActor.getWidth() * (1.0f - CARD_OVERLAP) * (playerCards.size() - 1);
      }
      float x = centerX - (width / 2.0f) + (i * cardActor.getWidth() * (1.0f - CARD_OVERLAP));
      float y = centerY - (cardActor.getHeight() / 2.0f);
      cardActor.setPosition(x, y);

      cardActor.setOrigin(Align.center);
      cardActor.setRotation(0.0f);

      cardActor.setFaceDown(false);
      cardActor.toFront();

    }

    // update the split cards
    for (CardActor cardActor : splitCardActors) {
      cardActor.setSize(cardWidth, cardHeight);
    }

    // update the discard pile
    Rectangle rectangle = boardLayout.getBoundsForDiscardPile();
    for (CardActor cardActor : discardPile) {
      cardActor.setSize(rectangle.getWidth() * boardActor.getWidth(),
          rectangle.getHeight() * boardActor.getHeight());
      cardActor.setPosition(rectangle.getX() * boardActor.getWidth()
          + (rectangle.getWidth() * boardActor.getWidth() / 1.8f) + boardActor.getX(),
          (1.0f - rectangle.getY()) * boardActor.getHeight()
          - rectangle.getHeight() * boardActor.getHeight() + boardActor.getY());
      cardActor.setRotation(0.0f);
    }

    // update the card deck (undealt cards)
    for (Card card : game.getCardDeck().getUndealtCards()) {
      CardActor cardActor = cardActors.get(card.toString());
      cardActor.setSize(rectangle.getWidth() * boardActor.getWidth(),
          rectangle.getHeight() * boardActor.getHeight());
      cardActor.setPosition(rectangle.getX() * boardActor.getWidth()
          - (rectangle.getWidth() * boardActor.getWidth() / 2.0f) + boardActor.getX(),
          (1.0f - rectangle.getY()) * boardActor.getHeight()
          - rectangle.getHeight() * boardActor.getHeight() + boardActor.getY());
      cardActor.setRotation(0.0f);
      cardActor.setFaceDown(true);
    }

  }

  public void updateMarbleActors() {

    for (Player player : game.getPlayers()) {

      for (Marble marble : player.getMarbles()) {

        Rectangle rectangle = boardLayout.getBoundsForMarble(marble.getBoardIndex());
        float marbleHeight = rectangle.getHeight() * boardActor.getHeight();
        MarbleActor marbleActor = marbleActors[player.getPlayerNumber()][marble.getMarbleNumber()];
        marbleActor.setSize(rectangle.getWidth() * boardActor.getWidth(), marbleHeight);
        marbleActor.setPosition(rectangle.getX() * boardActor.getWidth(),
            (1.0f - rectangle.getY()) * boardActor.getHeight() - marbleHeight);
        marbleActor.setOrigin(Align.center);
        marbleActor.setRotation(270.0f
            - (float) (boardLayout.getAngleForBoardIndex(marble.getBoardIndex())
            * 180.0 / Math.PI));

      }

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

    updateBoardActors();
    updateMarbleActors();
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
      boardBackgroundImage = new Image(maurersMarblesGame.getAssetManager().get(
          "board_background.png", Texture.class));
      boardBackgroundImage.setColor(0.75f, 0.75f, 0.75f, 1);
      addActor(boardBackgroundImage);

      // create the images for the board spaces
      boardSpaceImages = new Image[game.getBoard().getNumberOfPlayableSpaces()];
      for (int i = 0; i < boardSpaceImages.length; i++) {
        boardSpaceImages[i] = new Image(maurersMarblesGame.getAssetManager().get("board_space.png",
            Texture.class));
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

      float minCardDisplayHeight = viewport.getWorldHeight() * 0.2f;
      float portraitSize = viewport.getWorldWidth();
      if (portraitSize > viewport.getWorldHeight() - minCardDisplayHeight) {
        portraitSize = viewport.getWorldHeight() - minCardDisplayHeight;
      }

      float minCardDisplayWidth = viewport.getWorldWidth() * 0.3f;
      float landscapeSize = viewport.getWorldHeight();
      if (landscapeSize > viewport.getWorldWidth() - minCardDisplayWidth) {
        landscapeSize = viewport.getWorldWidth() - minCardDisplayWidth;
      }

      if (portraitSize > landscapeSize) {
        // portrait
        portrait = true;
        setSize(portraitSize, portraitSize);
        setPosition((viewport.getWorldWidth() - portraitSize) / 2.0f,
            viewport.getWorldHeight() - getHeight());
      } else {
        // landscape
        portrait = false;
        setSize(landscapeSize, landscapeSize);
        // board on left
        setPosition(0, (viewport.getWorldHeight() - landscapeSize) / 2.0f);
        // board on right
        //setPosition(viewport.getWorldWidth() - getWidth(), 0);
      }
      boardBackgroundImage.setSize(getWidth(), getHeight());

      // update the board spaces
      for (int boardIndex = 0; boardIndex < boardSpaceImages.length; boardIndex++) {
        Rectangle rectangle = boardLayout.getBoundsForSpace(boardIndex);
        float boardSpaceHeight = rectangle.getHeight() * getHeight();
        boardSpaceImages[boardIndex].setSize(rectangle.getWidth() * getWidth(), boardSpaceHeight);
        boardSpaceImages[boardIndex].setPosition(rectangle.getX() * getWidth(),
            (1.0f - rectangle.getY()) * getHeight() - boardSpaceHeight);
        boardSpaceImages[boardIndex].setOrigin(Align.center);
        boardSpaceImages[boardIndex].setRotation(270.0f
            - (float) (boardLayout.getAngleForBoardIndex(boardIndex) * 180.0 / Math.PI));
      }

    }

  }

  protected class CardActor extends Group {

    Image backImage;
    Image frontImage;
    Label label;

    public CardActor(Card card) {
      backImage = new Image(maurersMarblesGame.getAssetManager().get("card_back.png",
          Texture.class));
      backImage.setColor(Color.SKY);
      frontImage = new Image(maurersMarblesGame.getAssetManager().get("card_"
          + card.toString().toLowerCase() + ".png", Texture.class));
      label = new Label(" " + card.toString(), new Label.LabelStyle(new BitmapFont(),
          card.getSuit().equals(Card.Suit.CLUBS) || card.getSuit().equals(Card.Suit.SPADES)
          ? Color.BLACK : Color.RED));
      addActor(backImage);
      addActor(frontImage);
      addActor(label);
    }

    public void setFaceDown(boolean faceDown) {
      if (faceDown) {
        backImage.setVisible(true);
        frontImage.setVisible(false);
        label.setVisible(false);
      } else {
        backImage.setVisible(false);
        frontImage.setVisible(true);
        label.setVisible(true);
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
      label.setY(height - label.getHeight());
    }

    @Override
    public void setSize(float width, float height) {
      super.setSize(width, height);
      backImage.setSize(width, height);
      frontImage.setSize(width, height);
      label.setY(height - label.getHeight());
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
      marbleImage = new Image(maurersMarblesGame.getAssetManager().get("marble.png",
          Texture.class));
      marbleImage.setColor(getColorForPlayer(playerNumber));
      addActor(marbleImage);
    }

    @Override
    public void setSize(float width, float height) {
      super.setSize(width, height);
      marbleImage.setSize(width, height);
    }

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
