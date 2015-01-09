/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.redis;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import redis.clients.jedis.Jedis;

import com.almende.eve.state.AbstractState;
import com.almende.eve.state.State;
import com.almende.eve.state.redis.RedisStateBuilder.RedisStateProvider;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class RedisState.
 */
public class RedisState extends AbstractState<JsonNode> implements State {
	private static final Logger			LOG		= Logger.getLogger(RedisState.class
														.getName());
	private final RedisStateProvider	provider;
	private static final String			KEYS	= "Keys";

	private String makeKey(final String key) {
		return getId() + "_" + key;
	}

	/**
	 * Instantiates a new redis state.
	 *
	 * @param id
	 *            the id
	 * @param redisStateProvider
	 *            the redis state provider
	 * @param params
	 *            the params
	 */
	public RedisState(final String id,
			final RedisStateProvider redisStateProvider, final ObjectNode params) {
		super(id, redisStateProvider, params);
		provider = redisStateProvider;
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.state.State#remove(java.lang.String)
	 */
	@Override
	public Object remove(String key) {
		final Jedis redis = provider.getInstance();
		final String nkey = makeKey(key);

		JsonNode res = JOM.createNullNode();
		try {
			res = JOM.getInstance().readTree(redis.get(nkey));
		} catch (JsonProcessingException e) {
			LOG.log(Level.WARNING, "Couldn't read:" + nkey, e);
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Couldn't read:" + nkey, e);
		}
		redis.del(nkey);

		provider.returnInstance(redis);
		return res;
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.state.State#containsKey(java.lang.String)
	 */
	@Override
	public boolean containsKey(String key) {
		return keySet().contains(key);
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.state.State#keySet()
	 */
	@Override
	public Set<String> keySet() {
		final Jedis redis = provider.getInstance();
		Set<String> keys = redis.smembers(getId()+"_"+KEYS);
		provider.returnInstance(redis);
		
		final Set<String> cleanKeys = new HashSet<String>();
		for (String key : keys){
			cleanKeys.add(key.replaceFirst(getId()+"_", ""));
		}
		return cleanKeys;
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.state.State#clear()
	 */
	@Override
	public void clear() {
		final Jedis redis = provider.getInstance();
		Set<String> keys = redis.smembers(getId()+"_"+KEYS);
		for (String key : keys) {
			redis.del(key);
			redis.srem(getId()+"_"+KEYS, key);
		}
		provider.returnInstance(redis);
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.state.State#size()
	 */
	@Override
	public int size() {
		Long res = Long.valueOf(0);
		final Jedis redis = provider.getInstance();
		res = redis.scard(getId()+"_"+KEYS);
		provider.returnInstance(redis);
		return res.intValue();
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.state.AbstractState#get(java.lang.String)
	 */
	@Override
	public JsonNode get(String key) {
		final Jedis redis = provider.getInstance();
		final String nkey = makeKey(key);

		JsonNode res = NullNode.getInstance();
		try {
			final String data = redis.get(nkey);
			if (data != null && !data.trim().isEmpty()) {
				res = JOM.getInstance().readTree(data);
			}
		} catch (JsonProcessingException e) {
			LOG.log(Level.WARNING, "Couldn't read:" + nkey, e);
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Couldn't read:" + nkey, e);
		}
		provider.returnInstance(redis);
		return res;
	}

	@Override
	public JsonNode locPut(final String key, JsonNode value) {
		if (value == null) {
			value = NullNode.getInstance();
		}
		final Jedis redis = provider.getInstance();
		final String nkey = makeKey(key);
		redis.set(nkey, value.toString());
		redis.sadd(getId()+"_"+KEYS, nkey);
		provider.returnInstance(redis);
		return value;
	}

	@Override
	public boolean locPutIfUnchanged(final String key, final JsonNode newVal,
			JsonNode oldVal) {
		boolean result = false;
		final Jedis redis = provider.getInstance();
		try {
			JsonNode cur = get(key);
			if (oldVal == null) {
				oldVal = NullNode.getInstance();
			}
			final String nkey = makeKey(key);
			if (oldVal.equals(cur) || oldVal.toString().equals(cur.toString())) {
				redis.set(nkey, newVal.toString());
				redis.sadd(getId()+"_"+KEYS, nkey);
				result = true;
			}
		} catch (Exception e) {
			LOG.log(Level.WARNING, "", e);
			// Don't let users loop if exception is thrown. They
			// would get into a deadlock....
			result = true;
		}
		provider.returnInstance(redis);
		return result;
	}
}
