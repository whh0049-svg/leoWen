package com.stocks.service;

import com.stocks.model.HistoricalPrice;
import com.stocks.model.Stock;
import com.stocks.repository.HistoricalPriceRepository;
import com.stocks.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private HistoricalPriceRepository priceRepository;

    @InjectMocks
    private StockServiceImpl service;

    private Stock hsbc;

    @BeforeEach
    void setUp() {
        hsbc = new Stock(1L, "HSBA", "HSBC Holdings PLC", "Banking", "LSE");
    }

    @Test
    @DisplayName("addStock saves and returns the stock when symbol is unique")
    void addStock_uniqueSymbol_savesAndReturns() {
        when(stockRepository.findBySymbol("HSBA")).thenReturn(Optional.empty());
        when(stockRepository.save(any())).thenReturn(hsbc);

        Stock result = service.addStock(new Stock(null, "HSBA", "HSBC Holdings PLC", "Banking", "LSE"));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.symbol()).isEqualTo("HSBA");
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    @DisplayName("addStock throws when symbol already exists")
    void addStock_duplicateSymbol_throwsIllegalArgument() {
        when(stockRepository.findBySymbol("HSBA")).thenReturn(Optional.of(hsbc));

        assertThatThrownBy(() -> service.addStock(new Stock(null, "HSBA", "Dup", "Banking", "LSE")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HSBA");

        verify(stockRepository, never()).save(any());
    }

    @Test
    @DisplayName("getAllStocks delegates to the repository")
    void getAllStocks_delegatesToRepository() {
        when(stockRepository.findAll()).thenReturn(List.of(hsbc));

        List<Stock> result = service.getAllStocks();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().symbol()).isEqualTo("HSBA");
    }

    @Test
    @DisplayName("getStockById returns Optional.empty() when not found")
    void getStockById_notFound_returnsEmpty() {
        when(stockRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(service.getStockById(99L)).isEmpty();
    }

    @Test
    @DisplayName("addPrice delegates to price repository")
    void addPrice_delegatesToRepository() {
        HistoricalPrice price = new HistoricalPrice(null, 1L, LocalDate.of(2024, 6, 3),
                new BigDecimal("6.21"), new BigDecimal("6.28"),
                new BigDecimal("6.31"), new BigDecimal("6.18"), 28_000_000L);
        HistoricalPrice saved = new HistoricalPrice(5L, 1L, price.priceDate(),
                price.openPrice(), price.closePrice(), price.highPrice(), price.lowPrice(), price.volume());
        when(priceRepository.save(any())).thenReturn(saved);

        HistoricalPrice result = service.addPrice(price);

        assertThat(result.id()).isEqualTo(5L);
        verify(priceRepository).save(price);
    }
}
