package de.reza.documentclassifier.utils;

import net.lingala.zip4j.ZipFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DatasetProcessor {

    /**
     * Extract received training files (pdf files) in models/uuid/dataset. After extracting the zip file will be deleted
     * @param dataset   Provided dataset as zip file
     * @param uuid      uuid of the model
     * @return          Path to the pdf files.
     */
    public String unzip(MultipartFile dataset, String uuid){

        File dic = new File("models/" + uuid);
        dic.mkdirs();
        Path filepath = Paths.get("models/" + uuid, dataset.getOriginalFilename());
        try {
            dataset.transferTo(filepath);
            new ZipFile(filepath.toFile())
                    .extractAll("models/" + uuid + "/dataset/");
            filepath.toFile().delete();
            return "models/" + uuid + "/dataset/";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
