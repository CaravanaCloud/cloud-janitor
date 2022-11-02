package cj.aws.attribution;

import cj.aws.AWSWrite;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

@Named("create-trail-table")
@Dependent
public class CreateTrailTable extends AWSWrite {
    /*
    private static final String ATHENA_DEFAULT_DATABASE = "default";
    private static final String ATHENA_OUTPUT_BUCKET = "s3://jufaerma.us-east-1";
    @Inject
    AWSClients aws;

    @Override
    public void runSafe() {
        log().info("Creating CloudTrail Athena Table");
        var storageLocation = "openshift-dev-cloudtrail";
        var athenaTableName = storageLocation + "-tbl";
        var accountId = "269733383066";
        var location = "s3://openshift-dev-cloudtrail/AWSLogs/%s/CloudTrail/".formatted(accountId);
        var athena = aws.newAthenaClient(Region.US_EAST_1);
        var SQL = """
                  CREATE EXTERNAL TABLE `%s`(
                            `eventversion` string COMMENT 'from deserializer', 
                            `useridentity` struct<type:string,principalid:string,arn:string,accountid:string,invokedby:string,accesskeyid:string,username:string,sessioncontext:struct<attributes:struct<mfaauthenticated:string,creationdate:string>,sessionissuer:struct<type:string,principalid:string,arn:string,accountid:string,username:string>>> COMMENT 'from deserializer', 
                            `eventtime` string COMMENT 'from deserializer', 
                            `eventsource` string COMMENT 'from deserializer', 
                            `eventname` string COMMENT 'from deserializer', 
                            `awsregion` string COMMENT 'from deserializer', 
                            `sourceipaddress` string COMMENT 'from deserializer', 
                            `useragent` string COMMENT 'from deserializer', 
                            `errorcode` string COMMENT 'from deserializer', 
                            `errormessage` string COMMENT 'from deserializer', 
                            `requestparameters` string COMMENT 'from deserializer', 
                            `responseelements` string COMMENT 'from deserializer', 
                            `additionaleventdata` string COMMENT 'from deserializer', 
                            `requestid` string COMMENT 'from deserializer', 
                            `eventid` string COMMENT 'from deserializer', 
                            `resources` array<struct<arn:string,accountid:string,type:string>> COMMENT 'from deserializer', 
                            `eventtype` string COMMENT 'from deserializer', 
                            `apiversion` string COMMENT 'from deserializer', 
                            `readonly` string COMMENT 'from deserializer', 
                            `recipientaccountid` string COMMENT 'from deserializer', 
                            `serviceeventdetails` string COMMENT 'from deserializer', 
                            `sharedeventid` string COMMENT 'from deserializer', 
                            `vpcendpointid` string COMMENT 'from deserializer')
                          COMMENT 'CloudTrail table for openshift-dev-cloudtrail bucket'
                          ROW FORMAT SERDE 
                            'com.amazon.emr.hive.serde.CloudTrailSerde' 
                          STORED AS INPUTFORMAT 
                            'com.amazon.emr.cloudtrail.CloudTrailInputFormat' 
                          OUTPUTFORMAT 
                            'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
                          LOCATION
                            '%s'
                          TBLPROPERTIES (
                            'classification'='cloudtrail')        
                """.formatted(
                athenaTableName,
                location
        );
        log().info(SQL);
        String queryExecutionId = submitAthenaQuery(athena, SQL);
        waitForQueryToComplete(athena, queryExecutionId);
        processResultRows(athena, queryExecutionId);
        athena.close();
        log().info("Table creation completed;");
    }
    // Submits a sample query to Amazon Athena and returns the execution ID of the query
    public static String submitAthenaQuery(AthenaClient athenaClient, String SQL) {

        try {

            // The QueryExecutionContext allows us to set the database
            QueryExecutionContext queryExecutionContext = QueryExecutionContext.builder()
                    .database(ATHENA_DEFAULT_DATABASE).build();

            // The result configuration specifies where the results of the query should go
            ResultConfiguration resultConfiguration = ResultConfiguration.builder()
                    .outputLocation(ATHENA_OUTPUT_BUCKET)
                    .build();

            StartQueryExecutionRequest startQueryExecutionRequest = StartQueryExecutionRequest.builder()
                    .queryString(SQL)
                    .queryExecutionContext(queryExecutionContext)
                    .   resultConfiguration(resultConfiguration)
                    .build();

            StartQueryExecutionResponse startQueryExecutionResponse = athenaClient.startQueryExecution(startQueryExecutionRequest);
            return startQueryExecutionResponse.queryExecutionId();

        } catch (AthenaException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return "";
    }

    // Wait for an Amazon Athena query to complete, fail or to be cancelled
    public static void waitForQueryToComplete(AthenaClient athenaClient, String queryExecutionId)  {
        GetQueryExecutionRequest getQueryExecutionRequest = GetQueryExecutionRequest.builder()
                .queryExecutionId(queryExecutionId).build();

        GetQueryExecutionResponse getQueryExecutionResponse;
        boolean isQueryStillRunning = true;
        while (isQueryStillRunning) {
            getQueryExecutionResponse = athenaClient.getQueryExecution(getQueryExecutionRequest);
            String queryState = getQueryExecutionResponse.queryExecution().status().state().toString();
            if (queryState.equals(QueryExecutionState.FAILED.toString())) {
                throw new RuntimeException("The Amazon Athena query failed to run with error message: " + getQueryExecutionResponse
                        .queryExecution().status().stateChangeReason());
            } else if (queryState.equals(QueryExecutionState.CANCELLED.toString())) {
                throw new RuntimeException("The Amazon Athena query was cancelled.");
            } else if (queryState.equals(QueryExecutionState.SUCCEEDED.toString())) {
                isQueryStillRunning = false;
            } else {
                // Sleep an amount of time before retrying again
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("The current status is: " + queryState);
        }
    }

    // This code retrieves the results of a query
    public static void processResultRows(AthenaClient athenaClient, String queryExecutionId) {

        try {

            // Max Results can be set but if its not set,
            // it will choose the maximum page size
            GetQueryResultsRequest getQueryResultsRequest = GetQueryResultsRequest.builder()
                    .queryExecutionId(queryExecutionId)
                    .build();

            GetQueryResultsIterable getQueryResultsResults = athenaClient.getQueryResultsPaginator(getQueryResultsRequest);

            for (GetQueryResultsResponse result : getQueryResultsResults) {
                List<ColumnInfo> columnInfoList = result.resultSet().resultSetMetadata().columnInfo();
                List<Row> results = result.resultSet().rows();
                processRow(results, columnInfoList);
            }

        } catch (AthenaException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void processRow(List<Row> row, List<ColumnInfo> columnInfoList) {
        for (Row myRow : row) {
            List<Datum> allData = myRow.data();
            for (Datum data : allData) {
                System.out.println("The value of the column is "+data.varCharValue());
            }
        }
    }

     */
}
