package com.kdsoftware.mancalarealtime.services;

import com.google.gson.Gson;
import com.kdsoftware.mancalarealtime.DTOs.GameResponse;
import com.kdsoftware.mancalarealtime.DTOs.GameStateResponse;
import com.kdsoftware.mancalarealtime.DTOs.PlayerResponse;
import com.kdsoftware.mancalarealtime.DTOs.ResponseObject;
import com.kdsoftware.mancalarealtime.models.Direction;
import com.kdsoftware.mancalarealtime.models.Game;
import com.kdsoftware.mancalarealtime.models.GameState;
import com.kdsoftware.mancalarealtime.models.Player;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class GameService
{
    private final ArrayList<Game> activeGames;
    public static final int  MAX_ALLOWED_PARTICIPANTS = 2;

    public GameService()
    {
        activeGames = new ArrayList<>();
    }

    public ResponseObject<GameResponse> createNewGame()
    {
        var response = new ResponseObject<GameResponse>();
        //Whenever we create a new game, the first player is created
        //and their direction of play game is always to the LEFT
        var game = new Game();
        var player = new Player(Direction.LEFT);
        player.setTurnNumber(0);

        //We can go ahead and add the player as a participant to the game
        //because we know there isn't anyone else in there yet
        game.addParticipant(player);
        this.activeGames.add(game);

        var playerResponse = new PlayerResponse(player.getIdentifier(), player.getDirection());

        var gameStateResponse = new GameStateResponse(
                game.getPits(),
                game.getState().toString(),
                game.getIdentifier(),
                player.getIdentifier()
        );

        response.setResult(new GameResponse(gameStateResponse, playerResponse));
        response.setMessage("New game successfully created");
        return response;
    }

    public ResponseObject<GameResponse> joinExisting()
    {
       var response = new ResponseObject<GameResponse>();

       //Only a second player can join an existing game, so LEFT is already taken
       var player = new Player(Direction.RIGHT);
       player.setTurnNumber(1);

       //Find the first non-full game and add the participant there
        var availableGame = activeGames.stream()
                .filter(game -> game.getParticipantCount() < MAX_ALLOWED_PARTICIPANTS)
                .findFirst();

        availableGame.ifPresentOrElse(game -> {
            //This means we found a game and can add our player there
            game.addParticipant(player);
            response.setMessage("Successfully added to a game");
            var playerResponse = new PlayerResponse(player.getIdentifier(), player.getDirection());

            var gameStateResponse = new GameStateResponse(
                    game.getPits(),
                    game.getState().toString(),
                    game.getIdentifier(),
                    Game.nextPlayerId
            );

            response.setResult(new GameResponse(gameStateResponse, playerResponse));
        }, () -> response.setMessage("No open games, create new instead?"));
        return response;
    }

    public ResponseObject<GameStateResponse> startGame(String gameIdentifier)
    {
        var response = new ResponseObject<GameStateResponse>();
        var activeGame = activeGames.stream()
                .filter(game -> game.getIdentifier().equals(gameIdentifier))
                .findFirst();

        activeGame.ifPresentOrElse(game -> {
            game.start();
            var gameStateResponse = new GameStateResponse(
                    game.getPits(),
                    game.getState().toString(),
                    game.getIdentifier(),
                    Game.nextPlayerId
            );
            response.setResult(gameStateResponse);
        }, () -> response.setMessage("Game with given identifier not found"));
        return response;
    }

    public ResponseObject<GameStateResponse> makeMove(int startIndex, String playerIdentifier, String gameIdentifier)
    {
        var response = new ResponseObject<GameStateResponse>();
        var activeGame = activeGames.stream()
                .filter(game -> game.getIdentifier().equals(gameIdentifier))
                .findFirst();
        activeGame.ifPresentOrElse(game -> {
            var ruleViolations = game.makeMove(startIndex, playerIdentifier);

            if(!ruleViolations.isEmpty())
            {
                response.setMessage(new Gson().toJson(ruleViolations));
            }
            var gameStateResponse = new GameStateResponse(
                    game.getPits(),
                    game.getState().toString(),
                    game.getIdentifier(),
                    Game.nextPlayerId
            );
            response.setResult(gameStateResponse);
        }, () -> response.setMessage("Game with given identifier not found"));
        return response;
    }
}
