package nl.marcenschede.invoice.core;

/**
 * Policy for calculating the subtotals per VAT-tariff.
 * Depending on this policy, rounding of values may differ.
 */
public enum VatCalculationPolicy {

    /**
     * The VAT, exclVAT and inclVAT amounts are calculated by summarizing the values per line
     */
    VAT_CALCULATION_PER_LINE,

    /**
     * The exclVAT or inclVAT amount is summarized from the lines.
     * The VAT amount and inclVAT resp exclVAT amount are calculated.
     */
    VAT_CALCULATION_ON_TOTAL;

}
