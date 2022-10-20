package de.reza.documentclassifier.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.reza.documentclassifier.pojo.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class JsonProcessor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Maps a {@link List<Token>} to a json file
     * @param tokenList     provided list of {@link Token}
     * @param uuid          uuid of the model
     * @param filename      classname
     */
    public void createJsonFile(List<Token> tokenList, String uuid, String filename){

        try {
            log.info("JSON file created: {}", filename);
            objectMapper.writeValue(new File("models/"+ uuid + "/" + filename + ".json"), tokenList);
        } catch (IOException e) {
            log.info("JSON file could not be created");
        }
    }

    /**
     * Maps a json file to a  {@link List<Token>}
     * @param jsonFile      class
     * @return              list of {@link Token} from the class
     */
     public List<Token> readJsonFile(File jsonFile){
         try {
             return objectMapper.readValue(jsonFile, new TypeReference<>(){});
         } catch (IOException e) {
             log.error("JSON file could not be read");
             return null;
         }
     }
}
