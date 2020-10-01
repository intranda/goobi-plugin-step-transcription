package de.intranda.goobi.plugins;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


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
import lombok.Setter;
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

    private String imageFolderName = "";

    private List<Image> allImages = new ArrayList<>();

    @Getter
    @Setter
    private Image image = null;

    @Getter
    @Setter
    private String ocrText = "";

    private String selectedImageFolder;

    private int THUMBNAIL_SIZE_IN_PIXEL = 1500;
    private int imageIndex;



    @Override
    public void initialize(Step step, String returnPath) {
        this.returnPath = returnPath;
        this.step = step;

        // read parameters from correct block in configuration file
        SubnodeConfiguration myconfig = ConfigPlugins.getProjectAndStepConfig(title, step);
//        value = myconfig.getString("value", "default value");
        allowTaskFinishButtons = myconfig.getBoolean("allowTaskFinishButtons", false);
        log.info("Transcription step plugin initialized");

        //        possibleImageFolder = Arrays.asList(myconfig.getStringArray("foldername"));
        //        if (!possibleImageFolder.isEmpty()) {
        //            selectedImageFolder = possibleImageFolder.get(0);
        //        } else {
        selectedImageFolder = "master";
        //        }
        String imageFolder = null;
        try {
            imageFolder = step.getProzess().getConfiguredImageFolder(selectedImageFolder);
        } catch (IOException | InterruptedException | SwapException | DAOException e) {
            log.error(e);
        }

        initImageList(imageFolder);

        //TESTING
        try {
            test();
        } catch (IOException | SwapException | DAOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void test() throws IOException, SwapException, DAOException, InterruptedException {
       
        getOCR(image.getImageName());
        
        String[] lines = ocrText.split("\r\n|\r|\n");
        int iLines =   lines.length +1;
        
        ocrText = ocrText.concat(System.lineSeparator() + "New transcription line test " +  iLines);
        
        setOCR(image.getImageName());
    }

    /**
     * @param myconfig
     * @param imageFolder
     */
    public void initImageList(String imageFolder) {
        this.imageFolderName = imageFolder;
        allImages.clear();
        Path path = Paths.get(imageFolderName);
        //        List<NamePart> nameParts = initImageNameParts(myconfig);
        if (StorageProvider.getInstance().isFileExists(path)) {
            List<String> imageNameList = StorageProvider.getInstance().list(imageFolderName, NIOFileUtils.imageOrObjectNameFilter);
            int order = 1;
            for (String imagename : imageNameList) {
                Image currentImage;
//                Path imagePath = Paths.get(imageFolderName, imagename);
                try {
                    currentImage = new Image(getStep().getProzess(), imageFolderName, imagename, order, THUMBNAIL_SIZE_IN_PIXEL);
                    //                    currentImage = new SelectableImage(imagePath, order, THUMBNAIL_SIZE_IN_PIXEL);
                    //                    currentImage.initNameParts(nameParts);
                    allImages.add(currentImage);
                    order++;
                } catch (IOException | InterruptedException | SwapException | DAOException e) {
                    log.error("Error initializing image " + imagename, e);
                }
            }
            setImageIndex(0);
        }
    }

    /**
     * 
     * Adjusts the variable imageIndex by the passed int, ensureing it does not go out of bounds in the process if displaOCR is set it also updates
     * the OCRtext
     * 
     */
    public void setImageIndex(int imageIndex) {
        this.imageIndex = imageIndex;
        if (this.imageIndex < 0) {
            this.imageIndex = 0;
        }
        if (this.imageIndex >= getSizeOfImageList()) {
            this.imageIndex = getSizeOfImageList() - 1;
        }
        if (this.imageIndex >= 0) {
            image = allImages.get(this.imageIndex);
        }

        getOCR(image.getImageName());
    }

    public int getSizeOfImageList() {
        return allImages.size();
    }

    /**
     * 
     * tries to retrieve OCR text for current image and puts it in ocrText
     * 
     */
    public void getOCR(String imageFile) {

        ocrText = "";
        
        if (image != null) {
           String filename = FilenameUtils.removeExtension(imageFile);    
            ocrText = FilesystemHelper.getOcrFileContent(step.getProzess(), filename);
        }
    }

    /**
     * 
     * Reads ocrText and saves to the current image's ocr file
     * 
     * @throws IOException
     * @throws InterruptedException
     * @throws DAOException
     * @throws SwapException
     * 
     */
    public void setOCR(String imageFile) throws IOException, SwapException, DAOException, InterruptedException {

        StorageProviderInterface sp = StorageProvider.getInstance();
        String ocrDir = step.getProzess().getOcrTxtDirectory();

        if (!imageFile.isEmpty()) {

            Path pathOCR = Paths.get(ocrDir);
            //create dir if not already there
            if (!sp.isFileExists(pathOCR)) {
                sp.createDirectories(pathOCR);
            }
           
           String filename = FilenameUtils.removeExtension(imageFile);
            Path txtPath = Paths.get(ocrDir, filename + ".txt");

            PrintWriter out = null;
            try {
                out = new PrintWriter(txtPath.toString());
                out.println(ocrText);
            } finally {
                out.close();
            }

//            if (!FilesystemHelper.isOcrFileExists(step.getProzess(), filename)) {
//                Path txtPath = Paths.get(ocrDir, filename + ".txt");
//                if (!sp.isFileExists(txtPath)) {
//                    sp.createFile(txtPath);
//                }
//            }
//            ocrText = FilesystemHelper.getOcrFileContent(step.getProzess(), filename);
        }
    }

    @Override
    public PluginGuiType getPluginGuiType() {
        return PluginGuiType.FULL;
        // return PluginGuiType.PART;
        // return PluginGuiType.PART_AND_FULL;
        // return PluginGuiType.NONE;
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
