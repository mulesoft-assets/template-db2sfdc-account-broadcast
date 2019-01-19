
# Anypoint Template: Database to Salesforce Account Broadcast

Broadcasts changes in database accounts to Salesforce in real time. The detection criteria and fields to move are configurable. Additional systems can be easily added to be notified of changes. Real time synchronization is achieved via configurable rapid polling of the database.  You can use the template for free with Mule Runtime Enterprise Edition, CloudHub, or as a trial in Anypoint Studio.

![80f67320-DatabaseToSalesforceAcctBrdct.png](https://exchange2-file-upload-service-kprod.s3.us-east-1.amazonaws.com/80f67320-DatabaseToSalesforceAcctBrdct.png)

![80f67320-DatabaseToSalesforceAcctBrdct.png](https://exchange2-file-upload-service-kprod.s3.us-east-1.amazonaws.com/80f67320-DatabaseToSalesforceAcctBrdct.png)

# License Agreement
This template is subject to the conditions of the 
<a href="https://s3.amazonaws.com/templates-examples/AnypointTemplateLicense.pdf">MuleSoft License Agreement</a>.
Review the terms of the license before downloading and using this template. You can use this template for free 
with the Mule Enterprise Edition, CloudHub, or as a trial in Anypoint Studio.

# Use Case
As a Salesforce administrator I want to synchronize accounts between a database and a Salesforce organization.

This template is a foundation for the process of broadcasting accounts from a database to a Salesfoce instance. Everytime there is new account or a change in an already existing one, the integration polls for changes in the database source instance and is responsible for upserting the account into Salesforce.

Requirements have been set not only to be used as examples, but also to establish a starting point to adapt your integration to your requirements.

As implemented, this template leverages the batch module. The batch job is divided into *Process* and *On Complete* stages. The integration is triggered by a scheduler defined in the flow that triggers the application, queries the newest database updates or creations that match a filter criteria and executes the batch job. During the *Process* stage, each database account isbe filtered depending on if it has an existing matching Account in Salesforce. The last step of the *Process* stage groups the accounts and creates or updates them in Salesforce. Finally during the *On Complete* stage, the template logs output statistics data on the console.

# Considerations

To make this template run, there are certain preconditions that must be considered. All of them deal with the preparations in both the source (database) and destination (Salesforce) systems, that must be made for this template to run smoothly. 
Failing to do so can lead to unexpected behavior of the template.

This template illustrates the broadcast use case between a database and Salesforce, thus it requires a database instance to work.
The template comes packaged with a SQL script to create the database table that the template uses. It is your responsibility to use that script to create the table in an available schema and change the configuration accordingly. The SQL script file can be found in src/main/resources/account.sql.

## Database Considerations

This template uses date time or timestamp fields from the database to do comparisons and take further actions.
While the template handles the time zone by sending all such fields in a neutral time zone, it cannot handle time offsets.
We define time offsets as the time difference that may surface between date time and timestamp fields from different systems due to a differences in the system's internal clock.
Take this in consideration and take the actions needed to avoid the time offset.

### As a Data Source

There are no considerations with using a database as a data origin.


## Salesforce Considerations

- Where can I check that the field configuration for my Salesforce instance is the right one? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=checking_field_accessibility_for_a_particular_field.htm&language=en_US">Salesforce: Checking Field Accessibility for a Particular Field</a>.
- How can I modify the Field Access Settings? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=modifying_field_access_settings.htm&language=en_US">Salesforce: Modifying Field Access Settings</a>.


### As a Data Destination

There are no considerations with using Salesforce as a data destination.

# Run it!

You can run this template on premises or in the cloud.

## Running On Premises
In this section we help you run your template on your computer.


### Where to Download Anypoint Studio and the Mule Runtime
If you are a newcomer to Mule, here is where to get the tools.

- [Download Anypoint Studio](https://www.mulesoft.com/platform/studio)
- [Download Mule runtime](https://www.mulesoft.com/lp/dl/mule-esb-enterprise)


### Importing a Template into Studio
In Studio, click the Exchange X icon in the upper left of the taskbar, log in with your
Anypoint Platform credentials, search for the template, and click **Open**.


### Running on Studio
After you import your template into Anypoint Studio, follow these steps to run it:

1. Locate the properties file `mule.dev.properties`, in src/main/resources.
2. Complete all the properties in the "Properties to Configure" section.
3. Right click the template project folder.
4. Hover your mouse over `Run as`.
5. Click `Mule Application (configure)`.
6. Inside the dialog, select Environment and set the variable `mule.env` to the value `dev`.
7. Click `Run`.


### Run on Mule Standalone
Complete all properties in one of the property files, for example in mule.prod.properties and run your app with the corresponding environment variable. To follow the example, this is `mule.env=prod`. 


## Run on CloudHub
While creating your application on CloudHub (or you can do it later as a next step), go to Runtime Manager > Manage Application > Properties to set the environment variables listed in "Properties to Configure" as well as the **mule.env**.


### Deploy your Anypoint Template on CloudHub
Studio provides an easy way to deploy your template directly to CloudHub, for the specific steps to do so check this


## Properties to Configure
To use this template, configure properties (credentials, configurations, etc.) in the properties file or in CloudHub from Runtime Manager > Manage Application > Properties. The sections that follow list example values.

### Application Configuration

**Batch Aggregator Configuration**
- page.size `200`

**Scheduler Configuration**

- scheduler.frequency `10000`
- scheduler.startDelay `100`

**Watermarking Default Last Query Timestamp**

- watermark.default.expression `2018-12-13T03:00:59Z`

**Database Connector Configuration**

- db.host `localhost`
- db.port `3306`
- db.user `user-name1`
- db.password `user-password1`
- db.databasename `dbname1`

**Note:** If you need to connect to a different database, provide a JAR file for the library and change the value of that field in the connector.

**Salesforce Connector Configuration**

- sfdc.username `joan.baez@orgb`
- sfdc.password `JoanBaez456`
- sfdc.securityToken `ces56arl7apQs56XTddf34X`

# API Calls
Salesforce imposes limits on the number of API calls that can be made. Therefore calculating this amount may be an important factor to consider. The account broadcast template calls to the API can be calculated using the formula:

- ***1 + X + X / ${page.size}*** -- Where ***X*** is the number of accounts to be synchronized on each run. 
- Divide by ***${page.size}***  because by default, accounts are gathered in groups of ${page.size} for each upsert API call in the commit step. Also consider that these calls execute repeatedly every polling cycle.	

For instance if 10 records are fetched from an origin instance, then 12 API calls have to be made (1 + 10 + 1).


# Customize It!

This brief guide intends to give a high level idea of how this template is built and how you can change it according to your needs.
As Mule applications are based on XML files, this page describes the XML files used with this template.

More files are available such as test classes and Mule application files, but to keep it simple, we focus on these XML files:

* config.xml
* businessLogic.xml
* endpoints.xml
* errorHandling.xml

## config.xml
Configuration for connectors and configuration properties are set in this file. Even change the configuration here, all parameters that can be modified are in properties file, which is the recommended place to make your changes. However if you want to do core changes to the logic, you need to modify this file.

In the Studio visual editor, the properties are on the *Global Element* tab.


## businessLogic.xml

The functional aspect of the template is implemented on this XML, directed by a batch job that's responsible for creations and updates. 
The several message processors constitute four high level actions that fully implement the logic of this template:

1. Job execution is invoked from SchedulerFlow (endpoints.xml) every time there is a new query executed asking for created or updated accounts.
2. During the *Process* stage, each database account is filtered depending on if it has an existing matching account in Salesforce.
3. The last step of the *Process* stage groups the accounts and creates or updates them in Salesforce.
4. Finally during the *On Complete* stage, the template logs the output statistics data on the console.

## endpoints.xml

This file is formed by two flows.

The scheduler flow contains the Scheduler endpoint that periodically triggers the watermarking flow and executes the batch job process.

The watermarking flow contains watermarking logic that queries the database for updated or created accounts that meet the defined criteria in the query since the last polling. The last invocation timestamp is stored by using the Object Store component and updates after each database query.

## errorHandling.xml

This is the right place to handle how your integration reacts depending on the different exceptions. 
This file provides error handling that is referenced by the main flow in the business logic.
