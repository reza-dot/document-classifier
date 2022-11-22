package de.reza.documentclassifier.config;

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
@Slf4j
public class TesseractConfig {

    @Value("${DPI}")
    private int dpi;

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
