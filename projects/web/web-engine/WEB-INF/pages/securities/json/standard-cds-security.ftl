<#escape x as x?html>
<#include "security-header.ftl">
	"tradeDate": "${security.tradeDate}",
	"maturityDate": "${security.maturityDate}",
	"referenceEntity": "${security.referenceEntity.scheme} - ${security.referenceEntity.value}",
	"buyProtection": <#if security.buyProtection> "Buy protection" <#else> "Sell protection" </#if>,
	"notional": {
		"amount": "${security.notional.amount}",
		"currency": "${security.notional.currency}"
	},
	"debtSeniority": "${security.debtSeniority}",
	"coupon": "${security.coupon}",
<#include "security-footer.ftl">
</#escape>