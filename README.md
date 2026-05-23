# kvadraOS_test_deleteDuplicates_app
# Для решения выбрал второй вариант с приложением удаляющим повторяющиеся контакты

## extra task(var 4):
```kotlin
package org.example.kvadraTests

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun main() {
    val employees = listOf(
        Employee("Иван Иванов", 993),
        Employee("Пётр Петров", 994),
        Employee("Алексей Сидоров", 995),
        Employee("Дмитрий Смирнов", 999),
        Employee("Сергей Кузнецов", 1000),
        Employee("Никита Попов", 1001),
        Employee("Андрей Васильев", 1993),
        Employee("Михаил Новиков", 1994),
        Employee("Владимир Фёдоров", 1995),
        Employee("Егор Морозов", 1996),
        Employee("Максим Волков", 2000),
        Employee("Роман Алексеев", 2001),
        Employee("Артём Лебедев", 2002),
        Employee("Кирилл Семёнов", 2005),
        Employee("Олег Егоров", 2006),
        Employee("Иван Петров", 2007),
        Employee("Пётр Иванов", 2008)
    )

    val today = LocalDate.of(2026, 5, 18)
    val monday = today.with(java.time.DayOfWeek.MONDAY)
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM")

    val res = mutableListOf<Pair<String, String>>()
    for (i in 0..6) {
        val date = monday.plusDays(i.toLong())
        val diffDays = ChronoUnit.DAYS.between(today, date).toInt()
        val anniversaries = employees.mapNotNull { emp ->
            val daysOnDate = emp.baseDays + diffDays

            if (daysOnDate >= 1000 && daysOnDate % 1000 == 0) {
                "${emp.name} - $daysOnDate дней"
            } else null
        }
        val text = anniversaries.joinToString("\n")
        res.add(date.format(dateFormatter) to text)
    }

    println("Результат выборки на неделю с ${monday.format(dateFormatter)} по ${monday.plusDays(6).format(dateFormatter)}:")
    println()
    res.forEach { (date, text) ->
        println("$date:")
        if (text.isBlank()) println(" (нет юбилеев)")
        else text.lines().forEach { line -> println(" $line") }
        println()
    }
}

data class Employee(val name: String, val baseDays: Int)
```
