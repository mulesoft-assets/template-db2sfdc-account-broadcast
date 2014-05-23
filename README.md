# Anypoint Template: Database to SalesForce Account Broadcast

+ [License Agreement](#licenseagreement)
+ [Use Case](#usecase)
+ [Run it!](#runit)
    * [Running on premise](#runonpremise)
    * [Running on CloudHub](#runoncloudhub)
    * [Properties to be configured](#propertiestobeconfigured)
+ [API Calls](#apicalls)
+ [Customize It!](#customizeit)
    * [config.xml](#configxml)
    * [endpoints.xml](#endpointsxml)
    * [businessLogic.xml](#businesslogicxml)
    * [errorHandling.xml](#errorhandlingxml)


# License Agreement <a name="licenseagreement"/>
Note that using this template is subject to the conditions of this [License Agreement](AnypointTemplateLicense.pdf).
Please review the terms of the license before downloading and using this template. In short, you are allowed to use the template for free with Mule ESB Enterprise Edition, CloudHub, or as a trial in Anypoint Studio.

# Use Case <a name="usecase"/>
As a SalesForce admin I want to synchronize accounts from Database to SalesForce organization.

This Template should serve as a foundation for the process of broadcasting accounts from Database to Salesfoce instance. Everytime there is a new account or a change in an already existing one, the integration will poll for changes in Database source instance and it will be responsible for updating the account in the SalesForce.

Requirements have been set not only to be used as examples, but also to establish a starting point to adapt your integration to your requirements.

As implemented, this Anypoint Template leverage the [Batch Module](http://www.mulesoft.org/documentation/display/current/Batch+Processing). The batch job is divided in Input, Process and On Complete stages. The integration is triggered by a poll defined in the flow that is going to trigger the application, querying newest Database updates/creations matching a filter criteria and executing the batch job. During the Process stage, each Database Account will be filtered depending on, if it has an existing matching user in the SalesForce. The last step of the Process stage will group the accounts and create/update them in SalesForce. Finally during the On Complete stage the Anypoint Template will logoutput statistics data into the console.

# Run it! <a name="runit"/>

Simple steps to get Database to SalesForce Accounts Broadcast running.

**Note:** This particular Anypoint Template illustrate the broadcast use case between Database and a SalesForce, thus it requires a Database instance to work.
The Anypoint Template comes packaged with a SQL script to create the Database table that uses. It is the user responsability to use that script to create the table in an available schema and change the configuration accordingly. The SQL script file can be found in [src/main/resources/account.sql] (../master/src/main/resources/account.sql)


## Running on premise <a name="runonpremise"/>

In this section we detail the way you have to run you Anypoint Temple on you computer.


### Running on Studio <a name="runonstudio"/>
Once you have imported your Anypoint Template into Anypoint Studio you need to follow these steps to run it:

+ Locate the properties file `mule.dev.properties`, in src/main/resources
+ Complete all the properties required as per the examples in the section [Properties to be configured](#propertiestobeconfigured)
+ Once that is done, right click on you Anypoint Template project folder 
+ Hover you mouse over `"Run as"`
+ Click on  `"Mule Application"`


### Running on Mule ESB stand alone  <a name="runonmuleesbstandalone"/>
Complete all properties in one of the property files, for example in [mule.prod.properties] (../blob/master/src/main/resources/mule.prod.properties) and run your app with the corresponding environment variable to use it. To follow the example, this will be `mule.env=prod`.

Once your app is all set and started, there is no need to do anything else. The application will poll SalesForce to know if there are any newly created or updated objects and synchronice them.


## Running on CloudHub <a name="runoncloudhub"/>

While [creating your application on CloudHub](http://www.mulesoft.org/documentation/display/current/Hello+World+on+CloudHub) (Or you can do it later as a next step), you need to go to Deployment > Advanced to set all environment variables detailed in **Properties to be configured** as well as the **mule.env**. 

### Deploying your Anypoint Template on CloudHub <a name="deployingyouranypointtemplateoncloudhub"/>
Mule Studio provides you with really easy way to deploy your Template directly to CloudHub, for the specific steps to do so please check this [link](http://www.mulesoft.org/documentation/display/current/Deploying+Mule+Applications#DeployingMuleApplications-DeploytoCloudHub)


## Properties to be configured (With examples)<a name="propertiestobeconfigured"/>

In order to use this Template you need to configure properties (Credentials, configurations, etc.) either in properties file or in CloudHub as Environment Variables. Detail list with examples:

### Application configuration
+ poll.frequencyMillis `60000`
+ poll.startDelayMillis `0`
+ watermark.defaultExpression `YESTERDAY`

### Database Connector configuration
+ database.url=jdbc:mysql://192.168.224.130:3306/mule?user=mule&password=mule

If it is required to connect to a different Database there should be provided the jar for the library and changed the value of that field in the connector.

### SalesForce Connector configuration
+ sfdc.a.username `joan.baez@orgb`
+ sfdc.a.password `JoanBaez456`
+ sfdc.a.securityToken `ces56arl7apQs56XTddf34X`
+ sfdc.a.url `https://login.salesforce.com/services/Soap/u/26.0`


# API Calls <a name="apicalls"/>

SalesForce imposes limits on the number of API Calls that can be made. Therefore calculating this amount may be an important factor to consider. Account Migration Template calls to the API can be calculated using the formula:

***X + ceil(X / 200)***

Being ***X*** the number of Accounts to be synchronized on each run. 

The division by ***200*** is because, by default, Users are gathered in groups of 200 for each Upsert API Call in the commit step. 

For instance if 10 records are fetched from origin instance, then 11 API calls will be made (10 + 1).


# Customize It!<a name="customizeit"/>

This brief guide intends to give a high level idea of how this Template is built and how you can change it according to your needs.
As mule applications are based on XML files, this page will be organised by describing all the XML that conform the Template.
Of course more files will be found such as Test Classes and [Mule Application Files](http://www.mulesoft.org/documentation/display/current/Application+Format), but to keep it simple we will focus on the XMLs.

Here is a list of the main XML files you'll find in this application:

* [config.xml](#configxml)
* [endpoints.xml](#endpointsxml)
* [businessLogic.xml](#businesslogicxml)
* [errorHandling.xml](#errorhandlingxml)


## config.xml<a name="configxml"/>
Configuration for Connectors and [Properties Place Holders](http://www.mulesoft.org/documentation/display/current/Configuring+Properties) are set in this file. **Even you can change the configuration here, all parameters that can be modified here are in properties file, and this is the recommended place to do it so.** Of course if you want to do core changes to the logic you will probably need to modify this file.

In the visual editor they can be found on the *Global Element* tab.

## endpoints.xml<a name="endpointsxml"/>
This is the file where you will found the inbound and outbound sides of your integration app.
This Template has only an [HTTP Inbound Endpoint](http://www.mulesoft.org/documentation/display/current/HTTP+Endpoint+Reference) as the way to trigger the use case.

## businessLogic.xml<a name="businesslogicxml"/>
Functional aspect of the Template is implemented on this XML, directed by one flow responsible of excecuting the logic.
For the pourpose of this particular Template the *mainFlow* just excecutes a [Batch Job](http://www.mulesoft.org/documentation/display/current/Batch+Processing). which handles all the logic of it.
This flow has Exception Strategy that basically consists on invoking the *defaultChoiseExceptionStrategy* defined in *errorHandling.xml* file.


## errorHandling.xml<a name="errorhandlingxml"/>
Contains a [Catch Exception Strategy](http://www.mulesoft.org/documentation/display/current/Catch+Exception+Strategy) that is only Logging the exception thrown (If so). As you imagine, this is the right place to handle how your integration will react depending on the different exceptions. 

# Testing the Template <a name="testingthetemplate"/>

You will notice that the Template has been shipped with test.

**Consideration:** This template has only Integration Tests with a particular aspect compared to other templates.
You can run any of them by just doing right click on the class and clicking on run as Junit test.

Do bear in mind that you'll have to tell the test classes which property file to use.
For you convinience you can add a file mule.test.properties and locate it in "src/test/resources".
In the run configurations of the test just make sure to add the following property:

+ -Dmule.env=test
