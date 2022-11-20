package de.reza.documentclassifier.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.util.LoadLibs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * Initialized Tesseract-OCR engine
 */
@Configuration
@Getter
@Slf4j
public class TesseractConfig {

    @Getter(AccessLevel.NONE)
    @Value("${DPI}")
    private int dpi;
    @Getter(AccessLevel.NONE)
    @Value("${ocr.model}")
    private String model;

    /**
     * Initializes a Tesseract-OCR engine with defined configurations
     */
    @Bean
    public Tesseract getInstance() {
        File tessDataFolder = LoadLibs.extractTessResources("tessdata");
        Tesseract instance = new Tesseract();
        instance.setDatapath(tessDataFolder.getPath());
        instance.setLanguage(model);
        instance.setVariable("user_defined_dpi", String.valueOf(dpi));
        instance.setOcrEngineMode(ITessAPI.TessOcrEngineMode.OEM_DEFAULT);
        instance.setPageSegMode(ITessAPI.TessPageSegMode.PSM_AUTO_ONLY);
        log.info("Tesseract-OCR engine successfully initialized");
        return instance;
    }
}
