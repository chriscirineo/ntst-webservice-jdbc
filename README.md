# Netsmart Web Service JDBC Driver (SBC SOAP)

This project provides a custom JDBC pass-through driver that translates standard SQL queries into SOAP requests for a Netsmart Web Service endpoint. It allows SQL clients (like DBeaver) to query the SOAP service as if it were a standard relational database.

## Requirements
- **Java Development Kit (JDK) 21** or higher.
- An active network connection to access the target Netsmart Web Service.

## Building the Project
This project uses Gradle. A Gradle wrapper is included, so you do not need to install Gradle manually. 
To build the fat JAR (which includes all required dependencies like XML parsers):

1. Open a terminal or command prompt at the root of the project directory.
2. Run the `shadowJar` task to package the driver and its dependencies:
   - **Windows:** `gradlew.bat shadowJar`
   - **Mac/Linux:** `./gradlew shadowJar`
3. The compiled driver will be generated in the `build/libs/` directory. Look for the file named `ntst-webservice-jdbc-1.0.jar`.

## Using with DBeaver
Once you have built the JAR file, you can add it to DBeaver to start querying your webservice securely.

### Step 1: Add the Driver to DBeaver
1. Open DBeaver.
2. In the top menu bar, go to **Database** -> **Driver Manager**.
3. Click the **New** button to create a new driver profile.
4. Fill out the **Settings** tab with the following:
   - **Driver Name:** Netsmart SOAP JDBC *(or any name you prefer)*
   - **Driver Type:** Generic
   - **Class Name:** `com.ntst.jdbc.SoapDriver`
   - **URL Template:** `jdbc:soap-ws://{host}/{database}?env={server}` *(Optional, but helps with the UI)*
5. Switch to the **Libraries** tab.
6. Click **Add File** and navigate to your compiled jar (`build/libs/ntst-webservice-jdbc-1.0.jar`). Select it.
7. Click **OK** to save the new driver profile.

### Step 2: Configure a New Connection
1. In DBeaver, go to **Database** -> **New Database Connection**.
2. Search for **Netsmart SOAP JDBC** (the name you assigned in Step 1) and select it. Click **Next**.
3. On the connection settings screen, locate the **JDBC URL** field. 
4. **Enter your Web Service URL and Environment Parameter:**
   
   The URL uses a custom protocol format. You must omit `https://` because the driver injects it automatically. Append your environment code (like `UAT`, `SBOX`, or `PRD`) using the `env` query parameter.

   **Format:**
   ```text
   jdbc:soap-ws://<host_name>/<endpoint_path>?env=<environment_code>
   ```
   
   **Example:**
   ```text
   jdbc:soap-ws://{tenant}nx.netsmartcloud.com/csp/{tenant}/{database}/WEBSVC.Query.cls?env=UAT
   ```
   *(Note: If the `env` parameter is omitted, the driver will fallback to `BLD` by default.)*

5. **Enter your Credentials:**
   - **Username:** Enter your designated webservice username.
   - **Password:** Enter your webservice password.
   *(These standard JDBC properties are extracted and injected into the SOAP `<wsse:UsernameToken>` security header).*

6. **Test the Connection:** 
   Click the **Test Connection ...** button at the bottom left to verify your configuration. If successful, DBeaver will pop up a window indicating that it connected to the "Netsmart Web Service Driver".
7. Click **Finish**.

## Querying
Once connected, you can open a new SQL editor in DBeaver and run queries just like you would against a normal SQL database:

```sql
SELECT * FROM TABLE_NAME
```

**How it works under the hood:**
1. The driver captures your query.
2. It attempts to parse the "table name" from the SQL string.
3. It embeds your query, credentials, and environment code into a `<soap:Envelope>`.
4. It makes a secure HTTP POST request to the endpoint.
5. The response XML is parsed, columns are mapped dynamically, and the data is presented neatly in DBeaver's result grid.