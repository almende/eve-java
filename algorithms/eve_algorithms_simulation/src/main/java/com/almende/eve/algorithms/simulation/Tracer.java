package com.almende.eve.algorithms.simulation;

import java.net.URI;

import com.almende.util.uuid.UUID;

class Tracer {
	private String	id		= null;
	private URI		owner	= null;

	public Tracer() {}

	public String getId() {
		if (id == null) {
			id = new UUID().toString();
		}
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public URI getOwner() {
		return owner;
	}

	public void setOwner(final URI owner) {
		this.owner = owner;
	}

	@Override
	public int hashCode() {
		if (this.id == null) {
			return -1;
		}
		return this.id.hashCode();
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		if (this.id == null) {
			return false;
		}
		if (!(o instanceof Tracer)) {
			return false;
		}
		final Tracer other = (Tracer) o;
		return this.id.equals(other.id);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + ":" + this.id + ":" + this.owner;
	}
}
