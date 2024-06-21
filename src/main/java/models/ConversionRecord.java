package models;

import java.util.Date;

public class ConversionRecord {
    private String baseCurrency;
    private String targetCurrency;
    private float baseAmount;

    private float resultingAmount;
    private Date timeConversion;

    public ConversionRecord(String baseCurrency, float baseAmount, String targetCurrency, float resultingAmount, Date timeConversion) {
        this.baseCurrency = baseCurrency;
        this.baseAmount = baseAmount;
        this.targetCurrency = targetCurrency;
        this.resultingAmount = resultingAmount;
        this.timeConversion = timeConversion;
    }

    public float getResultingAmount() {
        return resultingAmount;
    }

    public void setResultingAmount(float resultingAmount) {
        this.resultingAmount = resultingAmount;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public float getBaseAmount() {
        return baseAmount;
    }

    public Date getTimeConversion() {
        return timeConversion;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public void setTargetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public void setBaseAmount(float baseAmount) {
        this.baseAmount = baseAmount;
    }

    public void setTimeConversion(Date timeConversion) {
        this.timeConversion = timeConversion;
    }

    @Override
    public String toString() {
        return "ConversionRecord{" +
                "baseCurrency='" + baseCurrency + '\'' +
                ", targetCurrency='" + targetCurrency + '\'' +
                ", amount=" + baseAmount +
                ", timeConversion=" + timeConversion +
                '}';
    }
}
