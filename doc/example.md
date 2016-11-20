# Examples for using Gember Invoice

## Calculation of pro forma invoice totals

In order to calculate the total amounts of an invoice, the calculator requires to know the VAT tariffs and company data. Based on this data and the invoice data the totals can be calculated.

    Function<VatRepository, Function<Company, Function<InvoiceData, InvoiceTotals>>> invoiceCalculatorFactory =
        InvoiceCalculatorFactory.getInvoiceCalculatorFactory();

    Function<Company, Function<InvoiceData, InvoiceTotals>> invoiceCalculatorFactoryWithVatTariffs =
        invoiceCalculatorFactory.apply(vatRepository);

    Function<InvoiceData, InvoiceTotals> invoiceCalculatorFactoryWithCompany =
        invoiceCalculatorFactoryWithVatTariffs.apply(company);

    InvoiceTotals totals = invoiceCalculatorFactoryWithCompany.apply(invoiceData);
   
        
## Creation of an invoice

To create a definitive invoice, an invoice number supplier and an event consumer are required.

    Function<Consumer<InvoiceCreationEvent>, Consumer<InvoiceData>> invoiceCreationFunction =
        InvoiceCreationFactory.invoiceCreationFactory(invoiceNumberSupplier, company, vatRepository);

    Consumer<InvoiceData> consumer = invoiceCreationFunction.apply(eventConsumer);

    consumer.accept(invoiceData);

## Invoice creation in batch

    Function<Consumer<InvoiceCreationEvent>, Consumer<InvoiceData>> invoiceCreationFunction =
        InvoiceCreationFactory.invoiceCreationFactory(invoiceNumberSupplier, company, vatRepository);

    Consumer<InvoiceData> consumer = invoiceCreationFunction.apply(eventConsumer);

    List<InvoiceData> invoiceData = ...
    invoiceData.stream().forEach(consumer);


## Description of input parameters

### VatRepository class

### Company

The company is defined as the company responsible for sending a the invoice to a (business or private) customer. The company interface has the following structure:

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
    
Due to tax regulations the VatCalculationPolicy should be made as a long term choiche. Please check your local tax regulations on this subject.

### VatCalculationPolicy

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

### getVatRegistrations

The vatRegistrations map has a list of vat registrations ids per EU country that are valid for the company.

Example:
[ {NL, NL123456789B01},
  {BE, BE8758758173} ]

## Invoice number supplier
The invoice number supplier is of type Supplier<Long> which will create a subsequent, unique number each time the get() method is invoked.

IMPORTANT:
Due to tax regulations invoice numbers, of invoices sent to customer, should be subsequent. It is not allowed leave gaps of unsent invoices in number blocks!

## Event consumer
After creating the invoice totals and assigning a unique invoice number, an invoice creation event is created.
This event contains all relevant data and is sent to the event consumer. 



