<#import "/libs/utils.ftl" as libUtils>
<#assign pageJS>
    <script type="application/javascript" src="${utils.static_url("/users/new-user.js")}"></script>
</#assign>
<@layout.layout page_js=pageJS>
    <div class="px-3 py-3 pt-md-5 pb-md-4 mx-auto">
        <h3 class="display-8 text-center pb-5">Users</h3>
        <form action="/users/new-user" method="post" enctype="application/x-www-form-urlencoded" id="newForm">
            <div class="row mb-3">
                <label for="name" class="col-sm-3 col-form-label">Full Name*</label>
                <div class="col-sm-9">
                    <input name="fullName" class="form-control" id="full-name" value="${(record.fullName)!""}"/>
                    <@libUtils.showError "fullName"/>
                </div>
            </div>
            <div class="row mb-3">
                <label for="investorLocation" class="col-sm-3 col-form-label">Email*</label>
                <div class="col-sm-9 align-items-center">
                    <input name="email" class="form-control" id="email" value="${(record.email)!""}"/>
                    <@libUtils.showError "email"/>
                </div>
            </div>
            <div class="row mb-3">
                <label for="contact" class="col-sm-3 col-form-label">Age*</label>
                <div class="col-sm-2">
                    <input name="age" type="number" class="form-control" value="${(record.age)!""}"/>
                    <@libUtils.showError "age"/>
                </div>
            </div>
            <div class="row mb-3">
                <label class="col-sm-3 col-form-label">Gender*</label>
                <div class="col-sm-3">
                    <select class="form-select form-control-sm" name="gender">
                        <option <#if record.gender == "MALE">selected</#if> value="MALE">MALE</option>
                        <option <#if record.gender == "MALE">selected</#if> value="FEMALE">FEMALE</option>
                    </select>
                    <@libUtils.showError "gender"/>
                </div>
            </div>
            <div>
                <div class="float-end ms-2"><input type="submit" value="Save" class="btn btn-primary btn-sm"/></div>
            </div>
        </form>
    </div>
</@layout.layout>
