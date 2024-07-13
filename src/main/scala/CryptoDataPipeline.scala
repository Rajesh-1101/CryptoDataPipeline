import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._
import scalaj.http._

object CryptoDataPipeline {
  def main(args: Array[String]): Unit = {
    println("Hello!")

    // Initialize Spark session
    val spark = SparkSession.builder()
      .appName("CryptoDataPipeline")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // API URL
    val url = "https://api.coincap.io/v2/assets"

    // Fetch data from API
    val response = Http(url)
      .header("Content-Type", "application/json")
      .header("Accept-Encoding", "deflate")
      .asString

    // Parse JSON response
    val jsonString = response.body
    val jsonDF = spark.read.json(Seq(jsonString).toDS())

    // Normalize JSON to DataFrame
    val cryptoDF = jsonDF.select(explode(col("data")).as("data"))
      .select("data.id", "data.rank", "data.symbol", "data.name", "data.supply", "data.maxSupply",
        "data.marketCapUsd", "data.volumeUsd24Hr", "data.priceUsd", "data.changePercent24Hr", "data.vwap24Hr")

    // Print schema and show a few rows of the DataFrame
    cryptoDF.printSchema()
    cryptoDF.show(5, truncate = false)

    // Define JDBC URL and properties
    val jdbcUrl = "jdbc:sqlserver://your_serverName;databaseName=Your_databaseName"
    val connectionProperties = new java.util.Properties()
    connectionProperties.put("user", "your_username")
    connectionProperties.put("password", "your_password")
    connectionProperties.put("driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver")

    // Write DataFrame to SQL Server
    cryptoDF.write
      .mode("append")
      .jdbc(jdbcUrl, "FactCryptos", connectionProperties)

    // Stop Spark session
    spark.stop()
  }
}