
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
## Equities <a name="equity-example"></a>

![Equity Portfolio View](https://github.com/McLeodMoores/starling/blob/mcleodmoores/examples/examples-simulated/docs/images/equity-portfolio-view.png)

The "Equity Portfolio View" references a long/short equity portfolio consisting of stocks in the S&P 500 index. It shows some simple portfolio analytic values, the live daily P&L and VaR.

### FairValue
This is simply the live price of one share multiplied by the number of shares held in the portfolio

### CAPM Beta
This is the ratio of the covariance of the trade / sub-portfolio returns and SPX returns (the underlying index) divided by the variance of SPX returns.

### Sharpe Ratio
This measures the excess return with respect to SPX per unit of risk (i.e. the standard deviation of returns) for the trade / sub-portfolio.

### PnL
This shows the live daily P&L - the difference between the last close price and current price multiplied by the number of shares. This is an additive quantity and so portfolio-level P&L is simply the sum of the P&l of its positions.

### HistoricalVaR
This is one-day horizon VaR, assuming an underlying normal distribution of returns, at 99% confidence level. Two years of daily returns are used. 

## ETFs <a name="etf-example"></a>
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
