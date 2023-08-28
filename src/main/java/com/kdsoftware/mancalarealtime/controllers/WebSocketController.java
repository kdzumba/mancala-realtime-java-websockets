package com.kdsoftware.mancalarealtime.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.kdsoftware.mancalarealtime.DTOs.GameStateResponse;
import com.kdsoftware.mancalarealtime.DTOs.MoveRequest;
import com.kdsoftware.mancalarealtime.DTOs.ResponseObject;
import com.kdsoftware.mancalarealtime.services.GameService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WebSocketController
{
    private final GameService gameService;

    public WebSocketController(GameService gameService)
    {
        this.gameService = gameService;
    }

    @MessageMapping("/start-game")
    @SendTo("/mancala/starts")
    @ResponseBody
    public ResponseObject<GameStateResponse> startGame(String gameIdentifier)
    {
        return gameService.startGame(gameIdentifier);
    }

    @MessageMapping("/make-move")
    @SendTo("/mancala/moves")
    @ResponseBody
    public ResponseObject<GameStateResponse> makeMove(String json)
    {
        var gson = new Gson();
        var request = gson.fromJson(json, MoveRequest.class);
        return gameService.makeMove(Integer.parseInt(request.startIndex()), request.playerId(), request.gameId());
    }
}
