package be.uliege.speam.team03.MDTools.Services;

import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.models.InstrumentPictures;
import be.uliege.speam.team03.MDTools.repositories.InstrumentPicturesRepository;
import be.uliege.speam.team03.MDTools.repositories.InstrumentRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Service
public class InstrumentPictureService {

    @Value("${pictures.folder}")
    private String picturesFolder;

    @Value("${pictures.new.folder}")
    private String newPicturesFolder;

    private final InstrumentRepository instrumentRepository;
    private final InstrumentPicturesRepository instrumentPicturesRepository;

    public InstrumentPictureService(InstrumentRepository instrumentRepository, InstrumentPicturesRepository instrumentPicturesRepository) {
        this.instrumentRepository = instrumentRepository;
        this.instrumentPicturesRepository = instrumentPicturesRepository;
    }

    public void processNewInstrumentPictures() {
        File folder = new File(newPicturesFolder);

        if (!folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("Invalid new pictures folder path: " + newPicturesFolder);
        }

        // Parcours des nouvelles images uniquement
        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".png")) {
                String reference = file.getName().replace(".png", ""); // Extraire la référence

                // Rechercher l'instrument correspondant
                Optional<Instruments> instrumentOpt = instrumentRepository.findByReference(reference);

                if (instrumentOpt.isPresent()) {
                    Instruments instrument = instrumentOpt.get();

                    // Vérifier si l'entrée existe déjà
                    boolean alreadyExists = instrumentPicturesRepository
                            .findAll()
                            .iterator()
                            .hasNext();

                    if (!alreadyExists) {
                        // Ajouter un nouvel enregistrement dans la table instrument_pictures
                        InstrumentPictures newPicture = new InstrumentPictures(instrument, "pictures/instrument_pictures/" + file.getName());
                        instrumentPicturesRepository.save(newPicture);

                        // Déplacer l'image traitée dans le dossier principal
                        moveFileToMainFolder(file);
                    }
                } else {
                    System.out.println("Aucun instrument trouvé pour la référence : " + reference);
                }
            }
        }
    }

    private void moveFileToMainFolder(File file) {
        try {
            Path source = file.toPath();
            Path target = Path.of(picturesFolder, file.getName());
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Erreur lors du déplacement du fichier : " + file.getName());
            e.printStackTrace();
        }
    }
}
