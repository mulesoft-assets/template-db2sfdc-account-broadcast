package org.mule.templates.integration;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.construct.Flow;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.templates.builders.ObjectBuilder;

import com.mulesoft.module.batch.BatchTestHelper;

/**
 * The objective of this class is to validate the correct behavior of the flows
 * for this Mule Template that make calls to external systems.
 */
public class BusinessLogicIntegrationTest extends AbstractTemplateTestCase {

	protected static final int TIMEOUT = 60;
	private static final Logger log = Logger.getLogger(BusinessLogicIntegrationTest.class);
	private static final String ACCOUNT_NAME = "Account Test Name";
	private static final String ACCOUNT_NUMBER = "123456789";
	private static final String ACCOUNT_PHONE = "+421";
	
	private BatchTestHelper helper;
	private Map<String, Object> account;

	@Before
	public void setUp() throws Exception {
		helper = new BatchTestHelper(muleContext);
		createAccountInDB();
	}

	@After
	public void tearDown() throws Exception {
		final Map<String, Object> acc = new HashMap<String, Object>();
		acc.put("Name", account.get("Name"));
		deleteAccountFromDB(acc);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMainFlowNew() throws Exception {
		Flow flow = getFlow("mainFlow");
		MuleEvent event = flow.process(getTestEvent("", MessageExchangePattern.REQUEST_RESPONSE));

		helper.awaitJobTermination(120 * 1000, 500);
		helper.assertJobWasSuccessful();

		SubflowInterceptingChainLifecycleWrapper subflow = getSubFlow("selectAccountFromSalesforce");
		subflow.initialise();

		event = subflow.process(getTestEvent(account, MessageExchangePattern.REQUEST_RESPONSE));
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
