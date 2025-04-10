package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;

import be.uliege.speam.team03.MDTools.models.Instruments;

public interface InstrumentRepositoryCustom {
    List<Instruments> searchByKeywords(List<String> keywords);
}
