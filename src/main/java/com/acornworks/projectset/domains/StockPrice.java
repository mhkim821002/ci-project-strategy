package com.acornworks.projectset.domains;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StockPrice {
    private String ticker = "";
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone="GMT")
    private Calendar date;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private BigDecimal volume;

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;

        final StockPrice sp = (StockPrice)o;
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        
        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        return this.ticker.equals(sp.getTicker()) &&
        (format.format(this.date.getTime()).equals(format.format(sp.getDate().getTime()))) &&
        this.open.equals(sp.getOpen()) && 
        this.high.equals(sp.getHigh()) && 
        this.low.equals(sp.getLow()) && 
        this.close.equals(sp.getClose()) && 
        this.volume.equals(sp.getVolume());
    }
}
