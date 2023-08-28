package com.kdsoftware.mancalarealtime.DTOs;

import com.kdsoftware.mancalarealtime.models.Pit;

import java.util.ArrayList;

public record GameStateResponse(ArrayList<Pit> pits,
                                String state,
                                String identifier,
                                String nextPlayerIdentifier)
{
}
