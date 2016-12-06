package org.cytoscape.diffusion.internal;

import java.util.Map;
import java.util.List;

import java.io.IOException;
import java.io.ByteArrayOutputStream;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DiffusionJSON {

  private CyNetworkViewWriterFactory writerFactory;

  public DiffusionJSON(CyNetworkViewWriterFactory writerFactory) {
    this.writerFactory = writerFactory;
  }

  public String encode(CyNetwork network) throws IOException {
    final ByteArrayOutputStream stream = new ByteArrayOutputStream();
    CyWriter writer = this.writerFactory.createWriter(stream, network);
    String jsonString = null;
    try {
      writer.run(null);
      jsonString = stream.toString("UTF-8");
      stream.close();
    } catch (Exception e) {
      throw new IOException();
    }
    return jsonString;
  }

  public DiffusionResponse decode(String json) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(json, DiffusionResponse.class);
  }

}

class DiffusionResponse {

    private final Map<String, NodeAttributes> data;
    private final List<Object> errors;

    @JsonCreator
    public DiffusionResponse(@JsonProperty("data") final Map<String, NodeAttributes> data,
                             @JsonProperty("errors") final List<Object> errors) {
        this.data = data;
        this.errors = errors;
    }

    public Map<String, NodeAttributes> getData() {
        return data;
    }

    public List<Object> getErrors() {
      return errors;
    }

}

class NodeAttributes {

    private final Double heat;
    private final Integer rank;

    @JsonCreator
    public NodeAttributes(@JsonProperty("heat") final Double heat,
                 @JsonProperty("rank") Integer rank) {
        this.heat = heat;
        this.rank = rank;
    }

    public Double getHeat() {
        return heat;
    }

    public Integer getRank() {
        return rank;
    }

}
