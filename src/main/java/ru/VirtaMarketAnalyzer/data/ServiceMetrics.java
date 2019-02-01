package ru.VirtaMarketAnalyzer.data;

/**
 * Created by cobr123 on 01.02.2019.
 */
final public class ServiceMetrics {
    final String turnId;
    final double price;
    final long sales;
    final int unitCount;
    final int companyCount;
    final double revenuePerRetail;
    final String name;
    final String symbol;
    final String specialization;

    public ServiceMetrics(final String turnId,
                          final double price,
                          final long sales,
                          final int unitCount,
                          final int companyCount,
                          final double revenuePerRetail,
                          final String name,
                          final String symbol,
                          final String specialization
    ) {
        this.turnId = turnId;
        this.price = price;
        this.sales = sales;
        this.unitCount = unitCount;
        this.companyCount = companyCount;
        this.revenuePerRetail = revenuePerRetail;
        this.name = name;
        this.symbol = symbol;
        this.specialization = specialization;
    }

    public String getTurnId() {
        return turnId;
    }

    public double getPrice() {
        return price;
    }

    public long getSales() {
        return sales;
    }

    public int getUnitCount() {
        return unitCount;
    }

    public int getCompanyCount() {
        return companyCount;
    }

    public double getRevenuePerRetail() {
        return revenuePerRetail;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getSpecialization() {
        return specialization;
    }
}
