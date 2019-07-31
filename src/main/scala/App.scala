import com.datastax.bdp.graph.spark.graphframe._
import org.apache.spark.sql.SparkSession



object App {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder
      .appName("Graph Load Application")
      .enableHiveSupport()
      .getOrCreate()

    // if you're running from dse spark-shell, START HERE:

    import org.apache.spark.sql.functions._
    import org.apache.spark.sql.types._

    val ignoreNulls = Map("spark.cassandra.output.ignoreNulls" -> "false")
    spark.setCassandraConf(ignoreNulls)

    val graphName = "northwind"
    val g = spark.dseGraph(graphName)

    // Create Schemas for DataSets
    // We prefer this method when loading data.
    def customerSchema(): StructType = {
      StructType(Array(
        StructField("customerId", StringType, false),
        StructField("companyName", StringType, true),
        StructField("contactName", StringType, true),
        StructField("contactTitle", StringType, true),
        StructField("address", StringType, true),
        StructField("city", StringType, true),
        StructField("region", StringType, true),
        StructField("postalCode", StringType, true),
        StructField("country", StringType, true),
        StructField("phone", StringType, true),
        StructField("fax", StringType, true)))
    }

    // read in the CSV file from DSEFS
    val northwindDF = spark.read.format("csv") // format of our Northwind dataset is CSV. Many other options exist such as: parquet, orc, json, etc.
      .option("header", "true") // make sure we don't read in the headers as data
      .schema(customerSchema) // use the explicitly defined schema above to read in the CSV
      // .option("inferSchema", "true") --> Use this if you do NOT want to define schema explicitly and comment out .schema() above
      .load("dsefs:///northwind.csv") // the file path for our dataset within DSEFS.
      .withColumnRenamed("customerId", "customer_id")
      .withColumnRenamed("companyName", "company_name")
      .withColumnRenamed("contactName", "contact_name")
      .withColumnRenamed("postalCode", "postal_code")
      .withColumnRenamed("phone", "number")

    // run in shell to see column names:
    // northwindDF.printSchema

    // VERTICES
    val customerVertex = northwindDF.select(
      col("customer_id"),
      col("company_name"),
      col("contact_name"),
      col("contactTitle"),
      lit("customer") as "~label")

    val phoneVertex = northwindDF.select(
      col("number"),
      lit("contact_number") as "~label")

    // Write the vertex dataframes to the graph
    println("\nWriting customer vertices to the graph...")
    g.updateVertices(customerVertex, Seq("customer"), cache = true)

    println("\nWriting phone vertices to the graph...")
    g.updateVertices(phoneVertex, Seq("contact_number"), cache = false)

    // EDGES

    // Our edge connections already exist in our northwindDF
    // We just need to select the IDs for customers and phone
    val customerToPhoneDF = northwindDF.select(
      col("customer_id"),
      col("company_name"),
      lit("customer") as "srcLabel",
      col("number"),
      lit("contact_number") as "dstLabel",
      lit("contact_at") as "edgeLabel")

    // Now we can use the idColumn() function to get the ids in the proper edge format
    // the idColumn function takes the label and the keys to create the database id
    val customerToPhoneEdge = customerToPhoneDF.select(
      g.idColumn(
        col("company_name"),
        col("srcLabel"),
        col("customer_id")
      ) as "src",
      g.idColumn(
        col("dstLabel"),
        col("contact_number")
      ) as "dst",
      col("edgeLabel") as "~label")

    // Write the edge dataframe to the graph
    println("\nWriting customer to phone edges to the graph...")
    g.updateEdges(customerToPhoneEdge, cache = false)

    // run the following from the shell to see the vertices and edges added
    // g.V().hasLabel("customer").show(false)
    // g.V().hasLabel("phone").show(false)
    // g.E().show(false)
  }
}
