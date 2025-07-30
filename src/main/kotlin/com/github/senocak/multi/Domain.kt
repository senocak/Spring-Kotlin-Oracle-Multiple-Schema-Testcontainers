package com.github.senocak.multi

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Table
import org.springframework.data.repository.CrudRepository
import java.io.Serializable

@MappedSuperclass
open class BaseDomain(
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    var id: String? = null,
) : Serializable

@Entity
@Table(name = "users", schema = "USER_SCHEMA")
data class User(
    @Column(name = "name", nullable = false, length = 50) var name: String? = null,
    @Column(name = "email", nullable = false, length = 100) var email: String? = null,
    @Column(name = "password", nullable = false) var password: String? = null
) : BaseDomain()

@Entity
@Table(name = "addresses", schema = "ADDRESS_SCHEMA")
data class Address(
    @Column var name: String? = null
): BaseDomain()

interface AddressRepository: CrudRepository<Address, String> {
    fun findByName(address: String): Address?
}

interface UserRepository: CrudRepository<User, String> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
}
