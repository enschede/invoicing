package nl.marcenschede.invoice;

import nl.marcenschede.invoice.calcs.VatAmountSummaryFactory;
import nl.marcenschede.invoice.calculators.InvoiceCalculationsDelegate;
import nl.marcenschede.invoice.calculators.InvoiceCalculationsDelegateFactory;
import nl.marcenschede.invoice.tariffs.CountryTariffPeriodPercentageTuple;
import nl.marcenschede.invoice.tariffs.VatRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InvoiceImpl implements Invoice {
    private final VatRepository vatRepository;
    private final Function<String, Function<? super InvoiceLine, VatAmountSummary>> vatCalculatorForVatDeclarationCountry;

    private Company company;
    private Customer customer;
    private InvoiceType invoiceType;
    private Optional<String> countryOfOrigin;
    private Optional<String> countryOfDestination;
    private List<InvoiceLine> invoiceLines = new ArrayList<>();
    private Optional<ProductCategory> productCategory;

    public InvoiceImpl(VatRepository vatRepository) {
        this.vatRepository = vatRepository;

        Function<VatRepository, Function<String, Function<? super InvoiceLine, VatAmountSummary>>> vatCalculatorFactory =
                VatAmountSummaryFactory.createVatCalculatorWith2();
        vatCalculatorForVatDeclarationCountry = vatCalculatorFactory.apply(vatRepository);
    }

    @Override
    public Company getCompany() {
        return company;
    }

    @Override
    public void setCompany(Company company) {

        this.company = company;
    }

    @Override
    public Customer getCustomer() {
        return customer;
    }

    @Override
    public void setCustomer(Customer customer) {

        this.customer = customer;
    }

    @Override
    public void setInvoiceType(InvoiceType invoiceType) {

        this.invoiceType = invoiceType;
    }

    @Override
    public Optional<String> getCountryOfOrigin() {

        return countryOfOrigin;
    }

    @Override
    public void setCountryOfOrigin(Optional<String> countryOfOrigin) {

        this.countryOfOrigin = countryOfOrigin;
    }

    @Override
    public Optional<String> getCountryOfDestination() {
        return countryOfDestination;
    }

    @Override
    public void setCountryOfDestination(Optional<String> countryOfDestination) {
        this.countryOfDestination = countryOfDestination;
    }

    @Override
    public List<InvoiceLine> getInvoiceLines() {
        return invoiceLines;
    }

    @Override
    public void setInvoiceLines(List<InvoiceLine> invoiceLines) {
        this.invoiceLines = invoiceLines;
    }

    @Override
    public Optional<ProductCategory> getProductCategory() {
        return productCategory;
    }

    @Override
    public void setProductCategory(Optional<ProductCategory> productCategory) {
        this.productCategory = productCategory;
    }

    @Override
    public void setVatShifted(Boolean aBoolean) {

    }

    @Override
    public InvoiceTotals getInvoiceTotals() {
        InvoiceCalculationsDelegate calculationsHelper = InvoiceCalculationsDelegateFactory.newInstance(this);

        calculationsHelper.checkInvoiceValidity();

        return calculateInvoiceTotals(calculationsHelper);
    }

    private InvoiceTotals calculateInvoiceTotals(InvoiceCalculationsDelegate calculationsHelper) {
        Function<? super InvoiceLine, VatAmountSummary> vatCalculator =
                vatCalculatorForVatDeclarationCountry.apply(calculationsHelper.getVatDeclarationCountry());

        List<LineSummary> lineSummaries =
                calculationsHelper.getLineSummaries(vatCalculator);

        Map<CountryTariffPeriodPercentageTuple, VatAmountSummary> vatAmountSummaryPerPercentage =
                getVatAmountSummaryPerPercentage(lineSummaries);

        return new InvoiceTotals(
                calculationsHelper.getTotalInvoiceAmountExclVat(lineSummaries, vatCalculator),
                calculationsHelper.getInvoiceTotalVat(lineSummaries, vatCalculator),
                calculationsHelper.getTotalInvoiceAmountInclVat(lineSummaries, vatCalculator),
                vatAmountSummaryPerPercentage);
    }

    private Map<CountryTariffPeriodPercentageTuple, VatAmountSummary> getVatAmountSummaryPerPercentage(List<LineSummary> lineSummaries) {

        Map<CountryTariffPeriodPercentageTuple, List<LineSummary>> mapOfInvoiceLinesPerVatPercentage = getMapOfCountryTariffPeriodPercentageTuples(lineSummaries);

        return mapOfInvoiceLinesPerVatPercentage.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        optionalListEntry -> sumAmountsToVatAmountSummary(
                                optionalListEntry.getKey(),
                                optionalListEntry.getValue())));
    }

    private Map<CountryTariffPeriodPercentageTuple, List<LineSummary>> getMapOfCountryTariffPeriodPercentageTuples(List<LineSummary> lineSummaries) {
        return lineSummaries.stream()
                .collect(Collectors.groupingBy(lineSummary -> lineSummary.getCountryTariffPeriodPercentageTuple()));
    }

    private VatAmountSummary sumAmountsToVatAmountSummary(CountryTariffPeriodPercentageTuple percentage, List<LineSummary> lineSummaries) {
        final BigDecimal ZERO = new BigDecimal("0.00");

        return lineSummaries.stream()
                .map(lineSummary -> (VatAmountSummary) lineSummary)
                .reduce(new VatAmountSummary(percentage, ZERO, ZERO, ZERO), VatAmountSummary::add);
    }

    public static class NoRegistrationInOriginCountryException extends RuntimeException {
    }
}
