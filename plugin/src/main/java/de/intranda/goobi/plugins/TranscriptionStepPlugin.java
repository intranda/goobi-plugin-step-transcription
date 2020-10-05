package de.intranda.goobi.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This file is part of a plugin for Goobi - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginReturnValue;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IStepPluginVersion2;

import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.metadaten.Image;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@Log4j2
public class TranscriptionStepPlugin implements IStepPluginVersion2 {

    @Getter
    private String title = "intranda_step_transcription";
    @Getter
    private Step step;
    @Getter
    private String value;
    @Getter
    private boolean allowTaskFinishButtons;
    private String returnPath;

    @Getter
    private List<TranscriptionImage> images = new ArrayList<>();

    private String configuredImageFolder;

    @Override
    public void initialize(Step step, String returnPath) {
        this.returnPath = returnPath;
        this.step = step;

        // read parameters from correct block in configuration file
        SubnodeConfiguration pluginConfig = ConfigPlugins.getProjectAndStepConfig(title, step);
        allowTaskFinishButtons = pluginConfig.getBoolean("allowTaskFinishButtons", false);
        log.info("Transcription step plugin initialized");

        //        possibleImageFolder = Arrays.asList(myconfig.getStringArray("foldername"));
        //        if (!possibleImageFolder.isEmpty()) {
        //            selectedImageFolder = possibleImageFolder.get(0);
        //        } else {

        configuredImageFolder = pluginConfig.getString("imageFolder", "master");
        //        }

        try {
            initImageList(configuredImageFolder);
        } catch (SwapException | DAOException | IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            log.error(e);
        }
    }

    /**
     * @param myconfig
     * @param imageFolder
     * @throws InterruptedException
     * @throws IOException
     * @throws DAOException
     * @throws SwapException
     */
    public void initImageList(String configuredImageFolder) throws SwapException, DAOException, IOException, InterruptedException {
        String imageFolder = null;
        try {
            imageFolder = step.getProzess().getConfiguredImageFolder(configuredImageFolder);
        } catch (IOException | InterruptedException | SwapException | DAOException e) {
            log.error(e);
        }

        images.clear();
        Path path = Paths.get(imageFolder);
        String ocrDir = step.getProzess().getOcrTxtDirectory();
        if (StorageProvider.getInstance().isFileExists(path)) {
            List<Path> imageNameList = StorageProvider.getInstance().listFiles(imageFolder, NIOFileUtils.imageOrObjectNameFilter);
            int order = 1;
            for (Path imagePath : imageNameList) {
                Image image = new Image(step.getProzess(), configuredImageFolder, imagePath.getFileName().toString(), order, 800);
                String basename = FilenameUtils.removeExtension(imagePath.getFileName().toString());
                Path ocrFile = Paths.get(ocrDir, basename + ".txt");
                String currentOcr = readOcrFile(ocrFile);
                images.add(new TranscriptionImage(imagePath.getFileName().toString(), image, currentOcr, ocrFile));
                order++;
            }
        }
    }

    public String saveOcrAndExit() throws IOException {
        saveOcr();
        return finish();
    }

    public void saveOcr() throws IOException {
        if (images.isEmpty()) {
            return;
        }
        Files.createDirectories(images.get(0).getOcrPath().getParent());
        for (TranscriptionImage image : images) {
            Files.write(image.getOcrPath(), Arrays.asList(image.getOcrText().split("\n")));
        }
    }

    public String readOcrFile(Path ocrFile) throws IOException {
        if (!Files.exists(ocrFile)) {
            return "";
        }
        StorageProviderInterface sp = StorageProvider.getInstance();
        StringBuilder builder = new StringBuilder();
        String buffer = null;
        String encoding = FilesystemHelper.getFileEncoding(ocrFile);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(sp.newInputStream(ocrFile), encoding))) {
            while ((buffer = in.readLine()) != null) {
                builder.append(buffer.replaceAll("(\\s+)", " ")).append("<br/>");
            }
        }
        return builder.toString();
    }

    @Override
    public PluginGuiType getPluginGuiType() {
        return PluginGuiType.FULL;
    }

    @Override
    public String getPagePath() {
        return "/uii/plugin_step_transcription.xhtml";
    }

    @Override
    public PluginType getType() {
        return PluginType.Step;
    }

    @Override
    public String cancel() {
        return "/uii" + returnPath;
    }

    @Override
    public String finish() {
        return "/uii" + returnPath;
    }

    @Override
    public int getInterfaceVersion() {
        return 0;
    }

    @Override
    public HashMap<String, StepReturnValue> validate() {
        return null;
    }

    @Override
    public boolean execute() {
        PluginReturnValue ret = run();
        return ret != PluginReturnValue.ERROR;
    }

    @Override
    public PluginReturnValue run() {
        boolean successfull = true;
        // your logic goes here

        log.info("Transcription step plugin executed");
        if (!successfull) {
            return PluginReturnValue.ERROR;
        }
        return PluginReturnValue.FINISH;
    }
}
