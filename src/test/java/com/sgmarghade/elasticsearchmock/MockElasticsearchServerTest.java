package com.sgmarghade.elasticsearchmock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by swapnil on 25/01/16.
 */
public class MockElasticsearchServerTest {
    private MockElasticsearchServer server ;
    private final String  TEST_INDEX = "test-index";
    private final String  TEST_TYPE = "document";
    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup(){
        server = new MockElasticsearchServer("data-directory", Lists.newArrayList(TEST_INDEX));

    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void testShouldSaveDocumentForQuery() throws InterruptedException, ExecutionException, TimeoutException, JsonProcessingException {
        Map<String,String> data = new HashMap<String, String>();
        data.put("key1","value1");

        saveDocument(data);
        server.refresh(TEST_INDEX);

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

        Assert.assertEquals(1l,searchResponse.getHits().totalHits());
    }

    @Test
    public void testAdminApi() throws ExecutionException, InterruptedException {
        ClusterHealthResponse clusterIndexHealths = server.getClient().admin().cluster().prepareHealth().execute().get();
        Assert.assertEquals("elasticsearch",clusterIndexHealths.getClusterName());
    }

    @Test
    public void testDeleteIndex() throws InterruptedException, ExecutionException, TimeoutException, JsonProcessingException {
        Map<String,String> data = new HashMap<String, String>();
        data.put("key1","value1");

        saveDocument(data);
        server.refresh(TEST_INDEX);
        server.deleteIndex(TEST_INDEX);
        server.refresh(TEST_INDEX);

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

        Assert.assertEquals(0l,searchResponse.getHits().totalHits());
    }

    private void saveDocument(Map<String, String> data) throws InterruptedException, ExecutionException, TimeoutException, JsonProcessingException {
        server.getClient()
                .prepareIndex()
                .setIndex(TEST_INDEX)
                .setType(TEST_TYPE)
                .setId("1")
                .setSource(mapper.writeValueAsString(data))
                .execute()
                .get(2, TimeUnit.SECONDS);
    }


}
