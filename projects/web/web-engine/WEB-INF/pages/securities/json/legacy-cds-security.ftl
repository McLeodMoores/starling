<#escape x as x?html>
<#include "security-header.ftl">
	"tradeDate": "${security.tradeDate}",
	"startDate": "${security.startDate}",
	"maturityDate": "${security.maturityDate}",
	"referenceEntity": "${security.referenceEntity.scheme} - ${security.referenceEntity.value}",
	"buyProtection": <#if security.buyProtection> "Buy protection" <#else> "Sell protection" </#if>,
	"notional": {
		"amount": "${security.notional.amount}",
		"currency": "${security.notional.currency}"
	},
	"debtSeniority": "${security.debtSeniority.name}",
	"coupon": "${security.coupon}",
	"couponFrequency": "${security.couponFrequency.conventionName}",
	"dayCount": "${security.dayCount.conventionName}",
	"businessDayConvention": "${security.businessDayConvention.conventionName}",
	"calendars": {
	},
	"restructuringClause": "${security.restructuringClause.name}",
	"upfrontPayment": {
		"amount": "${security.upfrontPayment.amount}",
		"currency": "${security.upfrontPayment.currency}"
	},
	"feeSettlementDate": "${security.feeSettlementDate}",
	"accruedOnDefault": <#if security.accruedOnDefault> "Receive accrued on default" <#else> "Do not receive accrued on default" </#if>, 
	"fixedRecovery": "${security.fixedRecovery}",
<#include "security-footer.ftl">
</#escape>