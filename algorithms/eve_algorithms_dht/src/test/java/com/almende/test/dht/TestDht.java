/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.test.dht;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.BitSet;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.dht.Bucket;
import com.almende.dht.Constants;
import com.almende.dht.Key;
import com.almende.dht.Node;
import com.almende.dht.RoutingTable;
import com.almende.util.jackson.JOM;

/**
 * The Class TestRpc.
 */
public class TestDht extends TestCase {
	final static URI TESTURI = URI.create("http://localhost:1");

	/**
	 * Test keys.
	 */
	@Test
	public void testKeys() {
		final Key myKey = new Key(BitSet.valueOf(new long[] { 1029287173,
				128237194, 182934729 }));

		// Check equals:
		assertEquals("Identity test1 failed", myKey, myKey);
		assertEquals(
				"Identity test2 failed",
				myKey,
				new Key(BitSet.valueOf(new long[] { 1029287173, 128237194,
						182934729 })));
		assertNotSame("Identity test3 failed", myKey,
				new Key(BitSet.valueOf(new long[] { 1 })));
		assertNotSame(
				"Identity test4 failed",
				myKey,
				new Key(BitSet.valueOf(new long[] { 1029287173, 128237194,
						182934728 })));

		// Check distance:
		assertEquals(
				"Distance from 0-key not correct",
				myKey.dist(new Key()),
				new Key(BitSet.valueOf(new long[] { 1029287173, 128237194,
						182934729 })));
		assertEquals("Distance from myself not 0-key", new Key(),
				myKey.dist(myKey));
		assertEquals("Distance between 1 and 2 not 3-key",
				new Key(BitSet.valueOf(new long[] { 1 })).dist(new Key(BitSet
						.valueOf(new long[] { 2 }))),
				new Key(BitSet.valueOf(new long[] { 3 })));
		assertEquals(
				"Distance rank between 1 and 2 not 2",
				new Key(BitSet.valueOf(new long[] { 1 })).dist(
						new Key(BitSet.valueOf(new long[] { 2 }))).rank(), 2);

		// Check rank:
		assertEquals("Rank of 0-key not 0", new Key().rank(), 0);
		assertEquals("Rank of 1-key not 1",
				new Key(BitSet.valueOf(new long[] { 1 })).rank(), 1);
		assertEquals("Rank of second long-key not 65",
				new Key(BitSet.valueOf(new long[] { 0, 1 })).rank(), 65);
		assertEquals("Rank of full long-key not 64",
				new Key(BitSet.valueOf(new long[] { -1 })).rank(), 64);

		// Check mostly set lists:
		final BitSet val = new BitSet(Constants.BITLENGTH);
		val.set(0, Constants.BITLENGTH);
		assertEquals("Rank of full key not " + Constants.BITLENGTH,
				new Key(val).rank(), Constants.BITLENGTH);
		val.set(Constants.BITLENGTH - 1, false);
		assertEquals(
				"Rank of nearly full key not " + (Constants.BITLENGTH - 1),
				new Key(val).rank(), Constants.BITLENGTH - 1);
		val.set(Constants.BITLENGTH - 1);
		assertEquals("Constant BITLENGTH no longer 160?", 160,
				Constants.BITLENGTH);
		assertEquals(
				"Long-filled key not equals to full key",
				new Key(val),
				new Key(BitSet.valueOf(new long[] { -1, -1,
						Long.MAX_VALUE >>> 31 })));
		assertEquals("Filled key distance to 0-key is not filled key", new Key(
				val).dist(new Key()), new Key(val));

		// Check Random keys:
		assertNotSame("Two random keys are the same", Key.random(),
				Key.random());
		assertNotSame("Random key results in 0-key", Key.random(), new Key());

		// Check comparison:
		assertTrue(
				"1-key is not larger than 0-key",
				new Key().compareTo(new Key(BitSet.valueOf(new long[] { 1 }))) < 0);
		assertTrue("Random key is not larger than 0-key",
				new Key().compareTo(Key.random()) < 0);
		final BitSet val1 = new BitSet(Constants.BITLENGTH);
		val1.set(1);
		final BitSet val2 = new BitSet(Constants.BITLENGTH);
		val2.set(2);
		assertTrue("1-key is not smaller than 2-key",
				new Key(val1).compareTo(new Key(val2)) < 0);
		assertTrue("2-key is not larger than 1-key",
				new Key(val2).compareTo(new Key(val1)) > 0);
		assertTrue("2-key is not the same as 2-key",
				new Key(val2).compareTo(new Key(val2)) == 0);
		assertTrue("Full key is not larger than 1-key",
				new Key(val).compareTo(new Key(val1)) > 0);

		// Check sorting:
		final BitSet val3 = new BitSet(Constants.BITLENGTH);
		val3.set(3);
		final BitSet val4 = new BitSet(Constants.BITLENGTH);
		val4.set(4);

		final Key[] arr = new Key[] { new Key(val), new Key(val1),
				new Key(val2), new Key(val4), new Key(val3) };
		Arrays.sort(arr);
		assertEquals("Sorting incorrect", arr[0], new Key(val1));
		assertEquals("Sorting incorrect", arr[1], new Key(val2));
		assertEquals("Sorting incorrect", arr[2], new Key(val3));
		assertEquals("Sorting incorrect", arr[3], new Key(val4));
		assertEquals("Sorting incorrect", arr[4], new Key(val));

		final Key[] arr1 = new Key[] { Key.fromHexString("01"),
				Key.fromHexString("02"), Key.fromHexString("03"),
				Key.fromHexString("04"), Key.fromHexString("05"),
				Key.fromHexString("06"), Key.fromHexString("07"),
				Key.fromHexString("08") };
		Arrays.sort(arr1);
		assertEquals("Incorrect sorting", "[01, 02, 03, 04, 05, 06, 07, 08]",
				Arrays.toString(arr1));

		// Check SHA1 keying:
		assertEquals("Empty string sha1 key incorrect", Key.digest("")
				.toString(), "DA39A3EE5E6B4B0D3255BFEF95601890AFD80709");
		assertEquals("Teststring1 sha1 key incorrect", Key.digest("abc")
				.toString(), "A9993E364706816ABA3E25717850C26C9CD0D89D");
		assertEquals("Teststring2 sha1 key incorrect",
				Key.digest("abcdefghijklmnopqrstuvwxyz").toString(),
				"32D10C7B8CF96570CA04CE37F2A19D84240D3A89");

		// Check syntax sugar:
		assertEquals("From HexString results in incorrect key",
				Key.fromString("abc"),
				Key.fromHexString("A9993E364706816ABA3E25717850C26C9CD0D89D"));
		assertEquals(
				"From HexString results in incorrect key (empty string key)",
				Key.digest(""),
				Key.fromHexString("DA39A3EE5E6B4B0D3255BFEF95601890AFD80709"));
		assertEquals("From HexString doesn't create 1-key",
				new Key(BitSet.valueOf(new long[] { 1 })),
				Key.fromHexString("01"));
	}

	/**
	 * Test buckets.
	 */
	@Test
	public void testBuckets() {
		Bucket bucket = new Bucket(10);
		final Node test = new Node(Key.random(10), TESTURI);
		bucket.seenNode(test);
		
		assertEquals("getClosestNodes didn't return node", 1,
				bucket.getClosestNodes(new Key()).size());
		assertEquals("getClosestNodes didn't return same node", test,
				bucket.getClosestNodes(new Key()).get(0));

		bucket = new Bucket(-1);
		final Node test1 = new Node(Key.fromHexString("01"), TESTURI);
		final Node test2 = new Node(Key.fromHexString("02"), TESTURI);
		final Node test3 = new Node(Key.fromHexString("03"), TESTURI);
		final Node test4 = new Node(Key.fromHexString("04"), TESTURI);
		final Node test5 = new Node(Key.fromHexString("05"), TESTURI);
		final Node test6 = new Node(Key.fromHexString("06"), TESTURI);
		final Node test7 = new Node(Key.fromHexString("07"), TESTURI);
		final Node test8 = new Node(Key.fromHexString("08"), TESTURI);
		bucket.seenNode(test1);
		bucket.seenNode(test2);
		bucket.seenNode(test3);
		bucket.seenNode(test4);
		bucket.seenNode(test5);
		bucket.seenNode(test6);
		bucket.seenNode(test7);

		assertEquals("Incorrect order 0", test7,
				bucket.getClosestNodes(Key.fromHexString("0F"), 1).get(0));

		bucket.seenNode(test8);

		assertEquals("Incorrect order 1", test1,
				bucket.getClosestNodes(new Key(), 1).get(0));
		assertEquals("Incorrect order 2", test2,
				bucket.getClosestNodes(new Key(), 2).get(1));
		assertEquals("Incorrect order 3", test1,
				bucket.getClosestNodes(Key.fromHexString("10"), 1).get(0));
		assertEquals("Incorrect order 4", test5,
				bucket.getClosestNodes(Key.fromHexString("04"), 2).get(1));
		assertEquals("Incorrect order 5", test3,
				bucket.getClosestNodes(Key.fromHexString("04")).get(6));
		assertEquals("Incorrect order 6", test8,
				bucket.getClosestNodes(Key.fromHexString("04")).get(7));
		assertEquals("Incorrect order 7", test6,
				bucket.getClosestNodes(Key.fromHexString("07"), 2).get(1));

		assertEquals("Limit doesn't work", 3,
				bucket.getClosestNodes(new Key(), 3).size());
		assertEquals("Too large result", 8,
				bucket.getClosestNodes(new Key(), 10).size());

		assertEquals("Filter not working 1", test2, bucket.getClosestNodes(
				new Key(), 1, new Key[] { test1.getKey() }).get(0));
		assertEquals(
				"Filter not working 2",
				test6,
				bucket.getClosestNodes(new Key(), 3, new Key[] {
						test1.getKey(), test3.getKey(), test5.getKey() }).get(2));
	}

	/**
	 * Test table.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testTable() throws IOException {
		final RoutingTable rt = new RoutingTable(Key.fromHexString("04"));
		rt.seenNode(new Node(Key.fromHexString("05"), TESTURI));
		rt.seenNode(new Node(Key.fromHexString("09"), TESTURI));
		rt.seenNode(new Node(Key.fromHexString("0D"), TESTURI));
		rt.seenNode(new Node(Key.fromHexString("01"), TESTURI));

		final Bucket bucket = rt.getBucket(Key.fromHexString("0C"));
		assertEquals("Wrong node in bucket 1", Key.fromHexString("0D"),
				bucket.getClosestNodes(Key.fromHexString("0F"), 1).get(0).getKey());

		assertEquals("Wrong result length 1", 3,
				rt.getClosestNodes(Key.fromHexString("0F"), 3).size());
		assertEquals("Wrong result length 2", 2,
				rt.getClosestNodes(Key.fromHexString("0F"), 2).size());
		assertEquals("Wrong result length 3", 4,
				rt.getClosestNodes(Key.fromHexString("05"), 10).size());

		// Test serialization to JSON:
		final String json = JOM.getInstance().writeValueAsString(rt);
		final RoutingTable rt2 = JOM.getInstance().readValue(json,
				RoutingTable.class);

		assertEquals("Routing table key incorrect", Key.fromHexString("04"),
				rt2.getMyKey());

		final Bucket bucket2 = rt2.getBucket(Key.fromHexString("0C"));
		assertEquals("Wrong node in bucket 2", Key.fromHexString("0D"),
				bucket2.getClosestNodes(Key.fromHexString("0F"), 1).get(0).getKey());

		assertEquals("Wrong result length 4", 3,
				rt2.getClosestNodes(Key.fromHexString("0F"), 3).size());
		assertEquals("Wrong result length 5", 2,
				rt2.getClosestNodes(Key.fromHexString("0F"), 2).size());
		assertEquals("Wrong result length 6", 4,
				rt2.getClosestNodes(Key.fromHexString("05"), 10).size());

	}
}
