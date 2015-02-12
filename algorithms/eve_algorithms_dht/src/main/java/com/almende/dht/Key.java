/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.dht;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;
import java.util.Random;
import java.util.logging.Logger;

/**
 * The Class Key.
 */
public final class Key implements Comparable<Key> {
	private static final Logger	LOG	= Logger.getLogger(Key.class.getName());
	private BitSet				val;

	/**
	 * Instantiates a new key.
	 *
	 * @param val
	 *            the val
	 */
	public Key(final BitSet val) {
		this.val = val;
	}

	/**
	 * Instantiates a new key.
	 */
	public Key() {
		this.val = new BitSet(Constants.BITLENGTH);
	}

	/**
	 * Instantiates a new key.
	 *
	 * @param key
	 *            the key
	 */
	public Key(final String key) {
		this.val = BitSet.valueOf(hexToBytes(key));
	}

	/**
	 * Random.
	 *
	 * @return the key
	 */
	public static Key random() {
		final BitSet set = new BitSet(Constants.BITLENGTH);
		final Random rand = new Random();
		for (int i = 0; i < Constants.BITLENGTH; i++) {
			set.set(i, rand.nextBoolean());
		}
		return new Key(set);
	}

	/**
	 * Random key of rank X.
	 *
	 * @param rank
	 *            the rank
	 * @return the key
	 */
	public static Key random(int rank) {
		if (rank > Constants.BITLENGTH) {
			LOG.warning("Rank too high!");
			rank = Constants.BITLENGTH;
		}
		final BitSet set = new BitSet(Constants.BITLENGTH);
		final Random rand = new Random();
		for (int i = 0; i < rank - 2; i++) {
			set.set(i, rand.nextBoolean());
		}
		set.set(rank - 1);
		return new Key(set);
	}

	/**
	 * Digest.
	 *
	 * @param val
	 *            the val
	 * @return the key
	 */
	public static Key digest(final String val) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			return new Key(BitSet.valueOf(md.digest(val.getBytes("UTF-8"))));
		} catch (NoSuchAlgorithmException e) {
			LOG.severe("SHA1 unknown???!");
		} catch (UnsupportedEncodingException e) {
			LOG.severe("UTF-8 unknown???!");
		}
		return null;
	}

	/**
	 * From hex string.
	 *
	 * @param val
	 *            the val
	 * @return the key
	 */
	public static Key fromHexString(final String val) {
		return new Key(val);
	}

	/**
	 * From string.
	 *
	 * @param val
	 *            the val
	 * @return the key
	 */
	public static Key fromString(final String val) {
		return Key.digest(val);
	}

	/**
	 * Gets the val.
	 *
	 * @return the val
	 */
	public BitSet getVal() {
		return val;
	}

	/**
	 * Sets the val.
	 *
	 * @param val
	 *            the new val
	 */
	public void setVal(BitSet val) {
		this.val = val;
	}

	/**
	 * Rank.
	 *
	 * @return the int
	 */
	public int rank() {
		return val.length();
	}

	/**
	 * Dist.
	 *
	 * @param o
	 *            the o
	 * @return the key
	 */
	public Key dist(final Key o) {
		final BitSet res = ((BitSet) val.clone());
		res.xor(o.val);
		return new Key(res);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return val.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof Key))
			return false;
		if (o == this)
			return true;
		final Key other = (Key) o;
		return (val.equals(other.val));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return bytesToHex(val.toByteArray());
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
		if (len %2 == 1){
			throw new IllegalArgumentException("HexString uneven length:'"+s+"'");
		}
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
					.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Key o) {
		if (this.equals(o))
			return 0;
		if (rank() > o.rank())
			return 1;
		if (rank() < o.rank())
			return -1;
		int index = dist(o).rank() - 1;
		return val.get(index) ? 1 : -1;
	}

}
