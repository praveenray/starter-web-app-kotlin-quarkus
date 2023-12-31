/*
 * This file is generated by jOOQ.
 */
package my.starter.jooq.tables.pojos


import java.io.Serializable
import java.time.OffsetDateTime

import my.starter.jooq.enums.UserGender


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
data class User(
    var id: Int? = null,
    var email: String,
    var fullName: String,
    var age: Int,
    var gender: UserGender,
    var createdAt: OffsetDateTime? = null
): Serializable {


    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (this::class != other::class)
            return false
        val o: User = other as User
        if (this.id == null) {
            if (o.id != null)
                return false
        }
        else if (this.id != o.id)
            return false
        if (this.email != o.email)
            return false
        if (this.fullName != o.fullName)
            return false
        if (this.age != o.age)
            return false
        if (this.gender != o.gender)
            return false
        if (this.createdAt == null) {
            if (o.createdAt != null)
                return false
        }
        else if (this.createdAt != o.createdAt)
            return false
        return true
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + (if (this.id == null) 0 else this.id.hashCode())
        result = prime * result + this.email.hashCode()
        result = prime * result + this.fullName.hashCode()
        result = prime * result + this.age.hashCode()
        result = prime * result + this.gender.hashCode()
        result = prime * result + (if (this.createdAt == null) 0 else this.createdAt.hashCode())
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder("User (")

        sb.append(id)
        sb.append(", ").append(email)
        sb.append(", ").append(fullName)
        sb.append(", ").append(age)
        sb.append(", ").append(gender)
        sb.append(", ").append(createdAt)

        sb.append(")")
        return sb.toString()
    }
}
