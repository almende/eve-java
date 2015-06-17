/*
 * UUIDGen.java
 * 
 * Created on 09.08.2003.
 * 
 * eaio: UUID - an implementation of the UUID specification
 * Copyright (c) 2003-2013 Johann Burkard (jb@eaio.com) http://eaio.com.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.almende.util.uuid;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class contains methods to generate UUID fields. These methods have been
 * refactored out of "com.eaio.uuid.UUID".
 * <p>
 * Starting with version 2, this implementation tries to obtain the MAC address
 * of the network card. Under Microsoft Windows, the <code>ifconfig</code>
 * command is used which may pop up a command window in Java Virtual Machines
 * prior to 1.4 once this class is initialized. The command window is closed
 * automatically.
 * <p>
 * The MAC address code has been tested extensively in Microsoft Windows, Linux,
 * Solaris 8, HP-UX 11, but should work in MacOS X and BSDs, too.
 * 
 * @author <a href="mailto:jb@eaio.de">Johann Burkard</a>
 * @version UUIDGen.java 4714 2012-03-16 11:43:28Z johann $
 * @see <a href="http://johannburkard.de/software/uuid/">UUID</a>
 */
public final class UUIDGen {
	
	/**
	 * Instantiates a new uUID gen.
	 */
	private UUIDGen() {
	}
	
	/**
	 * The last time value. Used to remove duplicate UUIDs.
	 */
	private static AtomicLong	lastTime		= new AtomicLong(Long.MIN_VALUE);
	
	/**
	 * The cached MAC address.
	 */
	private static String		macAddress		= null;
	
	/**
	 * The current clock and node value.
	 */
	private static long			clockSeqAndNode	= 0x8000000000000000L;
	
	static {
		
		try {
			Class.forName("java.net.InterfaceAddress");
			macAddress = Class
					.forName("com.eaio.uuid.UUIDGen$HardwareAddressLookup")
					.newInstance().toString();
		} catch (final ExceptionInInitializerError err) {
			// Ignored.
		} catch (final ClassNotFoundException ex) {
			// Ignored.
		} catch (final LinkageError err) {
			// Ignored.
		} catch (final IllegalAccessException ex) {
			// Ignored.
		} catch (final InstantiationException ex) {
			// Ignored.
		} catch (final SecurityException ex) {
			// Ignored.
		}
		
		if (macAddress == null) {
			
			Process p = null;
			BufferedReader in = null;
			
			try {
				final String osname = System.getProperty("os.name", ""), osver = System
						.getProperty("os.version", "");
				
				if (osname.startsWith("Windows")) {
					p = Runtime.getRuntime().exec(
							new String[] { "ipconfig", "/all" }, null);
				}
				
				// Solaris code must appear before the generic code
				else if (osname.startsWith("Solaris")
						|| osname.startsWith("SunOS")) {
					if (osver.startsWith("5.11")) {
						p = Runtime.getRuntime().exec(
								new String[] { "dladm", "show-phys", "-m" },
								null);
					} else {
						final String hostName = getFirstLineOfCommand("uname",
								"-n");
						if (hostName != null) {
							p = Runtime.getRuntime().exec(
									new String[] { "/usr/sbin/arp", hostName },
									null);
						}
					}
				} else if (new File("/usr/sbin/lanscan").exists()) {
					p = Runtime.getRuntime().exec(
							new String[] { "/usr/sbin/lanscan" }, null);
				} else if (new File("/sbin/ifconfig").exists()) {
					p = Runtime.getRuntime().exec(
							new String[] { "/sbin/ifconfig", "-a" }, null);
				}
				
				if (p != null) {
					in = new BufferedReader(new InputStreamReader(
							p.getInputStream()), 128);
					String l = null;
					while ((l = in.readLine()) != null) {
						macAddress = MACAddressParser.parse(l);
						if (macAddress != null
								&& Hex.parseShort(macAddress) != 0xff) {
							break;
						}
					}
				}
				
			} catch (final SecurityException ex) {
				// Ignore it.
			} catch (final IOException ex) {
				// Ignore it.
			} finally {
				if (p != null) {
					try {
						in.close();
						p.getErrorStream().close();
						p.getOutputStream().close();
					} catch (final Exception e) {
						//ignore it
					}
					p.destroy();
				}
			}
			
		}
		
		if (macAddress != null) {
			clockSeqAndNode |= Hex.parseLong(macAddress);
		} else {
			try {
				final byte[] local = InetAddress.getLocalHost().getAddress();
				clockSeqAndNode |= (local[0] << 24) & 0xFF000000L;
				clockSeqAndNode |= (local[1] << 16) & 0xFF0000;
				clockSeqAndNode |= (local[2] << 8) & 0xFF00;
				clockSeqAndNode |= local[3] & 0xFF;
			} catch (final UnknownHostException ex) {
				clockSeqAndNode |= (long) (Math.random() * 0x7FFFFFFF);
			}
		}
		
		// Skip the clock sequence generation process and use random instead.
		
		clockSeqAndNode |= (long) (Math.random() * 0x3FFF) << 48;
		
	}
	
	/**
	 * Returns the current clockSeqAndNode value.
	 * 
	 * @return the clockSeqAndNode value
	 * @see UUID#getClockSeqAndNode()
	 */
	public static long getClockSeqAndNode() {
		return clockSeqAndNode;
	}
	
	/**
	 * Generates a new time field. Each time field is unique and larger than the
	 * previously generated time field.
	 * 
	 * @return a new time value
	 * @see UUID#getTime()
	 */
	public static long newTime() {
		return createTime(System.currentTimeMillis());
	}
	
	/**
	 * Creates a new time field from the given timestamp. Note that even
	 * identical
	 * values of <code>currentTimeMillis</code> will produce different time
	 * fields.
	 * 
	 * @param currentTimeMillis
	 *            the timestamp
	 * @return a new time value
	 * @see UUID#getTime()
	 */
	public static long createTime(final long currentTimeMillis) {
		
		long time;
		
		// UTC time
		
		long timeMillis = (currentTimeMillis * 10000) + 0x01B21DD213814000L;
		
		while (true) {
			final long current = lastTime.get();
			if (timeMillis > current) {
				if (lastTime.compareAndSet(current, timeMillis)) {
					break;
				}
			} else {
				if (lastTime.compareAndSet(current, current + 1)) {
					timeMillis = current + 1;
					break;
				}
			}
		}
		
		// time low
		
		time = timeMillis << 32;
		
		// time mid
		
		time |= (timeMillis & 0xFFFF00000000L) >> 16;
		
		// time hi and version
		
		// version 1
		time |= 0x1000 | ((timeMillis >> 48) & 0x0FFF);
		
		return time;
		
	}
	
	/**
	 * Returns the MAC address. Not guaranteed to return anything.
	 * 
	 * @return the MAC address, may be <code>null</code>
	 */
	public static String getMACAddress() {
		return macAddress;
	}
	
	/**
	 * Returns the first line of the shell command.
	 * 
	 * @param commands
	 *            the commands to run
	 * @return the first line of the command
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	static String getFirstLineOfCommand(final String... commands)
			throws IOException {
		
		Process p = null;
		BufferedReader reader = null;
		
		try {
			p = Runtime.getRuntime().exec(commands);
			reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()), 128);
			
			return reader.readLine();
		} finally {
			if (p != null) {
				try {
					reader.close();
					p.getErrorStream().close();
					p.getOutputStream().close();
				} catch (final Exception e) {
					//ignore it
				}
				p.destroy();
			}
		}
		
	}
	
	/**
	 * Scans MAC addresses for good ones.
	 */
	static class HardwareAddressLookup {
		
		/**
		 * To string.
		 * 
		 * @return the string
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			String out = null;
			try {
				final Enumeration<NetworkInterface> ifs = NetworkInterface
						.getNetworkInterfaces();
				if (ifs != null) {
					while (ifs.hasMoreElements()) {
						final NetworkInterface iface = ifs.nextElement();
						final byte[] hardware = iface.getHardwareAddress();
						if (hardware != null && hardware.length == 6
								&& hardware[1] != (byte) 0xff) {
							out = Hex.append(new StringBuilder(36), hardware)
									.toString();
							break;
						}
					}
				}
			} catch (final SocketException ex) {
				// Ignore it.
			}
			return out;
		}
		
	}
	
}
