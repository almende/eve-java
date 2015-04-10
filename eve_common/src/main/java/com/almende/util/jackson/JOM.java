/**
 * Singleton Jackson ObjectMapper
 */
package com.almende.util.jackson;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.BitSet;
import java.util.LinkedHashMap;

import com.almende.util.URIUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.joda.JodaModule;

/**
 * The Class JOM.
 */
public final class JOM {
	private static final ObjectMapper	MAPPER;
	private static final JOM			SINGLETON	= new JOM();
	static {
		MAPPER = SINGLETON.createInstance();
	}

	/**
	 * Instantiates a new jom.
	 */
	protected JOM() {}

	/**
	 * Gets the single instance of JOM.
	 * 
	 * @return single instance of JOM
	 */
	public static ObjectMapper getInstance() {
		return MAPPER;
	}

	/**
	 * Creates the object node.
	 * 
	 * @return the object node
	 */
	public static ObjectNode createObjectNode() {
		return MAPPER.createObjectNode();
	}

	/**
	 * Creates the array node.
	 * 
	 * @return the array node
	 */
	public static ArrayNode createArrayNode() {
		return MAPPER.createArrayNode();
	}

	/**
	 * Creates the null node.
	 * 
	 * @return the null node
	 */
	public static NullNode createNullNode() {
		return NullNode.getInstance();
	}

	/**
	 * Creates the instance.
	 * 
	 * @return the object mapper
	 */
	private synchronized ObjectMapper createInstance() {
		final ObjectMapper mapper = new ObjectMapper();

		mapper.setNodeFactory(new JsonNodeFactory() {
			private static final long	serialVersionUID	= -1340917885113347742L;

			@Override
			public ObjectNode objectNode() {
				return new ObjectNode(this,
						new LinkedHashMap<String, JsonNode>(2));
			}
		});

		// set configuration
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
		mapper.configure(
				DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, false);
		mapper.getFactory().configure(
				JsonFactory.Feature.CANONICALIZE_FIELD_NAMES, false);

		// Needed for o.a. JsonFileState
		mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);

		mapper.registerModule(new JodaModule());

		SimpleModule throwableModule = new SimpleModule("ThrowableModule",
				new Version(1, 0, 0, null, null, null)) {
			private static final long	serialVersionUID	= -7028757133086455336L;

			@Override
			public void setupModule(SetupContext context) {
				context.setMixInAnnotations(Throwable.class,
						ThrowableMixin.class);
			}
		};
		mapper.registerModule(throwableModule);

		SimpleModule bitSetModule = new SimpleModule("BitSetModule",
				new Version(1, 0, 0, null, null, null));
		bitSetModule.addSerializer(new CustomBitSetSerializer());
		bitSetModule.addDeserializer(BitSet.class,
				new CustomBitSetDeserializer());
		mapper.registerModule(bitSetModule);

		SimpleModule uriModule = new SimpleModule("UriModule", new Version(1,
				0, 0, null, null, null));
		uriModule.addDeserializer(URI.class, new CustomURIDeserializer());
		mapper.registerModule(uriModule);

		return mapper;
	}

	/**
	 * Gets the type factory.
	 * 
	 * @return the type factory
	 */
	public static TypeFactory getTypeFactory() {
		return MAPPER.getTypeFactory();
	}

	/**
	 * Gets the void.
	 * 
	 * @return the void
	 * @deprecated This method is no longer needed, you can directly use
	 *             Void.class
	 */
	@Deprecated
	public static JavaType getVoid() {
		return MAPPER.getTypeFactory().uncheckedSimpleType(Void.class);
	}

	/**
	 * Gets the simple type.
	 * 
	 * @param c
	 *            the c
	 * @return the simple type
	 * @deprecated This method is no longer needed, you can directly use
	 *             <Class>.class, e.g. String.class
	 */
	@Deprecated
	public static JavaType getSimpleType(final Class<?> c) {
		return MAPPER.getTypeFactory().uncheckedSimpleType(c);
	}

	/**
	 * The Class CustomBitSetSerializer.
	 */
	public class CustomBitSetSerializer extends StdSerializer<BitSet> {

		/**
		 * Instantiates a new custom bit set serializer.
		 */
		public CustomBitSetSerializer() {
			super(BitSet.class, true);
		}

		@Override
		public void serialize(BitSet value, JsonGenerator jgen,
				SerializerProvider provider) throws IOException,
				JsonGenerationException {
			jgen.writeStartObject();
			jgen.writeNumberField("size", value.size());
			jgen.writeStringField("hex", bytesToHex(value.toByteArray()));
			jgen.writeEndObject();
		}

	}

	/**
	 * The Class CustomBitSetDeserializer.
	 */
	public class CustomBitSetDeserializer extends StdDeserializer<BitSet> {
		private static final long	serialVersionUID	= 8734051359812526123L;

		/**
		 * Instantiates a new custom bit set deserializer.
		 */
		public CustomBitSetDeserializer() {
			super(BitSet.class);
		}

		@Override
		public BitSet deserialize(JsonParser jpar, DeserializationContext ctx)
				throws IOException, JsonProcessingException {
			final JsonNode node = jpar.readValueAsTree();
			if (!node.isObject()) {
				throw ctx.mappingException(BitSet.class);
			}
			final ObjectNode obj = (ObjectNode) node;
			final int size = obj.get("size").asInt();
			final byte[] value = hexToBytes(obj.get("hex").asText());
			final BitSet result = BitSet.valueOf(value);
			result.set(result.length(), size, false);
			return result;
		}

	}

	/**
	 * The Class CustomBitSetDeserializer.
	 */
	public class CustomURIDeserializer extends StdDeserializer<URI> {
		private static final long	serialVersionUID	= 8734051359812526123L;

		/**
		 * Instantiates a new custom bit set deserializer.
		 */
		public CustomURIDeserializer() {
			super(URI.class);
		}

		@Override
		public URI deserialize(JsonParser jpar, DeserializationContext ctx)
				throws IOException, JsonProcessingException {
			final JsonNode node = jpar.readValueAsTree();
			if (node.isTextual()) {
				try {
					return URIUtil.parse(node.asText());
				} catch (URISyntaxException e) {
					throw ctx.mappingException(URI.class);
				}
			}
			if (node.isObject()) {
				final String string = node.get("string").textValue();
				try {
					return URIUtil.parse(string);
				} catch (URISyntaxException e) {
					throw ctx.mappingException(URI.class);
				}
			}
			throw ctx.mappingException(URI.class);
		}

	}

	// From: http://stackoverflow.com/a/9855338
	final private static char[]	hexArray	= "0123456789ABCDEF".toCharArray();

	private static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	// From: http://stackoverflow.com/a/140861
	private static byte[] hexToBytes(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
					.digit(s.charAt(i + 1), 16));
		}
		return data;
	}
}
