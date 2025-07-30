package com.github.senocak.multi

import oracle.ucp.jdbc.PoolDataSource
import oracle.ucp.jdbc.PoolDataSourceFactory
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.sql.SQLException
import javax.sql.DataSource

@Configuration
class OracleConfiguration(
    private val dataSourceConfigs: DataSourceConfigs,
    private val ucpProperties: UcpProperties
) {
    /**
     * Configures and provides a UCP-based Oracle DataSource bean for CRM.
     *
     * @return a configured [DataSource] instance
     * @throws SQLException if an error occurs during DataSource configuration
     */
    @Bean
    fun crmDataSource(): DataSource {
        val dataSource: PoolDataSource = PoolDataSourceFactory.getPoolDataSource()
        dataSource.url = dataSourceConfigs.url
        dataSource.user = dataSourceConfigs.username
        dataSource.password = dataSourceConfigs.password
        // UCP-specific configurations
        dataSource.connectionFactoryClassName = ucpProperties.connectionFactoryClassName
        dataSource.initialPoolSize = ucpProperties.initialPoolSize
        dataSource.minPoolSize = ucpProperties.minPoolSize
        dataSource.maxPoolSize = ucpProperties.maxPoolSize
        dataSource.timeoutCheckInterval = ucpProperties.timeoutCheckInterval
        dataSource.inactiveConnectionTimeout = ucpProperties.inactiveConnectionTimeout
        dataSource.sqlForValidateConnection = ucpProperties.sqlForValidateConnection
        dataSource.validateConnectionOnBorrow = ucpProperties.isValidateConnectionOnBorrow
        dataSource.secondsToTrustIdleConnection = ucpProperties.secondsToTrustIdleConnection
        return dataSource
    }
}

@ConfigurationProperties(prefix = "spring.datasource")
class DataSourceConfigs : DataSourceProperties()

@ConfigurationProperties(prefix = "spring.datasource.ucp")
class UcpProperties {
    var url: String? = null
    var username: String? = null
    var password: String? = null
    var connectionFactoryClassName: String? = null
    var connectionPoolName: String? = null
    var initialPoolSize: Int = 0
    var minPoolSize: Int = 0
    var maxPoolSize: Int = 0
    var isValidateConnectionOnBorrow: Boolean = false
    var sqlForValidateConnection: String? = null
    var timeoutCheckInterval: Int = 0
    var inactiveConnectionTimeout: Int = 0
    var secondsToTrustIdleConnection: Int = 0
}
