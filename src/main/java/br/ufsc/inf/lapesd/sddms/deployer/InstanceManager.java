package br.ufsc.inf.lapesd.sddms.deployer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import br.ufsc.inf.lapesd.csv2rdf.CsvReader;

@Component
public class InstanceManager {
    private static final Logger logger = LoggerFactory.getLogger(InstanceManager.class);

    private final String instancesFolder = "instances";

    @Value("${config.rangePortStart}")
    private int rangePortStart = 0;

    @Value("${config.rangePortEnd}")
    private int rangePortEnd = 1;

    private List<Integer> availablePorts = new ArrayList<>();

    private List<RunningInstance> runningInstances = new ArrayList<>();

    @PostConstruct
    public void init() {
        for (int i = rangePortStart; i <= rangePortEnd; i++) {
            this.availablePorts.add(rangePortStart++);
        }
        startExistingInstances();
    }

    private void startExistingInstances() {
        File directory = new File(this.instancesFolder);
        File[] subdirs = directory.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        if (subdirs != null) {
            for (File dir : subdirs) {
                String requestId = dir.getName();
                logger.info("Starting sddms for requestID " + requestId);
                String instanceFolderPath = this.instancesFolder + File.separator + requestId;
                startService(requestId, instanceFolderPath);
            }
        }
    }

    @Async
    public void createAndStartInstance(DataServiceRequest dataServiceRequest) throws IOException, InterruptedException {
        String requestId = dataServiceRequest.getRequestId();

        String newInstanceFolderPath = this.instancesFolder + File.separator + requestId;
        String tdbFolderPath = newInstanceFolderPath + File.separator + "tdb";
        String rdfFolderPath = newInstanceFolderPath + File.separator + "rdf";

        createFolders(dataServiceRequest, newInstanceFolderPath, rdfFolderPath);
        convertToRdf(newInstanceFolderPath, rdfFolderPath, dataServiceRequest.getCsvSeparator(), dataServiceRequest.getCsvEncode());
        createTDB(newInstanceFolderPath, rdfFolderPath, tdbFolderPath);
        startService(requestId, newInstanceFolderPath);
    }

    private void convertToRdf(String newInstanceFolderPath, String rdfFolderPath, String csvSeparator, String csvEncode) {
        CsvReader csvReader = new CsvReader();
        csvReader.setCsvFilesFolder(newInstanceFolderPath + File.separator + "data");
        csvReader.setRdfFolder(rdfFolderPath);
        csvReader.setWriteToFile(true);
        csvReader.setCsvSeparator(csvSeparator);
        csvReader.setCsvEncode(csvEncode);
        csvReader.setOntologyFile(newInstanceFolderPath + File.separator + "ontology.owl");
        csvReader.setMappingFile(newInstanceFolderPath + File.separator + "mapping.jsonld");

        csvReader.process();
    }

    private void createFolders(DataServiceRequest dataServiceRequest, String newInstanceFolderPath, String rdfFolderPath) throws IOException {

        File root = new File(this.instancesFolder);
        if (!root.exists()) {
            root.mkdir();
        }

        File newInstanceFolder = new File(newInstanceFolderPath);
        if (!newInstanceFolder.exists()) {
            newInstanceFolder.mkdir();
        }

        File dataFolder = new File(newInstanceFolderPath + File.separator + "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        byte[] data = Base64.getDecoder().decode(dataServiceRequest.getDataFileBase64().getBytes());
        BufferedWriter writer = new BufferedWriter(new FileWriter(dataFolder.getPath() + File.separator + "data.csv"));
        writer.write(new String(data));
        writer.close();

        byte[] mapping = Base64.getDecoder().decode(dataServiceRequest.getMappingFileBase64().getBytes());
        writer = new BufferedWriter(new FileWriter(newInstanceFolderPath + File.separator + "mapping.jsonld"));
        writer.write(new String(mapping));
        writer.close();

        byte[] ontology = Base64.getDecoder().decode(dataServiceRequest.getOntologyFileBase64().getBytes());
        writer = new BufferedWriter(new FileWriter(newInstanceFolderPath + File.separator + "ontology.owl"));
        writer.write(new String(ontology));
        writer.close();

        File rdfFolder = new File(rdfFolderPath);
        if (!rdfFolder.exists()) {
            rdfFolder.mkdir();
        }
    }

    private void startService(String requestId, String newInstanceFolderPath) {
        int port = this.getAvailablePort();
        RunningInstance runningInstance = new RunningInstance(requestId, newInstanceFolderPath, port);
        (new Thread(runningInstance)).start();
        this.runningInstances.add(runningInstance);
    }

    private void createTDB(String newInstanceFolderPath, String rdfFolderPath, String tdbFolderPath) throws IOException, InterruptedException {

        String filesToProcess = "";
        File rdfFolder = new File(rdfFolderPath);
        String[] rdfFiles = rdfFolder.list();
        for (int i = 0; i < rdfFiles.length; i++) {
            filesToProcess = filesToProcess + rdfFolderPath + File.separator + rdfFiles[i] + " ";
        }

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {

            try {
                String commandToCreateTdb = "cmd /c set JENA_HOME=apache-jena-3.6.0 && apache-jena-3.6.0\\bat\\tdbloader.bat --loc %s %s";
                commandToCreateTdb = String.format(commandToCreateTdb, tdbFolderPath, filesToProcess);
                String line;
                Process p = Runtime.getRuntime().exec(commandToCreateTdb);
                BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                while ((line = bri.readLine()) != null) {
                    System.out.println(line);
                }
                bri.close();
                while ((line = bre.readLine()) != null) {
                    System.out.println(line);
                }
                bre.close();
                p.waitFor();
                System.out.println("Done.");
            } catch (Exception err) {
                err.printStackTrace();
            }

        } else {
            String line;
            String commandToCreateTdb = "apache-jena-3.6.0/bin/tdbloader --loc %s %s";
            commandToCreateTdb = String.format(commandToCreateTdb, tdbFolderPath, filesToProcess);
            Process p = Runtime.getRuntime().exec(commandToCreateTdb);
            BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = bri.readLine()) != null) {
                System.out.println(line);
            }
            bri.close();
            while ((line = bre.readLine()) != null) {
                System.out.println(line);
            }
            bre.close();
            p.waitFor();
            System.out.println("TDB created");

        }
    }

    public int getAvailablePort() {
        int instancePort = this.availablePorts.get(0);
        this.availablePorts.remove(0);
        return instancePort;
    }

    public List<RunningInstance> getRunningInstances() {
        return runningInstances;
    }

}
