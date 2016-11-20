package nl.marcenschede.invoice.core.calculators;

import nl.marcenschede.invoice.core.*;
import nl.marcenschede.invoice.core.calcs.VatAmountSummaryFactory;
import nl.marcenschede.invoice.core.tariffs.CountryTariffPeriodPercentageTuple;
import nl.marcenschede.invoice.core.tariffs.VatRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static nl.marcenschede.invoice.core.BigDecimalHelper.ZERO;

public abstract class InvoiceCalculationsDelegate {
    protected final Invoice invoice;
    protected final VatRepository vatRepository;

    public InvoiceCalculationsDelegate(Invoice invoice, VatRepository vatRepository) {
        this.invoice = invoice;
        this.vatRepository = vatRepository;
    }

    public abstract BigDecimal getTotalInvoiceAmountInclVat(List<LineSummary> lineSummaries, Function<? super InvoiceLine, VatAmountSummary> amountSummaryCalculator);

    public abstract BigDecimal getTotalInvoiceAmountExclVatOnTotals(List<LineSummary> lineSummaries, Function<? super InvoiceLine, VatAmountSummary> vatCalculator);

    public abstract BigDecimal getTotalInvoiceAmountExclVat(List<LineSummary> lineSummaries, Function<? super InvoiceLine, VatAmountSummary> amountSummaryCalculator);

    public abstract BigDecimal getInvoiceTotalVat(List<LineSummary> lineSummaries, Function<? super InvoiceLine, VatAmountSummary> amountSummaryCalculator);

    public abstract String getVatDeclarationCountry();

    private void checkInvoiceValidity() {
        validateRegistrationInOriginCountry();
    }

    private void validateRegistrationInOriginCountry() {
        String originCountry = CountryOfOriginHelper.getOriginCountry(invoice);
        if (!invoice.getCompany().getVatRegistrations().containsKey(originCountry))
            throw new InvoiceImpl.NoRegistrationInOriginCountryException();
    }

    public abstract CountryTariffPeriodPercentageTuple createCountryTariffPeriodPercentageTupleForCalcationStratery(CountryTariffPeriodPercentageTuple countryTariffPeriodPercentageTuple);

    public InvoiceTotals calculateInvoice() {
        checkInvoiceValidity();

        List<LineSummary> lineSummaries = calculateLineSummaries();

        Map<CountryTariffPeriodPercentageTuple, VatAmountSummary> vatAmountSummaryPerPercentage =
                calculateSubTotals(lineSummaries);

        return calculateInvoieTotals(lineSummaries, vatAmountSummaryPerPercentage);
    }

    private List<LineSummary> calculateLineSummaries() {
        Function<? super InvoiceLine, VatAmountSummary> vatCalculator = getVatAmountSummaryFunction();

        return invoice.getInvoiceLines().stream()
                .map(invoiceLine -> new LineSummary(vatCalculator.apply(invoiceLine), invoiceLine))
                .collect(Collectors.toList());
    }

    protected Function<? super InvoiceLine, VatAmountSummary> getVatAmountSummaryFunction() {
        return VatAmountSummaryFactory.createVatCalculatorWith()
                .apply(vatRepository)
                .apply(getVatDeclarationCountry());
    }

    private Map<CountryTariffPeriodPercentageTuple, VatAmountSummary> calculateSubTotals(List<LineSummary> lineSummaries) {
        return invoice.getCompany().getVatCalculationPolicy() == VatCalculationPolicy.VAT_CALCULATION_PER_LINE ?
                getVatAmountSummaryPerPercentagePerLine(lineSummaries) :
                getVatAmountSummaryPerPercentageOnSubTotals(lineSummaries);
    }

    private Map<CountryTariffPeriodPercentageTuple, VatAmountSummary> getVatAmountSummaryPerPercentagePerLine(List<LineSummary> lineSummaries) {

        Map<CountryTariffPeriodPercentageTuple, List<LineSummary>> mapOfInvoiceLinesPerVatPercentage = getMapOfCountryTariffPeriodPercentageTuples(lineSummaries);

        return mapOfInvoiceLinesPerVatPercentage.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        optionalListEntry -> sumAmountsToVatAmountSummary(
                                optionalListEntry.getKey(),
                                optionalListEntry.getValue())));
    }

    private Map<CountryTariffPeriodPercentageTuple,
            VatAmountSummary> getVatAmountSummaryPerPercentageOnSubTotals(List<LineSummary> lineSummaries) {

        Map<CountryTariffPeriodPercentageTuple, List<LineSummary>> mapOfInvoiceLinesPerVatPercentage =
                getMapOfCountryTariffPeriodPercentageTuples(lineSummaries);

        return mapOfInvoiceLinesPerVatPercentage.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        optionalListEntry ->
                                invoice.getInvoiceType() == InvoiceType.CONSUMER ?
                                        calculateVatAmountSummaryForConsumerInvoice(optionalListEntry) :
                                        calculateVatAmountSummaryForBusinessInvoice(optionalListEntry)));
    }

    private VatAmountSummary calculateVatAmountSummaryForBusinessInvoice(Map.Entry<CountryTariffPeriodPercentageTuple, List<LineSummary>> optionalListEntry) {
        BigDecimal totalExclVat = optionalListEntry.getValue().stream()
                .map(VatAmountSummary::getAmountExclVat)
                .reduce(ZERO, BigDecimal::add);

        BigDecimal numerator = optionalListEntry.getKey().getPercentage();
        BigDecimal denominator = new BigDecimal("100.00");
        BigDecimal totalVat = totalExclVat
                .multiply(numerator)
                .divide(denominator, 2, BigDecimal.ROUND_HALF_UP);

        BigDecimal totalInclVat = totalExclVat.add(totalVat);

        return new VatAmountSummary(
                optionalListEntry.getKey(),
                totalVat, totalExclVat, totalInclVat);
    }

    private VatAmountSummary calculateVatAmountSummaryForConsumerInvoice(Map.Entry<CountryTariffPeriodPercentageTuple, List<LineSummary>> optionalListEntry) {
        BigDecimal totalInclVat = optionalListEntry.getValue().stream()
                .map(VatAmountSummary::getAmountInclVat)
                .reduce(ZERO, BigDecimal::add);

        BigDecimal numerator = optionalListEntry.getKey().getPercentage();
        BigDecimal denominator = new BigDecimal("100.00").add(optionalListEntry.getKey().getPercentage());
        BigDecimal totalVat = totalInclVat
                .multiply(numerator)
                .divide(denominator, 2, BigDecimal.ROUND_HALF_UP);

        BigDecimal totalExVat = totalInclVat.subtract(totalVat);

        return new VatAmountSummary(
                optionalListEntry.getKey(),
                totalVat, totalExVat, totalInclVat);
    }

    private Map<CountryTariffPeriodPercentageTuple, List<LineSummary>> getMapOfCountryTariffPeriodPercentageTuples(List<LineSummary> lineSummaries) {
        return lineSummaries.stream()
                .collect(Collectors.groupingBy(VatAmountSummary::getCountryTariffPeriodPercentageTuple));
    }

    private VatAmountSummary sumAmountsToVatAmountSummary(CountryTariffPeriodPercentageTuple percentage, List<LineSummary> lineSummaries) {
        return lineSummaries.stream()
                .map(lineSummary -> (VatAmountSummary) lineSummary)
                .reduce(new VatAmountSummary(percentage, ZERO, ZERO, ZERO), VatAmountSummary::add);
    }

    private InvoiceTotals calculateInvoieTotals(List<LineSummary> lineSummaries, Map<CountryTariffPeriodPercentageTuple, VatAmountSummary> vatAmountSummaryPerPercentage) {

        return new InvoiceTotals(
                calculateTotalAmountExclVat(vatAmountSummaryPerPercentage),
                calculateTotalAmountVat(vatAmountSummaryPerPercentage),
                calculateTotalAmountInclVat(vatAmountSummaryPerPercentage),
                vatAmountSummaryPerPercentage,
                lineSummaries);
    }

    private BigDecimal calculateTotalAmountInclVat(Map<CountryTariffPeriodPercentageTuple, VatAmountSummary> vatAmountSummaryPerPercentage) {
        return vatAmountSummaryPerPercentage.values().stream()
                .map(VatAmountSummary::getAmountInclVat).reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalAmountVat(Map<CountryTariffPeriodPercentageTuple, VatAmountSummary> vatAmountSummaryPerPercentage) {
        return vatAmountSummaryPerPercentage.values().stream()
                .map(VatAmountSummary::getAmountVat).reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalAmountExclVat(Map<CountryTariffPeriodPercentageTuple, VatAmountSummary> vatAmountSummaryPerPercentage) {
        return vatAmountSummaryPerPercentage.values().stream()
                .map(VatAmountSummary::getAmountExclVat).reduce(ZERO, BigDecimal::add);
    }
}
