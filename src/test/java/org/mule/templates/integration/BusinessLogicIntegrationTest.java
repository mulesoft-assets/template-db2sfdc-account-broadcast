package org.mule.templates.integration;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.tck.probe.PollingProber;
import org.mule.tck.probe.Prober;
import org.mule.templates.builders.ObjectBuilder;

import com.mulesoft.module.batch.BatchTestHelper;

/**
 * The objective of this class is to validate the correct behavior of the flows
 * for this Mule Template that make calls to external systems.
 */
public class BusinessLogicIntegrationTest extends AbstractTemplateTestCase {

	protected static final String POLL_FLOW_NAME = "triggerFlow";
	protected static final int TIMEOUT = 60;
	private static final Logger log = Logger.getLogger(BusinessLogicIntegrationTest.class);
	private static final String ACCOUNT_NAME = "Account Test Name";
	private static final String ACCOUNT_NUMBER = "123456789";
	private static final String ACCOUNT_PHONE = "+421";
	private static final String DATABASE_NAME = "SFDC2DBAccountBroadcast" + new Long(new Date().getTime()).toString();
	private static final String DATABASE_URL = "jdbc:mysql://iappsandbox.cbbmvnwhlhi8.us-east-1.rds.amazonaws.com:3306/?user=iappsandbox&password=PMmulebells";
	private static final String TABLES_SQL_FILE = "src/main/resources/account.sql";
	
	private BatchTestHelper helper;
	private Map<String, Object> account;
	
	protected final Prober pollProber = new PollingProber(60000, 1000l);
	
	@BeforeClass
	public static void init() {
		System.setProperty("page.size", "1000");
		System.setProperty("poll.frequencyMillis", "10000");
		System.setProperty("poll.startDelayMillis", "20000");
		System.setProperty("watermark.default.expression", "#[groovy: new Date(System.currentTimeMillis() - 10000).format(\"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'\", TimeZone.getTimeZone('UTC'))]");
		System.setProperty("database.url", "jdbc:mysql://iappsandbox.cbbmvnwhlhi8.us-east-1.rds.amazonaws.com:3306/"+DATABASE_NAME+"?rewriteBatchedStatements=true&password=PMmulebells&user=iappsandbox");
	}

	@Before
	public void setUp() throws Exception {
		stopFlowSchedulers(POLL_FLOW_NAME);
		
		setUpDatabase();
		
		helper = new BatchTestHelper(muleContext);
		createAccountInDB();
	}
	

	@After
	public void tearDown() throws Exception {
		stopFlowSchedulers("triggerFlow");
		
		final Map<String, Object> acc = new HashMap<String, Object>();
		acc.put("Name", account.get("Name"));
		deleteAccountFromDB(acc);
		
		tearDownDataBase();
	}
	
	private void tearDownDataBase() {
		
		System.out.println("******************************** Delete Tables from MySQL DB **************************");
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		
			// Get a connection
			conn = DriverManager.getConnection(DATABASE_URL);
		
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("DROP SCHEMA "+DATABASE_NAME);
		} catch (Exception except) {
			except.printStackTrace();
		}
	}
	
	private void setUpDatabase() {
		
		System.out.println("******************************** Populate MySQL DB **************************");
		Connection conn = null;
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			
			// Get a connection
			System.err.println(DATABASE_URL);
			conn = DriverManager.getConnection("jdbc:mysql://iappsandbox.cbbmvnwhlhi8.us-east-1.rds.amazonaws.com:3306/?user=iappsandbox&password=PMmulebells");
			Statement stmt = conn.createStatement();
			FileInputStream fstream = new FileInputStream(TABLES_SQL_FILE);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String string = "CREATE DATABASE "+DATABASE_NAME;
			System.err.println(string);
			stmt.addBatch(string);
			stmt.addBatch("USE "+DATABASE_NAME);

			String strLine;
			StringBuffer createStatement = new StringBuffer();
			// Specify delimiter according to sql file
			while ((strLine = br.readLine()) != null) {
				if (strLine.length() > 0) {
					strLine.replace("\n", "");
					createStatement.append(strLine);
				}
			}
			stmt.addBatch(createStatement.toString());
			in.close();
		
			System.err.println(createStatement.toString());
			stmt.executeBatch();
			
		} catch (SQLException ex) {
			System.err.println("xxxxx");
		    // handle any errors
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		} catch (Exception except) {
			except.printStackTrace();
		}
	
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMainFlowNew() throws Exception {
		// Run poll and wait for it to run
		runSchedulersOnce("triggerFlow");

		helper.awaitJobTermination(120 * 1000, 500);
		helper.assertJobWasSuccessful();

		SubflowInterceptingChainLifecycleWrapper subflow = getSubFlow("selectAccountFromSalesforce");
		subflow.initialise();

		MuleEvent event = subflow.process(getTestEvent(account, MessageExchangePattern.REQUEST_RESPONSE));
		Map<String, Object> result = (Map<String, Object>) event.getMessage().getPayload();
		log.info("selectAccountFromSalesforce result: " + result);

		Assert.assertNotNull(result);
		Assert.assertEquals("There should be matching account Name in Salesforce now", account.get("Name"), result.get("Name"));
		Assert.assertEquals("There should be matching account AccountNumber in Salesforce now", account.get("AccountNumber"), result.get("AccountNumber"));
		Assert.assertEquals("There should be matching account Phone in Salesforce now", account.get("Phone"), result.get("Phone"));
	}

	private void deleteAccountFromDB(final Map<String, Object> account) throws Exception {
		final SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("deleteAccountFromDB");
		flow.initialise();

		final MuleEvent event = flow.process(getTestEvent(account, MessageExchangePattern.REQUEST_RESPONSE));
		final Object result = event.getMessage().getPayload();
		log.info("deleteAccountFromDB result: " + result);
	}

	private void createAccountInDB() throws Exception {
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("insertAccountDB");
		flow.initialise();
		account = createDbAccount();

		MuleEvent event = flow.process(getTestEvent(account, MessageExchangePattern.REQUEST_RESPONSE));
		Object result = event.getMessage().getPayload();
		log.info("insertAccountDB result: " + result);
	}

	private Map<String, Object> createDbAccount() {
		return ObjectBuilder.anAccount()
				.with("Name", ACCOUNT_NAME + System.currentTimeMillis())
				.with("AccountNumber", ACCOUNT_NUMBER)
				.with("Phone", ACCOUNT_PHONE)
				.with("NumberOfEmployees", 99)
				.build();
	}
}
