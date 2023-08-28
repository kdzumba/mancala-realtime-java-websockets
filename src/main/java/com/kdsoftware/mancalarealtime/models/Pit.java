package com.kdsoftware.mancalarealtime.models;

import lombok.Data;

@Data
public class Pit
{
    private final int index;
    private int stoneCount;
    private boolean isMancala;
    public Pit(int index)
    {
        this.index = index;
    }

    public void addStone()
    {
        this.stoneCount++;
    }

    public void removeStone()
    {
        this.stoneCount--;
    }
}
