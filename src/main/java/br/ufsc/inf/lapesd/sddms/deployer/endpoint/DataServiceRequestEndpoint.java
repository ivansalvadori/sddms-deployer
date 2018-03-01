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
import javax.ws.rs.core.UriInfo;

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
    @Produces(MediaType.APPLICATION_JSON)
    public Response listRunningInstances() {

        String scheme = uriInfo.getAbsolutePath().getScheme();
        String host = uriInfo.getAbsolutePath().getHost().toString();
        String urlIdPattern = "%s://%s:%s/sddms";

        List<RunningInstance> runningInstances = this.instanceManager.getRunningInstances();
        for (RunningInstance runningInstance : runningInstances) {
            String urlId = String.format(urlIdPattern, scheme, host, runningInstance.getPort());
            // runningInstance.setId(urlId);
        }
        return Response.ok(runningInstances).build();
    }

}
