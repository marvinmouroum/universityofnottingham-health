package de.mouroum.uno_health_app.handling

import de.mouroum.uno_health_app.Container
import de.mouroum.uno_health_app.Question
import de.mouroum.uno_health_app.Survey
import de.mouroum.uno_health_app.SurveyResponse
import java.util.*

class SurveyIterator(private var survey: Survey, lastKnownQuestion: Long? = null) {

    //private var currentContainer: Container? = null
    private var currentQuestion: Question? = null
    private val questionStack = Stack<Question>()

    init {
        stackQuestions(survey.questions)
        lastKnownQuestion?.let {
            if (!select(it)) stackQuestions(survey.questions)
        }
    }

    fun next(currentAnswer: SurveyResponse? = null): Question? {
        if (currentAnswer != null) {
            if (currentQuestion?.id != currentAnswer.questionId) {
                throw IllegalArgumentException("Expected an answer for current question ${currentQuestion?.id}")
            }
            currentQuestion?.container?.let {
                if (shouldAnswerSubquestions(it, currentAnswer)) {
                    stackQuestions(it.subQuestions)
                }
            }
        }

        currentQuestion = if (questionStack.isEmpty()) null else questionStack.pop()

        return currentQuestion
    }

    private fun stackQuestions(questions: Iterable<Question>) =
        questionStack.addAll(questions.reversed())

    private fun shouldAnswerSubquestions(
        container: Container,
        currentAnswer: SurveyResponse?
    ): Boolean {
        // FIXME not clear what container.boolDependsOn represent?
        if (container.boolDependsOn) return true
        return currentAnswer?.answerIds?.intersect(container.choiceDependsOn)?.isNotEmpty()
            ?: false
    }


    private fun select(questionId: Long): Boolean {
        while (!questionStack.isEmpty()) {
            val question = questionStack.peek()
            if (question.id == questionId) {
                currentQuestion = question
                return true
            }
            questionStack.pop()
            question.container?.run {
                stackQuestions(subQuestions)
            }
        }
        //not found reset the stack to initial
        stackQuestions(survey.questions)
        return false
    }
}