/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.redis;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.almende.eve.capabilities.AbstractCapabilityBuilder;
import com.almende.eve.state.State;
import com.almende.eve.state.StateService;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A service for managing MemoryState objects.
 */
public class RedisStateBuilder extends AbstractCapabilityBuilder<RedisState> {
	private static final Logger						LOG			= Logger.getLogger(RedisStateBuilder.class
																		.getName());
	private static Map<String, RedisStateProvider>	instances	= new ConcurrentHashMap<String, RedisStateProvider>();

	/*
	 * (non-Javadoc)
	 * @see
	 * com.almende.eve.capabilities.CapabilityService#get(com.fasterxml.jackson.
	 * databind
	 * .JsonNode, com.almende.eve.capabilities.handler.Handler, java.lang.Class)
	 */
	@Override
	public RedisState build() {
		final RedisStateProvider provider = getInstanceByParams(getParams());
		if (provider != null) {
			return provider.get(getParams());
		} else {
			LOG.warning("Couldn't get RedisStateProvider!");
			return null;
		}
	}

	private RedisStateProvider getInstanceByParams(final ObjectNode params) {
		final RedisStateConfig config = RedisStateConfig.decorate(params);
		String key = config.getHost() + ":" + config.getDbId();
		if (instances.containsKey(key)) {
			return instances.get(key);
		} else {
			synchronized (instances) {
				if (!instances.containsKey(key)) {
					final RedisStateProvider result = new RedisStateProvider(
							params);
					if (result != null) {
						instances.put(key, result);
					}
				}
				return instances.get(key);
			}
		}
	}

	class RedisStateProvider implements StateService {
		private final JedisPool		pool;
		private final int			id;
		private static final String	IDKEY	= "AgentIds";

		public RedisStateProvider(final ObjectNode params) {
			final RedisStateConfig config = RedisStateConfig.decorate(params);
			pool = new JedisPool(new JedisPoolConfig(), config.getHost());
			id = config.getDbId();
		}

		@Override
		public void delete(State instance) {
			delete(instance, false);
		}
		
		@Override
                public void delete(State instance, Boolean instanceOnly) {
		        if(!instanceOnly) {
        		    Jedis redis = getInstance();
                            if (redis.sismember(IDKEY, instance.getId())) {
                                    redis.srem(IDKEY, instance.getId());
                            }
                            returnInstance(redis);
		        }
                }

		@Override
		public Set<String> getStateIds() {
			Jedis redis = getInstance();
			Set<String> res = redis.smembers(IDKEY);
			returnInstance(redis);
			return res;
		}

		public RedisState get(final ObjectNode params) {
			final RedisStateConfig config = RedisStateConfig.decorate(params);
			final RedisState result = new RedisState(config.getId(), this,
					config);
			Jedis redis = getInstance();
			if (!redis.sismember(IDKEY, config.getId())) {
				redis.sadd(IDKEY, config.getId());
			}
			returnInstance(redis);
			return result;
		}

		public Jedis getInstance() {
			Jedis res = pool.getResource();
			res.select(id);
			return res;
		}

		public void returnInstance(final Jedis instance) {
			if (pool != null && instance != null) {
				pool.returnResource(instance);
			}
		}
	}

}
