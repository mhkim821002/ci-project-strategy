package com.acornworks.projectset.domains;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TradingResult {
    private List<TradingData> stockPrices;
    private BigDecimal grossReturn;

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;

        final TradingResult tr = (TradingResult)o;

        return this.stockPrices.equals(tr.getStockPrices()) &&
        this.grossReturn.equals(tr.getGrossReturn());
    }


}
