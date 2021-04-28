[![New Relic Experimental header](https://github.com/newrelic/opensource-website/raw/master/src/images/categories/Experimental.png)](https://opensource.newrelic.com/oss-category/#new-relic-experimental)

# New Relic Java Instrumentation for JDBC executeBatch

Provides instrumentation for tracking the executeBatch method of the Statement, PreparedStatement and CallableStatement interfaces as a database call.

## Installation
Due to API calls made by the instrumentation, use of this extension requires New Relic Java Agent version 6.1.0 or later.

To install:   
1. Download the latest release jar files.    
2. In the New Relic Java directory (the one containing newrelic.jar), create a directory named extensions if it does not already exist.   
3. Copy the downloaded jars into the extensions directory.   
4. Restart the application.   
## Getting Started

Once deployed the instrumentation will start to track the call to the executeBatch method as a database call.  It will show information related to the call in distributed traces.   
   
In particular, in the Performance tab for the call it will show the query as "Batch Execute n Queries" where n is the number of queries executed as part of the batch.   For calls to addBatch(String sql), it will represent the number of calls to this method before the executeBatch method is executed.  For PreparedStatment and CallableStatment it will represent the number of times that addBatch() is called.
   
![image](https://user-images.githubusercontent.com/8822859/116466509-2eb03e00-a834-11eb-963c-14c511957517.png)

In the Attributes tab, it will show the obfuscated queries that were added to the batch.   For the Statment interface, it will show each query added by a call to addBatch(String sql).   For PreparedStatement and CallableStatement it will show the query used to construct the instance.
   
![image](https://user-images.githubusercontent.com/8822859/116467343-302e3600-a835-11eb-8912-9fbcc83d08c7.png)


## Building

To build the extension jars from source, follow these steps:
### Build single extension
To build a single extension with name *extension*, do the following:
1. Set an environment variable *NEW_RELIC_EXTENSIONS_DIR* and set its value to the directory where you want the jar file built.
2. Run the command: gradlew *extension*:clean *extension*:install
### Build all extensions
To build all extensions, do the following:
1. Set an environment variable *NEW_RELIC_EXTENSIONS_DIR* and set its value to the directory where you want the jar file built.
2. Run the command: gradlew clean install

## Support

New Relic has open-sourced this project. This project is provided AS-IS WITHOUT WARRANTY OR DEDICATED SUPPORT. Issues and contributions should be reported to the project here on GitHub.

We encourage you to bring your experiences and questions to the [Explorers Hub](https://discuss.newrelic.com) where our community members collaborate on solutions and new ideas.

## Contributing

We encourage your contributions to improve [Project Name]! Keep in mind when you submit your pull request, you'll need to sign the CLA via the click-through using CLA-Assistant. You only have to sign the CLA one time per project. If you have any questions, or to execute our corporate CLA, required if your contribution is on behalf of a company, please drop us an email at opensource@newrelic.com.

**A note about vulnerabilities**

As noted in our [security policy](../../security/policy), New Relic is committed to the privacy and security of our customers and their data. We believe that providing coordinated disclosure by security researchers and engaging with the security community are important means to achieve our security goals.

If you believe you have found a security vulnerability in this project or any of New Relic's products or websites, we welcome and greatly appreciate you reporting it to New Relic through [HackerOne](https://hackerone.com/newrelic).

## License

[Project Name] is licensed under the [Apache 2.0](http://apache.org/licenses/LICENSE-2.0.txt) License.

>[If applicable: [Project Name] also uses source code from third-party libraries. You can find full details on which libraries are used and the terms under which they are licensed in the third-party notices document.]
