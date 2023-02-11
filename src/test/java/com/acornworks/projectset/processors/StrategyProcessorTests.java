package com.acornworks.projectset.processors;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import com.acornworks.projectset.common.Payload;
import com.acornworks.projectset.domains.StockPrice;
import com.acornworks.projectset.domains.StrategyName;
import com.acornworks.projectset.domains.TradingResult;
import com.opencsv.exceptions.CsvException;

@ExtendWith(MockitoExtension.class)
public class StrategyProcessorTests {
    @InjectMocks
    private StrategyProcessor processor;

    @Mock
    private RestTemplate restTemplate;

    @Test
    public void testGetBarSeriesFromStockData() throws ParseException, IOException, CsvException {
        final List<StockPrice> stockPrices = Payload
            .readHistoricalDataPayload("AAPL", "payloads/AAPL.csv");

        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        final BarSeries barSeries = processor.getBarSeriesFromStockData(stockPrices);
        final int dataCount = stockPrices.size();

        Assertions.assertEquals(dataCount, barSeries.getBarCount());

        final List<Integer> compareIndice = Arrays.asList(0, dataCount - 1);

        for(Integer idx : compareIndice) {
            final StockPrice comparePrice = stockPrices.get(idx);
            final Bar compareBar = barSeries.getBar(idx);
            final String expectedDate =  formatter.format(comparePrice.getDate().getTime());
            final String actualDate = formatter.format(Date.from(compareBar.getEndTime().toInstant()));

            Assertions.assertEquals(comparePrice.getOpen().toString(), compareBar.getOpenPrice().toString()); 
            Assertions.assertEquals(comparePrice.getHigh().toString(), compareBar.getHighPrice().toString()); 
            Assertions.assertEquals(comparePrice.getLow().toString(), compareBar.getLowPrice().toString()); 
            Assertions.assertEquals(comparePrice.getClose().toString(), compareBar.getClosePrice().toString()); 
            Assertions.assertEquals(comparePrice.getVolume().toString(), compareBar.getVolume().toString()); 
            Assertions.assertEquals(expectedDate, actualDate); 
        }
    }

    @Test
    public void testGetAnalysisResult() throws ParseException, IOException, CsvException {
        final List<StockPrice> stockPrices = Payload
            .readHistoricalDataPayload("AAPL", "payloads/AAPL.csv");

        StockPrice[] stockPriceArr = new StockPrice[stockPrices.size()];
        stockPriceArr = stockPrices.toArray(stockPriceArr);

        ReflectionTestUtils.setField(processor, "priceEndpointUrl", "https://localhost:65020");

        Mockito.when(restTemplate.getForEntity(ArgumentMatchers.anyString(), Mockito.eq(StockPrice[].class)))
            .thenReturn(new ResponseEntity<StockPrice[]>(stockPriceArr, HttpStatus.OK));

        final TradingResult adxResult = processor.getAnalysisResult("AAPL", StrategyName.ADX);
        Assertions.assertEquals("0.74021676671636781944728096106528", adxResult.getGrossReturn().toString());
        Assertions.assertEquals(stockPrices.size(), adxResult.getStockPrices().size());

        final TradingResult cciResult = processor.getAnalysisResult("AAPL", StrategyName.CCICorrelation);
        Assertions.assertEquals("1", cciResult.getGrossReturn().toString());
        Assertions.assertEquals(stockPrices.size(), cciResult.getStockPrices().size());

        final TradingResult geResult = processor.getAnalysisResult("AAPL", StrategyName.GlobalExtrema);
        Assertions.assertEquals("1.0143369856744770972637146569187", geResult.getGrossReturn().toString());
        Assertions.assertEquals(stockPrices.size(), geResult.getStockPrices().size());

        final TradingResult mmResult = processor.getAnalysisResult("AAPL", StrategyName.MovingMomentum);
        Assertions.assertEquals("1", mmResult.getGrossReturn().toString());
        Assertions.assertEquals(stockPrices.size(), mmResult.getStockPrices().size());

        final TradingResult rsi2Result = processor.getAnalysisResult("AAPL", StrategyName.RSI2);
        Assertions.assertEquals("0.83834037419617784386311436909214", rsi2Result.getGrossReturn().toString());
        Assertions.assertEquals(stockPrices.size(), rsi2Result.getStockPrices().size());
    }    
}
