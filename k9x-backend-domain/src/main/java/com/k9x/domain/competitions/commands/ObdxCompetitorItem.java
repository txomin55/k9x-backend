package com.k9x.domain.competitions.commands;

import com.k9x.domain.events.valueobjects.CompetitorDogSnapshot;

public record ObdxCompetitorItem(String dogIdentification, short startNumber, Short competitorNumber, boolean bih,
                                 String primer, boolean reserve, CompetitorDogSnapshot dogSnapshot) {

    public ObdxCompetitorItem {
        dogSnapshot = dogSnapshot == null ? CompetitorDogSnapshot.EMPTY : dogSnapshot;
    }

    public ObdxCompetitorItem withDogSnapshot(CompetitorDogSnapshot snapshot) {
        return new ObdxCompetitorItem(dogIdentification, startNumber, competitorNumber, bih, primer, reserve, snapshot);
    }
}
