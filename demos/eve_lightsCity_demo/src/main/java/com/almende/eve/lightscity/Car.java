/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 * Author: Luis F. M. Cunha
 */
package com.almende.eve.lightscity;

import java.io.IOException;
import java.net.URI;

import org.joda.time.DateTime;

import com.almende.eve.agent.Agent;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.formats.Params;
import com.almende.util.TypeUtil;

@Access(AccessType.PUBLIC)
public class Car extends Agent {

	enum directions {
		down, right, up, left
	};

	// Create a string type using TypeUtil. When the type is used a lot of times
	// it performs better declaring it this way
	private String							carNumber;
	private int								route			= 0;
	private int								route_index		= 0;

	private Position						carPosition		= new Position(0, 0);

	private directions						direction		= directions.down;
	private int								velocity		= 80;

	// map info
	private static int						verticalLine0	= 80;
	private static int						verticalLine1	= 380;
	private static int						verticalLine2	= 630;
	private static int						verticalLine3	= 1080;

	private static int						horizontalLine0	= 80;
	private static int						horizontalLine1	= 430;
	private static int						horizontalLine2	= 830;

	private static int						handDistance	= 25;

	private final Interception				interception00	= new Interception(
																	new Position(
																			verticalLine0
																					- handDistance,
																			horizontalLine0
																					- handDistance),
																	new Position(
																			verticalLine0
																					+ handDistance,
																			horizontalLine0
																					- handDistance),
																	new Position(
																			verticalLine0
																					- handDistance,
																			horizontalLine0
																					+ handDistance),
																	new Position(
																			verticalLine0
																					+ handDistance,
																			horizontalLine0
																					+ handDistance));

	private final Interception				interception01	= new Interception(
																	new Position(
																			verticalLine1
																					- handDistance,
																			horizontalLine0
																					- handDistance),
																	new Position(
																			verticalLine1
																					+ handDistance,
																			horizontalLine0
																					- handDistance),
																	new Position(
																			verticalLine1
																					- handDistance,
																			horizontalLine0
																					+ handDistance),
																	new Position(
																			verticalLine1
																					+ handDistance,
																			horizontalLine0
																					+ handDistance));

	private final Interception				interception02	= new Interception(
																	new Position(
																			verticalLine2
																					- handDistance,
																			horizontalLine0
																					- handDistance),
																	new Position(
																			verticalLine2
																					+ handDistance,
																			horizontalLine0
																					- handDistance),
																	new Position(
																			verticalLine2
																					- handDistance,
																			horizontalLine0
																					+ handDistance),
																	new Position(
																			verticalLine2
																					+ handDistance,
																			horizontalLine0
																					+ handDistance));

	private final Interception				interception03	= new Interception(
																	new Position(
																			verticalLine3
																					- handDistance,
																			horizontalLine0
																					- handDistance),
																	new Position(
																			verticalLine3
																					+ handDistance,
																			horizontalLine0
																					- handDistance),
																	new Position(
																			verticalLine3
																					- handDistance,
																			horizontalLine0
																					+ handDistance),
																	new Position(
																			verticalLine3
																					+ handDistance,
																			horizontalLine0
																					+ handDistance));

	private final Interception				interception10	= new Interception(
																	new Position(
																			verticalLine0
																					- handDistance,
																			horizontalLine1
																					- handDistance),
																	new Position(
																			verticalLine0
																					+ handDistance,
																			horizontalLine1
																					- handDistance),
																	new Position(
																			verticalLine0
																					- handDistance,
																			horizontalLine1
																					+ handDistance),
																	new Position(
																			verticalLine0
																					+ handDistance,
																			horizontalLine1
																					+ handDistance));

	private final Interception				interception12	= new Interception(
																	new Position(
																			verticalLine2
																					- handDistance,
																			horizontalLine1
																					- handDistance),
																	new Position(
																			verticalLine2
																					+ handDistance,
																			horizontalLine1
																					- handDistance),
																	new Position(
																			verticalLine2
																					- handDistance,
																			horizontalLine1
																					+ handDistance),
																	new Position(
																			verticalLine2
																					+ handDistance,
																			horizontalLine1
																					+ handDistance));

	private final Interception				interception13	= new Interception(
																	new Position(
																			verticalLine3
																					- handDistance,
																			horizontalLine1
																					- handDistance),
																	new Position(
																			verticalLine3
																					+ handDistance,
																			horizontalLine1
																					- handDistance),
																	new Position(
																			verticalLine3
																					- handDistance,
																			horizontalLine1
																					+ handDistance),
																	new Position(
																			verticalLine3
																					+ handDistance,
																			horizontalLine1
																					+ handDistance));

	private final Interception				interception20	= new Interception(
																	new Position(
																			verticalLine0
																					- handDistance,
																			horizontalLine2
																					- handDistance),
																	new Position(
																			verticalLine0
																					+ handDistance,
																			horizontalLine2
																					- handDistance),
																	new Position(
																			verticalLine0
																					- handDistance,
																			horizontalLine2
																					+ handDistance),
																	new Position(
																			verticalLine0
																					+ handDistance,
																			horizontalLine2
																					+ handDistance));

	private final Interception				interception21	= new Interception(
																	new Position(
																			verticalLine1
																					- handDistance,
																			horizontalLine2
																					- handDistance),
																	new Position(
																			verticalLine1
																					+ handDistance,
																			horizontalLine2
																					- handDistance),
																	new Position(
																			verticalLine1
																					- handDistance,
																			horizontalLine2
																					+ handDistance),
																	new Position(
																			verticalLine1
																					+ handDistance,
																			horizontalLine2
																					+ handDistance));

	private final Interception				interception23	= new Interception(
																	new Position(
																			verticalLine3
																					- handDistance,
																			horizontalLine2
																					- handDistance),
																	new Position(
																			verticalLine3
																					+ handDistance,
																			horizontalLine2
																					- handDistance),
																	new Position(
																			verticalLine3
																					- handDistance,
																			horizontalLine2
																					+ handDistance),
																	new Position(
																			verticalLine3
																					+ handDistance,
																			horizontalLine2
																					+ handDistance));

	private final int[][][]					route_array		= new int[][][] {
			{ { this.interception20.bottomLeft.y, directions.right.ordinal() },
			{ this.interception23.bottomRight.x, directions.up.ordinal() },
			{ this.interception03.topRight.y, directions.left.ordinal() },
			{ this.interception00.topLeft.x, directions.down.ordinal() } },
			{ { this.interception21.bottomLeft.y, directions.right.ordinal() },
			{ this.interception21.bottomRight.x, directions.up.ordinal() },
			{ this.interception01.topRight.y, directions.left.ordinal() },
			{ this.interception01.topLeft.x, directions.down.ordinal() } },
			{ { this.interception10.bottomLeft.y, directions.right.ordinal() },
			{ this.interception13.bottomRight.x, directions.up.ordinal() },
			{ this.interception13.topRight.y, directions.left.ordinal() },
			{ this.interception10.topLeft.x, directions.down.ordinal() } },
			{ { this.interception13.topLeft.y, directions.left.ordinal() },
			{ this.interception12.topRight.x, directions.up.ordinal() },
			{ this.interception02.bottomRight.y, directions.right.ordinal() },
			{ this.interception03.bottomLeft.x, directions.down.ordinal() } } };

	// constructor
	public Car() {}

	public void openConnectionAndStart() throws IOException {
		System.out.println("Connection opened with " + getId());
		// start();
	}

	public void setCarProperties(@Name("carNumber") String carNumber,
			@Name("route") int route, @Name("carPositionX") int carPositionX,
			@Name("carPositionY") int carPositionY) {

		this.route = route;
		this.carNumber = carNumber;
		this.carPosition = new Position(carPositionX, carPositionY);

	}

	public void start() {
		schedule("startDrive", null, DateTime.now().plusSeconds(2));
	}

	public void startDrive() throws IOException {

		int limit = route_array[route][route_index][0];
		boolean next = false;
		// Move car position, check next leg
		switch (direction) {
			case down:
				this.carPosition.incY(5);
				if (this.carPosition.getY() >= limit) {
					this.carPosition.setY(limit);
					next = true;
				}
				break;
			case right:
				this.carPosition.incX(5);
				if (this.carPosition.getX() >= limit) {
					this.carPosition.setX(limit);
					next = true;
				}
				break;
			case up:
				this.carPosition.decY(5);
				if (this.carPosition.getY() <= limit) {
					this.carPosition.setY(limit);
					next = true;
				}
				break;
			case left:
				this.carPosition.decX(5);
				if (this.carPosition.getX() <= limit) {
					this.carPosition.setX(limit);
					next = true;
				}
				break;
		}
		if (next) {
			this.direction = directions.values()[route_array[route][route_index][1]];
			route_index = (route_index + 1) % route_array[route].length;
		}
		updatePositionToBrowser(this.carPosition);

		// Call this function again
		schedule("startDrive", null, DateTime.now().plusMillis(this.velocity));

	}

	public Position getCarPosition() {
		return (this.carPosition);
	}

	// ----------------------------------------------------------------------------------------

	public void updatePositionToBrowser(Position carPosition)
			throws IOException {
		
		URI url = URI.create("wsclient:carBrowser" + this.carNumber);
		String method = "updatePosition";
		Params params = new Params();
		params.add("x", carPosition.getX());
		params.add("y", carPosition.getY());

		caller.call(url, method, params);
	}

	public String getCarDirection() {
		return this.direction.name();
	}

	public void setVelocity(@Name("vel") int vel) {
		this.velocity = vel;
	}

	// ------ Classes ------

	class Interception {
		public Position	topLeft;
		public Position	topRight;
		public Position	bottomLeft;
		public Position	bottomRight;

		public Interception(Position topLeft, Position topRight,
				Position bottomLeft, Position bottomRight) {
			this.topLeft = topLeft;
			this.topRight = topRight;
			this.bottomLeft = bottomLeft;
			this.bottomRight = bottomRight;
		}
	}
}
