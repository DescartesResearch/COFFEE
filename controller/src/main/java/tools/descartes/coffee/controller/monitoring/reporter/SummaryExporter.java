package tools.descartes.coffee.controller.monitoring.reporter;

import tools.descartes.coffee.controller.utils.FileUtils;
import org.springframework.stereotype.Component;

@Component
public class SummaryExporter {

    private final CsvExporter csvExporter;

    private String summaryFileName;

    public SummaryExporter(CsvExporter csvExporter) {
        this.csvExporter = csvExporter;
    }

    /**
     * creates the summary.txt file inside the output folder and sets up an
     * PrintWriter to it.
     * 
     * @return
     */
    public boolean setupSummaryExport() {
        return createSummaryTextFile() && FileUtils.setupSummaryWriter(summaryFileName);
    }

    private boolean createSummaryTextFile() {
        summaryFileName = csvExporter.getExportFolder() + "summary.txt";
        return FileUtils.createFile(summaryFileName);
    }

    public void writeToSummary(String content) {
        FileUtils.writeToSummaryFile(content);
    }

    public void cleanUpSummaryExport() {
        FileUtils.closeSummaryWriter();
    }

}
