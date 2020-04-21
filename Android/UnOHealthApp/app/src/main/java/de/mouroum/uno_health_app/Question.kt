package de.mouroum.uno_health_app

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import kotlin.reflect.KClass

class Survey(
    val id:Int,
    val questions:List<Question>,
    val nameId:String,
    val version:Int
)

open class SpecificQuestion

data class ChoiceQuestion(
    val answers:List<Answer>,
    val multiple:Boolean
): SpecificQuestion()

class BoolQuestion: SpecificQuestion()

class SubQuestion(
    val id:Int,
    val question:String,
    val type:String,
    val order: Int,
    val multiline:Boolean,
    val container:Container?
)

class Answer( val id:Int, val value:String)

class Container(
    val subQuestions:List<SubQuestion>,
    val dependsOn:List<Int>
)

class Question (
    @TypeFor(field = "specificQuestion", adapter = QuestionTypeAdapter::class)
    val type: String,
    val question: String,
    val order:Int,
    val defaultAnswer:Answer?,
    val container: Container?,

    var specificQuestion: SpecificQuestion?
)

class QuestionTypeAdapter: TypeAdapter<SpecificQuestion> {
    override fun classFor(type: Any): KClass<out SpecificQuestion> = when(type as String) {
        "CHOICE" -> ChoiceQuestion::class
        "BOOL" -> BoolQuestion::class
        else -> throw IllegalArgumentException("Unknown type: $type")
    }
}