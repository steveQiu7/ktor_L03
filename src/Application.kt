package com.example

import com.example.dao.City
import com.example.dao.User
import com.example.table.Cities
import com.example.table.Users
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.css.*
import kotlinx.html.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    val client = HttpClient(Apache) {
    }

    //先連線到 Ｈ2
    Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver"
    )

    //先把資料把建立起來
    transaction {
        SchemaUtils.create(Cities, Users)
    }

    //先寫一些資料進去 H2
    transaction {

        val cityStPeter = City.new { name = "St. Petersburg" }
        val cityMunich = City.new { name = "Munich" }

        User.new {
            name = "User A"
            city = cityStPeter
            age = 5
        }

        User.new {
            name = "User B"
            city = cityStPeter
            age = 27
        }

        User.new {
            name = "User C"
            city = cityMunich
            age = 42
        }
    }

    routing {

        //首頁:回傳 Hello, world
        get("/") {
            call.respondText("Hello, world")
        }

        //城市頁: 回傳所有城市的名字
        get("/cities") {
            val cities = transaction {
                City.all().joinToString { it.name }
            }
            call.respondText(cities)
        }

        //使用者頁:
        get("/users") {
            val users = transaction {
                User.all().joinToString { "${it.name} city:${it.city.name} age:${it.age}" }
            }
            call.respondText(users)
        }

        //insert 兩個
        get("/generate-user") {
            transaction {
                SchemaUtils.create(Cities, Users)

                val cityTaipeiCity = City.new { name = "Taipei" }
                val cityHischuCity = City.new { name = "HisnChu" }

                User.new {
                    name = "demo"
                    city = cityTaipeiCity
                    age = 666
                }

                User.new {
                    name = "yaya"
                    city = cityHischuCity
                    age = 777
                }
            }
            call.respondText("generate-user success", contentType = ContentType.Text.Plain)
        }

        get("/get-user") {
            val users = transaction { User.all().joinToString { "user:${it.name} city:${it.city.name} age:${it.age}" } }
            call.respondText(users,contentType = ContentType.Text.Plain)
        }

    }
}

fun FlowOrMetaDataContent.styleCss(builder: CSSBuilder.() -> Unit) {
    style(type = ContentType.Text.CSS.toString()) {
        +CSSBuilder().apply(builder).toString()
    }
}

fun CommonAttributeGroupFacade.style(builder: CSSBuilder.() -> Unit) {
    this.style = CSSBuilder().apply(builder).toString().trim()
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}
