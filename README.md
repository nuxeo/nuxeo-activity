# Nuxeo Activity

This addon provides a service to store and retrieve activities done in Nuxeo.


## Building and deploying

### How to build

You can build Nuxeo Activity with:

    $ mvn clean install

### How to deploy

#### Deploy the module

Copy the `nuxeo-activity-*.jar` into your Nuxeo instance in `nxserver/bundles` and restart.

#### Configure the Datasource

Nuxeo Activity relies on a Datasource `nxactivities` which is not defined in a default distribution.
The easiest way to add it is to use the provided `activity` template. You need to copy the `templates/activity` folder into your Nuxeo instance:

    $ cp -r templates/activity $NUXEO_HOME/templates/

Edit the `bin/nuxeo.conf` file to deploy the `activity` template after the default configuration:

    nuxeo.templates=default,activity

Restart the Nuxeo instance.


## About Nuxeo

Nuxeo provides a modular, extensible Java-based [open source software platform for enterprise content management] [1] and packaged applications for [document management] [2], [digital asset management] [3] and [case management] [4]. Designed by developers for developers, the Nuxeo platform offers a modern architecture, a powerful plug-in model and extensive packaging capabilities for building content applications.

[1]: http://www.nuxeo.com/en/products/ep
[2]: http://www.nuxeo.com/en/products/document-management
[3]: http://www.nuxeo.com/en/products/dam
[4]: http://www.nuxeo.com/en/products/case-management

More information on: <http://www.nuxeo.com/>


