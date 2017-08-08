
Example Views
-------------

# Table of Contents
1. [Introduction](#introduction)
2. [Equities](#equity-example)
3. [ETFs](#etf-example)
4. [Swaps](#swap-example)
5. [FX Forwards](#fx-forwards-example)
6. [FX Options](#fx-options-example)
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



## Swaps <a name="swap-example"></a>
  ### Swap Portfolio View
  ### Swap Pricing Details View
  ### AUD Swaps View
## Swaptions
## FX Forwards <a name="fx-forwards-example"></a>
## FX Options <a name="fx-options-example"></a>
## Bonds
  ### US Treasuries View
  ### GB Corporates View
## CDS <a name="cds-example"></a>
## Equity Options
