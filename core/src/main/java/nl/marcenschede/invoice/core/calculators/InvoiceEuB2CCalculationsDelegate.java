package nl.marcenschede.invoice.core.calculators;

import nl.marcenschede.invoice.core.*;
import nl.marcenschede.invoice.core.tariffs.CountryTariffPeriodPercentageTuple;
import nl.marcenschede.invoice.core.tariffs.VatRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

import static nl.marcenschede.invoice.core.BigDecimalHelper.ZERO;

public class InvoiceEuB2CCalculationsDelegate extends InvoiceCalculationsDelegate {

    public InvoiceEuB2CCalculationsDelegate(Invoice invoice, VatRepository vatRepository) {
        super(invoice, vatRepository);
    }

    @Override
    public BigDecimal getTotalInvoiceAmountInclVat(List<LineSummary> lineSummaries, Function<? super InvoiceLine, VatAmountSummary> amountSummaryCalculator) {
        return lineSummaries.stream()
                .map(VatAmountSummary::getAmountInclVat)
                .reduce(ZERO, BigDecimal::add);

    }

    @Override
    public BigDecimal getTotalInvoiceAmountExclVat(List<LineSummary> lineSummaries, Function<? super InvoiceLine, VatAmountSummary> amountSummaryCalculator) {
        return lineSummaries.stream()
                .map(VatAmountSummary::getAmountExclVat)
                .reduce(ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotalInvoiceAmountExclVatOnTotals(List<LineSummary> lineSummaries, Function<? super InvoiceLine, VatAmountSummary> vatCalculator) {
        return ZERO;
    }

    @Override
    public BigDecimal getInvoiceTotalVat(List<LineSummary> lineSummaries, Function<? super InvoiceLine, VatAmountSummary> amountSummaryCalculator) {
        return lineSummaries.stream()
                .map(VatAmountSummary::getAmountVat)
                .reduce(ZERO, BigDecimal::add);
    }

    @Override
    public String getVatDeclarationCountry() {

        String destinationCountry = CountryOfDestinationHelper.getDestinationCountry(invoice);

        Company company = invoice.getCompany();
        Boolean companyHasRegistrationInDestinationCountry =
                company.getVatRegistrations().containsKey(destinationCountry);

        return companyHasRegistrationInDestinationCountry ?
                destinationCountry : CountryOfOriginHelper.getOriginCountry(invoice);
    }

    @Override
    public CountryTariffPeriodPercentageTuple createCountryTariffPeriodPercentageTupleForCalcationStratery(
            CountryTariffPeriodPercentageTuple countryTariffPeriodPercentageTuple) {

        return countryTariffPeriodPercentageTuple;
    }
}
