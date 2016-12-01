package org.cytoscape.diffusion.internal;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import java.io.IOException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

class HeatMap {

  private Map<String, Double> map;

  public HeatMap(Map<String, Double> map) {
    this.map = map;
  }

  public static HeatMap parseJSON(String JSON) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
		return new HeatMap(objectMapper.readValue(JSON, Map.class));
  }

  public Set<Map.Entry<String, Double>> getHeats() {
    return this.map.entrySet();
  }

}
