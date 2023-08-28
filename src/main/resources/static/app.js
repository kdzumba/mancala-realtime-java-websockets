const stompClient = new StompJs.Client({
    brokerURL: "ws://localhost:8080/mancala-game-websocket"
});

const player = {
    direction: "",
    identifier: ""
};

const GameStatuses = {
    IN_PROGRESS: "IN_PROGRESS",
    COMPLETE: "COMPLETE",
    READY: "READY",
    CREATED: "CREATED"
}

const game = {
    identifier: "",
    state: "",
    pits: []
}

slideGameUp();

stompClient.onConnect = (frame) => {
    console.log("Connected: " + frame)

    stompClient.subscribe("/mancala/starts", (response) => {
        let responseObject = JSON.parse(response.body);
        if(responseObject.result !== null && responseObject.result !== undefined)
        {
            console.log(responseObject.result.state);
            console.log(responseObject);
            game.pits = responseObject.result.pits;
            game.state = responseObject.result.state;
            toggleMakeMove(responseObject.result.nextPlayerIdentifier === player.identifier);
            toggleStartGame(true);
        }
        console.log(game);
    });

    stompClient.subscribe("/mancala/moves", (response) => {
        let responseObject = JSON.parse(response.body);
        if(responseObject.result !== null && responseObject.result !== undefined)
        {
            game.pits = responseObject.result.pits;
            game.state = responseObject.result.state;
            toggleMakeMove(responseObject.result.nextPlayerIdentifier === player.identifier);
            updateGameBoard();
            console.log(responseObject)
        }
    });
};

function toggleMakeMove(isCurrentPlayer)
{
    $("#make-move").prop("disabled", isCurrentPlayer === false || game.state === GameStatuses.COMPLETE);
}

function toggleStartGame(isGameReady)
{
    $("#start-game").prop("disabled", isGameReady === true);
}

function toggleJoinGame(enable)
{
    $("#join-existing").prop("disabled", enable === false);
}

function slideGameUp()
{
    $("#game-container").slideUp();
}

function  slideGameDown()
{
    $("#game-container").slideDown();
}

function createNewGame()
{
    $.post("/mancala/create-new-game", function (response) {
        if(response.result !== null && response.result !== undefined)
        {
            player.direction = response.result.playerResponse.direction;
            player.identifier = response.result.playerResponse.identifier;
            game.pits = response.result.gameStateResponse.pits;
            game.identifier = response.result.gameStateResponse.identifier;

            //Only activate a socket communication once a new game is created
            stompClient.activate();
            slideGameDown();
            toggleJoinGame(false);
        }
    });
}

function joinGame()
{
    $.post("/mancala/join-game", function (response) {
        if(response.result !== null && response.result !==undefined)
        {
            player.direction = response.result.playerResponse.direction;
            player.identifier = response.result.playerResponse.identifier;
            game.pits = response.result.gameStateResponse.pits;
            game.identifier = response.result.gameStateResponse.identifier;

            //Only activate a socket communication once accepted to a valid game
            stompClient.activate();
            toggleStartGame(true);
            slideGameDown();
            toggleJoinGame(false);
        }
        else
        {
            alert(response.message);
        }
    });
}

function startGame()
{
    stompClient.publish({
        destination: "/mancala/start-game",
        body: game.identifier
    });
}

function makeMove(holeIndex)
{
    console.log("Making moves with index: ", holeIndex);
    stompClient.publish({
        destination: "/mancala/make-move",
        body: JSON.stringify({startIndex: holeIndex, gameId: game.identifier, playerId: player.identifier})
    });
}

function updateGameBoard()
{
    $("#index0").html(game.pits[0].stoneCount)
    $("#index1").html(game.pits[1].stoneCount)
    $("#index2").html(game.pits[2].stoneCount)
    $("#index3").html(game.pits[3].stoneCount)
    $("#index4").html(game.pits[4].stoneCount)
    $("#index5").html(game.pits[5].stoneCount)
    $("#index6").html(game.pits[6].stoneCount)
    $("#index7").html(game.pits[7].stoneCount)
    $("#index8").html(game.pits[8].stoneCount)
    $("#index9").html(game.pits[9].stoneCount)
    $("#index10").html(game.pits[10].stoneCount)
    $("#index11").html(game.pits[11].stoneCount)
    $("#index12").html(game.pits[12].stoneCount)
    $("#index13").html(game.pits[13].stoneCount)
}

$(function () {
    $("form").on("submit", (e) => e.preventDefault());
    $("#create-new").click(() => createNewGame());
    $("#join-existing").click(() => joinGame());
    $("#start-game").click(() => startGame());
    $("#make-move").click(() => makeMove($("#holeIndex").val()))
})