
# Examples Using Simulated Data
-------------------------------
All values in these examples are calculated using simulated live and historical market data, which is generated when the server is initialised.  

## Table of Contents
1. [Equities](#equity-example)
2. [ETFs](#etf-example)
3. [FX Forwards](#fx-forwards-example)
4. [FX Options](#fx-options-example)
5. [Swaps](#swap-example)
6. [Credit](#credit-example)


## Equities <a name="equity-example"></a>
-----------------------------------------

### Equity Portfolio View

![Equity Portfolio View](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/equity-portfolio-view.png)

This view references a long-only equity portfolio consisting of stocks in the S&P 500 index. It shows some simple portfolio analytic values, the live daily P&L and VaR.

The portfolio structure consists of sub-portfolios of equity trades aggregated by sector. 

#### FairValue
This is the (live) price of one share multiplied by the number of shares held in the portfolio

#### CAPM Beta
This is the ratio of the covariance of the trade / sub-portfolio returns and SPX returns (the underlying index) to the variance of SPX returns.

#### Sharpe Ratio
This measures the excess return (return of the trade / sub-portfolio minus the return of the index) with respect to SPX per unit of risk (i.e. the standard deviation of returns) for the trade / sub-portfolio.

#### PnL
This shows the live daily P&L - the difference between the last close price and current price multiplied by the number of shares. This is an additive quantity and so portfolio-level P&L is simply the sum of the P&l of its positions.

#### HistoricalVaR
This is the one-day historical VaR at 99% confidence level. Two years of daily returns are used. 

The properties of the outputs show what values can be changed to customise the results. 

![VaR Properties](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/var-properties.png)

Going to the view definition editor:

![View Definition Editor 1](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/go-to-view-definition.png)

![View Definition Editor 2](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/edit-equity-view-definition-1.png)

and adding a column (```+add column```), we select ```HistoricalVaR``` from the dropdown and add properties (```+add constraint```):
  - Change the percentile to 99.73%
  - Change the sampling frequency to weekly
  - Change the VaR horizon to 7 days

![View Definition Editor 3](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/edit-equity-view-definition-2.png)

After saving this view definition, we go back to the analyics viewer and see that a second ```HistoricalVaR``` column has appeared. The properties show our changes, and we can see the effect on the distribution that changing these parameters has had by looking at the returns as a distribution (the top distribution is daily returns, the lower is weekly returns).

![Second VaR Column](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/equity-portfolio-view-new-column.png)

**NOTE**: if you're running the examples server and edit or add view definitions, these will be overwritten with the originals / deleted if the databases are re-initialised.


## ETFs <a name="etf-example"></a>
----------------------------------

### Futures View

![Futures View](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/futures-view.png)

This view shows analytics for a small equity futures portfolio, calculated with a mark-to-market method (i.e. using market quotes directly, rather than implying a forward curve).

#### Present Value
The mark-to-market price of the position: market quote minus the previous close (i.e. the margined amount) multiplied by number of contracts.

#### Delta
The change in value of the position for a change in value of the underlying - for mark-to-market pricing, this is the unit amount multiplied by the number of contracts multiplied by the future price.

#### Forward
The forward rate implied by the future - for mark-to-market pricing, this is just the future market quote.


## FX Forwards <a name="fx-forwards-example"></a>
Both FX forward examples reference a portfolio containing AUD, EUR, CHF and GBP vs USD forwards. All prices are calculated using a discounting method, where each pay / receive leg is discounted with a currency-specific curve.

The USD discounting curve is a simple curve constructed from cash deposits and is constructed first for each currency pair. The discounting curves for the other currencies are constructed using FX forward quotes, which are used with the USD curve to imply the interest rate. The interpolation in all cases is a monotonic constrained cubic spline with linear extrapolation at both ends. The instruments used to construct the curves are shown in the table below.

| Tenor \ Curve Name| AUD FX     | CHF FX     | EUR FX     | GBP FX     | USD Deposit |
|-------|------------|------------|------------|------------|-------------|
|  1W   | FX Forward | FX Forward | FX Forward | FX Forward | Deposit     |
|  2W   | FX Forward | FX Forward | FX Forward | FX Forward | Deposit     |
|  3W   | FX Forward | FX Forward | FX Forward | FX Forward | Deposit     |
|  1M   | FX Forward | FX Forward | FX Forward | FX Forward | Deposit     |
|  2M   |            |            |            |            | Deposit     |
|  3M   | FX Forward | FX Forward | FX Forward | FX Forward | Deposit     |
|  4M   |            |            |            |            | Deposit     |
|  5M   |            |            |            |            | Deposit     |
|  6M   | FX Forward | FX Forward | FX Forward | FX Forward | Deposit     |
|  9M   | FX Forward | FX Forward | FX Forward | FX Forward | Deposit     |
|  1Y   | FX Forward | FX Forward | FX Forward | FX Forward | Deposit     |
|  2Y   | FX Forward | FX Forward | FX Forward | FX Forward | Deposit     |
|  3Y   | FX Forward | FX Forward | FX Forward | FX Forward | Deposit     |
|  4Y   | FX Forward | FX Forward | FX Forward | FX Forward | Deposit     |
|  5Y   | FX Forward | FX Forward | FX Forward | FX Forward | Deposit     |
|  6Y   | FX Forward | FX Forward | FX Forward | FX Forward |             |
|  7Y   | FX Forward | FX Forward | FX Forward | FX Forward |             |
|  8Y   | FX Forward | FX Forward | FX Forward | FX Forward |             |
|  9Y   | FX Forward | FX Forward | FX Forward | FX Forward |             |
| 10Y   | FX Forward | FX Forward | FX Forward | FX Forward |             |

### FX Forward Details View

![FX Forward Details View](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/fx-forward-details.png)

#### FX Present Value
The FX present value is the discounted value of the pay and receive amounts of the forward. This multi-valued output is summed at the portfolio level, giving a total PV in each currency.

![FX PV](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/fx-forward-fx-pv-top-level.png)

#### Present Value
The present value is the sum of the discounted pay and receive amounts converted into the required currency.

#### FX Forward Details
This output gives the pricing details for each leg of the trade: the pay and receive amounts, the discount factors used for each leg and equivalent zero rate.

![FX Forward Details](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/fx-forward-details-details.png)

Note that this value is not summed at portfolio node level (which is why the column appears to be empty when the positions are collapsed to portfolio nodes). Whether or not a value can be summed is a decision for the person writing the code that integrates an analytics library into the platform.

### FX Forward View

![FX Forward View](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/fx-forward-view.png)

This view shows the present value and exposures to the underlyings for the portfolio of FX forwards.

#### Present Value
The same output as in the previous view: the discounted value of each leg converted into the result currency and then summed.

#### FX Currency Exposure
This is the sensitivity of the PV of each leg to changes in the spot rate.

#### Bucketed PV01
This is the change in PV of the trade to a change of 1 basis point in the **market quote** used to construct the curve. Each trade has sensitivities to two curves - the pay and receive currency discounting curves. We have made the decision to return vectors of zeroes when there is no sensitivity (e.g. a GBP/USD trade does not have any sensitivity to the EUR curve), but returning no result at all is a valid output - in this case, the cells in the grid would be empty.

**Note:** all trades in this portfolio are CCY/USD, so there will be sensitivities to the USD discounting curve and the CCY discounting curve. If the trade were CCY1/CCY2, there would be sensitivities to the two currency discounting curves *plus* the USD discounting curve, as the USD curve was used as the base curve when constructing curves from FX forwards.

When there are multiple columns with the same output name but different properties , hovering over the header will show details of which column is which:

![FX Result Properties](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/fx-forward-properties.png)

Looking at an AUD/USD trade, we can see sensitivities to the USD and AUD curves at approximately the maturity of the trade (the interpolation is not local, so there are some values outside the two surrounding nodes), and no sensitivity to a curve in another currency.

![FX Bucketed PV01](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/fx-forward-pv01.png)


## FX Options <a name="fx-options-example"></a>
-----------------------------------------------

### FX Option View

![FX Option View](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/fx-option-view.png)

This view shows analytics for a portfolio of vanilla European FX options priced using the Black model. The curve configurations are the same as those used to price [FX forwards](#fx-forwards-example). The volatility surfaces for each currency pair are quoted as ATM, 15 risk reversal and butterfly, and 25 risk reversal and butterfly. These data are converted to an interpolated delta matrix before pricing. 

#### Present Value
The present value of the trade as calculated by the Black model.

#### FX Currency Exposure
The spot delta in each currency.

#### Bucketed PV01
This is the change in PV of the trade to a change of 1 basis point in the **market quote** used to construct the curve. 

Note that in this porfolio, there are options that are not CCY/USD (EUR/GBP, in this case). These options have sensitivities to the GBP, EUR and USD curves, because the USD curve was used to construct the EUR and GBP curves.

![FX Option PV01](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/fx-option-pv01.png)

#### Vega Matrix
This is the vega with respect to the node points of the put delta matrix. The total vega is dispersed to surrounding node points: the amounts depend on the time and delta interpolation methods used.

![Vega Matrix](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/fx-vega-matrix.png)

#### Vega Quote Matrix
This is the vega with respect to the **market quotes** that the volatility surface was constructed from.

![Vega Quote Matrix](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/fx-vega-quote-matrix.png)

### FX Option Greeks View
This is another view of the same portfolio, this time returning greeks and their value equivalents (i.e. the greeks scaled by trade details). Again, Black pricing and the same curves / surface definitions are used.

![FX Option Greeks](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/fx-option-greeks.png)

Note that there are several non-additive quantities (e.g. implied volatility) that only give values at position level.

#### Present Value
The Black present value of the trade.

#### Security Implied Volatility
The implied volatility that was used in the Black equations. This is taken from an interpolated surface.

#### Forward Delta
The change in the price (not present value) with respect to the forward FX rate.

#### Forward Vega
The change in price with respect to the implied volatility.

#### Forward Gamma
The change in forward delta with respect to the forward FX rate: equivalently, the second derivative of the price with respect to the forward FX rate.

#### Forward Driftless Theta
The change in price of the option due to time decay only, i.e. not considering the drift of any other underlyings.

#### ValueDelta
The change in present value of the trade with respect to the forward FX rate.

#### ValueGamma
The change in value delta of the trade with respect to the forward FX rate.

#### ValueVega
The change in present value of the trade with respect to the implied volatility.

#### ValueVanna
The change in present value of the trade with respect to the forward FX rate and volatility: equivalently, the change in value delta with respect to implied volatility or change in value vega with respect to the forward FX rate.

#### ValueVomma 
The change in value vega of the trade with respect to the implied volatility i.e. the second derivative of the present value with respect to volatility.

## Swaps <a name="swap-example"></a>
------------------------------------
All swap example views reference a portfolio containing vanilla USD, EUR, CHF, JPY and GBP swaps. The curves for each currency are constructed using two-curve configurations: a discounting curve constructed with cash and OIS, and a forward LIBOR/TIBOR/etc. curve constructed using the appropriate index and vanilla fixed / \*IBOR swaps. The interpolation in all cases is a monotonic constrained cubic spline with linear extrapolation at both ends. The interpolation on the \*IBOR curves (used to calculate the forward rates) is performed on the zero rates.

| Tenor \ Curve Name | CHF Discounting | CHF 6M LIBOR | EUR Discounting | EUR 6M EURIBOR | GBP Discounting | GBP 6M LIBOR | JPY Discounting | JPY 6M TIBOR | USD Discounting | USD 3M LIBOR |
|--------------------|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|
|ON|Deposit||Deposit||Deposit||Deposit||Deposit||
|1M|OIS||OIS||OIS||OIS||OIS||
|2M|OIS||OIS||OIS||OIS||OIS||
|3M|OIS||OIS||OIS||OIS||OIS|LIBOR|
|4M|OIS||OIS||OIS||OIS||OIS||
|5M|OIS||OIS||OIS||OIS||OIS||
|6M|OIS|LIBOR|OIS|EURIBOR|OIS|LIBOR|OIS|TIBOR|OIS||
|9M|OIS||OIS||OIS||OIS||OIS||
|1Y|OIS|SWAP|OIS|SWAP|OIS|SWAP|OIS|SWAP|OIS|SWAP|
|2Y|OIS|SWAP|OIS|SWAP|OIS|SWAP|OIS|SWAP|OIS|SWAP|
|3Y|OIS|SWAP|OIS|SWAP|OIS|SWAP|OIS|SWAP|OIS|SWAP|
|4Y|OIS|SWAP|OIS|SWAP|OIS|SWAP|OIS|SWAP|OIS|SWAP|
|5Y|OIS|SWAP|OIS|SWAP|OIS|SWAP|OIS|SWAP|OIS|SWAP|
|6Y||SWAP||SWAP||SWAP||SWAP||SWAP|
|7Y||SWAP||SWAP||SWAP||SWAP||SWAP|
|8Y||SWAP||SWAP||SWAP||SWAP||SWAP|
|9Y||SWAP||SWAP||SWAP||SWAP||SWAP|
|10Y|OIS|SWAP|OIS|SWAP|OIS|SWAP|OIS|SWAP|OIS|SWAP|
|15Y||SWAP||SWAP||SWAP||SWAP||SWAP|
|20Y||SWAP||SWAP||SWAP||SWAP||SWAP|
|25Y||SWAP||SWAP||SWAP||SWAP||SWAP|
|30Y||SWAP||SWAP||SWAP||SWAP||SWAP|
                   
### Swap Details View

This view shows the present value of the swaps and details of the inputs used to calculate this value.

![Swap Details](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/swap-details-view.png)

#### Present Value
The present value of the swaps, using one curve for discounting cash-flows and the appropriate index curve to calculate the forward rates of the swaps. Note that there is not a portfolio-level value because we haven't specified a currency for the results.

#### Fixed Cash Flows

![Swap Fixed Leg](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/swap-fixed-leg-details.png)

The table shows all data for each cash-flow that is used to calculate the present value of the fixed leg:
    - The notional.
    - The fixed rate.
    - The start and end interest accrual dates. These dates are adjusted by the fixed leg business day convention, using the appropriate regional holiday calendar.
    - The payment year fraction - this is calculated from the accrual dates using the fixed leg day-count convention.
    - The payment amount - this is the notional multiplied by the fixed rate multiplied by the payment year fraction.
    - This is the payment time that is used to get the discount factor from the discounting yield curve
    - The discount factor used to get the present value of the payment amount. The zero rate is continuously compounded.
    - The discounted payment amount.
    
#### Floating Cash Flows

![Swap Floating Leg](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/swap-floating-leg-details.png)

This table shows all data for each cash-flow that is used to calculate the present vlaue of the floating leg:
  - The notional.
  - The spread (added to the underlying index level).
  - The gearing (multiplied by the underlying index level).
  - The underlying index tenor.
  - The payment date.
  - The payment time.
  - The start and end accrual date. 
  - The accrual year fraction.
  - The start and end fixing dates. Note that these dates are not neccesarily the same as the accrual start and end dates, as different business day adjustments may apply.
  - The fixing year fraction.
  - The fixed rate. This is only available for seasoned swaps where the floating rate has fixed.
  - The payment discount factor. This is the discount factor applied to any known or projected payments and is taken from the discounting curve.
  - The payment amount. This is a known amount if a floating rate has fixed.
  - The discounted payment amount. The known payment amount multiplied by the discount factor.
  - The forward rate. This is calculated using the forward (projection) curve.
  - The projected amount. This is the notional multiplied by the forward rate multiplied by the fixing year fraction.
  - The discounted projected amount. This is the projected amount discounted using the discounting curve.

### Swap View

This is a view that shows present value and PV01s for the same swap portfolio as above, using the same curve configurations.

![Swap View](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/swap-view.png)

#### Present Value
The present value of the swaps in USD.

#### Par Rate
The par rate of the swap i.e. the rate that would price the swap to par. This is not additive, so there are only values at the trade level.

#### Bucketed PV01
As for the FX forward and option views, these are the sensitivities of a swap to each curve used overall in the portfolio. As there is no coupling between curves of different currencies in the configuration used in this view, each swap will have sensitivities to the discounting and forward curve for the appropriate currency, and zero sensitivities to all other curves. For example, a USD swap has no sensitivities to the EUR discounting curve.

![Swap PV01](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/swap-pv01.png)


### AUD Swap View
In the previous swap examples, the portfolio contained swaps one underlying index per currency (e.g. 3M LIBOR for USD, 6M LIBOR for GBP). This meant that we only needed to construct two curves for each currency, a discounting and forward curve. The Australian swap market is different: the most liquid fixed / floating swaps are quarterly / quarterly up to three years and semi-annual / semi-annual for longer maturities. Obviously, we could construct the 3M bank bill curve up to three years and extrapolate for longer maturities, or the 6M bank bill with sparse data for maturities between six months and four years, but this introduces a fair amount of model risk due to the interpolation / extrapolation methods chosen. Instead, we can use basis (3M x 6M bank bill) swaps on the 3M and 6M curves. This means that there is coupling between the two bank bill curves and they must be constructed at the same time.

| Tenor \ Curve Name | AUD Discounting | AUD 3M BANK BILL | AUD 6M BANK BILL |
|--------------------|-----------------|------------------|------------------|
| ON                 | Overnight index |                  |                  |
| 1M                 | OIS             |                  |                  |
| 2M                 | OIS             |                  |                  |
| 3M                 | OIS             | Bank bill index  |                  |
| 4M                 | OIS             |                  |                  |
| 5M                 | OIS             |                  |                  |
| 6M                 | OIS             | 3x6M FRA         | Bank bill index  |
| 9M                 | OIS             | 6x9M FRA         |                  |
| 1Y                 | OIS             | 3M fixed/3M bank bill swap       | 3M bank bill/6M bank bill swap |
| 2Y                 | OIS             | 3M fixed/3M bank bill swap       | 3M bank bill/6M bank bill swap |
| 3Y                 | OIS             | 3M fixed/3M bank bill swap       | 3M bank bill/6M bank bill swap |
| 4Y                 | OIS             |                  |                  |
| 5Y                 | OIS             | 3M bank bill/6M bank bill swap   | 6M fixed/6M bank bill swap|
| 7Y                 |                 | 3M bank bill/6M bank bill swap   | 6M fixed/6M bank bill swap|
| 10Y                | OIS             | 3M bank bill/6M bank bill swap   | 6M fixed/6M bank bill swap|

We have some choice about the calculation method: although the 3M and 6M curves depend on the discounting curve, the discounting curve does not depend on either of the bank bill curves. This means that we can either construct all three curves at once, or we can construct the discounting curve first and use this as an input when building bank bill curves. The latter method means that the root-finding problem is smaller and so the calculation is faster. However, the results are the same:

![AUD swap view](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/aud-swap-view.png)

Going to the ```PRIMITIVES``` tab:

![Primitives tab](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/aud-swap-primitives.png)

we display the three curves calculated using the two methods:

![AUD 6M bank bill](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/aud-6m-bank-bill.png)

We have constructed this view with side-by-side comparisons of calculations (```Column Sets```):

![Column Set 1](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/aud-column-set-1.png)

![Column Set 2](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/aud-column-set-2.png)

This is a convenient way of seeing the effects of different calculation methods and makes retrieving the correct values programmatically or via REST easier.


## Credit <a name="credit-example"></a>

### Credit View
This view shows the results of calculations on a portfolio of corporate bonds and CDS. The CDS are priced using the Starling implementation of the ISDA CDS model. The bonds are priced using two curves: 

* the same yield curve that is used in pricing CDS (of the same currency - in fact, all trades in this portfolio are USD) is used to discount any bond payments that are known but that have not settled; 
* a bond yield curve that depends on the country and credit rating of the bond. This curve is constructed using generic-type bond quotes, i.e. the bonds in the portfolio are not used to construct the curves. 

There are many possible ways that the bond yield curve can be specified. In this example, the credit rating and country are used to pick the correct curve, but configurations can be created that use currency, country only, sector, rating or issuer, or any combination of these trade properties. For example, the US Treasury example view uses the country in which the bonds were issued.

The bond curves can be viewed by navigating to the ```PRIMITIVES``` tab:

![Credit View Primitives](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/credit-view-primitives.png)

The trades can be aggregated by issuer:

![Credit View](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/credit-view.png)

#### Present Value
The present value of the trades.

#### Clean Price
The clean price of the bonds and CDS i.e. the value of the security ignoring any accrued interest. Note that this is non-additive, so only trade-level results are shown.

#### Credit Spread
The equivalent CDS spread for a bond, calculated by finding a constant hazard rate that reprices the bond given a yield curve and then using this hazard rate to calculate the par spread of a standard CDS.

#### Hazard Rate
The constant hazard rate implied by the bond or CDS price. 

### US Treasuries View
This view shows side-by-side results for US Treasury bonds and bills using two different methods to construct the yield curve: discounting and Nelson-Siegel.

The ```PRIMITIVES``` tab shows the two curves that are used in the calculations. The curve in the upper right is constructed using an interpolated discounted curve, while that in the middle right shows a curve constructed using the same bonds but using the Nelson-Siegel method.

![Treasury View Primitives](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/treasury-view-primitives.png)

The ```PORTFOLIO``` tab shows side-by-side values of present value, yield to maturity, modified and Macaulay duration, and chas-flow information for the trades.

![Treasury View](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/treasury-view.png)

Each pair of calculations is identical apart from the type of the bond yield curves that are used. Hovering over the column header will show which curve type is used.

![Modified Duration 1](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/modified-duration-1.png)

![Modified Duration 2](https://github.com/McLeodMoores/starling/blob/master/examples/examples-simulated/docs/images/modified-duration-2.png)

The curve type can also be found from the properties of the results for the trade or portfolio.

#### Present Value
The present value of the bonds and bills.

#### Yield to Maturity
This is the total return of a bond assuming that it is held until the bond matures.

#### Modified Duration
This is the percentage change of the price of a bond when the yield changes by one percent.

#### Macaulay Duration
This is the weighted average maturity of the cash flows of a bond.

#### Bond Details
This shows all cash flows and pricing inputs for a bond or bill.
