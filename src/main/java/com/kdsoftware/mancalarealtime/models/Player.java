package com.kdsoftware.mancalarealtime.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.UUID;

@Data
public class Player
{
    private final Direction direction;
    private final String identifier;
    private int turnNumber = Integer.MIN_VALUE;
    public Player(Direction direction)
    {
        this.direction = direction;
        this.identifier = UUID.randomUUID().toString();
    }
}
