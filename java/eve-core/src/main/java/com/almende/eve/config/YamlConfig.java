/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.config;

import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.almende.util.TypeUtil;

/**
 * The Class YamlConfig.
 */
public final class YamlConfig {
	
	/**
	 * @param inputStream
	 * @return Map<String,Object> filled with configuration data.
	 */
	public static final Map<String, Object> load(final InputStream inputStream) {
		TypeUtil<Map<String, Object>> typeUtil = new TypeUtil<Map<String, Object>>(){};
		final Yaml yaml = new Yaml();
		return typeUtil.inject(yaml.loadAs(inputStream, Map.class));
	}
}
