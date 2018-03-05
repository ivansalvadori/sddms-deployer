package br.ufsc.inf.lapesd.sddms.deployer.endpoint;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.ufsc.inf.lapesd.sddms.deployer.DataServiceRequest;
import br.ufsc.inf.lapesd.sddms.deployer.InstanceManager;
import br.ufsc.inf.lapesd.sddms.deployer.RunningInstance;

@Path("dataServiceRequest")
@Component
public class DataServiceRequestEndpoint {

    @Context
    private UriInfo uriInfo;

    @Autowired
    private InstanceManager instanceManager;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewDataService(DataServiceRequest dataServiceRequest) throws IOException, InterruptedException {
        instanceManager.createAndStartInstance(dataServiceRequest);
        return Response.ok().build();
    }

    @GET
    @Produces({ "application/ld+json;qs=1", "application/n-quads", "application/rdf+thrift", "application/x-turtle", "application/x-trig", "application/rdf+xml", "text/turtle", "application/trix", "application/turtle", "text/n-quads", "application/rdf+json", "application/trix+xml", "application/trig", "text/trig", "application/n-triples", "text/nquads", "text/plain" })
    public Response listRequests() {
        InfModel requestsOntoModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);

        String scheme = uriInfo.getAbsolutePath().getScheme();
        String host = uriInfo.getAbsolutePath().getHost().toString();
        String requestedUri = uriInfo.getRequestUri().toASCIIString();

        String sddmsUriPattern = "%s://%s:%s/sddms";
        String requestUriPattern = "%s/%s";

        List<RunningInstance> runningInstances = this.instanceManager.getRunningInstances();
        for (RunningInstance runningInstance : runningInstances) {
            String dataServiceRequestUri = String.format(requestUriPattern, requestedUri, runningInstance.getRequestId());
            String sddmsUri = String.format(sddmsUriPattern, scheme, host, runningInstance.getPort());
            Resource resourceType = requestsOntoModel.createResource("http://sddms.com.br/ontology/DataServiceRequest");
            Resource requestOntoModel = requestsOntoModel.createResource(dataServiceRequestUri, resourceType);
            requestOntoModel.addProperty(ResourceFactory.createProperty("http://sddms.com.br/ontology/runningDataServiceInstanceUri"), sddmsUri);
            requestOntoModel.addProperty(ResourceFactory.createProperty("http://sddms.com.br/ontology/dataServiceRequestId"), runningInstance.getRequestId());
        }
        return Response.ok(requestsOntoModel).build();
    }

    @GET
    @Path("{requestId}")
    @Produces({ "application/n-quads", "application/ld+json;qs=1", "application/rdf+thrift", "application/x-turtle", "application/x-trig", "application/rdf+xml", "text/turtle", "application/trix", "application/turtle", "text/n-quads", "application/rdf+json", "application/trix+xml", "application/trig", "text/trig", "application/n-triples", "text/nquads", "text/plain" })
    public Response listRequest() {
        return Response.status(Status.NOT_IMPLEMENTED).build();
    }

}
