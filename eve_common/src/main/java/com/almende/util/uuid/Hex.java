/*
 * Hex.java
 * Created 04.07.2003.
 * eaio: UUID - an implementation of the UUID specification Copyright (c)
 * 2003-2013 Johann Burkard (jb@eaio.com)
 * http://eaio.com.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated
 * documentation files (the "Software"), to deal in the Software without
 * restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the
 * Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.almende.util.uuid;

import java.io.IOException;

/**
 * Number-to-hexadecimal and hexadecimal-to-number conversions.
 * 
 * @author <a href="mailto:jb@eaio.com">Johann Burkard</a>
 * @version Hex.java 4714 2012-03-16 11:43:28Z johann $
 * @see <a href="http://johannburkard.de/software/uuid/">UUID</a>
 */
public final class Hex {

	/**
	 * Instantiates a new hex.
	 */
	private Hex() {}

	/** The Constant DIGITS. */
	private static final char[]	DIGITS	= { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * Turns a <code>short</code> into hex octets.
	 * 
	 * @param a
	 *            the {@link Appendable}, may not be <code>null</code>
	 * @param in
	 *            the integer
	 * @return {@link Appendable}
	 */
	public static Appendable append(final Appendable a, final short in) {
		return append(a, (long) in, 4);
	}

	/**
	 * Turns a <code>short</code> into hex octets.
	 * 
	 * @param a
	 *            the {@link Appendable}, may not be <code>null</code>
	 * @param in
	 *            the integer
	 * @param length
	 *            the number of octets to produce
	 * @return {@link Appendable}
	 */
	public static Appendable append(final Appendable a, final short in,
			final int length) {
		return append(a, (long) in, length);
	}

	/**
	 * Turns an <code>int</code> into hex octets.
	 * 
	 * @param a
	 *            the {@link Appendable}, may not be <code>null</code>
	 * @param in
	 *            the integer
	 * @return {@link Appendable}
	 */
	public static Appendable append(final Appendable a, final int in) {
		return append(a, (long) in, 8);
	}

	/**
	 * Turns an <code>int</code> into hex octets.
	 * 
	 * @param a
	 *            the {@link Appendable}, may not be <code>null</code>
	 * @param in
	 *            the integer
	 * @param length
	 *            the number of octets to produce
	 * @return {@link Appendable}
	 */
	public static Appendable append(final Appendable a, final int in,
			final int length) {
		return append(a, (long) in, length);
	}

	/**
	 * Turns a <code>long</code> into hex octets.
	 * 
	 * @param a
	 *            the {@link Appendable}, may not be <code>null</code>
	 * @param in
	 *            the long
	 * @return {@link Appendable}
	 */
	public static Appendable append(final Appendable a, final long in) {
		return append(a, in, 16);
	}

	/**
	 * Turns a <code>long</code> into hex octets.
	 * 
	 * @param a
	 *            the {@link Appendable}, may not be <code>null</code>
	 * @param in
	 *            the long
	 * @param length
	 *            the number of octets to produce
	 * @return {@link Appendable}
	 */
	public static Appendable append(final Appendable a, final long in,
			final int length) {
		try {
			int lim = (length << 2) - 4;
			while (lim >= 0) {
				a.append(DIGITS[(byte) (in >> lim) & 0x0f]);
				lim -= 4;
			}
		} catch (final IOException ex) {
			// Bla
		}
		return a;
	}

	/**
	 * Turns a <code>byte</code> array into hex octets.
	 * 
	 * @param a
	 *            the {@link Appendable}, may not be <code>null</code>
	 * @param bytes
	 *            the <code>byte</code> array
	 * @return {@link Appendable}
	 */
	public static Appendable append(final Appendable a, final byte[] bytes) {
		try {
			for (final byte b : bytes) {
				a.append(DIGITS[(byte) ((b & 0xF0) >> 4)]);
				a.append(DIGITS[(byte) (b & 0x0F)]);
			}
		} catch (final IOException ex) {
			// Bla
		}
		return a;
	}

	/**
	 * Parses a <code>long</code> from a hex encoded number. This method will
	 * skip all characters that are not 0-9,
	 * A-F and a-f.
	 * <p>
	 * Returns 0 if the {@link CharSequence} does not contain any interesting
	 * characters.
	 * 
	 * @param s
	 *            the {@link CharSequence} to extract a <code>long</code> from,
	 *            may not be <code>null</code>
	 * @return a <code>long</code>
	 */
	public static long parseLong(final CharSequence s) {
		long out = 0;
		byte shifts = 0;
		char c;
		for (int i = 0; i < s.length() && shifts < 16; i++) {
			c = s.charAt(i);
			if ((c > 47) && (c < 58)) {
				++shifts;
				out <<= 4;
				out |= c - 48;
			} else if ((c > 64) && (c < 71)) {
				++shifts;
				out <<= 4;
				out |= c - 55;
			} else if ((c > 96) && (c < 103)) {
				++shifts;
				out <<= 4;
				out |= c - 87;
			}
		}
		return out;
	}

	/**
	 * Parses a <code>short</code> from a hex encoded number. This method will
	 * skip all characters that are not 0-9,
	 * A-F and a-f.
	 * <p>
	 * Returns 0 if the {@link CharSequence} does not contain any interesting
	 * characters.
	 * 
	 * @param s
	 *            the {@link CharSequence} to extract a <code>short</code> from,
	 *            may not be <code>null</code>
	 * @return a <code>short</code>
	 */
	public static short parseShort(final String s) {
		short out = 0;
		byte shifts = 0;
		char c;
		for (int i = 0; i < s.length() && shifts < 4; i++) {
			c = s.charAt(i);
			if ((c > 47) && (c < 58)) {
				++shifts;
				out <<= 4;
				out |= c - 48;
			} else if ((c > 64) && (c < 71)) {
				++shifts;
				out <<= 4;
				out |= c - 55;
			} else if ((c > 96) && (c < 103)) {
				++shifts;
				out <<= 4;
				out |= c - 87;
			}
		}
		return out;
	}

}
