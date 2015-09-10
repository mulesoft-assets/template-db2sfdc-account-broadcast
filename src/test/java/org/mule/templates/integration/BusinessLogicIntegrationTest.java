/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
import org.mule.templates.db.MySQLDbCreator;

import com.mulesoft.module.batch.BatchTestHelper;

/**
 * The objective of this class is to validate the correct behavior of the flows
 * for this Mule Template that make calls to external systems.
 */
public class BusinessLogicIntegrationTest extends AbstractTemplateTestCase {

	private static final Logger LOGGER = LogManager.getLogger(BusinessLogicIntegrationTest.class);

	protected static final String POLL_FLOW_NAME = "triggerFlow";
	protected static final int TIMEOUT = 60;
	
	private static final String ACCOUNT_NAME = "Account Test Name";
	private static final String ACCOUNT_NUMBER = "123456789";
	private static final String ACCOUNT_PHONE = "+421";

	private static final String PATH_TO_TEST_PROPERTIES = "./src/test/resources/mule.test.properties";
	private static final String PATH_TO_SQL_SCRIPT = "src/main/resources/account.sql";
	private static final String DATABASE_NAME = "DB2SFDCAccountBroadcast" + new Long(new Date().getTime()).toString();
	private static final MySQLDbCreator DBCREATOR = new MySQLDbCreator(DATABASE_NAME, PATH_TO_SQL_SCRIPT, PATH_TO_TEST_PROPERTIES);
	
	private BatchTestHelper helper;
	private Map<String, Object> account;
	private SubflowInterceptingChainLifecycleWrapper deleteAccountFromSalesforce;
	private SubflowInterceptingChainLifecycleWrapper selectAccountFromSalesforce;
	
	protected final Prober pollProber = new PollingProber(60000, 1000l);
	
	@BeforeClass
	public static void init() {
		System.setProperty("page.size", "1000");
		System.setProperty("poll.frequencyMillis", "10000");
		System.setProperty("poll.startDelayMillis", "20000");
		System.setProperty("watermark.default.expression", "#[groovy: new Date(System.currentTimeMillis() - 10000).format(\"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'\", TimeZone.getTimeZone('UTC'))]");

		System.setProperty("database.url", DBCREATOR.getDatabaseUrlWithName());
	}

	@Before
	public void setUp() throws Exception {
		stopFlowSchedulers(POLL_FLOW_NAME);
		DBCREATOR.setUpDatabase();
		
		helper = new BatchTestHelper(muleContext);

		selectAccountFromSalesforce = getSubFlow("selectAccountFromSalesforce");
		selectAccountFromSalesforce.initialise();

		deleteAccountFromSalesforce = getSubFlow("deleteAccountFromSalesforce");
		deleteAccountFromSalesforce.initialise();

		createAccountInDB();
	}
	

	@After
	public void tearDown() throws Exception {
		stopFlowSchedulers("triggerFlow");
		
		final Map<String, Object> acc = new HashMap<String, Object>();
		acc.put("Name", account.get("Name"));
		acc.put("Id", account.get("Id"));
		deleteAccountFromSalesforce(acc);
		
		DBCREATOR.tearDownDataBase();
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testMainFlowNew() throws Exception {
		// Run poll and wait for it to run
		runSchedulersOnce("triggerFlow");

		helper.awaitJobTermination(120 * 1000, 500);
		helper.assertJobWasSuccessful();

		MuleEvent event = selectAccountFromSalesforce.process(getTestEvent(account, MessageExchangePattern.REQUEST_RESPONSE));
		Map<String, Object> result = (Map<String, Object>) event.getMessage().getPayload();
		LOGGER.info("selectAccountFromSalesforce result: " + result);

		Assert.assertNotNull(result);
		Assert.assertNotNull(result.get("Id"));
		Assert.assertEquals(account.get("Industry"), result.get("Industry"));
		Assert.assertEquals(account.get("AccountNumber"), result.get("AccountNumber"));

		account.put("Id", result.get("Id"));

		Assert.assertEquals("There should be matching account Name in Salesforce now", account.get("Name"), result.get("Name"));
	}

	private void deleteAccountFromSalesforce(final Map<String, Object> acc) throws Exception {

		List<Object> idList = new ArrayList<Object>();
		idList.add(acc.get("Id"));

		deleteAccountFromSalesforce.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
	}

	private void createAccountInDB() throws Exception {
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("insertAccountDB");
		flow.initialise();
		account = createDbAccount();

		MuleEvent event = flow.process(getTestEvent(account, MessageExchangePattern.REQUEST_RESPONSE));
		Object result = event.getMessage().getPayload();
		LOGGER.info("insertAccountDB result: " + result);
	}

	private Map<String, Object> createDbAccount() {
		DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd hh:mm:ss");		
		System.out.println("bbb: " + df.print(new Date().getTime() + 24 * 60 * 60 * 1000));
		return ObjectBuilder.anAccount()
				.with("Name", ACCOUNT_NAME + System.currentTimeMillis())
				.with("AccountNumber", ACCOUNT_NUMBER)
				.with("Phone", ACCOUNT_PHONE)
				.with("NumberOfEmployees", 99)
				.with("Industry", "Education")
				.with("LastModifiedDate", df.print(new Date().getTime() + 24 * 60 * 60 * 1000))
				.build();
	}
}
