<#escape x as x?html>
<@page title="Holidays" jquery=true aceXmlEditor=true>

<#-- SECTION Holiday search -->
<@section title="Holiday search" if=searchRequest??>
  <@form method="GET" action="${uris.holidays()}">
  <p>
    <@rowin label="Type">
      <select name="type">
        <option value="" <#if searchRequest.type = ''>selected</#if>></option>
        <option value="CURRENCY" <#if searchRequest.type = 'CURRENCY'>selected</#if>>Currency</option>
        <option value="BANK" <#if searchRequest.type = 'BANK'>selected</#if>>Bank</option>
        <option value="SETTLEMENT" <#if searchRequest.type = 'SETTLEMENT'>selected</#if>>Settlement</option>
        <option value="TRADING" <#if searchRequest.type = 'TRADING'>selected</#if>>Trading</option>
      </select>
    </@rowin>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${searchRequest.name}" /></@rowin>
    <@rowin><input type="submit" value="Search" /></@rowin>
  </p>
  </@form>

<#-- SUBSECTION Holiday results -->
<#if searchResult??>
<@subsection title="Results">
  <@table items=searchResult.documents paging=paging empty="No holidays" headers=["Name","Reference","Type","Version valid from","Actions"]; item>
      <td><a href="${uris.holiday(item.uniqueId)}">${item.name}</a></td>
      <td>${item.uniqueId.value}</td>
      <td>${item.holiday.type}</td>
      <td>${item.versionFromInstant}</td>
      <td><a href="${uris.holiday(item.uniqueId)}">View</a></td>
  </@table>
</@subsection>
</#if>
</@section>


<#-- SECTION Add holiday -->
<@section title="Add holiday">
  <@form method="POST" action="${uris.holidays()}" id="addForm">
  <p>
    <@rowin label="Name"><input type="text" size="30" name="name" value="" /></@rowin>
    <@rowin label="Type">
      <select name="type">
        <option value=""></option>
        <#list holidayDescriptionMap?keys as key><option value="${key}">${holidayDescriptionMap[key]}</option></#list>
      </select>
    </@rowin>
    <@rowin>
      <div id="ace-xml-editor"></div>
    </@rowin>
    <input type="hidden" name="holidayxml" id="holiday-xml"/>
    <@rowin><input type="submit" value="Add" /></@rowin>
    
    <#noescape><@xmlEditorScript formId="addForm" inputId="holiday-xml"></@xmlEditorScript></#noescape>  
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
