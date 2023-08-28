package com.kdsoftware.mancalarealtime.controllers;

import com.kdsoftware.mancalarealtime.DTOs.GameResponse;
import com.kdsoftware.mancalarealtime.DTOs.ResponseObject;
import com.kdsoftware.mancalarealtime.services.GameService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mancala")
public class HttpController
{
    private final GameService gameService;
    public HttpController(GameService gameService)
    {
        this.gameService = gameService;
    }

    @PostMapping("/join-game")
    @ResponseBody
    public ResponseObject<GameResponse> joinExistingGame()
    {
        return gameService.joinExisting();
    }

    @PostMapping("/create-new-game")
    @ResponseBody
    public ResponseObject<GameResponse> createNewGame()
    {
        return gameService.createNewGame();
    }
}
