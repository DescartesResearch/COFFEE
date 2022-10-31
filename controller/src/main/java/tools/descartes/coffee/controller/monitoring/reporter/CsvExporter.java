package tools.descartes.coffee.controller.monitoring.reporter;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import tools.descartes.coffee.controller.config.ControllerProperties;
import tools.descartes.coffee.controller.config.LoadGeneratorProperties;
import tools.descartes.coffee.controller.utils.FileUtils;
import org.springframework.stereotype.Component;

@Component
public class CsvExporter {

    private final ControllerProperties controllerProperties;
    private final LoadGeneratorProperties loadGeneratorProperties;

    private String exportFolder;

    public CsvExporter(ControllerProperties controllerProperties, LoadGeneratorProperties loadGeneratorProperties) {
        this.controllerProperties = controllerProperties;
        this.loadGeneratorProperties = loadGeneratorProperties;
    }

    public String getExportFolder() {
        return exportFolder;
    }

    public boolean prepareCsvExportFolder() {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm"));
        String sequenceName = extractSequenceName(controllerProperties.getTestScript());
        exportFolder = controllerProperties.getReportDirectory() + "/" + sequenceName + "_" + timestamp + "/";
        return FileUtils.createDirectory(exportFolder);
    }

    public boolean exportDataToCsv(String type, List<?> data) {
        String targetFilePath = getCsvExportFilePath(type);
        List<String[]> csvData = new ArrayList<>();

        csvData.add(getObjectProperties(data.iterator().next()));

        for (Object object : data) {
            csvData.add(getObjectPropertyValues(object));
        }

        return FileUtils.createFile(targetFilePath) && FileUtils.writeToCsvFile(targetFilePath, csvData);
    }

    public boolean copyLoadCsvOutputToMaterials() {
        String csvLoadPath = loadGeneratorProperties.getRequestLoggingFile();
        String loadTargetPath = getCsvExportFilePath("LOAD");

        return FileUtils.createFile(loadTargetPath) && FileUtils.copyFile(csvLoadPath, loadTargetPath);
    }

    private String extractSequenceName(String testScriptName) {
        int from = testScriptName.lastIndexOf("/") + 1;
        int to = testScriptName.lastIndexOf(".");
        return testScriptName.substring(from, to);
    }

    private String[] getObjectProperties(Object object) {
        return getAllFields(new LinkedList<>(), object.getClass())
                .stream()
                .map(Field::getName).toArray(String[]::new);
    }

    private String[] getObjectPropertyValues(Object object) {
        return getAllFields(new LinkedList<>(), object.getClass())
                .stream()
                .map(field -> {
                    try {
                        return field.get(object).toString();
                    } catch (Exception e) {
                        System.err.println("Error occurred while processing object fields: " + e.getMessage());
                        return null;
                    }
                }).toArray(String[]::new);
    }

    private List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }

    private String getCsvExportFilePath(String type) {
        return exportFolder + type + ".csv";
    }

}
