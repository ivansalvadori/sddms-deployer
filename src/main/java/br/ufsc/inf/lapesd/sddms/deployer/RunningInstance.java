package br.ufsc.inf.lapesd.sddms.deployer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RunningInstance implements Runnable {
    private final int port;
    private final String instanceFolderPath;
    private final String requestId;

    public RunningInstance(String requestId, String instanceFolderPath, int port) {
        super();
        this.port = port;
        this.instanceFolderPath = instanceFolderPath;
        this.requestId = requestId;
    }

    public int getPort() {
        return port;
    }

    @Override
    public void run() {
        String command = "java -jar sddms.jar --server.port=%s --config.persistenceType=%s --config.tdbPath=%s --config.ontologyFile=%s --config.ontologyFormat=%s --config.managedUri=%s --config.importExternalWebResources=%s";

        boolean importExternalWebResources = true;

        JsonObject mappingConfing = this.readConfigMapping(this.instanceFolderPath + "/mapping.jsonld");
        String ontologyFormat = mappingConfing.get("ontologyFormat").getAsString();
        String managedUri = mappingConfing.get("managedUri").getAsString();

        command = String.format(command, this.port, "TdbSingleModel", instanceFolderPath + "/tdb", instanceFolderPath + "/ontology.owl", ontologyFormat, managedUri, importExternalWebResources);
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);

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
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public String getRequestId() {
        return requestId;
    }

}
