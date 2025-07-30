package com.github.senocak.multi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

fun main(args: Array<String>) {
    runApplication<SpringKotlinTestcontainersOracleMultiSchemaApplication>(*args)
}

@SpringBootApplication
@ConfigurationPropertiesScan
@RestController
class SpringKotlinTestcontainersOracleMultiSchemaApplication(
    private val userRepository: UserRepository,
    private val roleRepository: AddressRepository
){
    @GetMapping(value = ["/"])
    fun index(): String = "Welcome to the Spring Kotlin Testcontainers Oracle Multi Schema Application!"

    @GetMapping(value = ["/users"])
    fun getUsers(): List<User> =
        userRepository.findAll().toList()

    @GetMapping(value = ["/roles"])
    fun getRoles(): List<Address> =
        roleRepository.findAll().toList()
}
