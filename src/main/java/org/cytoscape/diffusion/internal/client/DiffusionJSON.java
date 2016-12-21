package org.cytoscape.diffusion.internal.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;

import com.fasterxml.jackson.databind.ObjectMapper;

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
