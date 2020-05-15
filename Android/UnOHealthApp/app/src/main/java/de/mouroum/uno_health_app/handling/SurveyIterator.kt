package de.mouroum.uno_health_app.handling

import de.mouroum.uno_health_app.Container
import de.mouroum.uno_health_app.Question
import de.mouroum.uno_health_app.Survey
import de.mouroum.uno_health_app.SurveyResponse
import java.util.*

class SurveyIterator(private var survey: Survey, lastKnownQuestion: Long? = null) {

    private var currentContainer: Container? = null
    private val questionStack = Stack<Question>()

    init {
        stackQuestions(survey.questions)
        lastKnownQuestion?.let {
            if (it >= 0) {
                select(it)
            }
        }
    }

    private fun stackQuestions(container: Container?) {
        container?.subQuestions?.let {
            stackQuestions(it)
        }
    }

    private fun stackQuestions(questions: List<Question>) {
        questions.asReversed().forEach {
            questionStack.push(it)
        }
    }

    private fun shouldStackSubQuestions(
        container: Container,
        currentAnswer: SurveyResponse?
    ): Boolean {
        if (container.boolDependsOn) return true
        return currentAnswer?.answerIds?.intersect(container.choiceDependsOn)?.isNotEmpty()
            ?: false
    }

    fun next(currentAnswer: SurveyResponse? = null): Question? {
        currentContainer?.let {
            if (shouldStackSubQuestions(it, currentAnswer)) {
                stackQuestions(it)
            }
        }
        if (questionStack.isEmpty()) {
            currentContainer = null
            return null
        }
        currentContainer = questionStack.peek().container
        return questionStack.pop()
    }

    private fun select(questionId: Long) {
        if (questionStack.isEmpty()) {
            stackQuestions(survey.questions)
            return
        }
        currentContainer?.let {
            stackQuestions(it)
        }
        if (questionStack.peek().id == questionId) {
            return
        } else {
            currentContainer = questionStack.pop().container
        }
        return select(questionId)
    }
}