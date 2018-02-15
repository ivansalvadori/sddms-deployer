package br.ufsc.inf.lapesd.sddms.deployer.endpoint;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import br.ufsc.inf.lapesd.csv2rdf.CsvReader;
import br.ufsc.inf.lapesd.sddms.deployer.DataServiceRequest;

@Path("dataServiceRequest")
@Component
public class DataServiceRequestEndpoint {
    private final String instancesFolder = "instances";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewDataService(DataServiceRequest dataServiceRequest) throws InterruptedException {
        try {
            createInstance(dataServiceRequest);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Response.ok().build();
    }

    private void createInstance(DataServiceRequest dataServiceRequest) throws IOException, InterruptedException {
        String requestId = dataServiceRequest.getRequestId();
        File root = new File(this.instancesFolder);
        if (!root.exists()) {
            root.mkdir();
        }

        String newInstanceFolderPath = this.instancesFolder + File.separator + requestId;
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

        String rdfFolderPath = newInstanceFolderPath + File.separator + "rdf";
        File rdfFolder = new File(rdfFolderPath);
        if (!rdfFolder.exists()) {
            rdfFolder.mkdir();
        }

        CsvReader csvReader = new CsvReader();
        csvReader.setMappingFile(newInstanceFolderPath + File.separator + "mapping.jsonld");
        csvReader.setCsvFilesFolder(newInstanceFolderPath + File.separator + "data");
        csvReader.setRdfFolder(rdfFolderPath);
        csvReader.process();

        String filesToProcess = "";
        String[] rdfFiles = rdfFolder.list();
        for (int i = 0; i < rdfFiles.length; i++) {
            filesToProcess = filesToProcess + rdfFolderPath + File.separator + rdfFiles[i] + " ";
        }

        String tdbFolder = newInstanceFolder + File.separator + "tdb";

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {

            try {
                String commandToCreateTdb = "cmd /c set JENA_HOME=apache-jena-3.6.0 && apache-jena-3.6.0\\bat\\tdbloader.bat --loc %s %s";
                commandToCreateTdb = String.format(commandToCreateTdb, tdbFolder, filesToProcess);
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
            commandToCreateTdb = String.format(commandToCreateTdb, tdbFolder, filesToProcess);
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

            startSddms(newInstanceFolderPath);

        }
    }

    private void startSddms(String newInstanceFolderPath) throws IOException {
        String command = "java -jar sddms.jar --server.port=%s --config.persistenceType=%s --config.tdbPath=%s --config.ontologyFile=%s --config.ontologyFormat=%s --config.managedUri=%s --config.importExternalWebResources=%s";

        int instancePort = 9999;
        boolean importExternalWebResources = true;

        JsonObject mappingConfing = this.readConfigMapping(newInstanceFolderPath + "/mapping.jsonld");
        String ontologyFormat = mappingConfing.get("ontologyFormat").getAsString();
        String managedUri = mappingConfing.get("managedUri").getAsString();

        command = String.format(command, instancePort, "TdbSingleModel", newInstanceFolderPath + "/tdb", newInstanceFolderPath + "/ontology.owl", ontologyFormat, managedUri, importExternalWebResources);
        Process p = Runtime.getRuntime().exec(command);
        String line;
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
    }

    private JsonObject readConfigMapping(String mappingFile) {
        try (FileInputStream inputStream = FileUtils.openInputStream(new File(mappingFile))) {
            String mappingContextString = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
            JsonObject mappingJsonObject = new JsonParser().parse(mappingContextString).getAsJsonObject();
            JsonObject mappingConfing = mappingJsonObject.get("@configuration").getAsJsonObject();
            return mappingConfing;

        } catch (IOException e) {
            throw new RuntimeException("Mapping file not found");
        }
    }

}
