package com.acornworks.projectset.common;

import com.acornworks.projectset.domains.StockPrice;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class Payload {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonNode readPayload(String resourcePath) throws IOException {
        final String jsonStr = readPayloadString(resourcePath);
        final JsonNode payloadNode = objectMapper.readTree(jsonStr);

        return payloadNode;
    }

    public static String readPayloadString(String resourcePath) throws IOException {
        String jsonStr = "";

        try(FileReader fr = new FileReader(Payload.class.getClassLoader().getResource(resourcePath).getFile())) {
            try(BufferedReader br = new BufferedReader(fr)) {
                String line = null;
                StringBuffer sb = new StringBuffer();

                while((line = br.readLine()) != null) {
                    sb.append(line + System.lineSeparator());
                }

                jsonStr = sb.toString();
            }
        }

        return jsonStr;
    }

    public static List<StockPrice> readHistoricalDataPayload(String symbol, String resourcePath) throws ParseException, IOException, CsvException {
        final String jsonStr = readPayloadString(resourcePath);
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        final List<StockPrice> returnList = new ArrayList<>();

        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        try(CSVReader reader = new CSVReader(new StringReader(jsonStr))) {
            final List<String[]> readLines = reader.readAll();
            
            for (final String[] lineStrs : readLines) {
                if (lineStrs[0].equals("Date")) {
                    continue;
                }

                final Calendar dateCal = Calendar.getInstance();
                dateCal.setTime(formatter.parse(lineStrs[0]));

                returnList.add(new StockPrice(
                    symbol, 
                    dateCal, 
                    new BigDecimal(lineStrs[1]), 
                    new BigDecimal(lineStrs[2]), 
                    new BigDecimal(lineStrs[3]), 
                    new BigDecimal(lineStrs[4]), 
                    new BigDecimal(lineStrs[6]))
                );
            }
        } 
        
        return returnList;
    }
    
}
