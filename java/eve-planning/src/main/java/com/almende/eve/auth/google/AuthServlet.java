/**
 * AuthServlet for Google OAuth 2.0
 * 
 * Jos de Jong, 2013-02-15
 * 
 * Used libraries:
 * jackson-annotations-2.0.0.jar
 * jackson-core-2.0.0.jar
 * jackson-databind-2.0.0.jar
 * 
 * Documentation:
 * https://developers.google.com/accounts/docs/OAuth2WebServer
 */
package com.almende.eve.auth.google;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.almende.eve.config.Config;
import com.almende.eve.rpc.jsonrpc.JSONRequest;
import com.almende.eve.rpc.jsonrpc.jackson.JOM;
import com.almende.util.HttpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class AuthServlet.
 */
@SuppressWarnings("serial")
public class AuthServlet extends HttpServlet {
	private final Logger		logger			= Logger.getLogger(this
														.getClass()
														.getSimpleName());
	
	// Specify the correct client id and secret for web applications
	// Create them at the Google API console:
	// https://code.google.com/apis/console/
	private String				CLIENT_ID		= null;
	private String				CLIENT_SECRET	= null;
	
	/**
	 * The redirect uri.
	 */
	String						REDIRECT_URI	= null;
	
	// hard coded uri's
	private final String		AGENTS_METHOD	= "setAuthorization";
	private final String		OAUTH_URI		= "https://accounts.google.com/o/oauth2";
	private final String		CONFIG_FILENAME	= "/WEB-INF/eve.yaml";
	private final String		SPACE			= " ";
	private final String		SCOPE			= "https://www.googleapis.com/auth/userinfo.email"
														+ SPACE
														+ "https://www.googleapis.com/auth/userinfo.profile"
														+ SPACE
														+ "https://www.googleapis.com/auth/calendar"
														+ SPACE
														+ "https://www.googleapis.com/auth/tasks";
	
	private final ObjectMapper	mapper			= new ObjectMapper();
	
	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() {
		try {
			// load configuration
			final InputStream is = getServletContext().getResourceAsStream(
					CONFIG_FILENAME);
			final Config config = new Config(is);
			
			CLIENT_ID = config.get("google", "client_id");
			if (CLIENT_ID == null) {
				throw new Exception(
						"Parameter 'google.client_id' missing in config");
			}
			
			CLIENT_SECRET = config.get("google", "client_secret");
			if (CLIENT_SECRET == null) {
				throw new Exception(
						"Parameter 'google.client_secret' missing in config");
			}
			
			REDIRECT_URI = config.get("google_auth_servlet_url");
			
			// TODO: cleanup deprecated parameter some day (deprecated since
			// 2013-02-15)
			if (REDIRECT_URI == null) {
				REDIRECT_URI = config.get("auth_google_servlet_url");
				if (REDIRECT_URI != null) {
					logger.warning("Parameter 'auth_google_servlet_url' is deprecated. "
							+ "Use 'google_auth_servlet_url' instead.");
				}
			}
			
			if (REDIRECT_URI == null) {
				final String path = "auth_google_servlet_url";
				final Exception e = new Exception("Config parameter '" + path
						+ "' is missing");
				e.printStackTrace();
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(final HttpServletRequest req,
			final HttpServletResponse resp) throws IOException {
		// read parameters code and access_token from query parameters or
		// cookies
		final String code = req.getParameter("code");
		final String state = req.getParameter("state");
		final String error = req.getParameter("error");
		final String agentUrl = req.getParameter("agentUrl");
		final String agentMethod = req.getParameter("agentMethod");
		final String applicationCallback = req
				.getParameter("applicationCallback");
		
		final PrintWriter out = resp.getWriter();
		resp.setContentType("text/html");
		
		// print error if any
		if (error != null) {
			printPageStart(out);
			printError(out, error);
			printPageEnd(out);
			return;
		}
		
		// directly redirect to Google authorization page if an agents URL is
		// provided
		if (agentUrl != null && agentMethod != null) {
			redirectToGoogleAuthorization(resp, agentUrl, agentMethod,
					applicationCallback);
			return;
		}
		
		// First step: show a form to authenticate
		if (code == null && state == null) {
			printPageStart(out);
			printAuthorizeForm(out);
			printPageEnd(out);
			return;
		}
		
		// Second step: exchange code with authentication token
		// After having authorized at google, the user is send back to this
		// servlet, with the url containing a code and status
		if (code != null && state != null) {
			final ObjectNode auth = exchangeCodeForAuthorization(code);
			
			// TODO: remove logging
			// System.out.println("Authorization code exchanged for access token: "
			// + mapper.writeValueAsString(auth));
			
			if (auth.has("error")) {
				printPageStart(out);
				printError(out, mapper.writeValueAsString(auth.get("error")));
				printPageEnd(out);
				return;
			}
			
			final ObjectNode stateJson = mapper.readValue(state,
					ObjectNode.class);
			final String statusAgentUrl = stateJson.has("agentUrl") ? stateJson
					.get("agentUrl").asText() : null;
			final String statusAgentMethod = stateJson.has("agentMethod") ? stateJson
					.get("agentMethod").asText() : null;
			final String statusApplicationCallback = stateJson
					.has("applicationCallback") ? stateJson.get(
					"applicationCallback").asText() : null;
			
			// send the retrieved authorization to the agent
			sendAuthorizationToAgent(statusAgentUrl, statusAgentMethod, auth);
			
			if (statusApplicationCallback != null) {
				resp.sendRedirect(statusApplicationCallback);
				return;
			} else {
				printPageStart(out);
				printSuccess(out, statusAgentUrl);
				printPageEnd(out);
				return;
			}
		}
	}
	
	private String createAuthorizationUrl() throws IOException {
		final Map<String, String> params = new HashMap<String, String>();
		params.put("scope", SCOPE);
		params.put("redirect_uri", REDIRECT_URI);
		params.put("response_type", "code");
		params.put("access_type", "offline");
		params.put("client_id", CLIENT_ID);
		params.put("approval_prompt", "force");
		final String url = HttpUtil.appendQueryParams(OAUTH_URI + "/auth",
				params);
		
		return url;
	}
	
	private void redirectToGoogleAuthorization(final HttpServletResponse resp,
			final String agentUrl, final String agentMethod,
			final String applicationCallback) throws IOException {
		String url = createAuthorizationUrl();
		final ObjectNode st = JOM.createObjectNode();
		st.put("agentUrl", agentUrl);
		st.put("agentMethod", agentMethod);
		if (applicationCallback != null) {
			st.put("applicationCallback", applicationCallback);
		}
		
		url += "&state="
				+ URLEncoder.encode(JOM.getInstance().writeValueAsString(st),
						"UTF-8");
		resp.sendRedirect(url);
	}
	
	private ObjectNode exchangeCodeForAuthorization(final String code)
			throws IOException {
		final Map<String, String> params = new HashMap<String, String>();
		params.put("code", code);
		params.put("client_id", CLIENT_ID);
		params.put("client_secret", CLIENT_SECRET);
		params.put("redirect_uri", REDIRECT_URI);
		params.put("grant_type", "authorization_code");
		final String res = HttpUtil.postForm(OAUTH_URI + "/token", params);
		
		final ObjectNode json = mapper.readValue(res, ObjectNode.class);
		return json;
	}
	
	private void sendAuthorizationToAgent(final String agentUrl,
			final String agentMethod, final ObjectNode auth) throws IOException {
		final JSONRequest rpcRequest = new JSONRequest(agentMethod, auth);
		HttpUtil.post(agentUrl, rpcRequest.toString());
	}
	
	private void printPageStart(final PrintWriter out) {
		out.print("<html>"
				+ "<head>"
				+ "<title>Authorize Eve agents</title>"
				+ "<style>"
				+ "body {width: 700px;}"
				+ "body, th, td, input {font-family: arial; font-size: 10pt; color: #4d4d4d;}"
				+ "th {text-align: left;}"
				+ "input[type=text] {border: 1px solid lightgray;}"
				+ ".error {color: red;}"
				+ "</style>"
				+ "</head>"
				+ "<body>"
				+ "<h1>Authorize Eve agents</h1>"
				+ "<p>"
				+ "On this page, you can grant an Eve agent access to your data, "
				+ "for example access to your calendar." + "</p>");
	}
	
	private void printPageEnd(final PrintWriter out) {
		out.print("</body>" + "</html>");
	}
	
	private void printAuthorizeForm(final PrintWriter out) throws IOException {
		final String url = createAuthorizationUrl();
		out.print("<script type='text/javascript'>"
				+ "function auth() {"
				+ "  var state={"
				+ "    \"agentUrl\": document.getElementById('agentUrl').value,"
				+ "    \"agentMethod\": document.getElementById('agentMethod').value"
				+ "  };"
				+ "  var url='"
				+ url
				+ "'+ '&state=' + encodeURI(JSON.stringify(state));"
				+ "  window.location.href=url;"
				+ "}"
				+ "</script>"
				+ "<table>"
				+ "<tr><td>Agent url</td><td><input type='text' id='agentUrl' value=''"
				+ " style='width: 400px;'/></td></tr>"
				+ "<tr><td>Agent method</td><td><input type='text' id='agentMethod' value='"
				+ AGENTS_METHOD
				+ "' style='width: 400px;'/></td></tr>"
				+ "<tr><td><button onclick='auth();'>Authorize</button></td></tr>"
				+ "</table>"
				+ "<script type='text/javascript'>"
				+ "  document.getElementById('agentUrl').value = document.location.origin + '/agents/agentid/';"
				+ "</script>");
	}
	
	private void printSuccess(final PrintWriter out, final String agentUrl) {
		out.print("<p>Agent is succesfully authorized.</p>");
		out.print("<p><a href=\"" + agentUrl + "\">" + agentUrl + "</a></p>");
		out.print("<button onclick='window.location.href=\"" + REDIRECT_URI
				+ "\";'>Ok</button>");
	}
	
	private void printError(final PrintWriter out, final String error) {
		out.print("<p>An error occurred</p>");
		out.print("<pre class='error'>" + error + "</pre>");
		out.print("<button onclick='window.location.href=\"" + REDIRECT_URI
				+ "\";'>Ok</button>");
	}
}
