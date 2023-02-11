package com.acornworks.projectset.domains;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TradingData extends StockPrice {
    private boolean buy = false;
    private boolean hold = false;
    
    public void importFromStockPrice(StockPrice stockPrice) {
        this.setTicker(stockPrice.getTicker());
        this.setDate(stockPrice.getDate());
        this.setOpen(stockPrice.getOpen());
        this.setHigh(stockPrice.getHigh());
        this.setLow(stockPrice.getLow());
        this.setClose(stockPrice.getClose());
        this.setVolume(stockPrice.getVolume());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;

        final TradingData td = (TradingData) o;

        return (this.buy == td.buy) &&
            (this.hold == td.hold) && 
            super.equals((StockPrice)td);
    }
}
