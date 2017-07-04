package io.jaegertracing.qe.rest.jaxrs.fasterxml.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ClientObjectMapper extends ObjectMapper {

	private static final long serialVersionUID = 1L;

	public ClientObjectMapper() {
		config(this);
	}

	public static ObjectMapper config(ObjectMapper mapper) {
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true)
				.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
						false)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
						false)
				.setSerializationInclusion(JsonInclude.Include.NON_NULL);

		return mapper;
	}

}
