package tasklist

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.datetime.*
import java.io.File
import kotlin.system.exitProcess


data class Task(
    var priority: String,
    var date: String,
    var time: String,
    var taskDescription: List<String>,
    var dueDate: String
) {
    override fun toString(): String = "| $date | $time | $priority | $dueDate "
}

class TaskList {
    private var taskList = mutableListOf<Task>()

    fun menu() {
        val jsonFile = File("tasklist.json")
        if (jsonFile.exists()) {
            taskList = readJson(jsonFile)!!
        }

        while (true) {
            println("Input an action (add, print, edit, delete, end):")
            when (readln().lowercase()) {
                "add" -> {
                    addTasks()
                }

                "print" -> {
                    printTasks()
                }

                "edit" -> {
                    printTasks(); editTasks()
                }

                "delete" -> {
                    printTasks(); deleteTasks()
                }

                "end" -> {
                    if (taskList.isNotEmpty()) {
                        writeJson(jsonFile, taskList)
                    }
                    println("Tasklist exiting!"); exitProcess(0)
                }
                else -> println("The input action is invalid")
            }
        }
    }

    private fun addTasks() {
        val priority: String = getPriority()
        val date = getDate()
        val time = getTime().toString()
        val dueDate = getDueDate(date)
        val taskDescription = getTaskDescription()

        val task = Task(priority, date.toString(), time, taskDescription, dueDate)
        taskList.add(task)
    }

    private fun getTaskDescription(): MutableList<String> {
        println("Input a new task (enter a blank line to end):")
        // Get first mainTask line
        while (true) {
            val mainTask = readln().trim()
            if (mainTask.isEmpty()) {
                println("The task is blank"); menu()
            } else {
                val taskDescription = mutableListOf(mainTask)
                // Get subtask if any are given
                while (true) {
                    val subTask = readln().trim()
                    if (subTask.isEmpty()) {
                        return taskDescription
                    }
                    taskDescription.add(subTask)
                }
            }
        }
    }

    private fun getDueDate(taskDate: LocalDate): String {
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
        val numberOfDays = currentDate.daysUntil(taskDate)
        val dueDate = when {
            numberOfDays > 0 -> "\u001B[102m \u001B[0m" // In time
            numberOfDays < 0 -> "\u001B[101m \u001B[0m" // Overdue
            else -> "\u001B[103m \u001B[0m" // Today
        }
        return dueDate
    }

    private fun deleteTasks() {
        println("Input the task number (1-${taskList.size}):")
        val taskIndex = readln().toIntOrNull()
        if (taskIndex !in 1..taskList.size || taskIndex == null) {
            println("Invalid task number")
            deleteTasks()
        } else {
            taskList.removeAt(taskIndex - 1)
            println("The task is deleted")
        }
    }


    private fun editTasks() {
        println("Input the task number (1-${taskList.size}):")
        val taskIndex = readln().toIntOrNull()
        if (taskIndex !in 1..taskList.size || taskIndex == null) {
            println("Invalid task number")
            editTasks()
        } else {
            while (true) {
                println("Input a field to edit (priority, date, time, task):")
                val input = readln()
                when (input) {
                    "priority" -> {
                        taskList[taskIndex - 1].priority = getPriority()
                        println("The task is changed")
                        menu()
                    }

                    "date" -> {
                        val date = getDate()
                        val dueDate = getDueDate(date)
                        taskList[taskIndex - 1].date = date.toString()
                        taskList[taskIndex - 1].dueDate = dueDate
                        println("The task is changed")
                        menu()
                    }

                    "time" -> {
                        val time = getTime().toString()
                        taskList[taskIndex - 1].time = time
                        println("The task is changed")
                        menu()
                    }

                    "task" -> {
                        taskList[taskIndex - 1].taskDescription = getTaskDescription()
                        println("The task is changed")
                        menu()
                    }

                    else -> println("Invalid field")
                }
            }
        }
    }

    private fun getPriority(): String {
        while (true) {
            println("Input the task priority (C, H, N, L):")
            val priority = readln().uppercase().trimIndent()
            when (priority) {
                "C" -> return "\u001B[101m \u001B[0m" //Red
                "H" -> return "\u001B[103m \u001B[0m" //Yellow
                "N" -> return "\u001B[102m \u001B[0m" //Green
                "L" -> return "\u001B[104m \u001B[0m" //Blue
            }
        }
    }

    private fun getDate(): LocalDate {
        return try {
            println("Input the date (yyyy-mm-dd):")
            val date = readln().split("-")
            val year = date[0].toInt()
            val month = date[1].toInt()
            val day = date[2].toInt()
            LocalDate(year, month, day)
        } catch (e: IllegalArgumentException) {
            println("The input date is invalid")
            getDate()
        }
    }

    private fun getTime(): LocalTime {
        return try {
            println("Input the time (hh:mm):")
            val time = readln().split(":")
            val hour = time[0].toInt()
            val min = time[1].toInt()
            LocalTime(hour, min)
        } catch (e: IllegalArgumentException) {
            println("The input time is invalid")
            getTime()
        }
    }

    private fun printTasks() {
        if (taskList.isEmpty()) {
            println("No tasks have been input")
            menu()
        }
        // Print Header
        println("+----+------------+-------+---+---+--------------------------------------------+")
        println("| N  |    Date    | Time  | P | D |                   Task                     |")
        println("+----+------------+-------+---+---+--------------------------------------------+")

        // Print information for Each task
        taskList.forEachIndexed { index, task ->
            print("| ${index + 1}  ".padEnd(5) + task)

            // Print taskDescription for each task
            task.taskDescription.forEachIndexed { i, description ->
                if (i == 0) {
                    val firstTask = description.chunked(44)
                    println("|${firstTask[0].padEnd(44)}|")
                    for (x in 1 until firstTask.size) println("|    |            |       |   |   |${firstTask[x].padEnd(44)}|")
                } else {
                    for (line in description.chunked(44)) println("|    |            |       |   |   |${line.padEnd(44)}|")
                }
            }
            println("+----+------------+-------+---+---+--------------------------------------------+")
        }
    }

    private fun readJson(jsonFile: File): MutableList<Task>? {
        val jsonString = jsonFile.readText()
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val type = Types.newParameterizedType(MutableList::class.java, Task::class.java)
        val mutableListJsonAdapter = moshi.adapter<MutableList<Task>>(type)
        return mutableListJsonAdapter.fromJson(jsonString)
    }

    private fun writeJson(jsonFile: File, taskList: MutableList<Task>) {
        if (!jsonFile.exists()) jsonFile.createNewFile()
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val type = Types.newParameterizedType(MutableList::class.java, Task::class.java)
        val mutableListJsonAdapter = moshi.adapter<MutableList<Task>>(type)
        val jsonString = mutableListJsonAdapter.indent(" ").toJson(taskList)
        jsonFile.writeText(jsonString)
    }
}

fun main() {
    val taskList = TaskList()
    taskList.menu()
}
