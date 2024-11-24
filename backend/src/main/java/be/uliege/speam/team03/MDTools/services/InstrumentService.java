package be.uliege.speam.team03.MDTools.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.models.InstrumentPictures;
import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.repositories.InstrumentPicturesRepository;
import be.uliege.speam.team03.MDTools.repositories.InstrumentRepository;

@Service
public class InstrumentService {
    private final InstrumentRepository instrumentRepository;
    private final InstrumentPicturesRepository instrumentPicturesRepository;

    public InstrumentService(InstrumentRepository instrumentRepository, InstrumentPicturesRepository instrumentPicturesRepository) {
        this.instrumentRepository = instrumentRepository;
        this.instrumentPicturesRepository = instrumentPicturesRepository;
    }

    public List<InstrumentDTO> getAllInstruments() {
        List<Instruments> instruments = (List<Instruments>) instrumentRepository.findAll();
        return instruments.stream()
                .map(instrument -> new InstrumentDTO(
                        instrument.getInstrument_id(),
                        instrument.getSupplier().getSupplierName(),
                        instrument.getCategory().getShape(),
                        instrument.getReference(),
                        instrument.getSupplierDescription(),
                        instrument.getPrice(),
                        instrument.isObsolete()))
                .collect(Collectors.toList());
    }

    public List<String> getPhotosByReference(String reference) {
        return instrumentPicturesRepository.findByInstrumentReference(reference)
                .stream()
                .map(InstrumentPictures::getPicturePath) // Extract the picture_path
                .collect(Collectors.toList());
    }
}


