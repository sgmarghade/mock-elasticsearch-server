Elasticsearch mock will help you to embed inmemory single node elasticsearch in test cases to mock actual
elasticsearch calls and still run end to end application use cases .

#Usage
```
MockElasticsearchServer server = new MockElasticsearchServer("TmpDataDirectoryPath",<List of indices to be deleted on shutdown>)

// Saving data

 server.getClient()
                .prepareIndex()
                .setIndex(TEST_INDEX)
                .setType(TEST_TYPE)
                .setId("1")
                .setSource(mapper.writeValueAsString(data))
                .execute()
                .get(2, TimeUnit.SECONDS);

server.refresh(TEST_INDEX);

// Getting data
SearchResponse searchResponse = server.getClient()
                .prepareSearch(TEST_INDEX)
                .setTypes(TEST_TYPE)
                .setQuery(
                        QueryBuilders.constantScoreQuery(
                                FilterBuilders.boolFilter()
                                        .must(FilterBuilders.termFilter("key1", "value1"))))
                .setNoFields()
                .setSize(1)
                .execute()
                .actionGet();


// Deleting index
server.deleteIndex(TEST_INDEX);
server.refresh(TEST_INDEX);


//Shutdown server
server.shutdown();

```

