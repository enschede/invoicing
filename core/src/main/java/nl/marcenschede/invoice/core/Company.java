package nl.marcenschede.invoice.core;

import java.util.Map;

public interface Company {

    /**
     * Denotes the way the VAT subtotals are calculated.
     */
    public VatCalculationPolicy getVatCalculationPolicy();

    /**
     * Denotes the default EU country as source of the services and shipped goods
     */
    public String getPrimaryCountryIso();

    /**
     * List of EU countries and VAT registration ids
     */
    public Map<String, String> getVatRegistrations();
}
