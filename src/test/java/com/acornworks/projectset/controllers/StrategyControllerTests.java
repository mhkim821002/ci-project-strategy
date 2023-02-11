package com.acornworks.projectset.controllers;

import static org.mockito.ArgumentMatchers.any;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import com.acornworks.projectset.common.Payload;
import com.acornworks.projectset.domains.StockPrice;
import com.acornworks.projectset.domains.TradingResult;
import com.acornworks.projectset.processors.StrategyProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = StrategyController.class)
public class StrategyControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StrategyProcessor strategyProcessor;

    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testGetAnalysis() throws Exception {
        List<StockPrice> mockPrices = Payload.readHistoricalDataPayload("AAPL", "payloads/AAPL.csv");
        StockPrice[] stockPriceArr = new StockPrice[mockPrices.size()];
        stockPriceArr = mockPrices.toArray(stockPriceArr);

        Mockito.when(restTemplate.getForEntity(ArgumentMatchers.anyString(), Mockito.eq(StockPrice[].class)))
            .thenReturn(new ResponseEntity<StockPrice[]>(stockPriceArr, HttpStatus.OK));
        
        ReflectionTestUtils.setField(strategyProcessor, "priceEndpointUrl", "https://localhost:65020");
        ReflectionTestUtils.setField(strategyProcessor, "logger", LoggerFactory.getLogger(strategyProcessor.getClass()));
        ReflectionTestUtils.setField(strategyProcessor, "restTemplate", restTemplate);

        Mockito.when(strategyProcessor.getAnalysisResult(any(), any())).thenCallRealMethod();
        Mockito.when(strategyProcessor.getBarSeriesFromStockData(any())).thenCallRealMethod();
        
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/strategy/AAPL/ADX")
                .accept(MediaType.APPLICATION_JSON);
        
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        Assertions.assertEquals(200, result.getResponse().getStatus());

        final TradingResult expectedResult = objectMapper.readValue(
            Payload.readPayloadString("payloads/adx-strategy.json"), TradingResult.class);
        
        final TradingResult actualResult = objectMapper.readValue(
            result.getResponse().getContentAsString(), TradingResult.class);

        Assertions.assertEquals(expectedResult, actualResult);
    }    
}
