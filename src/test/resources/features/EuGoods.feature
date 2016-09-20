Feature: As I salesman I want to sell and invoice goods in other EU countries

  Background:
    Given A company in "NL" with vat calculation policy is "VAT_CALCULATION_ON_TOTAL"
    And the company has VAT id "NL0123456789B01" in "NL"
    And An invoiceline worth "100.00" euro "excl" VAT with "High" vat level and referencedate is "2016-01-01"
    And An invoiceline worth "100.00" euro "excl" VAT with "Low1" vat level and referencedate is "2016-01-01"
    And An invoiceline worth "100.00" euro "excl" VAT with "Zero" vat level and referencedate is "2016-01-01"

  Scenario: Goods are invoiced to a B2B customer in another EU country
    Given the company has VAT id "DE0123456789B01" in "DE"
    And A customer with VAT id "DE67890" and default country is "DE"
    And Country of origin is "NL"
    And Country of destination is "DE"
    When A "consumer" invoice is created at "2016-01-01"
    Then The total amount including VAT is "300.00"
    And The total amount excluding VAT is "300.00"
    And The total amount VAT is "0.00"
    And The VAT amount for percentage "0.00" is "0.00"