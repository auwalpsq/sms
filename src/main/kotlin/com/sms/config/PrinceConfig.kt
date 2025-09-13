package com.sms.config

import com.princexml.wrapper.Prince
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PrinceConfig(
    @Value("\${prince.executable}") private val princePath: String,
    @Value("\${prince.log:/tmp/prince.log}") private val princeLog: String,
    @Value("\${prince.enableJs:false}") private val enableJs: Boolean
) {
    @Bean
    fun prince(): Prince {
        val prince = Prince(princePath)                     // pass path to engine
        prince.setLog(princeLog)                            // optional log location
        prince.setJavaScript(enableJs)                      // enable JS if needed
        // do NOT set BaseURL here (set per-request if necessary)
        return prince
    }
}