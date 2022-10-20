package de.reza.documentclassifier.utils;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
@Service
@Slf4j
public class DatasetProcessor {

    /**
     * Extract received training files (pdf files) in models/uuid/dataset. After extracting the zip file will be deleted
     * @param dataset   Provided dataset as zip file
     * @param uuid      uuid of the model
     * @return          Path to the pdf files.
     */
    public String unzip(MultipartFile dataset, String uuid) {

        if (!dataset.isEmpty()) {
            File dic = new File("models/" + uuid);
            boolean createFolderSuccessful = dic.mkdirs();
            if (createFolderSuccessful) {
                Path filepath = Paths.get("models/" + uuid, dataset.getOriginalFilename());
                try {
                    dataset.transferTo(filepath);
                    new ZipFile(filepath.toFile()).extractAll("models/" + uuid + "/trainingFiles/");
                    boolean removeZipFile = filepath.toFile().delete();
                    if (removeZipFile) {
                        log.info("Zip file deleted");
                        return "models/" + uuid + "/trainingFiles/";
                    }
                } catch (IOException e) {
                    return null;
                }
            }
        }
            return null;
    }
}
