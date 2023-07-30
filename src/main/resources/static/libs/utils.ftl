<#macro showError key>
    <#if errorMap[key]??>
        <div class="form-text text-danger">${errorMap[key]}</div>
    </#if>
</#macro>
