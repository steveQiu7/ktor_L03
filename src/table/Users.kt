package com.example.table

import com.example.table.Cities
import org.jetbrains.exposed.dao.id.IntIdTable

object Users : IntIdTable() {
    val name = varchar("name",50).index()
    val city = reference("city", Cities)
    val age = integer("age")
}