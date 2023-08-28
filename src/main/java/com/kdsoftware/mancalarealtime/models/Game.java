package com.kdsoftware.mancalarealtime.models;

import com.kdsoftware.mancalarealtime.services.GameService;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class Game
{
    private final ArrayList<Player> participants;
    private final ArrayList<Pit> pits;
    private GameState state;
    private final String identifier;
    private final int MAX_NUMBER_OF_PITS = 14;
    public static String nextPlayerId;
    private  final Map<Integer, Integer> oppositeIndexMap;
    private final Map<Integer, List<Integer>> playerIndicesMap;
    public Game()
    {
        this.identifier = UUID.randomUUID().toString();
        this.state = GameState.CREATED;
        participants = new ArrayList<>();
        pits = new ArrayList<>();

        oppositeIndexMap = Stream.of(new Integer[][] {
                {1, 13},
                {2, 12},
                {3, 11},
                {4, 10},
                {5, 9},
                {6, 8},
                {8, 6},
                {9, 5},
                {10, 4},
                {11, 3},
                {12, 2},
                {13, 1}
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        //Map of each player's pit indices excluding the player's bit pits
        playerIndicesMap = new HashMap<>();
        var initialPlayerIndices = List.of(1, 2, 3, 4, 5, 6);
        var secondPlayerIndices = List.of(8, 9, 10, 11, 12, 13);
        playerIndicesMap.put(0, initialPlayerIndices);
        playerIndicesMap.put(1, secondPlayerIndices);
    }

    public void start()
    {
        for(int index = 0; index < MAX_NUMBER_OF_PITS; index++)
        {
            var pit = new Pit(index);
            pit.setStoneCount(6);

            if(index == 0 || index == MAX_NUMBER_OF_PITS / 2)
            {
                pit.setMancala(true);
            }
            pits.add(pit);
        }

        //The first player(who created the game) plays first
        nextPlayerId = participants.get(0).getIdentifier();
        state = GameState.IN_PROGRESS;
    }

    public void addParticipant(Player player)
    {
        participants.add(player);
        if(participants.size() == GameService.MAX_ALLOWED_PARTICIPANTS)
            this.setState(GameState.READY);
    }

    public int getParticipantCount()
    {
        return participants.size();
    }


    /**
     *
     * @param startIndex
     * @param playerId
     */
    public ArrayList<String> makeMove(int startIndex, String playerId)
    {
        var violatedRules = new ArrayList<String>();

        var playerOpt = participants.stream().filter(p -> p.getIdentifier().equals(playerId)).findFirst();
        if(playerOpt.isPresent())
        {
            var player = playerOpt.get();
            violatedRules = checkViolatedRules(startIndex, player);
            if(!violatedRules.isEmpty())
            {
                return violatedRules;
            }
            var lastPitIndex = sowAndReturnIndex(startIndex, player);
            checkSpecialPlay(lastPitIndex, player);
        }
        if(isGameOver())
        {
            setState(GameState.COMPLETE);
            completeGame();
        }
        return violatedRules;
    }

    /**
     * We need to check if this player is actually allowed to make a move
     * at startIndex
     * @param startIndex index of the pit they want to move
     * @param player current player
     * @return A list of violated rules if any, empty list otherwise
     */
    ArrayList<String> checkViolatedRules(int startIndex, Player player)
    {
        var violatedRules = new ArrayList<String>();

        if(isGameOver())
        {
            violatedRules.add("Game over, create a new one to play again");
        }
        //Should only be allowed to play if it's the player's turn
        if(!player.getIdentifier().equals(nextPlayerId))
        {
            violatedRules.add("Not the current player's turn");
        }

        //Players should only be able to move stones from pits associated with their turn
        if(!playerIndicesMap.get(player.getTurnNumber()).contains(startIndex))
        {
            violatedRules.add("Pit now allowed for the current player");
        }
        return violatedRules;
    }

    /**
     * Sows stones from pit at startIndex and returns the last index sowed into
     * @param startIndex index of pit to move stones from
     * @param player current player
     * @return index of the last pit that was sowed into
     */
    private int sowAndReturnIndex(int startIndex, Player player) {
        var holeToEmpty = pits.get(startIndex);

        //We need to know the index at which we shouldn't sow any pebbles
        var opponentBitPitIndex = participants.size() % (player.getTurnNumber() + 1);

        //We want to start distributing them from the pit just left of startIndex
        //Left direction means in increasing holes indices (Anticlockwise) so +1
        var nextPitIndex = 0;
        if(startIndex != MAX_NUMBER_OF_PITS - 1)
            nextPitIndex = startIndex + 1;

        //Keep adding stones to the next pit until there are no stones to move
        while(holeToEmpty.getStoneCount() > 0)
        {
            if(nextPitIndex == opponentBitPitIndex)
                break;

            var currentPit = pits.get(nextPitIndex);
            currentPit.addStone();
            holeToEmpty.removeStone();
            if(nextPitIndex + 1 >= MAX_NUMBER_OF_PITS)
            {
                nextPitIndex = (nextPitIndex + 1) % MAX_NUMBER_OF_PITS;
            }
            else
            {
                nextPitIndex++;
            }
        }
        //The nextPitIndex is incremented before loop termination so we are 1 past, hence the -1
        //If we landed on the second player's big pit (index 0), then we need to loop back
        if(nextPitIndex == 0)
            return MAX_NUMBER_OF_PITS - 1;
        return nextPitIndex - 1;
    }

    /**
     * Gets the index of the big pit for player at playerIndex
     * @param playerIndex the index of the player within the participants list
     * @return the index of the big pit for player at playerIndex
     */
    private int getBigPitIndex(int playerIndex)
    {
        if(playerIndex == 0)
        {
            return MAX_NUMBER_OF_PITS / 2;
        }
        return 0;
    }

    /**
     * Check to see if the current player should play again, or it's the next player's turn
     * @param lastPitIndex The index of the last pit the current player's move landed on
     * @param player The player making the move
     */
    private void updateNextPlayerId(int lastPitIndex, Player player)
    {
        var playerIndex = player.getTurnNumber();
        var bigPitIndex = getBigPitIndex(playerIndex);

        //If the last sow was at the player's big pit, they keep their turn
        if(bigPitIndex == lastPitIndex)
        {
            nextPlayerId = participants.get(playerIndex).getIdentifier();
        }
        else
        {
            nextPlayerId = participants.get((playerIndex + 1) % GameService.MAX_ALLOWED_PARTICIPANTS).getIdentifier();
        }
    }

    /**
     * Captures the stone at the lastPitIndex and stones in the last pit's index
     * and moves them to the current player's big pit. This only happens if the
     * current player's move landed on their empty 
     * @param lastPitIndex the last pit index that the current player's move landed on
     * @param playerIndex Index of the current player in the list of participants
     */
    private void captureStones(int lastPitIndex, int playerIndex)
    {
        //The last pit index was empty if it now contains a single stone (means there was
        //nothing before, and now we added it)
        if(pits.get(lastPitIndex).getStoneCount() == 1)
        {
            var oppositeIndex = oppositeIndexMap.get(lastPitIndex);
            var bigPitIndex = getBigPitIndex(playerIndex);

            //Move the stone at lastPitIndex to the player's bigPit
            var previousBigPitCount = pits.get(bigPitIndex).getStoneCount();
            var oppositeIndexPitCount = pits.get(oppositeIndex).getStoneCount();
            pits.get(bigPitIndex).setStoneCount(previousBigPitCount + oppositeIndexPitCount + 1);

            //The last pit we landed to and its opposite shouldn't have stones anymore
            pits.get(lastPitIndex).setStoneCount(0);
            pits.get(oppositeIndex).setStoneCount(0);
        }
    }

    /**
     * Checks the rules of special play (keeping the turn and capturing stones) and executes
     * them accordingly
     * @param lastPitIndex The last pit index that the current player's move landed on
     * @param player The current player who made the move
     */
    private void checkSpecialPlay(int lastPitIndex, Player player)
    {
        updateNextPlayerId(lastPitIndex, player);
        captureStones(lastPitIndex, player.getTurnNumber());
    }

    /**'
     * Check if any of the player's pits are empty, in which case we should end the game
     * @return
     */
    private boolean isGameOver()
    {
        var firstParticipantPits = playerIndicesMap.get(0);
        if(firstParticipantPits.stream().allMatch(index -> pits.get(index).getStoneCount() == 0))
            return true;

        var secondParticipantPits = playerIndicesMap.get(1);
        return secondParticipantPits.stream().allMatch(index -> pits.get(index).getStoneCount() == 0);
    }

    private void completeGame()
    {
        var firstPlayerSum = playerIndicesMap.get(0).stream().map(index -> pits.get(index).getStoneCount()).mapToInt(Integer::intValue).sum();
        var secondPlayerSum = playerIndicesMap.get(1)
                .stream()
                .map(index -> pits.get(index).getStoneCount())
                .mapToInt(Integer::intValue)
                .sum();

        //Means player 1 ran out of stones, so player 2's stones are added to their big pit
        if(firstPlayerSum == 0)
        {
            var currentBitPitCount = pits.get(getBigPitIndex(1)).getStoneCount();
            pits.get(getBigPitIndex(1)).setStoneCount(currentBitPitCount + secondPlayerSum);
        }
        else if (secondPlayerSum == 0)
        {
            var currentBitPitCount = pits.get(getBigPitIndex(0)).getStoneCount();
            pits.get(getBigPitIndex(1)).setStoneCount(currentBitPitCount + firstPlayerSum);
        }
    }
}
