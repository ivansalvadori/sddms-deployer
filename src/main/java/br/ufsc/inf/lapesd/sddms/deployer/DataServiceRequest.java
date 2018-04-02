package br.ufsc.inf.lapesd.sddms.deployer;

public class DataServiceRequest {

    private String requestId;
    private String mappingFileBase64;
    private String dataFileBase64;
    private String ontologyFileBase64;
    private String csvSeparator;
    private String csvEncode;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getMappingFileBase64() {
        return mappingFileBase64;
    }

    public void setMappingFileBase64(String mappingFileBase64) {
        this.mappingFileBase64 = mappingFileBase64;
    }

    public String getDataFileBase64() {
        return dataFileBase64;
    }

    public void setDataFileBase64(String dataFileBase64) {
        this.dataFileBase64 = dataFileBase64;
    }

    public String getOntologyFileBase64() {
        return ontologyFileBase64;
    }

    public void setOntologyFileBase64(String ontologyFileBase64) {
        this.ontologyFileBase64 = ontologyFileBase64;
    }

    public String getCsvSeparator() {
        return csvSeparator;
    }

    public void setCsvSeparator(String csvSeparator) {
        this.csvSeparator = csvSeparator;
    }

    public String getCsvEncode() {
        return csvEncode;
    }

    public void setCsvEncode(String csvEncode) {
        this.csvEncode = csvEncode;
    }

}
