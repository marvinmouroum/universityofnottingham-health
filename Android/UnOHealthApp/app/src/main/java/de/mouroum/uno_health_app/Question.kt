package de.mouroum.uno_health_app

import java.io.Serializable

data class Survey (
    val id:Int,
    val questions:List<Question>,
    val nameId:String,
    val version:Int,
    val title:String,
    val description:String
) : Serializable

data class Answer( val id:Long, val value:String) : Serializable

data class Container(
    val subQuestions:List<Question>,
    val choiceDependsOn:List<Long>,
    val boolDependsOn:Boolean
) : Serializable

data class Question (
    val id:Long,
    val type: AnswerType,
    val question: String,
    val order:Int,
    val defaultAnswer:Answer?,
    val container: Container?,
    val entries: List<Question>,
    val answers:List<Answer>,
    val multiple:Boolean,
    val multiline:Boolean,
    val length:Int,

    // Slider
    val minValue:Int,
    val maxValue:Int,
    val minText:String,
    val maxText:String

) : Serializable

data class SurveyResponse(
    val questionId: Long,
    val answerIds: List<Long>?,
    val boolAnswer: Boolean?,
    val textAnswer: String?,
    val rangeAnswer: Int?,
    val checklistAnswer: Map<Long, Boolean>?
)

data class Verification(
    val verificationToken: String,
    val confirmationToken: String?
)

enum class AnswerType {
    BOOL , CHOICE, TEXT, CHECKLIST, RANGE
}