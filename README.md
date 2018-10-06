
# Anypoint Template: Database to Salesforce Account Broadcast

+ [License Agreement](#licenseagreement)
+ [Use Case](#usecase)
+ [Considerations](#considerations)
	* [DB Considerations](#dbconsiderations)
	* [Salesforce Considerations](#salesforceconsiderations)
+ [Run it!](#runit)
	* [Running on premise](#runonopremise)
	* [Running on Studio](#runonstudio)
	* [Running on Mule ESB stand alone](#runonmuleesbstandalone)
	* [Running on CloudHub](#runoncloudhub)
	* [Deploying your Anypoint Template on CloudHub](#deployingyouranypointtemplateoncloudhub)
	* [Properties to be configured (With examples)](#propertiestobeconfigured)
+ [API Calls](#apicalls)
+ [Customize It!](#customizeit)
	* [config.xml](#configxml)
	* [businessLogic.xml](#businesslogicxml)
	* [endpoints.xml](#endpointsxml)
	* [errorHandling.xml](#errorhandlingxml)


# License Agreement <a name="licenseagreement"/>
Note that using this template is subject to the conditions of this [License Agreement](AnypointTemplateLicense.pdf).
Please review the terms of the license before downloading and using this template. In short, you are allowed to use the template for free with Mule ESB Enterprise Edition, CloudHub, or as a trial in Anypoint Studio.

# Use Case <a name="usecase"/>
As a Salesforce administrator I want to synchronize accounts between a database and a Salesforce organization.

This template is a foundation for the process of broadcasting accounts from a database to a Salesfoce instance. Everytime there is new account or a change in an already existing one, the integration polls for changes in the database source instance and is responsible for upserting the account into Salesforce.

Requirements have been set not only to be used as examples, but also to establish a starting point to adapt your integration to your requirements.

As implemented, this template leverages the batch module. The batch job is divided into *Process* and *On Complete* stages. The integration is triggered by a scheduler defined in the flow that triggers the application, queries the newest database updates or creations that match a filter criteria and executes the batch job. During the *Process* stage, each database account isbe filtered depending on if it has an existing matching Account in Salesforce. The last step of the *Process* stage groups the accounts and creates or updates them in Salesforce. Finally during the *On Complete* stage, the template logs output statistics data on the console.

# Considerations <a name="considerations"/>

To make this template run, there are certain preconditions that must be considered. All of them deal with the preparations in both the source (database) and destination (Salesforce) systems, that must be made in order for all to run smoothly. 
Failing to do so could lead to unexpected behavior of the template.

This template illustrate the broadcast use case between a database and Salesforce, thus it requires a database instance to work.
The template comes packaged with a SQL script to create the database table that the template uses. It is your responsibility to use that script to create the table in an available schema and change the configuration accordingly. The SQL script file can be found in src/main/resources/account.sql.

## DB Considerations <a name="dbconsiderations"/>

There may be a few things that you need to know regarding DB, in order for this template to work.

This Anypoint Template may be using date time/timestamp fields from the DB in order to do comparisons and take further actions.
While the template handles the time zone by sending all such fields in a neutral time zone, it can not handle **time offsets**.
We define as **time offsets** the time difference that may surface between date time/timestamp fields from different systems due to a differences in the system's internal clock.
The user of this template should take this in consideration and take the actions needed to avoid the time offset.

### As source of data

There are no particular considerations for this Anypoint Template regarding DB as data origin.


## Salesforce Considerations <a name="salesforceconsiderations"/>

There may be a few things that you need to know regarding Salesforce, in order for this template to work.

In order to have this template working as expected, you should be aware of your own Salesforce field configuration.

### FAQ

 - Where can I check that the field configuration for my Salesforce instance is the right one?

    [Salesforce: Checking Field Accessibility for a Particular Field][1]

- Can I modify the Field Access Settings? How?

    [Salesforce: Modifying Field Access Settings][2]


[1]: https://help.salesforce.com/HTViewHelpDoc?id=checking_field_accessibility_for_a_particular_field.htm&language=en_US
[2]: https://help.salesforce.com/HTViewHelpDoc?id=modifying_field_access_settings.htm&language=en_US


### As destination of data

There are no particular considerations for this Anypoint Template regarding Salesforce as data destination.









# Run it! <a name="runit"/>
Simple steps to get Database to Salesforce Account Broadcast running.


## Running on premise <a name="runonopremise"/>
In this section we detail the way you should run your Anypoint Template on your computer.


### Where to Download Mule Studio and Mule ESB
First thing to know if you are a newcomer to Mule is where to get the tools.

+ You can download Mule Studio from this [Location](http://www.mulesoft.com/platform/mule-studio)
+ You can download Mule ESB from this [Location](http://www.mulesoft.com/platform/soa/mule-esb-open-source-esb)


### Importing an Anypoint Template into Studio
Mule Studio offers several ways to import a project into the workspace, for instance: 

+ Anypoint Studio Project from File System
+ Packaged mule application (.jar)

You can find a detailed description on how to do so in this [Documentation Page](http://www.mulesoft.org/documentation/display/current/Importing+and+Exporting+in+Studio).


### Running on Studio <a name="runonstudio"/>
Once you have imported you Anypoint Template into Anypoint Studio you need to follow these steps to run it:

+ Locate the properties file `mule.dev.properties`, in src/main/resources
+ Complete all the properties required as per the examples in the section [Properties to be configured](#propertiestobeconfigured)
+ Once that is done, right click on you Anypoint Template project folder 
+ Hover you mouse over `"Run as"`
+ Click on  `"Mule Application (configure)"`
+ Inside the dialog, select Environment and set the variable `"mule.env"` to the value `"dev"`
+ Click `"Run"`


### Running on Mule ESB stand alone <a name="runonmuleesbstandalone"/>
Complete all properties in one of the property files, for example in [mule.prod.properties] (../master/src/main/resources/mule.prod.properties) and run your app with the corresponding environment variable to use it. To follow the example, this will be `mule.env=prod`. 


## Running on CloudHub <a name="runoncloudhub"/>
While [creating your application on CloudHub](http://www.mulesoft.org/documentation/display/current/Hello+World+on+CloudHub) (Or you can do it later as a next step), you need to go to Deployment > Advanced to set all environment variables detailed in **Properties to be configured** as well as the **mule.env**.


### Deploying your Anypoint Template on CloudHub <a name="deployingyouranypointtemplateoncloudhub"/>
Mule Studio provides you with really easy way to deploy your Template directly to CloudHub, for the specific steps to do so please check this [link](http://www.mulesoft.org/documentation/display/current/Deploying+Mule+Applications#DeployingMuleApplications-DeploytoCloudHub)


## Properties to be configured (With examples) <a name="propertiestobeconfigured"/>
In order to use this Mule Anypoint Template you need to configure properties (Credentials, configurations, etc.) either in properties file or in CloudHub as Environment Variables. Detail list with examples:
### Application configuration
**Batch Aggregator Configuration**
+ page.size `200`

**Scheduler Configuration**
+ scheduler.frequency `10000`
+ scheduler.startDelay `100`

**Watermarking Default Last Query Timestamp**
+ watermark.default.expression `2018-12-13T03:00:59Z`

**Database Connector Configuration**
+ db.host `localhost`
+ db.port `3306`
+ db.user `user-name1`
+ db.password `user-password1`
+ db.databasename `dbname1`

**Note:** If you need to connect to a different database, provide a JAR file for the library and change the value of that field in the connector.

**Salesforce Connector Configuration**
+ sfdc.username `joan.baez@orgb`
+ sfdc.password `JoanBaez456`
+ sfdc.securityToken `ces56arl7apQs56XTddf34X`

# API Calls <a name="apicalls"/>
Salesforce imposes limits on the number of API calls that can be made. Therefore calculating this amount may be an important factor to consider. The account broadcast template calls to the API can be calculated using the formula:

***1 + X + X / ${page.size}***

***X*** is the number of accounts to be synchronized on each run. 

Dividing by ***${page.size}*** is because, by default, accounts are gathered in groups of ${page.size} for each upsert API call in the commit step. Also consider that these calls execute repeatedly every polling cycle.	

For instance if 10 records are fetched from an origin instance, then 12 API calls have to be made (1 + 10 + 1).


# Customize It!<a name="customizeit"/>
This brief guide intends to give a high level idea of how this Anypoint Template is built and how you can change it according to your needs.
As mule applications are based on XML files, this page will be organized by describing all the XML that conform the Anypoint Template.
Of course more files will be found such as Test Classes and [Mule Application Files](http://www.mulesoft.org/documentation/display/current/Application+Format), but to keep it simple we will focus on the XMLs.

Here is a list of the main XML files you'll find in this application:

* [config.xml](#configxml)
* [endpoints.xml](#endpointsxml)
* [businessLogic.xml](#businesslogicxml)
* [errorHandling.xml](#errorhandlingxml)


## config.xml<a name="configxml"/>
Configuration for Connectors and [Configuration Properties](http://www.mulesoft.org/documentation/display/current/Configuring+Properties) are set in this file. **Even you can change the configuration here, all parameters that can be modified here are in properties file, and this is the recommended place to do it so.** Of course if you want to do core changes to the logic you will probably need to modify this file.

In the visual editor they can be found on the *Global Element* tab.


## businessLogic.xml<a name="businesslogicxml"/>
The functional aspect of the template is implemented on this XML, directed by a batch job that's responsible for creations and updates. 
The several message processors constitute four high level actions that fully implement the logic of this template:

1. Job execution is invoked from SchedulerFlow (endpoints.xml) every time there is a new query executed asking for created or updated accounts.
2. During the *Process* stage, each database account is filtered depending on if it has an existing matching account in Salesforce.
3. The last step of the *Process* stage groups the accounts and creates or updates them in Salesforce.
4. Finally during the *On Complete* stage, the template logs the output statistics data on the console.



## endpoints.xml<a name="endpointsxml"/>
This file is formed by two flows.

The scheduler flow contains the Scheduler endpoint that periodically triggers the watermarking flow and executes the batch job process.

The watermarking flow contains watermarking logic that queries the database for updated or created accounts that meet the defined criteria in the query since the last polling. The last invocation timestamp is stored by using the Object Store component and updates after each database query.



## errorHandling.xml<a name="errorhandlingxml"/>
This is the right place to handle how your integration will react depending on the different exceptions. 
This file holds a [Error Handling](http://www.mulesoft.org/documentation/display/current/Error+Handling) that is referenced by the main flow in the business logic.



