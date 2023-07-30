<#import "/libs/utils.ftl" as libUtils>

<@layout.layout>
    <div class="d-flex justify-content-end">
        <a href="/users/new-user" class="btn btn-sm btn-success">Create</a>
    </div>
    <table class="table">
        <thead>
            <tr>
                <th scope="col">#</th>
                <th scope="col">ID</th>
                <th scope="col">Full Name</th>
                <th scope="col">EMAIL</th>
                <th scope="col">AGE</th>
                <th scope="col">CREATED AT</th>
            </tr>
            <tbody>
                <#list users as user>
                    <tr>
                        <td>${user?index}</td>
                        <td>${user.id}</td>
                        <td>${user.fullName}</td>
                        <td>${user.email}</td>
                        <td>${user.age}</td>
                        <td>${user.createdAt}</td>
                    </tr>
                </#list>
        </tbody>
        </thead>
    </table>
</@layout.layout>>