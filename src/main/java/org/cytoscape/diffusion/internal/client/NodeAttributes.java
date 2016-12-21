package org.cytoscape.diffusion.internal.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NodeAttributes {

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