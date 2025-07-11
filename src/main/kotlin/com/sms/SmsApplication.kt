package com.sms

import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.page.Push
import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@MapperScan("com.sms.mappers")
@SpringBootApplication
@Push
class SmsApplication : AppShellConfigurator

fun main(args: Array<String>) {
	runApplication<SmsApplication>(*args)
}
