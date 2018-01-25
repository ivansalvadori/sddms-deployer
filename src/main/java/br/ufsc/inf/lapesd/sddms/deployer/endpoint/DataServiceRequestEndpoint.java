package br.ufsc.inf.lapesd.sddms.deployer.endpoint;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import br.ufsc.inf.lapesd.csv2rdf.CsvReader;
import br.ufsc.inf.lapesd.sddms.deployer.DataServiceRequest;

@Path("dataServiceRequest")
@Component
public class DataServiceRequestEndpoint {
    private final String instancesFolder = "instances";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewDataService(DataServiceRequest dataServiceRequest) {
        try {
            createInstance(dataServiceRequest);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Response.ok().build();
    }

    private void createInstance(DataServiceRequest dataServiceRequest) throws IOException {
        String requestId = dataServiceRequest.getRequestId();
        File root = new File(this.instancesFolder);
        if (!root.exists()) {
            root.mkdir();
        }

        File newInstanceFolder = new File(this.instancesFolder + File.separator + requestId);
        if (!newInstanceFolder.exists()) {
            newInstanceFolder.mkdir();
        }

        File dataFolder = new File(this.instancesFolder + File.separator + requestId + File.separator + "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        byte[] data = Base64.getDecoder().decode(dataServiceRequest.getDataFileBase64().getBytes());
        BufferedWriter writer = new BufferedWriter(new FileWriter(dataFolder.getPath() + File.separator + "data.csv"));
        writer.write(new String(data));
        writer.close();

        byte[] mapping = Base64.getDecoder().decode(dataServiceRequest.getMappingFileBase64().getBytes());
        writer = new BufferedWriter(new FileWriter(this.instancesFolder + File.separator + requestId + File.separator + "mapping.jsonld"));
        writer.write(new String(mapping));
        writer.close();

        byte[] ontology = Base64.getDecoder().decode(dataServiceRequest.getOntologyFileBase64().getBytes());
        writer = new BufferedWriter(new FileWriter(this.instancesFolder + File.separator + requestId + File.separator + "ontology.owl"));
        writer.write(new String(ontology));
        writer.close();

        File rdfFolder = new File(this.instancesFolder + File.separator + requestId + File.separator + "rdf");
        if (!rdfFolder.exists()) {
            rdfFolder.mkdir();
        }

        CsvReader csvReader = new CsvReader();
        csvReader.setMappingFile(this.instancesFolder + File.separator + requestId + File.separator + "mapping.jsonld");
        csvReader.setCsvFilesFolder(this.instancesFolder + File.separator + requestId + File.separator + "data");
        csvReader.setRdfFolder(this.instancesFolder + File.separator + requestId + File.separator + "rdf");
        csvReader.process();
    }
}
