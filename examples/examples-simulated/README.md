
Example Views
-------------

# Table of Contents
1. [Introduction](#introduction)
2. [Equities](#equity-example)
3. [ETFs](#etf-example)
4. [FX Forwards](#fx-forwards-example)
5. [FX Options](#fx-options-example)
6. [Swaps](#swap-example)
7. [CDS](#cds-example)

## Introduction <a name="introduction"></a>

### The Analytics UI

## Equities <a name="equity-example"></a>

### Equity Portfolio View

![Equity Portfolio View](https://github.com/McLeodMoores/starling/blob/mcleodmoores/examples/examples-simulated/docs/images/equity-portfolio-view.png)

This view references a long-only equity portfolio consisting of stocks in the S&P 500 index. It shows some simple portfolio analytic values, the live daily P&L and VaR.

The trades are originally arranged into sub-portfolios by sector. 

#### FairValue
This is the (live) price of one share multiplied by the number of shares held in the portfolio

#### CAPM Beta
This is the ratio of the covariance of the trade / sub-portfolio returns and SPX returns (the underlying index) and the variance of SPX returns.

#### Sharpe Ratio
This measures the excess return (return of the trade / sub-portfolio minus the return of the index) with respect to SPX per unit of risk (i.e. the standard deviation of returns) for the trade / sub-portfolio.

#### PnL
This shows the live daily P&L - the difference between the last close price and current price multiplied by the number of shares. This is an additive quantity and so portfolio-level P&L is simply the sum of the P&l of its positions.

#### HistoricalVaR
This is one-day horizon VaR, assuming an underlying normal distribution of returns, at 99% confidence level. Two years of daily returns are used. 

The properties of the result show what values can be changed to customise the results. 

![VaR Properties](https://github.com/McLeodMoores/starling/blob/mcleodmoores/examples/examples-simulated/docs/images/var-properties.png)

Going to the view definition editor:

![View Definition Editor 1](https://github.com/McLeodMoores/starling/blob/mcleodmoores/examples/examples-simulated/docs/images/go-to-view-definition.png)

![View Definition Editor 2](https://github.com/McLeodMoores/starling/blob/mcleodmoores/examples/examples-simulated/docs/images/edit-equity-view-definition-1.png)

and adding a column (```+add column```), we select ```HistoricalVaR``` from the dropdown and add properties (```+add constraint```):
  - Change the percentile to 99.73%
  - Change the sampling frequency to weekly
  - Change the VaR horizon to 7 days

![View Definition Editor 3](https://github.com/McLeodMoores/starling/blob/mcleodmoores/examples/examples-simulated/docs/images/edit-equity-view-definition-2.png)

After saving this view definition, we go back to the analyics viewer and see that a second ```HistoricalVaR``` column has appeared. The properties show our changes, and we can see the effect on the distribution that changing these parameters has had by looking at the returns as a distribution (the top distribution is daily returns, the lower is weekly returns).

![Second VaR Column](https://github.com/McLeodMoores/starling/blob/mcleodmoores/examples/examples-simulated/docs/images/equity-portfolio-view-new-column.png)

**NOTE**: if you're running the examples server and edit or add view definitions, these will be overwritten with the originals / deleted if the databases are re-initialised.


## ETFs <a name="etf-example"></a>

### Futures View

![Futures View](https://github.com/McLeodMoores/starling/blob/mcleodmoores/examples/examples-simulated/docs/images/futures-view.png)

This view shows analytics for a small equity futures portfolio, calculated with a mark-to-market method (i.e. using market quotes directly, rather than implying a forward curve).

#### Present Value
The mark-to-market price of the position: market quote minus the previous close (i.e. the margined amount) multiplied by number of contracts.

#### Delta
The change in value of the position for a change in value of the underlying - for mark-to-market pricing, this is the unit amount multiplied by the number of contracts multiplied by the future price.

#### Forward
The forward rate implied by the future - for mark-to-market pricing, this is just the future market quote.

## FX Forwards <a name="fx-forwards-example"></a>
Both FX forward examples reference a portfolio containing AUD, EUR, CHF and GBP vs USD forwards. All prices are calculated using a discounting method, where each pay / receive leg is discounted with a currency-specific curve.

The USD discounting curve is a simple curve constructed from cash deposits. The discounting curves for the other currencies are constructed using FX forward quotes, which are used with the USD curve to imply the interest rate. The interpolation in all cases is a monotonic constrained cubic spline with linear extrapolation at both ends. The instruments used to construct the curves are shown in the table below.

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

![FX Forward Details View](https://github.com/McLeodMoores/starling/blob/mcleodmoores/examples/examples-simulated/docs/images/fx-forward-details.png)

#### FX Present Value
The FX present value is the discounted value of the pay and receive amounts of the forward. This multi-valued output is summed at the portfolio level, giving a total PV in each currency.

![FX PV](https://github.com/McLeodMoores/starling/blob/mcleodmoores/examples/examples-simulated/docs/images/fx-forward-fx-pv-top-level.png)

#### Present Value
The present value is the sum of the discounted pay and receive amounts converted into the required currency.

#### FX Forward Details
This output gives the pricing details for each leg of the trade: the pay and recieve amounts, the discount factors used for each leg and equivalent zero rate.

![FX Forward Details](https://github.com/McLeodMoores/starling/blob/mcleodmoores/examples/examples-simulated/docs/images/fx-forward-details-details.png)

### FX Forward View

## FX Options <a name="fx-options-example"></a>

## Swaps <a name="swap-example"></a>

### Swap Portfolio View

### Swap Pricing Details View

### AUD Swaps View

## Swaptions

## Bonds

### US Treasuries View

### GB Corporates View

## CDS <a name="cds-example"></a>

## Equity Options
