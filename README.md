# Automating Real-Time Cryptocurrency Data Pipeline with Scala-Spark!
-------------------------------------------------------------

## Imports These lines import necessary classes and libraries
  #### SparkSession and DataFrame from `org.apache.spark.sql` These are essential for working with Spark SQL.
  #### functions from `org.apache.spark.sql` Imports functions provided by Spark SQL for data manipulation.
  #### scalaj.http._ Imports classes from the Scala HTTP library `scalaj.http`, used for making HTTP requests.

```scala
import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._
import scalaj.http._
```


```scala
object CryptoDataPipeline {
  def main(args: Array[String]): Unit = {
    println("Hello!")
```

- **Object and Main Method**: Defines a Scala object `CryptoDataPipeline` with a `main` method, which is the entry point for execution. Prints "Hello!" to indicate the start of execution.

```scala
    // Initialize Spark session
    val spark = SparkSession.builder()
      .appName("CryptoDataPipeline")
      .master("local[*]")
      .getOrCreate()
```

- **SparkSession Initialization**: Initializes a SparkSession named "CryptoDataPipeline" running locally (`master("local[*]")`). This is the entry point for interacting with Spark functionalities.

```scala
    import spark.implicits._
```

- **Implicits Import**: Imports Spark implicits to enable convenient conversion of standard Scala objects into `Dataset` and `DataFrame`.

```scala
    // API URL
    val url = "https://api.coincap.io/v2/assets"
```

- **API URL**: Defines the URL of the API endpoint (`https://api.coincap.io/v2/assets`) from which cryptocurrency data will be fetched.

```scala
    // Fetch data from API
    val response = Http(url)
      .header("Content-Type", "application/json")
      .header("Accept-Encoding", "deflate")
      .asString
```

- **API Request**: Uses the `scalaj.http` library to make an HTTP GET request (`Http(url)`). Headers `"Content-Type"` and `"Accept-Encoding"` are set to indicate that JSON data is expected.

```scala
    // Parse JSON response
    val jsonString = response.body
    val jsonDF = spark.read.json(Seq(jsonString).toDS())
```

- **JSON Parsing**: Reads the JSON response (`response.body`) into a String (`jsonString`). Converts `jsonString` into a Dataset (`Seq(jsonString).toDS()`) and then reads it into a DataFrame (`spark.read.json(...)`), resulting in `jsonDF`.

```scala
    // Normalize JSON to DataFrame
    val cryptoDF = jsonDF.select(explode(col("data")).as("data"))
      .select("data.id", "data.rank", "data.symbol", "data.name", "data.supply", "data.maxSupply",
        "data.marketCapUsd", "data.volumeUsd24Hr", "data.priceUsd", "data.changePercent24Hr", "data.vwap24Hr")
```

- **Data Transformation**: Uses Spark SQL functions (`explode`, `col`) to transform `jsonDF`. It explodes the `data` column (which contains arrays of JSON objects) into multiple rows, and then selects specific fields (`id`, `rank`, etc.) to form a new DataFrame `cryptoDF`.

```scala
    // Print schema and show a few rows of the DataFrame
    cryptoDF.printSchema()
    cryptoDF.show(5, truncate = false)
```

- **Schema and Data Display**: Prints the schema of `cryptoDF` (column names and data types) and shows the first 5 rows of `cryptoDF` without truncating column values.

```scala
    // Define JDBC URL and properties
    val jdbcUrl = "jdbc:sqlserver://your_serverName;databaseName=Your_databaseName"
    val connectionProperties = new java.util.Properties()
    connectionProperties.put("user", "your_username")
    connectionProperties.put("password", "your_password")
    connectionProperties.put("driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver")
```

- **JDBC Connection Properties**: Defines the JDBC URL (`jdbcUrl`) for connecting to a SQL Server database (`jdbc:sqlserver://your_serverName;databaseName=Your_databaseName`). Sets up connection properties (`user`, `password`, `driver`) required for authentication and database driver.

```scala
    // Write DataFrame to SQL Server
    cryptoDF.write
      .mode("append")
      .jdbc(jdbcUrl, "FactCryptos", connectionProperties)
```

- **Data Writing**: Writes the `cryptoDF` DataFrame to a SQL Server table named `FactCryptos` using JDBC. The `mode("append")` specifies that new data should be appended to the existing table.

```scala
    // Stop Spark session
    spark.stop()
  }
}
```

- **SparkSession Shutdown**: Stops the SparkSession to release resources after the data processing and writing operations are completed.
