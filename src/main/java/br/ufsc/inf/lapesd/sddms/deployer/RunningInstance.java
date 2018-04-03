package br.ufsc.inf.lapesd.sddms.deployer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RunningInstance implements Runnable {
    private final int port;
    private final String instanceFolderPath;
    private final String requestId;
    private final String resourceDomain;

    public RunningInstance(String requestId, String instanceFolderPath, int port, String resourceDomain) {
        super();
        this.port = port;
        this.instanceFolderPath = instanceFolderPath;
        this.requestId = requestId;
        this.resourceDomain = resourceDomain;
    }

    public int getPort() {
        return port;
    }

    @Override
    public void run() {
        String command = "java -jar sddms.jar --server.port=%s --config.persistenceType=%s --config.tdbPath=%s --config.ontologyFile=%s --config.ontologyFormat=%s --config.managedUri=%s --config.importExternalWebResources=%s";

        boolean importExternalWebResources = true;

        String resourceDomain = this.resourceDomain;
        String ontologyFormat = "n3";

        command = String.format(command, this.port, "TdbSingleModel", instanceFolderPath + "/tdb", instanceFolderPath + "/ontology.owl", ontologyFormat, resourceDomain, importExternalWebResources);
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

    public String getRequestId() {
        return requestId;
    }

}
