package tools.descartes.coffee.controller.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FileUtils {
    private static final Logger logger = Logger.getLogger("FileUtils");
    private static PrintWriter summaryWriter;

    public static boolean copyFile(String src, String target) {
        Path sourcePath = Paths.get(src);
        Path targetPath = Paths.get(target);
        try {
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error occurred copying file " + src + " to " + target, e);
            return false;
        }

        return true;
    }

    public static boolean fileExists(String file) {
        File f = new File(file);
        return f.exists();
    }

    public static boolean createFile(String fileName) {
        File file = new File(fileName);

        if (file.exists()) {
            logger.info("file " + fileName + " already exists!");
            return true;
        }

        try {
            return file.createNewFile();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error occurred creating file " + fileName, e);
            return false;
        }
    }

    public static boolean createDirectory(String directory) {
        File file = new File(directory);

        if (file.exists()) {
            logger.info("directory " + directory + " already exists!");
            return true;
        }

        try {
            return file.mkdirs();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error occurred creating directory " + directory, e);
            return false;
        }
    }

    /**
     * to avoid recreating a PrintWriter setup an static one
     * 
     * @param summaryFileName file name of existing summary file
     * @return successfully setup the corresponding PrintWriter
     */
    public static boolean setupSummaryWriter(String summaryFileName) {
        File summaryFile = new File(summaryFileName);
        if (!summaryFile.exists()) {
            logger.log(Level.WARNING, "Summary text file " + summaryFile.getName() + " does not exists.");
            return false;
        }

        try {
            FileUtils.summaryWriter = new PrintWriter(summaryFile);
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING,
                    "Error occurred creating a PrintWriter for summary file " + summaryFile.getName()
                            + " - file not found.",
                    e);
            return false;
        }

        return true;
    }

    public static void writeToSummaryFile(String content) {
        if (FileUtils.summaryWriter != null && !FileUtils.summaryWriter.checkError()) {
            FileUtils.summaryWriter.println(content);
        }
    }

    public static void closeSummaryWriter() {
        if (FileUtils.summaryWriter != null) {
            FileUtils.summaryWriter.close();
        }
    }

    public static boolean writeToCsvFile(String file, List<String[]> dataLines) {
        File csvOutputFile = new File(file);
        if (!csvOutputFile.exists()) {
            logger.log(Level.WARNING, "Csv output file " + csvOutputFile.getName() + " does not exists.");
            return false;
        }

        PrintWriter pw;
        try {
            pw = new PrintWriter(csvOutputFile);
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING,
                    "Error occurred creating a PrintWriter for csv file " + csvOutputFile.getName()
                            + " - file not found.",
                    e);
            return false;
        }

        dataLines.stream()
                .map(FileUtils::convertToCSV)
                .forEach(pw::println);
        pw.close();

        return true;
    }

    private static String convertToCSV(String[] data) {
        return Stream.of(data)
                .map(FileUtils::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    private static String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    private FileUtils() {

    }
}
