<#import "/layout/header.ftl" as hdr>
<#import "/layout/footer.ftl" as ftr>

<#macro layout page_css="" page_js="" headerTitle="">
<!doctype html>
<html>
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
        <meta name="description" content="">
        <meta name="author" content="">
        <link rel="icon" href="${utils.static_url("/favicon.ico")}">

        <title>Example Web App</title>

        <link rel="stylesheet"  href="${utils.static_url("/bootstrap.min.css")}">
        <link rel="stylesheet" href="${utils.static_url("/styles.css")}"/>
        ${page_css}

        <script type="application/javascript" src="${utils.static_url("/jquery.min.js")}"></script>
        <script type="application/javascript" src="${utils.static_url("/bootstrap.min.js")}"></script>
        ${page_js}
    </head>
    <body class="container py-3">
        <@hdr.header title=headerTitle/>
        <#nested>
        <@ftr.footer/>
    </body>
</html>
</#macro>
