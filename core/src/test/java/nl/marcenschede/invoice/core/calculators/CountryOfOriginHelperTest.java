package nl.marcenschede.invoice.core.calculators;

import nl.marcenschede.invoice.core.*;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CountryOfOriginHelperTest {

    @Test
    public void shouldSelectOriginCountryFromInvoice() {

        Invoice invoice = new Invoice() {
            @Override
            public Company getCompany() {
                return null;
            }

            @Override
            public void setCompany(Company company) {

            }

            @Override
            public Customer getCustomer() {
                return null;
            }

            @Override
            public void setCustomer(Customer customer) {

            }

            @Override
            public InvoiceType getInvoiceType() {
                return null;
            }

            @Override
            public void setInvoiceType(InvoiceType invoiceType) {

            }

            @Override
            public Optional<String> getCountryOfOrigin() {
                return Optional.of("XX");
            }

            @Override
            public void setCountryOfOrigin(Optional<String> productOrigin) {

            }

            @Override
            public Optional<String> getCountryOfDestination() {
                return null;
            }

            @Override
            public void setCountryOfDestination(Optional<String> countryOfDestination) {

            }

            @Override
            public List<InvoiceLine> getInvoiceLines() {
                return null;
            }

            @Override
            public void setInvoiceLines(List<InvoiceLine> invoiceLines) {

            }

            @Override
            public Optional<ProductCategory> getProductCategory() {
                return null;
            }

            @Override
            public void setProductCategory(Optional<ProductCategory> productCategory) {

            }

            @Override
            public boolean getVatShifted() {
                return false;
            }

            @Override
            public void setVatShifted(Boolean aBoolean) {

            }

            @Override
            public InvoiceTotals getInvoiceTotals() {
                return null;
            }
        };

        String originCountry = CountryOfOriginHelper.getOriginCountry(invoice);

        assertThat(originCountry, is("XX"));
    }

    @Test
    public void shouldSelectOriginCountryFromCompany() {

        Invoice invoice = new Invoice() {
            @Override
            public Company getCompany() {
                return new Company() {
                    @Override
                    public VatCalculationPolicy getVatCalculationPolicy() {
                        return null;
                    }

                    @Override
                    public String getPrimaryCountryIso() {
                        return "YY";
                    }

                    @Override
                    public Map<String, String> getVatRegistrations() {
                        return null;
                    }
                };
            }

            @Override
            public void setCompany(Company company) {

            }

            @Override
            public Customer getCustomer() {
                return null;
            }

            @Override
            public void setCustomer(Customer customer) {

            }

            @Override
            public InvoiceType getInvoiceType() {
                return null;
            }

            @Override
            public void setInvoiceType(InvoiceType invoiceType) {

            }

            @Override
            public Optional<String> getCountryOfOrigin() {
                return Optional.empty();
            }

            @Override
            public void setCountryOfOrigin(Optional<String> productOrigin) {

            }

            @Override
            public Optional<String> getCountryOfDestination() {
                return null;
            }

            @Override
            public void setCountryOfDestination(Optional<String> countryOfDestination) {

            }

            @Override
            public List<InvoiceLine> getInvoiceLines() {
                return null;
            }

            @Override
            public void setInvoiceLines(List<InvoiceLine> invoiceLines) {

            }

            @Override
            public Optional<ProductCategory> getProductCategory() {
                return null;
            }

            @Override
            public void setProductCategory(Optional<ProductCategory> productCategory) {

            }

            @Override
            public boolean getVatShifted() {
                return false;
            }

            @Override
            public void setVatShifted(Boolean aBoolean) {

            }

            @Override
            public InvoiceTotals getInvoiceTotals() {
                return null;
            }
        };

        String originCountry = CountryOfOriginHelper.getOriginCountry(invoice);

        assertThat(originCountry, is("YY"));
    }

    @Test(expected = CountryOfOriginHelper.NoOriginCountrySetException.class)
    public void shouldThrowExceptionSelectWhenOriginCountryFromCompanyNotSet() {

        Invoice invoice = new Invoice() {
            @Override
            public Company getCompany() {
                return new Company() {
                    @Override
                    public VatCalculationPolicy getVatCalculationPolicy() {
                        return null;
                    }

                    @Override
                    public String getPrimaryCountryIso() {
                        return null;
                    }

                    @Override
                    public Map<String, String> getVatRegistrations() {
                        return null;
                    }
                };
            }

            @Override
            public void setCompany(Company company) {

            }

            @Override
            public Customer getCustomer() {
                return null;
            }

            @Override
            public void setCustomer(Customer customer) {

            }

            @Override
            public InvoiceType getInvoiceType() {
                return null;
            }

            @Override
            public void setInvoiceType(InvoiceType invoiceType) {

            }

            @Override
            public Optional<String> getCountryOfOrigin() {
                return Optional.empty();
            }

            @Override
            public void setCountryOfOrigin(Optional<String> productOrigin) {

            }

            @Override
            public Optional<String> getCountryOfDestination() {
                return null;
            }

            @Override
            public void setCountryOfDestination(Optional<String> countryOfDestination) {

            }

            @Override
            public List<InvoiceLine> getInvoiceLines() {
                return null;
            }

            @Override
            public void setInvoiceLines(List<InvoiceLine> invoiceLines) {

            }

            @Override
            public Optional<ProductCategory> getProductCategory() {
                return null;
            }

            @Override
            public void setProductCategory(Optional<ProductCategory> productCategory) {

            }

            @Override
            public boolean getVatShifted() {
                return false;
            }

            @Override
            public void setVatShifted(Boolean aBoolean) {

            }

            @Override
            public InvoiceTotals getInvoiceTotals() {
                return null;
            }
        };

        String originCountry = CountryOfOriginHelper.getOriginCountry(invoice);
    }

}