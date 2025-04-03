package de.intranda.goobi.plugins;

import java.nio.file.Path;

import de.sub.goobi.metadaten.Image;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TranscriptionImage {
    private String imageName;
    private Image image;
    private String ocrText;
    private Path ocrPath;
}
