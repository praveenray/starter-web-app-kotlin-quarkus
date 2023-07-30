package my.starter.services

import jakarta.enterprise.context.ApplicationScoped
import my.starter.jooq.enums.UserGender
import my.starter.jooq.tables.pojos.User
import my.starter.jooq.tables.references.USERS
import my.starter.libs.Err
import my.starter.libs.Ok
import my.starter.libs.OpResult
import org.jooq.DSLContext
import org.jooq.exception.IntegrityConstraintViolationException

@ApplicationScoped
class UserService(
    private val jooq: DSLContext
) {
    fun getUsers(): List<User> {
        return jooq.select(USERS).from(USERS).orderBy(USERS.CREATED_AT.desc()).fetchInto(User::class.java)
    }

    fun createNewUser(record: Map<String, String?>): OpResult<Int, Map<String, String?>> {
        val fullName = record["fullName"]
        val email = record["email"]
        val gender = record["gender"]
        val age = record["age"]

        val errors = emptyMap<String, String>().plus(
            "fullName" to (
                    if (fullName.isNullOrBlank()) "fullName is missing"
                    else null
                )
        ).plus(
            "age" to (
                    if (age.isNullOrBlank()) "age is missing"
                    else if (age.toIntOrNull() == null) "Invalid Age Number"
                    else if (age.toInt() !in (10..100)) "Age must be within 10 and 100"
                    else null
                )
        ).plus(
            "email" to (if (email.isNullOrBlank()) "email is missing" else null)
        ).plus(
            "gender" to (
                    if (gender.isNullOrBlank()) "gender is missing"
                    else if (null == createUserGender(gender)) {
                        "Invalid Gender (MALE or FEMALE)"
                    } else null
                    )
        ).filter { (_, v) -> v != null }

        return if (errors.isEmpty()) {
            try {
                val user = User(
                    email = email!!,
                    fullName = fullName!!,
                    gender = createUserGender(gender)!!,
                    age =  age!!.toInt(),
                )
                jooq.insertInto(USERS).defaultValues()
                val userRecord = jooq.newRecord(USERS, user)
                userRecord.insert(USERS.fields().toList().minus(USERS.CREATED_AT))
                Ok(userRecord.id!!)
            } catch (e: IntegrityConstraintViolationException) {
                Err(mapOf("email" to "Email Exists"))
            } catch (e: Exception) {
                Err(mapOf("fullName" to "Error Creating User"))
            }
        } else Err(errors)
    }

    private fun createUserGender(gender: String?): UserGender? {
        return if (gender == null) {
            null
        } else try {
            UserGender.valueOf(gender)
        } catch (e: Exception) {
            null
        }
    }
}