package org.cytoscape.diffusion.internal.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TunableSetter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DiffusionJSON {

	private CyNetworkViewWriterFactory writerFactory;
	private final TunableSetter tunableSetter;

	public DiffusionJSON(CyNetworkViewWriterFactory writerFactory, final TunableSetter setter) {
		this.writerFactory = writerFactory;
		this.tunableSetter = setter;
	}

	public String encode(final CyNetwork network, final String inputHeatColumn) throws IOException {
		
		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		// This is a CXWriter object
		final CyWriter writer = this.writerFactory.createWriter(stream, network);
		
		// Warning: this is a hack.
		// Use reflection to force to set filter
		
		final Class<? extends CyWriter> cxWriterClass = writer.getClass();
		
		// Node column filter.  Add only name and input heat
		final List<String> nodeFilter = new ArrayList<>();
		nodeFilter.add(CyNetwork.NAME);
		nodeFilter.add(inputHeatColumn);
		
		// Specify aspect name to be written in the CX
		final List<String> aspects = new ArrayList<>();
		aspects.add("nodes");
		aspects.add("edges");
		aspects.add("nodeAttributes");
		
		try {
			Field aspectFilter = cxWriterClass.getField("aspectFilter");
		    aspectFilter.setAccessible(true);
		    final Object fieldValue = aspectFilter.get(writer);
		    final Class<? extends Object> fieldClass = fieldValue.getClass();
		    final Method setMethod = fieldClass.getDeclaredMethod("setSelectedValues", new Class[]{List.class});
		    setMethod.invoke(fieldValue, aspects);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			Field colFilter = cxWriterClass.getField("nodeColFilter");
			
			 // make it accessible
		    colFilter.setAccessible(true);
		    Object fieldValue = colFilter.get(writer);
		    Method myMethod =fieldValue.getClass().getDeclaredMethod("setSelectedValues", new Class[]{List.class});
		    myMethod.invoke(fieldValue, nodeFilter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Add aspect filter
		final Map<String, Object> m = new HashMap<String, Object>();
		m.put("writeSiblings", false);
		
		this.tunableSetter.applyTunables(writer, m);
		
		String jsonString = null;
		try {
			writer.run(null);
			jsonString = stream.toString("UTF-8");
			stream.close();
		} catch (Exception e) {
			throw new IOException();
		}
		
		System.out.println("\n\n\n ======================== Filtered CX ================");
		System.out.println(jsonString);
		System.out.println("\n\n\n ======================== Filtered CX END=============");
		
		return jsonString;
	}

	public DiffusionResponse decode(String json) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(json, DiffusionResponse.class);
	}

}
