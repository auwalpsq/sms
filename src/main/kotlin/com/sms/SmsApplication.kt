package com.sms

import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.theme.Theme
import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@MapperScan("com.sms.mappers")
@SpringBootApplication
@Push
@Theme(value="sms-styles")
class SmsApplication : AppShellConfigurator

fun main(args: Array<String>) {
	runApplication<SmsApplication>(*args)
}
