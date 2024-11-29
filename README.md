# Example-app
An Example Web App to get you booted quickly with Kotlin+JooQ+Quarkus

### Development Setup
1. Make sure JDK 17 is installed and JAVA_HOME is pointing to JDK 17 Home
2. `java -version` should say 17
3. docker must be installed and running (use docker desktop on Windows)
4. Run Postgres in docker if you don't have dedicated postgres:
   1. `docker run -d --rm --name pg -ePOSTGRES_PASSWORD=postgres -p5432:5432   -t postgres`
   2. Make sure you can connect to Postgres instance: username/password/database are all `postgres`
4. Run on command line: ./gradlew quarkusDev
5. Application is running on http://localhost:8080/users
6. You can make code changes and refresh the browser. Usually `quarkusDev` picks up code changes without need to restart the app.

### Development Tech Stack
1. [Quarkus](https://quarkus.io/)
2. [Postgres](https://www.postgresql.org/)
3. [Jooq](https://www.jooq.org/)
4. [TestContainers](https://testcontainers.com/)
4. [Freemarker](https://freemarker.apache.org/index.html)
5. [LiquiBase](https://www.liquibase.org/)
6. [bootstrap 5](https://getbootstrap.com/docs/5.0/getting-started/introduction/)

### Other tasks
Jooq works by generating Kotlin Code from a database schema. It examines postgres and creates corresponding DTOs and POJOs. In order
to do so, it needs access to a working postgres schema. We provide this via a testcontainer. A gradle task called `generateJooqClasses` is provided to generate code.
You can simply run this task whenever you make changes to database schema: `./gradlew generateJooqClasses` - you must have docker service running before running this task.

This task does the following:
1. Instantiate a new postgres docker container (using TestContainer)
2. Runs the database (Liquibase) migration from `src/main/resources/db/changelog` folder against this container
3. Connects Jooq to this database and generates Kotlin code. The generated code is in `src/main/kotlin/my/starter/jooq` folder. You are advised against changing anything in this folder by hand.

### Freemarker based UI
The Project uses [Freemarker](https://freemarker.apache.org/index.html) templates to build HTML UI pages. It's not a single page app that one would build using React or Angular. This is on purpose since Page Based 
HTML is simpler to understand and modify and you can get started without React or Typescript expertise. On the other hand, the architecture allows for gradual introduction
of React/Angular.

#### UI Architecture
Sitewide layout is applied via a Freemarker macro called [layout](src/main/resources/static/layout/index.ftl). Any page that needs this layout can '_include_' it on-demand:
 
        <@layout.layout>
            This is the page content with header and footer and other common elements applied from layout macro.
        </@layout.layout>

Note that this is different from other templating engines where common layout is applied site wide in code. Our approach is more versatile since the _page_
decides which layout to use and a page can decide to use another layout with a simple change.

Common styles (bootstrap css) and js (e.g. jquery) are included in the layout and available to any page utilizing this layout.

Page specific javascript can be included by introducing a `pageJS` variable in each page. Please see [new-user.ftl](src/main/resources/static/users/new-user.ftl) for an example.

Another cool feature is how simple it is to handle release specific static assets (javascript/images/css etc). Every static url on these pages follows a pattern like 
`/static/0.0.1/bootstrap.min.css` where `0.0.1` is a release version. This generates an URL like 'http://host/static/0.0.1/bootstrap.min.js' . Once a
new application is deployed (e.g. 0.0.2), simple modify the property `release.version` in [application.properties](src/main/resources/application.properties). This will generate a new URL forcing Browsers to
download the resource fresh bypassing it's cache. ALL static resources are served with a big cache timeout like 1 year which essentially means it'll be cached
indefinitely. This is safe since next app release would force a new URL as described, forcing a new download. Please see [StaticController](src/main/kotlin/my/starter/controllers/StaticController.kt) for details.
