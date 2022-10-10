package de.reza.documentclassifier;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.reza.documentclassifier.pojo.Token;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

public class JsonTest {

    @Test
    public void write() throws IOException {

        HashSet<Token> tokenSetClass = new HashSet<>();
        tokenSetClass.add(new Token("Das", 71, 74, 32));
        tokenSetClass.add(new Token("ist", 179, 99, 78));
        tokenSetClass.add(new Token("nur", 212, 175, 28));
        tokenSetClass.add(new Token("ein", 283, 225, 79));
        tokenSetClass.add(new Token("Test", 306, 227, 43));
        tokenSetClass.add(new Token("OCR", 354, 280, 118));
        tokenSetClass.add(new Token("funktioniert", 141, 367, 406));

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File("target/tokeList.json"), tokenSetClass);
    }

    @Test
    public void read() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        HashSet<Token> tokenSet = objectMapper.readValue(new File("target/tokeList.json"), new TypeReference<>() {
        });
        System.out.println(tokenSet.toString());

    }
}
