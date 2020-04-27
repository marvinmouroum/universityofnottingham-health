package de.mouroum.uno_health_app

data class Survey(
    val id:Int,
    val questions:List<Question>,
    val nameId:String,
    val version:Int,
    val description:String
)

data class SubQuestion(
    val id:Int,
    val question:String,
    val type:String,
    val order: Int,
    val multiline:Boolean,
    val container:Container?
)

data class Answer( val id:Int, val value:String)

data class Container(
    val subQuestions:List<SubQuestion>,
    val choiceDependsOn:List<Int>,
    val boolDependsOn:Boolean
)

data class Question (
    val id:Int,
    val type: String,
    val question: String,
    val order:Int,
    val defaultAnswer:Answer?,
    val container: Container?,

    val answers:List<Answer>,
    val multiple:Boolean

)

data class Student (
    var name: String? = null,
    var address: String? = null)

data class Test (
    var id: Int)